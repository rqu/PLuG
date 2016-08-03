package ch.usi.dag.dislreserver.shadow;

import java.lang.reflect.Modifier;

import org.objectweb.asm.Type;


final class PrimitiveShadowClass extends ShadowClass {

    PrimitiveShadowClass (
        final long netReference, final Type type,
        final ShadowObject classLoader
    ) {
        super (netReference, type, classLoader);
    }

    //

    @Override
    public int getModifiers () {
        // Primitive type classes are ABSTRACT, FINAL, and PUBLIC.
        return Modifier.ABSTRACT | Modifier.FINAL | Modifier.PUBLIC;
    }

    //

    /**
     * @see Class#isInstance(Object)
     */
    @Override
    public boolean isInstance (final ShadowObject object) {
        return false;
    }


    /**
     * @see Class#isAssignableFrom(Class)
     */
    @Override
    public boolean isAssignableFrom (final ShadowClass other) {
        return this.equals (other);
    }

    //

    /**
     * @see Class#getName()
     */
    @Override
    public String getName () {
        // Avoid Type.getInternalName() -- returns null for primitive types.
        return getCanonicalName ();
    }

	//

    /**
     * @see Class#getSuperclass()
     */
    @Override
    public ShadowClass getSuperclass () {
        return null;
    }


    @Override
    public String [] getInterfaces () {
        return new String [0];
    }

    //

    @Override
    public FieldInfo getField (final String fieldName) throws NoSuchFieldException {
        throw new NoSuchFieldException (getCanonicalName () + "." + fieldName);
    }


    @Override
    public FieldInfo [] getFields () {
        return new FieldInfo [0];
    }

    //

    @Override
    public FieldInfo getDeclaredField (final String fieldName) throws NoSuchFieldException {
        throw new NoSuchFieldException (getCanonicalName () + "." + fieldName);
    }


    @Override
    public FieldInfo [] getDeclaredFields () {
        return new FieldInfo [0];
    }

    //

    @Override
    public MethodInfo [] getMethods () {
        return new MethodInfo [0];
    }


    @Override
    public MethodInfo getMethod (
        final String methodName, final String [] argumentNames
    ) throws NoSuchMethodException {
        throw new NoSuchMethodException (
            getCanonicalName () + "." + methodName + _descriptorsToString (argumentNames)
        );
    }

    //

    @Override
    public MethodInfo [] getDeclaredMethods () {
        return new MethodInfo [0];
    }


    @Override
    public MethodInfo getDeclaredMethod (
        final String methodName, final String [] argumentNames
    ) throws NoSuchMethodException {
        throw new NoSuchMethodException (
            getCanonicalName () + "." + methodName + _descriptorsToString (argumentNames)
        );
    }

    //

    @Override
    public String [] getDeclaredClasses () {
        return new String [0];
    }

}
