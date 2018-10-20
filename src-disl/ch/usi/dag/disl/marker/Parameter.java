package ch.usi.dag.disl.marker;

/**
 * Used for marker parameter parsing.
 */
public final class Parameter {

    protected String __value;

    protected String __delimiter;

    /**
     * Create parameter with a value.
     */
    public Parameter(final String value) {
        __value = value;
    }

    /**
     * Set delimiter for multi-value parsing.
     */
    public void setMultipleValDelim(final String delim) {
        __delimiter = delim;
    }

    /**
     * Get parameter value.
     */
    public String getValue() {
        return __value;
    }

    /**
     * @return Array of values split using a predefined delimiter.
     */
    public String [] getMultipleValues () {
        return __value.split (__delimiter);
    }
}
