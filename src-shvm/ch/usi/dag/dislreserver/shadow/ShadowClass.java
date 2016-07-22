package ch.usi.dag.dislreserver.shadow;

import java.util.Formatter;


public abstract class ShadowClass extends ShadowObject {

    private final ShadowObject __classLoader;

    //

    protected ShadowClass (
        final long netReference, final ShadowObject classLoader
    ) {
        super (netReference, null);
        __classLoader = classLoader;
    }

    //


    // No need to expose the interface to user
    // getId() should be sufficient
    protected final int getClassId () {
        return NetReferenceHelper.get_class_id (getNetRef ());
    }


    public final ShadowObject getShadowClassLoader () {
        return __classLoader;
    }


    public abstract boolean isArray ();


    public abstract ShadowClass getComponentType ();


    public abstract boolean isInstance (ShadowObject obj);


    public abstract boolean isAssignableFrom (ShadowClass klass);


    public abstract boolean isInterface ();


    public abstract boolean isPrimitive ();


    public abstract boolean isAnnotation ();


    public abstract boolean isSynthetic ();


    public abstract boolean isEnum ();


    public abstract String getName ();


    public abstract String getCanonicalName ();


    public abstract String [] getInterfaces ();


    public abstract String getPackage ();


    public abstract ShadowClass getSuperclass ();


    //

    @Override
    public boolean equals (final Object object) {
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

    public abstract FieldInfo [] getFields ();


    public abstract FieldInfo getField (String fieldName)
    throws NoSuchFieldException;


    public abstract MethodInfo [] getMethods ();


    public abstract MethodInfo getMethod (
        String methodName, String [] argumentNames
    ) throws NoSuchMethodException;


    public abstract String [] getDeclaredClasses ();


    public abstract FieldInfo [] getDeclaredFields ();


    public abstract FieldInfo getDeclaredField (String fieldName)
    throws NoSuchFieldException;


    public abstract MethodInfo [] getDeclaredMethods ();


    public abstract MethodInfo getDeclaredMethod (
        String methodName, String [] argumentNames
    ) throws NoSuchMethodException;


    public MethodInfo getMethod (
        final String methodName, final ShadowClass [] arguments
    ) throws NoSuchMethodException {
        return getMethod (methodName, classesToStrings (arguments));
    }


    public MethodInfo getDeclaredMethod (
        final String methodName, final ShadowClass [] arguments
    ) throws NoSuchMethodException {
        return getDeclaredMethod (methodName, classesToStrings (arguments));
    }


    protected static String [] classesToStrings (final ShadowClass [] arguments) {
        if (arguments == null) {
            return new String [0];
        }

        final int size = arguments.length;
        final String [] argumentNames = new String [size];

        for (int i = 0; i < size; i++) {
            argumentNames [i] = arguments [i].getName ();
        }

        return argumentNames;
    }


    protected static String argumentNamesToString (final String [] argumentNames) {
        final StringBuilder buf = new StringBuilder ();
        buf.append ("(");

        if (argumentNames != null) {
            for (int i = 0; i < argumentNames.length; i++) {
                if (i > 0) {
                    buf.append (", ");
                }

                buf.append (argumentNames [i]);
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
