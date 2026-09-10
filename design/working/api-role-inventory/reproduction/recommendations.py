"""Attach explicitly reviewed preliminary recommendations to generated evidence."""
import json
import pathlib
import shutil
import collections

root=pathlib.Path.cwd()
out=root/'design/working/api-role-inventory'
data=json.loads((out/'inventory.json').read_text())
rows={r['type']:r for r in data['types']}
strong={
 'org.hibernate.jdbc.Work':('hibernate-core/src/main/java/org/hibernate/jdbc/Work.java',11,'Documented callback supplied to Session.doWork; concrete and lambda test implementations.'),
 'org.hibernate.jdbc.ReturningWork':('hibernate-core/src/main/java/org/hibernate/jdbc/ReturningWork.java',11,'Documented callback supplied to Session.doReturningWork; concrete and lambda test implementations.'),
 'org.hibernate.jdbc.AbstractWork':('hibernate-core/src/main/java/org/hibernate/jdbc/AbstractWork.java',10,'Documented abstract base for user-supplied JDBC work; test subclasses.'),
 'org.hibernate.jdbc.AbstractReturningWork':('hibernate-core/src/main/java/org/hibernate/jdbc/AbstractReturningWork.java',10,'Documented abstract base for returning JDBC work; test subclasses.'),
 'org.hibernate.Interceptor':('hibernate-core/src/main/java/org/hibernate/Interceptor.java',15,'Explicit user callback contract and registration points; dedicated interceptor tests.'),
 'org.hibernate.EntityNameResolver':('hibernate-core/src/main/java/org/hibernate/EntityNameResolver.java',8,'Documents supplying an application resolver; registration APIs and test implementations.'),
 'org.hibernate.CustomEntityDirtinessStrategy':('hibernate-core/src/main/java/org/hibernate/CustomEntityDirtinessStrategy.java',10,'Javadoc includes an application-written implementation and nested callback lambda.'),
 'org.hibernate.CustomEntityDirtinessStrategy$AttributeChecker':('hibernate-core/src/main/java/org/hibernate/CustomEntityDirtinessStrategy.java',47,'The documented custom dirtiness strategy supplies this callback through doDirtyChecking.'),
 'org.hibernate.SessionFactoryObserver':('hibernate-core/src/main/java/org/hibernate/SessionFactoryObserver.java',10,'Documented configured lifecycle observer; source explicitly discusses compatibility for existing implementations.'),
 'org.hibernate.boot.model.naming.ImplicitNamingStrategy':('hibernate-core/src/main/java/org/hibernate/boot/model/naming/ImplicitNamingStrategy.java',32,'Javadoc explicitly anticipates custom application implementations; test subclasses of the default implementation provide indirect evidence.'),
 'org.hibernate.boot.model.naming.PhysicalNamingStrategy':('hibernate-core/src/main/java/org/hibernate/boot/model/naming/PhysicalNamingStrategy.java',29,'Configured custom naming contract, exercised by user-style naming strategy tests.'),
 'org.hibernate.query.TupleTransformer':('hibernate-core/src/main/java/org/hibernate/query/TupleTransformer.java',12,'Application-supplied result callback with setter and lambda/concrete tests; currently incubating.'),
 'org.hibernate.query.ResultListTransformer':('hibernate-core/src/main/java/org/hibernate/query/ResultListTransformer.java',14,'Application-supplied list callback with setter and tests; currently incubating.'),
 'org.hibernate.query.criteria.CriteriaDefinition':('hibernate-core/src/main/java/org/hibernate/query/criteria/CriteriaDefinition.java',28,'Public documentation expressly prescribes anonymous subclassing, not just overriding an abstract callback.'),
 'org.hibernate.envers.RevisionListener':('hibernate-envers/src/main/java/org/hibernate/envers/RevisionListener.java',9,'Javadoc requires an implementation passed to RevisionEntity; integration tests supply one.'),
 'org.hibernate.envers.EntityTrackingRevisionListener':('hibernate-envers/src/main/java/org/hibernate/envers/EntityTrackingRevisionListener.java',8,'Extension of the supplied revision callback, exercised by custom revision listener tests.'),
 'org.hibernate.generator.BeforeExecutionGenerator':('hibernate-core/src/main/java/org/hibernate/generator/BeforeExecutionGenerator.java',10,'Explicit custom generator and annotation-registration contract; user-defined generator tests.'),
 'org.hibernate.generator.OnExecutionGenerator':('hibernate-core/src/main/java/org/hibernate/generator/OnExecutionGenerator.java',33,'Documents required overrides and user-written generators; generated-value tests exercise them.'),
 'org.hibernate.id.IdentifierGenerator':('hibernate-core/src/main/java/org/hibernate/id/IdentifierGenerator.java',25,'Explicitly documented extension point; custom generator tests.'),
 'org.hibernate.binder.TypeBinder':('hibernate-core/src/main/java/org/hibernate/binder/TypeBinder.java',15,'Javadoc demonstrates an application annotation with a custom Binder implementation; currently incubating.'),
 'org.hibernate.binder.AttributeBinder':('hibernate-core/src/main/java/org/hibernate/binder/AttributeBinder.java',14,'Explicit user-written annotation binding contract; currently incubating.'),
}
use_review={
 'org.hibernate.Session':'Application-facing session facade; implementations in the inventory are principally Hibernate runtime machinery. Third-party wrappers remain an open question.',
 'org.hibernate.StatelessSession':'Application-facing session facade. External wrappers and implementation promises require explicit review.',
 'org.hibernate.SessionFactory':'Factory obtained from bootstrap; custom wrappers/delegators must be considered before excluding IMPLEMENT.',
 'org.hibernate.query.Query':'Application query facade; framework adapters may implement this outside the repository.',
 'org.hibernate.query.SelectionQuery':'Query contract primarily consumed by applications; no inference of USE-only from missing external evidence.',
 'org.hibernate.query.MutationQuery':'Query contract primarily consumed by applications; implementation intent remains unreviewed.',
 'org.hibernate.boot.Metadata':'Bootstrap result consumed by applications and integrations; review wrappers and existing documentation.',
 'org.hibernate.boot.MetadataBuilder':'Bootstrap builder obtained from Hibernate; review extension and delegating-builder use before declaring USE-only.',
}
for n,r in rows.items():
    if n in strong:
        path,line,reason=strong[n]
        r['preliminaryRole']='USE_IMPLEMENT_RECOMMENDED'
        r['recommendationEvidence']={'path':path,'line':line,'reason':reason,'confidence':'HIGH','approved':False}
    elif n in use_review:
        r['preliminaryRole']='USE_ONLY_REVIEW_CANDIDATE'
        r['recommendationEvidence']={'reason':use_review[n],'confidence':'LOW','approved':False}
    elif r['proposal']=='OUTSIDE_ORDINARY_EXTENSION_REVIEW':
        r['preliminaryRole']='ORDINARY_USE_SPECIAL_OR_NONEXTENSIBLE_SHAPE'
    else:
        r['preliminaryRole']='UNREVIEWED'
    if n.startswith(('org.hibernate.grammars.','org.hibernate.dialect.function.','org.hibernate.envers.boot.model.')):
        r['categoryReviewHint']='Review whether the existing API category is intentional before assigning supported API implementation roles.'
