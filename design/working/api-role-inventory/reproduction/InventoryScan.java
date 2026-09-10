import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.tools.*;
import com.sun.source.tree.*;
import com.sun.source.util.*;
import org.objectweb.asm.*;

/// Temporary, read-only bytecode and parse-only source inventory.
/// No classes are loaded or annotation processors executed.
/// @author Steve Ebersole
public class InventoryScan {
    static String q(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }
    static String arr(Collection<String> s) { return "[" + String.join(",", s.stream().map(InventoryScan::q).toList()) + "]"; }
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        if (args[1].equals("sources")) { sources(root, Path.of(args[2]), Path.of(args[3])); return; }
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Path.of(args[3])))) {
            for (String relative : Files.readAllLines(Path.of(args[2]))) {
                Path file = root.resolve(relative);
                var methods = new ArrayList<String>();
                var lambdas = new ArrayList<String>();
                var permitted = new ArrayList<String>();
                ClassReader reader = new ClassReader(Files.readAllBytes(file));
                reader.accept(new ClassVisitor(Opcodes.ASM9) {
                    String name, parent, source, outer;
                    String[] interfaces;
                    int access;
                    public void visit(int version, int flags, String n, String sig, String p, String[] is) {
                        name = n.replace('/', '.'); parent = p == null ? null : p.replace('/', '.');
                        interfaces = Arrays.stream(is).map(s -> s.replace('/', '.')).toArray(String[]::new);
                        access = flags;
                    }
                    public void visitSource(String s, String debug) { source = s; }
                    public void visitInnerClass(String n, String o, String inner, int flags) {
                        if (n.replace('/', '.').equals(name)) {
                            access = flags; outer = o == null ? null : o.replace('/', '.');
                        }
                    }
                    public void visitPermittedSubclass(String s) { permitted.add(s.replace('/', '.')); }
                    public MethodVisitor visitMethod(int flags, String n, String d, String sig, String[] ex) {
                        methods.add("{\"name\":"+q(n)+",\"desc\":"+q(d)+",\"access\":"+flags+"}");
                        return new MethodVisitor(Opcodes.ASM9) {
                            int line;
                            public void visitLineNumber(int l, Label start) { line = l; }
                            public void visitInvokeDynamicInsn(String dn, String dd, Handle b, Object... ba) {
                                if (!b.getOwner().equals("java/lang/invoke/LambdaMetafactory")) return;
                                Type type = Type.getReturnType(dd);
                                if (type.getSort() != Type.OBJECT) return;
                                lambdas.add("{\"target\":"+q(type.getClassName())+",\"method\":"+q(n+d)
                                        +",\"line\":"+line+"}");
                            }
                        };
                    }
                    public void visitEnd() {
                        out.println("{\"name\":"+q(name)+",\"super\":"+q(parent)+",\"sourceFile\":"+q(source)
                                +",\"interfaces\":"+arr(Arrays.asList(interfaces))+",\"access\":"+access
                                +",\"outer\":"+q(outer)+",\"permitted\":"+arr(permitted)+",\"path\":"+q(relative)
                                +",\"methods\":["+String.join(",",methods)+"],\"lambdas\":["+String.join(",",lambdas)+"]}");
                    }
                }, ClassReader.SKIP_FRAMES);
            }
        }
    }
    static void sources(Path root, Path paths, Path output) throws Exception {
        var compiler = ToolProvider.getSystemJavaCompiler();
        int errors = 0, count = 0;
        try (var manager = compiler.getStandardFileManager(null, null, null);
             var out = new PrintWriter(Files.newBufferedWriter(output))) {
            // Parse only: no symbol resolution, code generation, or annotation processing.
            for (String relative : Files.readAllLines(paths)) {
                var diagnostics = new DiagnosticCollector<JavaFileObject>();
                var task = (JavacTask)compiler.getTask(null, manager, diagnostics, List.of("-proc:none"), null,
                        manager.getJavaFileObjects(root.resolve(relative).toFile()));
                var trees = Trees.instance(task);
                for (CompilationUnitTree unit : task.parse()) {
                    String pkg = unit.getPackageName() == null ? "" : unit.getPackageName().toString();
                    List<String> imports = unit.getImports().stream().filter(i -> !i.isStatic())
                            .map(i -> i.getQualifiedIdentifier().toString()).toList();
                    new TreePathScanner<Void, String>() {
                        public Void visitClass(ClassTree node, String enclosing) {
                            long pos = trees.getSourcePositions().getStartPosition(unit, node);
                            long line = pos < 0 ? 0 : unit.getLineMap().getLineNumber(pos);
                            String simple = node.getSimpleName().toString();
                            if (simple.isEmpty()) simple = "<anonymous@"+line+">";
                            String name = enclosing == null ? (pkg.isEmpty()?simple:pkg+"."+simple) : enclosing+"$"+simple;
                            var parents = new ArrayList<String>();
                            if (node.getExtendsClause() != null) parents.add(node.getExtendsClause().toString());
                            node.getImplementsClause().forEach(t -> parents.add(t.toString()));
                            out.println("{\"name\":"+q(name)+",\"package\":"+q(pkg)+",\"imports\":"+arr(imports)
                                    +",\"parents\":"+arr(parents)+",\"path\":"+q(relative)+",\"line\":"+line
                                    +",\"kind\":"+q(node.getKind().name())+",\"modifiers\":"+q(node.getModifiers().toString())+"}");
                            return super.visitClass(node, name);
                        }
                    }.scan(unit, null);
                }
                long n = diagnostics.getDiagnostics().stream().filter(d -> d.getKind()==Diagnostic.Kind.ERROR).count();
                if (n>0) { errors++; System.err.println("PARSE_ERRORS "+relative+" "+n); }
                count++;
            }
        }
        System.err.println("Parsed "+count+" Java sources; "+errors+" files had diagnostics");
    }
}
