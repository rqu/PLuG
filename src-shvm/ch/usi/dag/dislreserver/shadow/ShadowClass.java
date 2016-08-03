package ch.usi.dag.dislreserver.shadow;

import java.util.Arrays;
import java.util.Formatter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public abstract class ShadowClass extends ShadowObject {

    /**
     * The type (class) represented by this shadow class.
     */
    private final Type __type;

    private final ShadowObject __classLoader;

    //

    protected ShadowClass (
        final long netReference, final Type type,
        final ShadowObject classLoader
    ) {
        super (netReference, null /* indicates Class instance */);

        __type = type;
        __classLoader = classLoader;
    }

    //

    // No need to expose the interface to user. getId() should be sufficient
    protected final int _classId () {
        return NetReferenceHelper.getClassId (getNetRef ());
    }


    protected final Type _type () {
        return __type;
    }

    //

    @Override
    public boolean equals (final Object object) {
        //
        // Two shadow classes are considered equal if they represent the
        // same class and have been loaded by the same class loader.
        //
        if (object instanceof ShadowClass) {
            final ShadowClass that = (ShadowClass) object;
            if (this.getName ().equals (that.getName ())) {
                return this.getShadowClassLoader ().equals (
                    that.getShadowClassLoader ()
                );
            }
        }

        return false;
    }


    @Override
    public int hashCode () {
        //
        // TODO LB: Check ShadowClass.hashCode() -- it's needed.
        //
        return super.hashCode ();
    }

    //

    public final ShadowObject getShadowClassLoader () {
        //
        // Should return null for primitive types or for classes
        // loaded by the bootstrap classloader.
        //
        return __classLoader;
    }

	//

    public String getName () {
        return __type.getInternalName ().replace ('/', '.');
    }


    public String getSimpleName () {
        return __simpleName (__type.getClassName ());
    }


    private static String __simpleName (final String name) {
        // If '.' is not found, index is -1 => +1 adjustment gives index 0
        return name.substring (name.lastIndexOf ('.') + 1);
    }


    public String getCanonicalName () {
        return __type.getClassName ();
    }


    public String getPackage () {
        final String name = getCanonicalName ();
        final int lastIndex = name.lastIndexOf ('.');

        // Class.getPackage() returns null for array/primitive classes
        return (lastIndex >= 0) ? name.substring (0, lastIndex) : null;
    }

	//

    public boolean isPrimitive () {
        // We rely on the ordering of sorts in ASM Type.
        return __type.getSort () < Type.ARRAY;
    }

    public boolean isArray () {
        return __type.getSort () == Type.ARRAY;
    };

    //

    public ShadowClass getComponentType () {
        return null;
    }


    public String getComponentDescriptor () {
        return null;
    }

	//

    public abstract boolean isInstance (ShadowObject obj);


    public abstract boolean isAssignableFrom (ShadowClass klass);

	//

    public abstract int getModifiers ();


    public boolean isInterface () {
        return __hasModifier (Opcodes.ACC_INTERFACE);
    }


    public boolean isAnnotation () {
        return __hasModifier (Opcodes.ACC_ANNOTATION);
    }


    public boolean isSynthetic () {
        return __hasModifier (Opcodes.ACC_SYNTHETIC);
    }


    public boolean isEnum () {
        return __hasModifier (Opcodes.ACC_ENUM);
    }


    private boolean __hasModifier (final int flag) {
        return (getModifiers () & flag) != 0;
    }

	//

    public abstract ShadowClass getSuperclass ();


    public abstract String [] getInterfaces ();

    //

    public abstract FieldInfo getField (String fieldName)
    throws NoSuchFieldException;


    public abstract FieldInfo [] getFields ();

	//

    public abstract FieldInfo getDeclaredField (String fieldName)
    throws NoSuchFieldException;


    public abstract FieldInfo [] getDeclaredFields ();

	//

    public abstract MethodInfo getMethod (
        String methodName, String [] argumentNames
    ) throws NoSuchMethodException;


    public MethodInfo getMethod (
        final String methodName, final ShadowClass [] arguments
    ) throws NoSuchMethodException {
        return getMethod (methodName, __toDescriptors (arguments));
    }


    public abstract MethodInfo [] getMethods ();

	//

    public abstract MethodInfo getDeclaredMethod (
        String methodName, String [] argumentNames
    ) throws NoSuchMethodException;


    public MethodInfo getDeclaredMethod (
        final String methodName, final ShadowClass ... arguments
    ) throws NoSuchMethodException {
        return getDeclaredMethod (methodName, __toDescriptors (arguments));
    }


    public abstract MethodInfo [] getDeclaredMethods ();

	//

    public abstract String [] getDeclaredClasses ();

	//

    private static String [] __toDescriptors (final ShadowClass [] types) {
        if (types == null) {
            return new String [0];
        }

        return Arrays.stream (types).map (ShadowClass::getName).toArray (String []::new);
    }


    protected static String _descriptorsToString (final String [] descriptors) {
        final StringBuilder buf = new StringBuilder ();
        buf.append ("(");

        if (descriptors != null) {
            String delimiter = "";
            for (final String descriptor : descriptors) {
                buf.append (delimiter);
                buf.append (descriptor);
                delimiter = ", ";
            }
        }

        buf.append (")");
        return buf.toString ();
    }

    //

    @Override
    public void formatTo (
        final Formatter formatter,
        final int flags, final int width, final int precision
    ) {
        // FIXME LB: ShadowClass instances do not have a ShadowClass (of Class)
        formatter.format ("java.lang.Class@%d <%s>", getId (), getName ());
    }

}
