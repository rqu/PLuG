package ch.usi.dag.dislreserver.shadow;

import java.util.Arrays;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;


public class MethodInfo {

    // TODO ! is this implementation of methods really working ??

    private final MethodNode __methodNode;

    private final int __modifiers;

    private final String __name;

    private final String __returnType;

    private final String [] __parameterTypes;

    private final String [] __exceptionTypes;


    public MethodInfo (final MethodNode methodNode) {
        __methodNode = methodNode;
        __name = methodNode.name;
        __modifiers = methodNode.access;
        __returnType = methodNode.desc.substring (methodNode.desc.indexOf (')') + 1);

        final Type [] parameters = Type.getArgumentTypes (methodNode.desc);
        final int size = parameters.length;
        __parameterTypes = new String [size];

        for (int i = 0; i < size; i++) {
            __parameterTypes [i] = parameters [i].getDescriptor ();
        }

        // to have "checked" array :(
        __exceptionTypes = methodNode.exceptions.toArray (new String [0]);
    }


    public MethodNode getMethodNode () {
        return __methodNode;
    }


    public String getName () {
        return __name;
    }


    public int getModifiers () {
        return __modifiers;
    }


    public String getReturnDescriptor () {
        return __returnType;
    }


    public String [] getParameterDescriptors () {
        return Arrays.copyOf (__parameterTypes, __parameterTypes.length);
    }


    public String [] getExceptionDescriptors () {
        return Arrays.copyOf (__exceptionTypes, __exceptionTypes.length);
    }


    public boolean isPublic () {
        return (__modifiers & Opcodes.ACC_PUBLIC) != 0;
    }

}
