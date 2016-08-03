package ch.usi.dag.dislreserver.shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.dislreserver.DiSLREServerFatalException;


final class ObjectShadowClass extends ShadowClass {

    // TODO ! is this implementation of methods really working ??

    private final ShadowClass __superClass;

    private ClassNode __classNode;

    private String __name;

    //

    ObjectShadowClass (
        final long netReference, final Type type,
        final ShadowObject classLoader, final ShadowClass superClass,
        final byte [] classCode
    ) {
        super (netReference, type, classLoader);

        __superClass = superClass;
        if (classCode == null || classCode.length == 0) {
            throw new DiSLREServerFatalException (
                "Creating class info for "+ type + " with no code provided"
            );
        }

        initializeClassInfo (classCode);
    }


    private List <MethodInfo> methods;

    private List <MethodInfo> public_methods;

    private List <FieldInfo> fields;

    private List <FieldInfo> public_fields;

    private List <String> innerclasses;


    private void initializeClassInfo (final byte [] classCode) {
        final ClassReader classReader = new ClassReader (classCode);
        __classNode = new ClassNode (Opcodes.ASM4);
        classReader.accept (__classNode, ClassReader.SKIP_DEBUG | ClassReader.EXPAND_FRAMES);

        __name = __classNode.name.replace ('/', '.');

        methods = new ArrayList <MethodInfo> (__classNode.methods.size ());
        public_methods = new LinkedList <MethodInfo> ();
        for (final MethodNode methodNode : __classNode.methods) {
            final MethodInfo methodInfo = new MethodInfo (methodNode);
            methods.add (methodInfo);

            if (methodInfo.isPublic ()) {
                public_methods.add (methodInfo);
            }
        }

        fields = new ArrayList <FieldInfo> (__classNode.fields.size ());
        public_fields = new LinkedList <FieldInfo> ();
        for (final FieldNode fieldNode : __classNode.fields) {
            final FieldInfo fieldInfo = new FieldInfo (fieldNode);
            fields.add (fieldInfo);

            if (fieldInfo.isPublic ()) {
                public_fields.add (fieldInfo);
            }
        }

        if (getSuperclass () != null) {
            for (final MethodInfo methodInfo : getSuperclass ().getMethods ()) {
                public_methods.add (methodInfo);
            }

            for (final FieldInfo fieldInfo : getSuperclass ().getFields ()) {
                public_fields.add (fieldInfo);
            }
        }

        innerclasses = new ArrayList <String> (__classNode.innerClasses.size ());
        for (final InnerClassNode innerClassNode : __classNode.innerClasses) {
            innerclasses.add (innerClassNode.name);
        }
    }


	//

    /**
     * @see Class#isInstance(Object)
     */
    @Override
    public boolean isInstance (final ShadowObject object) {
        // return equals(obj.getShadowClass());
        throw new UnsupportedOperationException ("not yet implemented");
    }


    /**
     * @see Class#isAssignableFrom(Class)
     */
    @Override
    public boolean isAssignableFrom (final ShadowClass other) {
        // while (other != null) {
        //     if (other.equals(this)) {
        //         return true;
        //     }
        //
        //     other = other.getSuperclass();
        // }
        //
        // return false;
        throw new UnsupportedOperationException ("not yet implemented");
    }

	//

    @Override
    public int getModifiers () {
        return __classNode.access;
    }

	//

    /**
     * @see Class#getSuperclass()
     */
    @Override
    public ShadowClass getSuperclass () {
        return __superClass;
    }


    @Override
    public ShadowClass [] getInterfaces () {
        throw new UnsupportedOperationException ("not yet implemented");
    }


    @Override
    public String [] getInterfaceDescriptors () {
        final List <String> interfaces = __classNode.interfaces;
        return interfaces.toArray (new String [interfaces.size ()]);
    }

	//

    @Override
    public FieldInfo [] getFields () {
        // to have "checked" array :(
        return public_fields.toArray (new FieldInfo [0]);
    }


    @Override
    public FieldInfo getField (final String fieldName) throws NoSuchFieldException {
        for (final FieldInfo fieldInfo : fields) {
            if (fieldInfo.isPublic () && fieldInfo.getName ().equals (fieldName)) {
                return fieldInfo;
            }
        }

        if (getSuperclass () == null) {
            throw new NoSuchFieldException (__name + "." + fieldName);
        }

        return getSuperclass ().getField (fieldName);
    }


    @Override
    public MethodInfo [] getMethods () {
        // to have "checked" array :(
        return public_methods.toArray (new MethodInfo [0]);
    }


    @Override
    public MethodInfo getMethod (
        final String methodName, final String [] argumentNames
    ) throws NoSuchMethodException {
        for (final MethodInfo methodInfo : public_methods) {
            if (methodName.equals (methodInfo.getName ()) &&
                Arrays.equals (argumentNames, methodInfo.getParameterDescriptors ())
            ) {
                return methodInfo;
            }
        }

        throw new NoSuchMethodException (
            __name + "." + methodName + _descriptorsToString (argumentNames)
        );
    }


    @Override
    public FieldInfo [] getDeclaredFields () {
        return fields.toArray (new FieldInfo [0]);
    }


    @Override
    public FieldInfo getDeclaredField (final String fieldName)
    throws NoSuchFieldException {

        for (final FieldInfo fieldInfo : fields) {
            if (fieldInfo.getName ().equals (fieldName)) {
                return fieldInfo;
            }
        }

        throw new NoSuchFieldException (__name + "." + fieldName);
    }


    @Override
    public MethodInfo [] getDeclaredMethods () {
        return methods.toArray (new MethodInfo [methods.size ()]);
    }

    @Override
    public MethodInfo getDeclaredMethod (
        final String methodName, final String [] argumentNames
    ) throws NoSuchMethodException {
        for (final MethodInfo methodInfo : methods) {
            if (methodName.equals (methodInfo.getName ()) &&
                Arrays.equals (argumentNames, methodInfo.getParameterDescriptors ())
            ) {
                return methodInfo;
            }
        }

        throw new NoSuchMethodException (
            __name + "." + methodName + _descriptorsToString (argumentNames)
        );
    }

	//

    @Override
    public String [] getDeclaredClassDescriptors () {
        return __classNode.innerClasses.stream ().unordered ()
            .map (icn -> Type.getObjectType (icn.name).getDescriptor ())
            .toArray (String []::new);
   }

}
