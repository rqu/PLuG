package ch.usi.dag.dislreserver.shadow;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import org.objectweb.asm.Type;


final class ArrayShadowClass extends ShadowClass {

    private final ShadowClass __superClass;

    private final ShadowClass __componentClass;

    //

    ArrayShadowClass (
        final long netReference, final Type type,
        final ShadowObject classLoader, final ShadowClass superClass,
        final ShadowClass componentClass
    ) {
        super (netReference, type, classLoader);

        __superClass = superClass;
        __componentClass = componentClass;
    }

	//

    public int getDimensionCount () {
        return _type ().getDimensions ();
    }


    @Override
    public ShadowClass getComponentType () {
        if (__componentClass != null) {
            return __componentClass;
        }

        throw new UnsupportedOperationException ("not yet implemented");
    }


    @Override
    public String getComponentDescriptor () {
        return _type ().getElementType ().getDescriptor ();
    }

    //

    /**
     * @see Class#isInstance(Object)
     */
    @Override
    public boolean isInstance (final ShadowObject object) {
        return equals (object.getShadowClass ());
    }


    /**
     * @see Class#isAssignableFrom(Class)
     */
    @Override
    public boolean isAssignableFrom (final ShadowClass other) {
        if (this.equals (other)) {
            return true;
        }

        if (other instanceof ArrayShadowClass) {
            // This is needed until we properly implement componentType.
            if (__componentClass == null) {
                throw new UnsupportedOperationException ("component type comparison not implemented yet");
            }

            return __componentClass.isAssignableFrom (other.getComponentType ());
        }

        return false;
    }

	//

    @Override
    public int getModifiers () {
        // Array classes are ABSTRACT and FINAL, privacy depends on the
        // privacy of the component type. Until we get valid component type,
        // we will make the array classes public.
        return Modifier.ABSTRACT | Modifier.FINAL | Modifier.PUBLIC;
    }

    //

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
        return new String [] {
            Type.getType (Cloneable.class).getDescriptor (),
            Type.getType (Serializable.class).getDescriptor ()
        };
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
    public FieldInfo getDeclaredField (final String fieldName)
    throws NoSuchFieldException {
        throw new NoSuchFieldException (getCanonicalName () + "." + fieldName);
    }


    @Override
    public FieldInfo [] getDeclaredFields () {
        return new FieldInfo [0];
    }

    //

    @Override
    public MethodInfo getMethod (final String methodName, final String [] argumentNames)
    throws NoSuchMethodException {

        for (final MethodInfo methodInfo : __superClass.getMethods ()) {
            if (methodName.equals (methodInfo.getName ())
                && Arrays.equals (argumentNames, methodInfo.getParameterDescriptors ())
            ) {
                return methodInfo;
            }
        }

        throw new NoSuchMethodException (
            getCanonicalName () + "." + methodName + _descriptorsToString (argumentNames)
        );
    }


    @Override
    public MethodInfo [] getMethods () {
        return getSuperclass ().getMethods ();
    }

    //

    @Override
    public MethodInfo getDeclaredMethod (final String methodName,
        final String [] argumentNames)
    throws NoSuchMethodException {
        throw new NoSuchMethodException (
            getCanonicalName () + "." + methodName + _descriptorsToString (argumentNames)
        );
    }


    @Override
    public MethodInfo [] getDeclaredMethods () {
        return new MethodInfo [0];
    }

}
