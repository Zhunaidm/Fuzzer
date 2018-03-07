import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

public class Instrumenter {
    public static void main(final String args[]) throws Exception {
        FileInputStream is = new FileInputStream(args[0]);

        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassAdapter ca = new ClassAdapter(cw, cr.getClassName());
        cr.accept(ca, ClassReader.EXPAND_FRAMES);

        FileOutputStream fos = new FileOutputStream(args[1]);
        fos.write(cw.toByteArray());
        fos.close();
    }
}

class ClassAdapter extends ClassVisitor implements Opcodes {
    private String className;
    public ClassAdapter(final ClassVisitor cv, String className) {
        super(ASM6, cv);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name,
            final String desc, final String signature, final String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        return mv == null ? null : new MethodAdapter(access, desc, mv, className);
    }
}

class MethodAdapter extends LocalVariablesSorter implements Opcodes {
    private String className;

    public MethodAdapter(int access, String desc,final MethodVisitor mv, String className) {
        super(ASM6, access, desc, mv);
        this.className = className;
    }


    @Override
    public void visitCode() {
        super.visitCode();

        
       mv.visitMethodInsn(INVOKESTATIC, "Data", "resetBranches", "()V", false);


        // Do call

        mv.visitCode();

    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {


        // For every branch covered increment the branchCounter variable
        mv.visitMethodInsn(INVOKESTATIC, "Data", "incBranches", "()V", false);


        // DO call
        mv.visitJumpInsn(opcode, label);        
    }

    /*  @Override
        public void visitInsn(int opcode) {
        switch(opcode) {
        case Opcodes.IRETURN:
        case Opcodes.FRETURN:
        case Opcodes.ARETURN:
        case Opcodes.LRETURN:
        case Opcodes.DRETURN:
        case Opcodes.RETURN:
    //  mv.visitVarInsn(ALOAD, data);
    // mv.visitMethodInsn(INVOKEVIRTUAL, "Data", "incBranches", "()V", false);
    break;
    default:
        } 
        mv.visitInsn(opcode);
        } */
}

