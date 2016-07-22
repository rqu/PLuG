package ch.usi.dag.dislreserver.shadow;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;


public class FieldInfo {

    // TODO ! is this implementation of methods really working ??

    private final FieldNode __fieldNode;

    private final int __modifiers;

    private final String __name;

    private final String __type;

    //

    public FieldInfo (final FieldNode fieldNode) {
        __fieldNode = fieldNode;
        __name = fieldNode.name;
        __type = fieldNode.desc;
        __modifiers = fieldNode.access;
    }


    public FieldNode getFieldNode () {
        return __fieldNode;
    }


    public String getName () {
        return __name;
    }


    public int getModifiers () {
        return __modifiers;
    }


    public String getDescriptor () {
        return __type;
    }


    public boolean isPublic () {
        return (__modifiers & Opcodes.ACC_PUBLIC) != 0;
    }

}
