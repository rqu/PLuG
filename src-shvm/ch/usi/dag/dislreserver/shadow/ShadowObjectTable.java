package ch.usi.dag.dislreserver.shadow;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import ch.usi.dag.dislreserver.DiSLREServerFatalException;
import ch.usi.dag.dislreserver.util.Logging;
import ch.usi.dag.util.logging.Logger;


public class ShadowObjectTable {

    private static final Logger __log = Logging.getPackageInstance ();

    //

    private static final int INITIAL_TABLE_SIZE = 10_000_000;

    private static ConcurrentHashMap <Long, ShadowObject>
        shadowObjects = new ConcurrentHashMap <> (INITIAL_TABLE_SIZE);

    //

    public static void register (final ShadowObject newObj) {
        if (newObj == null) {
            __log.warn ("attempting to register a null shadow object");
            return;
        }

        //

        final long objID = newObj.getId ();
        final ShadowObject exist = shadowObjects.putIfAbsent (objID, newObj);

        if (exist != null) {
            if (newObj.getId () == exist.getId ()) {
                if (__log.traceIsLoggable ()) {
                    __log.trace ("re-registering shadow object %x", objID);
                }

                if (newObj.equals (exist)) {
                    return;
                }

                if (newObj instanceof ShadowString) {
                    if (exist instanceof ShadowString) {
                        final ShadowString existShadowString = (ShadowString) exist;
                        final ShadowString newShadowString = (ShadowString) newObj;

                        if (existShadowString.toString () == null) {
                            existShadowString.setValue (newShadowString.toString ());
                            return;
                        }
                    }

                } else if (newObj instanceof ShadowThread) {
                    if (exist instanceof ShadowThread) {
                        final ShadowThread existShadowThread = (ShadowThread) exist;
                        final ShadowThread newShadowThread = (ShadowThread) newObj;

                        if (existShadowThread.getName () == null) {
                            existShadowThread.setName (newShadowThread.getName ());
                            existShadowThread.setDaemon (newShadowThread.isDaemon ());
                            return;
                        }
                    }
                }
            }

            throw new DiSLREServerFatalException ("Duplicated net reference");
        }
    }


    private static boolean isAssignableFromThread (ShadowClass klass) {
        while (!"java.lang.Object".equals (klass.getName ())) {
            if ("java.lang.Thread".equals (klass.getName ())) {
                return true;
            }

            klass = klass.getSuperclass ();
        }

        return false;
    }


    public static ShadowObject get (final long net_ref) {
        final long objID = NetReferenceHelper.get_object_id (net_ref);
        if (objID == 0) {
            // reserved ID for null
            return null;
        }

        ShadowObject retVal = shadowObjects.get (objID);
        if (retVal != null) {
            return retVal;
        }

        if (NetReferenceHelper.isClassInstance (objID)) {
            throw new DiSLREServerFatalException ("Unknown class instance");

        } else {
            // Only common shadow object will be generated here
            final ShadowClass klass = ShadowClassTable.get (NetReferenceHelper.get_class_id (net_ref));
            ShadowObject tmp = null;

            if ("java.lang.String".equals (klass.getName ())) {
                tmp = new ShadowString (net_ref, klass);

            } else if (isAssignableFromThread (klass)) {
                tmp = new ShadowThread (net_ref, klass);

            } else {
                tmp = new ShadowObject (net_ref, klass);
            }

            if ((retVal = shadowObjects.putIfAbsent (objID, tmp)) == null) {
                retVal = tmp;
            }

            return retVal;
        }
    }


    public static void freeShadowObject (final ShadowObject obj) {
        shadowObjects.remove (obj.getId ());
        ShadowClassTable.freeShadowObject (obj);
    }


    // TODO: find a more elegant way to allow users to traverse the shadow
    // object table
    public static Iterator <Entry <Long, ShadowObject>> getIterator () {
        return shadowObjects.entrySet ().iterator ();
    }


    public static Iterable <ShadowObject> objects () {
        return new Iterable <ShadowObject> () {
            @Override
            public Iterator <ShadowObject> iterator () {
                return shadowObjects.values ().iterator ();
            }
        };
    }


    // TODO LB: Make this interface per-shadow-world instead of static.

    public static void registerShadowThread (
        final long netReference, final String name, final boolean isDaemon
    ) {
        final int shadowClassId = NetReferenceHelper.get_class_id (netReference);
        final ShadowClass shadowClass = ShadowClassTable.get (shadowClassId);
        final ShadowThread shadowThread = new ShadowThread (
            netReference, shadowClass, name, isDaemon
        );

        register (shadowThread);
    }


    public static void registerShadowString (
        final long netReference, final String value
    ) {
        final int shadowClassId = NetReferenceHelper.get_class_id (netReference);
        final ShadowClass shadowClass = ShadowClassTable.get (shadowClassId);
        final ShadowString shadowString = new ShadowString (
            netReference, shadowClass, value
        );

        register (shadowString);
    }

}