data['summary']['highConfidenceImplementRecommendations']=len(strong)
data['summary']['useOnlyReviewCandidates']=len(use_review)
data['summary']['grammarExtensionShapes']=sum(r['type'].startswith('org.hibernate.grammars.') and r['shape'] in ('INTERFACE','EXTENSIBLE_CLASS','ABSTRACT_CLASS','SEALED_REVIEW') for r in rows.values())
data['summary']['mixedCategoryApiTypes']=sum(bool(r['mixedCategoryMembers']) for r in rows.values())
data['summary']['extensionTypesWithMixedCategoryMembers']=sum(bool(r['mixedCategoryMembers']) and r['shape'] in ('INTERFACE','EXTENSIBLE_CLASS','ABSTRACT_CLASS','SEALED_REVIEW') for r in rows.values())
data['summary']['stableExtensionTypesInCompatibilityArtifacts']=sum(r['compatibilityArtifact'] and 'INCUBATING' not in r['lifecycle'] and r['shape'] in ('INTERFACE','EXTENSIBLE_CLASS','ABSTRACT_CLASS','SEALED_REVIEW') for r in rows.values())
data['summary']['highConfidenceIncubating']=sum('INCUBATING' in rows[n]['lifecycle'] for n in strong)
data['summary']['preliminaryRoleDispositions']=dict(collections.Counter(r['preliminaryRole'] for r in rows.values()))
assert all(n in rows for n in strong.keys()|use_review.keys())
assert len(rows)==sum(data['summary']['shapes'].values())
assert sum(data['summary']['proposals'].values())==len(rows)
assert rows['org.hibernate.jdbc.Work']['shape']=='INTERFACE'
assert rows['org.hibernate.query.criteria.CriteriaDefinition']['shape']=='ABSTRACT_CLASS'
assert any(e['kind']=='LAMBDA_OR_METHOD_REFERENCE' for e in rows['org.hibernate.jdbc.Work']['evidence'])
assert rows['org.hibernate.envers.AuditReader']['proposal']=='UNREVIEWED_OWNER_OR_BENCHMARK_ONLY'
assert len(strong)==21 and len(use_review)==8
(out/'inventory.json').write_text(json.dumps(data,indent=2)+'\n')
(out/'summary.json').write_text(json.dumps(data['summary'],indent=2)+'\n')
lines=['# Preliminary API role recommendations','','These are recommendations, not approved classifications. All other extensible APIs remain unreviewed.','',
       '## Strong USE + IMPLEMENT recommendations','']
for n,(path,line,reason) in strong.items():
    r=rows[n]
    lines += [f'### {n}','','Lifecycle: '+(', '.join(r['lifecycle']) or 'stable')+'.', '', f'[{reason}]({root/path}:{line})','']
    ev=[e for e in r['evidence'] if e['context'] in ('TEST','PROVIDER_FIXTURE','TEST_INFRASTRUCTURE')]
    ev.sort(key=lambda e:(e['kind']!='DIRECT_HIERARCHY',e['origin']!='SOURCE',e['path']))
    for e in ev[:3]:
        lines.append(f'- {e["kind"]}: [{e["source"]}]({root/e["path"]}:{e.get("line",1) or 1})')
lines += ['', '## USE-only candidates requiring a contract decision','']
for n,reason in use_review.items(): lines += [f'- `{n}`: {reason}']
(out/'recommendations.md').write_text('\n'.join(lines)+'\n')
scripts=out/'reproduction'
scripts.mkdir(exist_ok=True)
for name in ('InventoryScan.java','prepare.py','analyze.py','recommendations.py'):
    source=pathlib.Path(__file__).parent/name
    if source.resolve() != (scripts/name).resolve():
        shutil.copyfile(source,scripts/name)
print(json.dumps(data['summary'],indent=2))
