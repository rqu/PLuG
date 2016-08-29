package ch.usi.dag.dislreserver.shadow;

final class NetReferenceHelper {

    /**
     * 40-bit object (instance) identifier starting at bit 0.
     */
    private static final short OBJECT_ID_POS = 0;
    private static final long OBJECT_ID_MASK = (1L << 40) - 1;

    /**
     * 22-bit class identifier starting at bit 40.
     */
    private static final short CLASS_ID_POS = 40;
    private static final long CLASS_ID_MASK = (1L << 22) - 1;

    /**
     * 1-bit class instance flag at bit 62.
     */
    private static final short CBIT_POS = 62;
    private static final long CBIT_MASK = (1L << 1) - 1;

    /**
     * 1-bit special payload flag at bit 63. Indicates that the object has
     * extra payload attached.
     */
    private static final short SPEC_POS = 63;
    private static final long SPEC_MASK = (1L << 1) - 1;

    //

    static long getUniqueId (final long netReference) {
        // The mask used here needs to have an absolute position.
        return netReference & ~(1L << SPEC_POS);
    }


    static long getObjectId (final long netReference) {
        return __bits (netReference, OBJECT_ID_POS, OBJECT_ID_MASK);
    }


    static int getClassId (final long netReference) {
        return (int) __bits (netReference, CLASS_ID_POS, CLASS_ID_MASK);
    }


    static boolean isClassInstance (final long netReference) {
        return __bits (netReference, CBIT_POS, CBIT_MASK) != 0;
    }


    static boolean isSpecial (final long netReference) {
        return __bits (netReference, SPEC_POS, SPEC_MASK) != 0;
    }

    //

    /**
     * Returns bits from the given {@code long} value shifted to the right
     * by a given amount and masked using the given mask.
     */
    private static long __bits (
        final long value, final short shift, final long mask
    ) {
        return (value >> shift) & mask;
    }

}
