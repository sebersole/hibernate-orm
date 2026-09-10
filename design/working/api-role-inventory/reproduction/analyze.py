"""Temporary API role audit. Generates evidence, never changes classifications."""
import collections
import functools
import hashlib
import json
import pathlib
import re
import subprocess

ROOT = pathlib.Path.cwd()
OUT = ROOT / 'design/working/api-role-inventory'
SCRATCH = ROOT / 'target/api-role-inventory'
metadata_path = ROOT / 'documentation/target/orm/reports/classifications.json'
metadata = json.loads(metadata_path.read_text())
elements = {e['id']: e for e in metadata['elements']}
types = {e['id'][5:]: e for e in metadata['elements'] if e['kind'] in ('TYPE','ANNOTATION_TYPE')}
api = {n:e for n,e in types.items() if e.get('category') == 'API'}
sources = [json.loads(s) for s in (SCRATCH/'sources.jsonl').read_text().splitlines()]
classes = [json.loads(s) for s in (SCRATCH/'bytecode.jsonl').read_text().splitlines()]
known = set(types) | {s['name'] for s in sources} | {c['name'] for c in classes}
dotnames = collections.defaultdict(set)
for n in known:
    dotnames[n.replace('$','.')].add(n)

def erased(s):
    depth=0; result=''
    for c in s:
        if c=='<': depth+=1
        elif c=='>': depth-=1
        elif not depth: result+=c
    return re.sub(r'@[\w.]+(?:\([^)]*\))?\s*','',result).strip()

def resolve(name, s):
    name=erased(name)
    if not name: return None
    def match(n):
        v=dotnames.get(n, set())
        return next(iter(v)) if len(v)==1 else None
    direct=match(name)
    if direct: return direct
    head, _, tail=name.partition('.')
    for imp in s['imports']:
        if not imp.endswith('.*') and imp.split('.')[-1]==head:
            result=match(imp+('.'+tail if tail else ''))
            if result: return result
    outer=s['name'].split('$')
    for count in range(len(outer),0,-1):
        result=match('.'.join(outer[:count])+'.'+name)
        if result: return result
    result=match((s['package']+'.' if s['package'] else '')+name)
    if result: return result
    matches=set()
    for imp in s['imports']:
        if imp.endswith('.*'):
            result=match(imp[:-1]+name)
            if result: matches.add(result)
    return next(iter(matches)) if len(matches)==1 else None

def context(path):
    if path.startswith('documentation/'): return 'DOCUMENTATION'
    if '/providerFixture/' in path or '/providerFixtureTest/' in path: return 'PROVIDER_FIXTURE'
    if '/jmh/' in path: return 'BENCHMARK'
    if re.search(r'/(?:test|\w*[Tt]est|\w*IntegrationTest)/',path): return 'TEST'
    if path.startswith('hibernate-testing/'): return 'TEST_INFRASTRUCTURE'
    if path.startswith('hibernate-core/'): return 'CORE_IMPLEMENTATION'
    return 'ORM_SUBPROJECT'

parents=collections.defaultdict(set)
for n,e in types.items():
    parents[n].update(r['target'][5:] for r in e.get('references',[]) if r['kind'] in ('SUPERCLASS','IMPLEMENTED_INTERFACE'))
source_by_name=collections.defaultdict(list)
source_file_index=collections.defaultdict(set)
unresolved=[]
for s in sources:
    source_by_name[s['name']].append(s)
    source_file_index[(s['package'],pathlib.Path(s['path']).name)].add(s['path'])
    s['resolvedParents']=[]
    for raw in s['parents']:
        p=resolve(raw,s)
        if p:
            s['resolvedParents'].append(p)
            parents[s['name']].add(p)
        else: unresolved.append({'source':s['name'],'parent':raw,'path':s['path']})

class_by_name={}
for c in sorted(classes,key=lambda c: '/java/main/' in c['path']):
    class_by_name[c['name']]=c

def ancestors(name):
    result=set(); todo=list(parents.get(name,()))
    while todo:
        p=todo.pop()
        if p not in result and p!=name:
            result.add(p); todo.extend(parents.get(p,()))
    return result

@functools.lru_cache(None)
def public_type(n):
    e=types.get(n)
    if not e: return None
    flags=e['structure']['modifiers']
    if not flags&5: return False
    if (e.get('owner') or '').startswith('type:'): return public_type(e['owner'][5:])
    return True

constructors=collections.defaultdict(list)
mixed_members=collections.defaultdict(list)
for e in metadata['elements']:
    if (e.get('owner') or '').startswith('type:'):
        owner=e['owner'][5:]
        if e['kind']=='CONSTRUCTOR': constructors[owner].append(e)
        if owner in api and e.get('category') != 'API' and e['kind'] in ('METHOD','CONSTRUCTOR','FIELD'):
            mixed_members[owner].append({'id':e['id'],'category':e.get('category'),'roles':e.get('spiRoles',[])})

