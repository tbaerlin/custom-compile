package de.marketmaker.istar.fusion.converter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BigIntegerDatatypeConverter {
    public static String marshal(Integer value) {
        throw new UnsupportedOperationException("not needed");
    }

    public static Integer unmarshal(String value) {
        return Integer.parseInt(value);
    }
}