package ch.usi.dag.dislreserver.shadow;

import java.util.Formatter;
import java.util.concurrent.atomic.AtomicReference;


// TODO ShadowString should better handle if String data are not send
// over network - throw a runtime exception ??
public final class ShadowString extends ShadowObject {

    private final AtomicReference <String> __value;

    //

    ShadowString (
        final long netReference, final ShadowClass shadowClass
    ) {
        this (netReference, shadowClass, null);
    }

    ShadowString (
        final long netReference, final ShadowClass shadowClass,
        final String value
    ) {
        super (netReference, shadowClass);
        __value = new AtomicReference <> (value);
    }

    //

    // TODO warn user that it will return null when the ShadowString is not yet sent.
    @Override
    public String toString () {
        return __value.get ();
    }


    void setValue (final String value) {
        __value.updateAndGet (v -> value);
    }

    //

    @Override
    public boolean equals (final Object obj) {
        // FIXME LB: This needs a comment!
        if (super.equals (obj)) {
            if (obj instanceof ShadowString) {
                final ShadowString that = (ShadowString) obj;
                if (__value != null) {
                    return __value.equals (that.__value);
                }
            }
        }

        return false;
    }


    @Override
    public int hashCode () {
        //
        // If two shadow strings are considered equal by the above equals()
        // method, then they certainly have the same hash code, because it
        // is derived from objectId (which in turn is derived from object tag,
        // a.k.a. net reference) that is ensured to be equal by the call to
        // super.equals().
        //
        // If they are not equal, nobody cares about the hash code.
        //
        return super.hashCode ();
    }


    //

    @Override
    public void formatTo (
        final Formatter formatter,
        final int flags, final int width, final int precision
    ) {
        super.formatTo (formatter, flags, width, precision);
        if (__value != null) {
            formatter.format (" <%s>", __value);
        }
    }

}
