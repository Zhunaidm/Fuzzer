import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;

public class Copy {
    public static void main(final String args[]) throws Exception {
        FileInputStream is = new FileInputStream(args[0]);

        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassAdapter ca = new ClassAdapter(cw);
        cr.accept(ca, 0);

        FileOutputStream fos = new FileOutputStream(args[1]);
        fos.write(cw.toByteArray());
        fos.close();
    }
}

class ClassAdapter extends ClassVisitor implements Opcodes {

    public ClassAdapter(final ClassVisitor cv) {
        super(ASM6, cv);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
            final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        return mv == null ? null : new MethodAdapter(mv);
    }
}

class MethodAdapter extends MethodVisitor implements Opcodes {

    public MethodAdapter(final MethodVisitor mv) {
        super(ASM6, mv);
    }
    public static int counter = 0;

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        /* TODO: System.err.println("BRANCH" + name); */
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("BRANCH " + counter);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        counter++;

        /* do call */
        mv.visitJumpInsn(opcode, label);        
    }
}

