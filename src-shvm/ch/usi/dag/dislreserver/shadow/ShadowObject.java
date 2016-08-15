package ch.usi.dag.dislreserver.shadow;

import java.util.Formattable;
import java.util.Formatter;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import ch.usi.dag.dislreserver.DiSLREServerFatalException;


public class ShadowObject implements Formattable {

    private final long __netReference;

    /**
     * The {@link ShadowClass} of this object. If {@code null}, then this object
     * represents an instance of the {@link Class} class, or (in a singular
     * case) the bootstrap class loader.
     */
    private final ShadowClass __shadowClass;

    private final AtomicReference <Object> __shadowState;

    //

    ShadowObject (final long netReference, final ShadowClass shadowClass) {
        __netReference = netReference;
        __shadowClass = shadowClass;
        __shadowState = new AtomicReference <> ();
    }

    //

    final long getNetRef () {
        return __netReference;
    }


    public final long getId () {
        return NetReferenceHelper.getObjectId (__netReference);
    }


    public ShadowClass getShadowClass () {
        if (__shadowClass != null) {
            return __shadowClass;

        } else {
            //
            // FIXME LB: Consider not exposing the BOOTSTRAP_CLASSLOADER reference to the user.
            //
            // Then there should be no need for this hackery.
            //
            if (this.equals (ShadowClassTable.BOOTSTRAP_CLASSLOADER)) {
                throw new NullPointerException ();
            }

            return ShadowClassTable.JAVA_LANG_CLASS.get ();
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
        //
        // The equality of shadow objects should be based purely on
        // the equality of the net reference. The value of some special
        // shadow objects can be updated lazily, so we should not really
        // compare the values.
        //
        if (object instanceof ShadowObject) {
            final ShadowObject that = (ShadowObject) object;
            return __netReference == that.__netReference;
        }

        return false;
    }

    //

    final void updateFrom (final ShadowObject other) {
        //
        // When updating value from another shadow object, the net reference
        // of this and the other object must be the same.
        //
        if (__netReference != other.__netReference) {
            throw new DiSLREServerFatalException (String.format (
                "attempting to update object 0x%x using object 0x%x",
                __netReference, other.__netReference
            ));
        }

        _updateFrom (other);
    }


    /**
     * This method is intended to be override by the subclasses to implement
     * updating of a shadow object's state. The caller of this method guarantees
     * that the net reference of the object to update from will be the same
     * as the net reference of this shadow object.
     *
     * @param object
     *        the {@link ShadowObject} instance to update from.
     */
    protected void _updateFrom (final ShadowObject object) {
        // By default do nothing.
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
