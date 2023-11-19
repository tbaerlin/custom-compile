/*
* NumberUtil.java
*
* Created on 01.10.2008 10:38:01
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.math.BigDecimal;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author Michael Lösch
 */
public class NumberUtil {

    public static double toDouble(String s) {
        return NumberFormat.getDecimalFormat().parse(s);
    }

    /**
     * Turns a formatted decimal value into a plain String representing the double, example:
     * toDoubleString("1.234,56") returns "1234.56"
     * @param s formatted double
     * @return double as String
     */
    public static String toDoubleString(String s) {
        return Double.toString(toDouble(s));
    }

    /**
     * Returns a string representation of this {@code BigDecimal} without an exponent field.
     * @param value The BigDecimal value.
     * @return The plain string (e.g. directly usable as a block parameter value) of the big decimal or null.
     */
    public static String toPlainStringValue(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    public static class Max {
        private double result;

        public Max(double start) {
            result = start;
        }

        public double add(String s) {
            return add(Double.parseDouble(s));
        }

        public double add(double f) {
            this.result = Math.max(this.result, f);
            return f;
        }

        public double getResult() {
            return this.result;
        }
    }
}
