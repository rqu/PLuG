package ch.usi.dag.disl;

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import ch.usi.dag.disl.localvar.ThreadLocalVar;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.JavaNames;

final class TLVInserter extends ClassVisitor {

    private final Set<ThreadLocalVar> threadLocalVars;

    public TLVInserter(final ClassVisitor cv, final Set<ThreadLocalVar> tlvs) {
        super(Opcodes.ASM4, cv);
        this.threadLocalVars = tlvs;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc,
            final String sig, final String[] exceptions) {

        // add field initialization
        if (JavaNames.isConstructorName (name)) {
            return new TLVInitializer(super.visitMethod(access, name, desc,
                    sig, exceptions), access, name, desc);
        }

        return super.visitMethod(access, name, desc, sig, exceptions);
    }

    @Override
    public void visitEnd() {

        // add fields
        for (final ThreadLocalVar tlv : threadLocalVars) {
            super.visitField(Opcodes.ACC_PUBLIC, tlv.getName(),
                    tlv.getTypeAsDesc(), null, null);
        }

        super.visitEnd();
    }

    private class TLVInitializer extends AdviceAdapter {

        private TLVInitializer(final MethodVisitor mv, final int access, final String name,
                final String desc) {

            super(Opcodes.ASM4, mv, access, name, desc);
        }

        @Override
        protected void onMethodEnter() {

            final String THREAD_CLASS_NAME =
                Type.getType(Thread.class).getInternalName();
            final String CURRENTTHREAD_METHOD_NAME = "currentThread";
            final String CURRENTTHREAD_METHOD_SIG =
                "()L" + THREAD_CLASS_NAME + ";";

            // for each thread local var insert initialization
            for (final ThreadLocalVar tlv : threadLocalVars) {

                final Label getDefaultValue = new Label();
                final Label putValue = new Label();

                // put this on the stack - for putfield
                visitVarInsn(ALOAD, 0);

                // -- inherited value --
                if (tlv.isInheritable()) {

                    // put current thread instance on the stack
                    visitMethodInsn(INVOKESTATIC,
                            THREAD_CLASS_NAME,
                            CURRENTTHREAD_METHOD_NAME,
                            CURRENTTHREAD_METHOD_SIG, false);

                    // if null, go to "get default value"
                    visitJumpInsn(IFNULL, getDefaultValue);

                    // put current thread instance on the stack
                    visitMethodInsn(INVOKESTATIC,
                            THREAD_CLASS_NAME,
                            CURRENTTHREAD_METHOD_NAME,
                            CURRENTTHREAD_METHOD_SIG, false);

                    // get value from parent thread ant put it on the stack
                    visitFieldInsn(GETFIELD, THREAD_CLASS_NAME, tlv.getName(),
                            tlv.getTypeAsDesc());

                    // go to "put value"
                    visitJumpInsn(GOTO, putValue);
                }

                // -- default value --
                visitLabel(getDefaultValue);

                // put the default value on the stack
                final Object defaultVal = tlv.getDefaultValue();
                if (defaultVal != null) {
                    // default value
                    switch (tlv.getType ().getSort ()) {
                    case Type.BOOLEAN:
                        if ((Boolean) defaultVal) {
                            visitInsn (Opcodes.ICONST_1);
                        } else {
                            visitInsn (Opcodes.ICONST_0);
                        }
                        break;

                    case Type.CHAR:
                    case Type.BYTE:
                    case Type.SHORT:
                    case Type.INT:
                        final int intValue = ((Number) defaultVal).intValue ();

                        if (-1 <= intValue && intValue <= 5) {
                            // The opcodes from ICONST_M1 to ICONST_5 are
                            // consecutive.
                            visitInsn (Opcodes.ICONST_0 + intValue);
                        } else if (Byte.MIN_VALUE <= intValue
                            && intValue <= Byte.MAX_VALUE) {
                            visitIntInsn (Opcodes.BIPUSH, intValue);
                        } else if (Short.MIN_VALUE <= intValue
                            && intValue <= Short.MAX_VALUE) {
                            visitIntInsn (Opcodes.SIPUSH, intValue);
                        } else {
                            visitLdcInsn (defaultVal);
                        }
                        break;

                    case Type.LONG:
                        final long longValue = ((Long) defaultVal).longValue ();

                        if (longValue == 0) {
                            visitInsn (Opcodes.LCONST_0);
                        } else if (longValue == 1) {
                            visitInsn (Opcodes.LCONST_1);
                        } else {
                            visitLdcInsn (defaultVal);
                        }
                        break;

                    case Type.FLOAT:
                        final float floatValue = ((Float) defaultVal).floatValue ();

                        if (floatValue == 0) {
                            visitInsn (Opcodes.FCONST_0);
                        } else if (floatValue == 1) {
                            visitInsn (Opcodes.FCONST_1);
                        } else if (floatValue == 2) {
                            visitInsn (Opcodes.FCONST_2);
                        } else {
                            visitLdcInsn (defaultVal);
                        }
                        break;

                    case Type.DOUBLE:
                        final double doubleValue = ((Double) defaultVal).doubleValue ();

                        if (doubleValue == 0) {
                            visitInsn (Opcodes.DCONST_0);
                        } else if (doubleValue == 1) {
                            visitInsn (Opcodes.DCONST_1);
                        } else {
                            visitLdcInsn (defaultVal);
                        }
                        break;

                    case Type.OBJECT:
                        visitLdcInsn (defaultVal);
                    default:
                        break;
                    }
                }
                else {

                    // if object or array
                    if(AsmHelper.isReferenceType(tlv.getType())) {
                        // insert null
                        visitInsn(ACONST_NULL);
                    }
                    // if basic type
                    else {
                        // insert 0 as default
                        switch (tlv.getType ().getSort ()) {
                        case Type.BOOLEAN:
                        case Type.CHAR:
                        case Type.BYTE:
                        case Type.SHORT:
                        case Type.INT:
                            visitInsn (ICONST_0);
                            break;

                        case Type.LONG:
                            visitInsn (LCONST_0);
                            break;

                        case Type.FLOAT:
                            visitInsn (FCONST_0);
                            break;

                        case Type.DOUBLE:
                            visitInsn (DCONST_0);
                            break;
                        default:
                            break;
                        }
                    }
                }

                // -- put value to the field --
                visitLabel(putValue);

                visitFieldInsn(PUTFIELD, THREAD_CLASS_NAME, tlv.getName(),
                        tlv.getTypeAsDesc());
            }
        }
    }
}
