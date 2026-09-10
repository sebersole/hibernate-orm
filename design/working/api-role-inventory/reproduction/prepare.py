import json
import pathlib
import subprocess

root = pathlib.Path.cwd()
out = root / 'target/api-role-inventory'
out.mkdir(parents=True, exist_ok=True)
files = subprocess.check_output(['rg', '--files', '--hidden', '--no-ignore', '-g', '*.class', '-g', '!**/.git/**', '-g', '!**/.gradle/**'], text=True).splitlines()
classes = sorted(p for p in files if '/target/classes/' in p and not p.startswith('local-build-'))
sources = sorted(p for p in subprocess.check_output(['git', 'ls-files', '*.java'], text=True).splitlines() if (root/p).is_file())
(out/'classes.list').write_text('\n'.join(classes)+'\n')
(out/'sources.list').write_text('\n'.join(sources)+'\n')
print(json.dumps({'class_files':len(classes),'source_files':len(sources),'output':str(out)}))