def shape(n,e):
    f=e['structure']['modifiers']; c=class_by_name.get(n,{})
    if not public_type(n): return 'NOT_PUBLICLY_ACCESSIBLE'
    if e['kind']=='ANNOTATION_TYPE': return 'ANNOTATION'
    if f&0x4000: return 'ENUM'
    if f&0x10: return 'FINAL_CLASS'
    if c.get('permitted') or any('sealed' in s['modifiers'] and 'non-sealed' not in s['modifiers'] for s in source_by_name[n]): return 'SEALED_REVIEW'
    if e['structure']['interfaceType']: return 'INTERFACE'
    if not any(e['structure']['modifiers']&5 for e in constructors[n]): return 'NO_PUBLIC_OR_PROTECTED_CONSTRUCTOR'
    return 'ABSTRACT_CLASS' if f&0x400 else 'EXTENSIBLE_CLASS'

evidence=collections.defaultdict(dict)
def add(target,s,kind,origin,**extra):
    row={'source':s['name'],'path':s['path'],'context':context(s['path']),'kind':kind,'origin':origin,**extra}
    if row['context']=='ORM_SUBPROJECT':
        module=s['path'].split('/src/')[0].split('/')[-1]
        module={'metamodel-generator':'hibernate-processor'}.get(module,module)
        if api[target]['artifact'].startswith(module+'-'):
            row['context']='OWNING_MODULE_IMPLEMENTATION'
    key=(row['source'],row['path'],kind,row.get('line',0),origin)
    evidence[target][key]=row

for s in sources:
    for p in set(s['resolvedParents']) & api.keys():
        add(p,s,'DIRECT_HIERARCHY','SOURCE',line=s['line'],abstract='abstract' in s['modifiers'] or s['kind']=='INTERFACE')
    for p in ancestors(s['name']) & api.keys() - set(s['resolvedParents']):
        add(p,s,'INDIRECT_HIERARCHY','SOURCE',line=s['line'])

source_missing=0; source_newer=0; bytecode_confirmed=0
for c in classes:
    module=c['path'].split('/target/classes/')[0]
    package=c['name'].rpartition('.')[0]
    candidates=[p for p in source_file_index.get((package,c['sourceFile']),()) if p.startswith(module+'/')]
    if len(candidates)!=1:
        source_missing+=1
        continue
    path=candidates[0]
    fresh=(ROOT/c['path']).stat().st_mtime >= (ROOT/path).stat().st_mtime
    if not fresh: source_newer+=1
    # Named class must still occur in current parsed source. Numeric anonymous/local
    # binary names cannot be fully matched without compilation; mark their evidence.
    anonymous=bool(re.search(r'\$\d',c['name']))
    if c['name'] not in source_by_name and not anonymous:
        source_missing+=1
        continue
    bytecode_confirmed+=1
    s={'name':c['name'],'path':path}
    extra={'bytecodeSourceNewer':not fresh,'anonymousOrLocal':anonymous}
    for l in c['lambdas']:
        if l['target'] in api:
            add(l['target'],s,'LAMBDA_OR_METHOD_REFERENCE','BYTECODE',line=l['line'],method=l['method'],**extra)
    for p in set([c['super']]+c['interfaces']) & api.keys():
        add(p,s,'DIRECT_HIERARCHY','BYTECODE',**extra)
    own={(m['name'],m['desc']) for m in c['methods'] if m['name'] not in ('<init>','<clinit>') and not m['access']&0x40a}
    bytecode_parents=set(p for p in [c['super']]+c['interfaces'] if p)
    inherited=set(bytecode_parents)
    for p in bytecode_parents: inherited.update(ancestors(p))
    for p in inherited & api.keys():
        parent=class_by_name.get(p)
        if not parent: continue
        overridden=[m['name']+m['desc'] for m in parent['methods'] if m['name'] not in ('<init>','<clinit>')
                    and (m['access']&5) and not m['access']&0x1a and (m['name'],m['desc']) in own]
        if overridden: add(p,s,'OVERRIDE_OR_IMPLEMENTATION_SIGNATURE','BYTECODE',methods=overridden,**extra)

