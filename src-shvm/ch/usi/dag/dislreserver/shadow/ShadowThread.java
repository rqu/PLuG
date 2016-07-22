package ch.usi.dag.dislreserver.shadow;

import java.util.Formattable;
import java.util.Formatter;


// TODO ShadowTrhead should better handle if String data are not send
// over network - throw a runtime exception ??
public class ShadowThread extends ShadowObject implements Formattable {

    private String __name;

    private boolean __isDaemon;


    public ShadowThread (
        final long netReference, final String name,
        final boolean isDaemon, final ShadowClass klass
    ) {
        super (netReference, klass);

        __name = name;
        __isDaemon = isDaemon;
    }


    // TODO warn user that it will return null when the ShadowThread is not yet sent.
    public String getName () {
        return __name;
    }


    // TODO warn user that it will return false when the ShadowThread is not yet sent.
    public boolean isDaemon () {
        return __isDaemon;
    }


    public void setName (final String name) {
        __name = name;
    }


    public void setDaemon (final boolean isDaemon) {
        __isDaemon = isDaemon;
    }


    @Override
    public boolean equals (final Object object) {
        //
        // TODO LB: Why do we need to check thread name or other fields?
        //
        if (super.equals (object)) {
            if (object instanceof ShadowThread) {
                final ShadowThread that = (ShadowThread) object;
                if (__name != null && __name.equals (that.__name)) {
                    return __isDaemon == that.__isDaemon;
                }
            }
        }

        return false;
    }


    @Override
    public int hashCode () {
        //
        // TODO LB: Check ShadowThread.hashCode() -- it's needed.
        //
        // If two shadow threads are considered equal by the above equals()
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
        formatter.format (" <%s>", (__name != null) ? __name : "unknown");
    }

}
