package ch.usi.dag.dislreserver.shadow;

import org.objectweb.asm.Type;


final class PrimitiveShadowClass extends ShadowClass {

    private final Type __type;

    //

    PrimitiveShadowClass (
        final long netReference, final Type type,
        final ShadowObject classLoader
    ) {
        super (netReference, classLoader);
        __type = type;
    }

    //

    @Override
    public boolean isArray () {
        return false;
    }


    @Override
    public ShadowClass getComponentType () {
        return null;
    }


    @Override
    public boolean isInstance (final ShadowObject obj) {
        return false;
    }


    @Override
    public boolean isAssignableFrom (final ShadowClass klass) {
        return equals (klass);
    }


    @Override
    public boolean isInterface () {
        return false;
    }


    @Override
    public boolean isPrimitive () {
        return true;
    }


    @Override
    public boolean isAnnotation () {
        return false;
    }


    @Override
    public boolean isSynthetic () {
        return false;
    }


    @Override
    public boolean isEnum () {
        return false;
    }


    @Override
    public String getName () {
        return __type.getClassName ();
    }


    @Override
    public String getCanonicalName () {
        return getName ();
    }


    @Override
    public String [] getInterfaces () {
        return new String [0];
    }


    @Override
    public String getPackage () {
        return null;
    }


    @Override
    public ShadowClass getSuperclass () {
        return null;
    }


    @Override
    public FieldInfo [] getFields () {
        return new FieldInfo [0];
    }


    @Override
    public FieldInfo getField (final String fieldName) throws NoSuchFieldException {
        throw new NoSuchFieldException (__type.getClassName () + "." + fieldName);
    }


    @Override
    public MethodInfo [] getMethods () {
        return new MethodInfo [0];
    }


    @Override
    public MethodInfo getMethod (
        final String methodName, final String [] argumentNames
    ) throws NoSuchMethodException {
        throw new NoSuchMethodException (
            __type.getClassName () + "." + methodName + argumentNamesToString (argumentNames)
        );
    }


    @Override
    public String [] getDeclaredClasses () {
        return new String [0];
    }


    @Override
    public FieldInfo [] getDeclaredFields () {
        return new FieldInfo [0];
    }


    @Override
    public FieldInfo getDeclaredField (final String fieldName) throws NoSuchFieldException {
        throw new NoSuchFieldException (__type.getClassName () + "." + fieldName);
    }


    @Override
    public MethodInfo [] getDeclaredMethods () {
        return new MethodInfo [0];
    }


    @Override
    public MethodInfo getDeclaredMethod (
        final String methodName, final String [] argumentNames
    ) throws NoSuchMethodException {
        throw new NoSuchMethodException (
            __type.getClassName () + "." + methodName + argumentNamesToString (argumentNames)
        );
    }

}
