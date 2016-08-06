package ch.usi.dag.dislreserver.shadow;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;


final class ObjectShadowClass extends ShadowClass {

    private final ShadowClass __superClass;

    private final ClassNode __classNode;

    //

    ObjectShadowClass (
        final long netReference, final Type type,
        final ShadowObject classLoader, final ShadowClass superClass,
        final ClassNode classNode
    ) {
        super (netReference, type, classLoader);

        __superClass = superClass;
        __classNode = classNode;
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
        // Strip modifiers that are not valid for a class.
        return __classNode.access & Modifier.classModifiers ();
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
        return __typeDescriptors (__classNode.interfaces.stream ());
    }

    //

    @Override
    protected Stream <FieldInfo> _declaredFields () {
        return __classNode.fields.stream ().map (FieldInfo::new);
    }


    @Override
    protected Stream <MethodInfo> _declaredMethods () {
        return __classNode.methods.stream ().map (MethodInfo::new);
    }

    //

    @Override
    public String [] getDeclaredClassDescriptors () {
        return __typeDescriptors (
            __classNode.innerClasses.stream ().map (icn -> icn.name)
        );
    }

    //

    private static String [] __typeDescriptors (final Stream <String> names) {
        return names.map (n -> Type.getObjectType (n).getDescriptor ()).toArray (String []::new);
    }

}
