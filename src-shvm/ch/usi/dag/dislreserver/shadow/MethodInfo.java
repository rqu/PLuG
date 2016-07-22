package ch.usi.dag.dislreserver.shadow;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;


public final class MethodInfo {

    private final MethodNode __methodNode;

    //

    MethodInfo (final MethodNode methodNode) {
        __methodNode = methodNode;
    }

    //

    public String getName () {
        return __methodNode.name;
    }


    public int getModifiers () {
        return __methodNode.access;
    }


    public String getDescriptor () {
        return __methodNode.desc;
    }


    public String getReturnDescriptor () {
        return Type.getReturnType (__methodNode.desc).getDescriptor ();
    }


    public String [] getParameterDescriptors () {
        return Arrays.stream (
            Type.getArgumentTypes (__methodNode.desc)
        ).map (Type::getDescriptor).toArray (String []::new);
    }


    public String [] getExceptionDescriptors () {
        final List <String> exceptions = __methodNode.exceptions;
        return exceptions.toArray (new String [exceptions.size ()]);
    }

    //

    public boolean isPublic() {
        return Modifier.isPublic (getModifiers ());
    }

}
