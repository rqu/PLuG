package ch.usi.dag.dislreserver.shadow;

import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.Type;

import ch.usi.dag.dislreserver.DiSLREServerFatalException;


public class ShadowClassTable {

    private static final int INITIAL_TABLE_SIZE = 10000;

    final static ShadowObject BOOTSTRAP_CLASSLOADER;

    static ShadowClass JAVA_LANG_CLASS;

    private static ConcurrentHashMap <ShadowObject, ConcurrentHashMap <String, byte []>> classLoaderMap;

    private static ConcurrentHashMap <Integer, ShadowClass> shadowClasses;

    static {
        BOOTSTRAP_CLASSLOADER = new ShadowObject (0, null);
        JAVA_LANG_CLASS = null;

        classLoaderMap = new ConcurrentHashMap <> (INITIAL_TABLE_SIZE);
        shadowClasses = new ConcurrentHashMap <> (INITIAL_TABLE_SIZE);

        classLoaderMap.put (BOOTSTRAP_CLASSLOADER, new ConcurrentHashMap <String, byte []> ());
    }


    public static void load (
        ShadowObject loader, final String className, final byte [] classCode, final boolean debug
    ) {
        ConcurrentHashMap <String, byte []> classNameMap;
        if (loader == null) {
            // bootstrap loader
            loader = BOOTSTRAP_CLASSLOADER;
        }

        classNameMap = classLoaderMap.get (loader);
        if (classNameMap == null) {
            final ConcurrentHashMap <String, byte []> tmp = new ConcurrentHashMap <String, byte []> ();
            if ((classNameMap = classLoaderMap.putIfAbsent (loader, tmp)) == null) {
                classNameMap = tmp;
            }
        }

        if (classNameMap.putIfAbsent (className.replace ('/', '.'), classCode) != null) {
            if (debug) {
                System.out.println ("DiSL-RE: Reloading/Redefining class "+ className);
            }
        }
    }


    public static ShadowClass newInstance (
        final long net_ref, final ShadowClass superClass, ShadowObject loader,
        final String classSignature, final String classGenericStr, final boolean debug
    ) {
        if (!NetReferenceHelper.isClassInstance (net_ref)) {
            throw new DiSLREServerFatalException ("Unknown class instance");
        }

        ShadowClass klass = null;
        final Type t = Type.getType (classSignature);

        if (t.getSort () == Type.ARRAY) {
            // TODO unknown array component type
            klass = new ArrayShadowClass (net_ref, loader, superClass, null, t);

        } else if (t.getSort () == Type.OBJECT) {
            ConcurrentHashMap <String, byte []> classNameMap;

            if (loader == null) {
                // bootstrap loader
                loader = BOOTSTRAP_CLASSLOADER;
            }

            classNameMap = classLoaderMap.get (loader);
            if (classNameMap == null) {
                throw new DiSLREServerFatalException ("Unknown class loader");
            }

            final byte [] classCode = classNameMap.get (t.getClassName ());
            if (classCode == null) {
                throw new DiSLREServerFatalException (
                    "Class "+ t.getClassName () + " has not been loaded"
                );
            }

            klass = new ObjectShadowClass (
                net_ref, classSignature, loader, superClass, classCode
            );

        } else {
            klass = new PrimitiveShadowClass (net_ref, loader, t);
        }

        final int classID = NetReferenceHelper.get_class_id (net_ref);
        final ShadowClass exist = shadowClasses.putIfAbsent (classID, klass);

        if (exist == null) {
            ShadowObjectTable.register (klass);

        } else if (!exist.equals (klass)) {
            throw new DiSLREServerFatalException ("Duplicated class ID");
        }

        if (JAVA_LANG_CLASS == null && "Ljava/lang/Class;".equals (classSignature)) {
            JAVA_LANG_CLASS = klass;
        }

        return klass;
    }


    public static ShadowClass get (final int classID) {
        if (classID == 0) {
            // reserved ID for java/lang/Class
            return null;
        }

        final ShadowClass klass = shadowClasses.get (classID);
        if (klass == null) {
            throw new DiSLREServerFatalException ("Unknown class instance");
        }

        return klass;
    }


    public static void freeShadowObject (final ShadowObject obj) {
        if (NetReferenceHelper.isClassInstance (obj.getNetRef ())) {
            final int classID = NetReferenceHelper.get_class_id (obj.getNetRef ());
            shadowClasses.remove (classID);

        } else if (classLoaderMap.keySet ().contains (obj)) {
            classLoaderMap.remove (obj);
        }
    }

}
