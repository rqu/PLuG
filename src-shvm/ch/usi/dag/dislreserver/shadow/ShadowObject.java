package ch.usi.dag.dislreserver.shadow;

import java.util.Formattable;
import java.util.Formatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


public class ShadowObject implements Formattable {

    private final long netRef;

    private final long shadowId;

    private final ShadowClass shadowClass;

    private final AtomicReference <Object> shadowState;

    //


    ShadowObject (final long netReference, final ShadowClass shadowClass) {
        this.netRef = netReference;
        this.shadowId = NetReferenceHelper.get_object_id (netReference);
        this.shadowClass = shadowClass;
        this.shadowState = new AtomicReference<> ();
    }


    //

    @Deprecated
    public final long getNetRef () {
        return netRef;
    }


    public final long getId () {
        return shadowId;
    }


    public ShadowClass getShadowClass () {
        if (shadowClass != null) {
            return shadowClass;

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
        return type.cast (shadowState.get ());
    }


    public final void setState (final Object state) {
        shadowState.set (state);
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
        final Class <T> type, final Supplier <T> supplier) {
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
        if (shadowState.compareAndSet (null, supplied)) {
            return supplied;
        }

        return __getState (type);
    }


    //

    @Override
    public int hashCode () {
        // TODO Consider also the class ID, only object ID is considered now.
        return 31 + (int) (shadowId ^ (shadowId >>> 32));
    }


    @Override
    public boolean equals (final Object object) {
        if (object instanceof ShadowObject) {
            final ShadowObject that = (ShadowObject) object;
            return this.netRef == that.netRef;
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
            (shadowClass != null) ? shadowClass.getName () : "<missing>",
            shadowId
        );
    }

}
