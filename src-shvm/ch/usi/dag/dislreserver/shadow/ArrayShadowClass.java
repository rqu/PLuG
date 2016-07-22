package ch.usi.dag.dislreserver.shadow;

import java.util.Arrays;

import org.objectweb.asm.Type;

import ch.usi.dag.dislreserver.DiSLREServerFatalException;


public class ArrayShadowClass extends ShadowClass {

    private final Type __type;

    private final ShadowClass __superClass;

    private final ShadowClass __componentClass;


    //

    ArrayShadowClass (
        final long netReference, final ShadowObject classLoader,
        final ShadowClass superClass, final ShadowClass componentClass,
        final Type type
    ) {
        super (netReference, classLoader);

        __type = type;
        __superClass = superClass;
        __componentClass = componentClass;
    }


    @Override
    public boolean isArray () {
        return true;
    }


    public int getArrayDimensions () {
        return __type.getDimensions ();
    }


    @Override
    public ShadowClass getComponentType () {
        // return arrayComponentClass;
        throw new DiSLREServerFatalException ("ArrayShadowClass.getComponentType not implemented");
    }


    @Override
    public boolean isInstance (final ShadowObject obj) {
        return equals (obj.getShadowClass ());
    }


    @Override
    public boolean isAssignableFrom (final ShadowClass klass) {
        return
            equals (klass)
            ||
            (
                (klass instanceof ArrayShadowClass)
                &&
                __componentClass.isAssignableFrom (klass.getComponentType ())
            );
    }


    @Override
    public boolean isInterface () {
        return false;
    }


    @Override
    public boolean isPrimitive () {
        return false;
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
        return __type.getDescriptor ().replace ('/', '.');
    }


    @Override
    public String getCanonicalName () {
        return __type.getClassName ();
    }


    @Override
    public String [] getInterfaces () {
        return new String [] { "java.lang.Cloneable", "java.io.Serializable" };
    }


    @Override
    public String getPackage () {
        return null;
    }


    @Override
    public ShadowClass getSuperclass () {
        return __superClass;
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
        return getSuperclass ().getMethods ();
    }


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
    public FieldInfo getDeclaredField (final String fieldName)
    throws NoSuchFieldException {
        throw new NoSuchFieldException (__type.getClassName () + "." + fieldName);
    }


    @Override
    public MethodInfo [] getDeclaredMethods () {
        return new MethodInfo [0];
    }


    @Override
    public MethodInfo getDeclaredMethod (final String methodName,
        final String [] argumentNames)
    throws NoSuchMethodException {
        throw new NoSuchMethodException (
            __type.getClassName () + "." + methodName + argumentNamesToString (argumentNames)
        );
    }

}
