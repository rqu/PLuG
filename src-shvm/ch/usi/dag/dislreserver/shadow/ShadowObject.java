package ch.usi.dag.dislreserver.shadow;

import java.util.Formattable;
import java.util.Formatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


public class ShadowObject implements Formattable {

    private final long __netReference;

    private final ShadowClass __shadowClass;

    private final AtomicReference <Object> __shadowState;

    //

    ShadowObject (final long netReference, final ShadowClass shadowClass) {
        __netReference = netReference;
        __shadowClass = shadowClass;
        __shadowState = new AtomicReference <> ();
    }

    //

    @Deprecated
    public final long getNetRef () {
        return __netReference;
    }


    public final long getId () {
        return NetReferenceHelper.getObjectId (__netReference);
    }


    public ShadowClass getShadowClass () {
        if (__shadowClass != null) {
            return __shadowClass;

        } else {
            if (this.equals (ShadowClassTable.BOOTSTRAP_CLASSLOADER)) {
                throw new NullPointerException ();
            }

            return ShadowClassTable.JAVA_LANG_CLASS;
        }
    }


    //

    public final Object getState () {
        return __getState (Object.class);
    }


    public final <T> T getState (final Class <T> type) {
        return __getState (type);
    }


    private final <T> T __getState (final Class <T> type) {
        return type.cast (__shadowState.get ());
    }


    public final void setState (final Object state) {
        __shadowState.set (state);
    }


    public final Object setStateIfAbsent (final Object state) {
        return computeStateIfAbsent (Object.class, () -> state);
    }


    public final <T> T setStateIfAbsent (final Class <T> type, final T state) {
        return computeStateIfAbsent (type, () -> state);
    }


    public final Object computeStateIfAbsent (final Supplier <Object> supplier) {
        return computeStateIfAbsent (Object.class, supplier);
    }


    public final <T> T computeStateIfAbsent (
        final Class <T> type, final Supplier <T> supplier
    ) {
        //
        // Avoid CAS if state is already present.
        // Otherwise compute new state and try to CAS the new state once.
        // If that fails, return whatever was there.
        //
        final T existing = __getState (type);
        if (existing != null) {
            return existing;
        }

        final T supplied = supplier.get ();
        if (__shadowState.compareAndSet (null, supplied)) {
            return supplied;
        }

        return __getState (type);
    }


    //

    @Override
    public int hashCode () {
        // TODO Consider also the class ID, only object ID is considered now.
        return 31 + (int) (getId () ^ (getId () >>> 32));
    }


    @Override
    public boolean equals (final Object object) {
        if (object instanceof ShadowObject) {
            final ShadowObject that = (ShadowObject) object;
            return __netReference == that.__netReference;
        }

        return false;
    }


    //

    @Override
    public void formatTo (
        final Formatter formatter,
        final int flags, final int width, final int precision
    ) {
        formatter.format (
            "%s@%x",
            (__shadowClass != null) ? __shadowClass.getName () : "<missing>",
            getId ()
        );
    }

}