rows=[]
for n,e in sorted(api.items()):
    sh=shape(n,e)
    ev=sorted(evidence[n].values(),key=lambda v:(v['context'],v['path'],v['source'],v['kind'],v['origin']))
    direct=[v for v in ev if v['kind'] in ('DIRECT_HIERARCHY','LAMBDA_OR_METHOD_REFERENCE')]
    contexts=sorted({v['context'] for v in direct})
    eligible=sh in ('INTERFACE','ABSTRACT_CLASS','EXTENSIBLE_CLASS','SEALED_REVIEW')
    if not eligible: proposal='OUTSIDE_ORDINARY_EXTENSION_REVIEW'
    elif any(v['context'] in ('DOCUMENTATION','PROVIDER_FIXTURE') for v in direct): proposal='IMPLEMENT_CANDIDATE_STRONGER_CONTEXT'
    elif any(v['context'] in ('TEST','ORM_SUBPROJECT','TEST_INFRASTRUCTURE') for v in direct): proposal='IMPLEMENT_CANDIDATE_REVIEW_EVIDENCE'
    elif direct: proposal='UNREVIEWED_OWNER_OR_BENCHMARK_ONLY'
    else: proposal='UNREVIEWED_NO_DIRECT_EVIDENCE'
    roots=sorted({s['path'] for s in source_by_name[n]})
    rows.append({'type':n,'shape':sh,'lifecycle':e.get('lifecycle',[]),'artifact':e['artifact'],
                 'compatibilityArtifact':e['artifact'] in {a['fileName'] for a in metadata['artifacts']},
                 'proposal':proposal,'contexts':contexts,'sourceFiles':roots,
                 'mixedCategoryMembers':mixed_members[n],'evidence':ev})

stats={'apiTypes':len(rows),'shapes':dict(collections.Counter(r['shape'] for r in rows)),
       'proposals':dict(collections.Counter(r['proposal'] for r in rows)),
       'artifacts':dict(collections.Counter(r['artifact'] for r in rows)),
       'eligible':sum(r['shape'] in ('INTERFACE','ABSTRACT_CLASS','EXTENSIBLE_CLASS','SEALED_REVIEW') for r in rows),
       'eligibleIncubating':sum(r['shape'] in ('INTERFACE','ABSTRACT_CLASS','EXTENSIBLE_CLASS','SEALED_REVIEW') and 'INCUBATING' in r['lifecycle'] for r in rows),
       'eligibleCompatibilityArtifacts':sum(r['shape'] in ('INTERFACE','ABSTRACT_CLASS','EXTENSIBLE_CLASS','SEALED_REVIEW') and r['compatibilityArtifact'] for r in rows),
       'sourceDeclarations':len(sources),'sourceFiles':len((SCRATCH/'sources.list').read_text().splitlines()),
       'bytecodeClasses':len(classes),'bytecodeWithUniqueCurrentSource':bytecode_confirmed,
       'bytecodeOmittedSourceMissingOrAmbiguous':source_missing,'bytecodeSourceNewer':source_newer,
       'sourceParentReferencesUnresolved':len(unresolved),
       'head':subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip(),
       'metadataSourceVersion':metadata['sourceVersion'],
       'metadataSha256':hashlib.sha256(metadata_path.read_bytes()).hexdigest()}
(OUT/'inventory.json').write_text(json.dumps({'summary':stats,'types':rows},indent=2)+'\n')
(OUT/'unresolved-source-parents.json').write_text(json.dumps(unresolved,indent=2)+'\n')
(OUT/'summary.json').write_text(json.dumps(stats,indent=2)+'\n')
lines=['# Preliminary API role inventory','','Generated evidence, not approved role classification. No absence-of-evidence inference of USE-only.','',
       '## Summary','', '```json',json.dumps(stats,indent=2),'```','',
       '## Complete type inventory','', '| Type | Shape | Lifecycle | Preliminary disposition | Direct evidence contexts |', '|---|---|---|---|---|']
for r in rows:
    n=r['type']
    label=f'`{n}`'
    if r['sourceFiles']: label=f'[{n}]({ROOT/r["sourceFiles"][0]})'
    lines.append('| '+' | '.join([label,r['shape'],', '.join(r['lifecycle']) or 'stable',r['proposal'],', '.join(r['contexts']) or 'none'])+' |')
lines += ['', '## Evidence for extension-review candidates', '', 'Full machine-readable evidence is in `inventory.json`. Source relationships are parse-only, import-resolved observations. Bytecode is supplementary and may predate source changes.']
for r in rows:
    if r['proposal']=='OUTSIDE_ORDINARY_EXTENSION_REVIEW': continue
    lines += ['', '### '+r['type'], '',r['proposal']+'; '+r['shape']+'.','']
    ds=[v for v in r['evidence'] if v['kind'] in ('DIRECT_HIERARCHY','LAMBDA_OR_METHOD_REFERENCE')]
    if not ds: lines+=['No direct implementation evidence found; implementation intent remains unreviewed.']
    seen=set()
    for ev in ds:
        key=(ev['source'],ev['path'],ev['kind'])
        if key in seen: continue
        seen.add(key)
        line=ev.get('line',1) or 1
        lines.append(f'- {ev["context"]}, {ev["kind"]}, {ev["origin"]}: [{ev["source"]}]({ROOT/ev["path"]}:{line})')
(OUT/'inventory.md').write_text('\n'.join(lines)+'\n')
print(json.dumps(stats,indent=2))
print('\nEXTENSION CANDIDATES WITH NON-CORE DIRECT EVIDENCE')
for r in rows:
    if r['proposal'].startswith('IMPLEMENT_CANDIDATE'):
        print(r['type'],r['shape'],','.join(r['contexts']))
