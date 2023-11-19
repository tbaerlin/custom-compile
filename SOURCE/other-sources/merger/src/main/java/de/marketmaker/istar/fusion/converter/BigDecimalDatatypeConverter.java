package de.marketmaker.istar.fusion.converter;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BigDecimalDatatypeConverter {
    public static String marshal(BigDecimal value) {
        throw new UnsupportedOperationException("not needed");
    }

    public static BigDecimal unmarshal(String value) {
        return new BigDecimal(value);
    }
}