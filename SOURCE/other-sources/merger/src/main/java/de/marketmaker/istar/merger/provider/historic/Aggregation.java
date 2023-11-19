/*
 * Aggregation.java
 *
 * Created on 13.08.2009 16:45:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum Aggregation {
    FIRST,
    MIN,
    MAX,
    LAST,
    SUM;

    public double aggregate(double[] values, int from, int to) {
        switch (this) {
            case FIRST:
                return firstDefined(values, from, to, Double.NaN);
            case LAST:
                return lastDefined(values, from, to, Double.NaN);
            case MIN:
                return minDefined(values, from, to, Double.NaN);
            case MAX:
                return maxDefined(values, from, to, Double.NaN);
            case SUM:
                return sumDefined(values, from, to, Double.NaN);
            default:
                throw new UnsupportedOperationException("no support for: " + name());
        }
    }

    private static double firstDefined(double[] values, int from, int to, double defaultVal) {
        final int i = firstDefinedAt(values, from, to);
        return i < to ? values[i] : defaultVal;
    }

    private static double lastDefined(double[] values, int from, int to, double defaultVal) {
        for (int i = to; i-- > from; ) {
            if (!Double.isNaN(values[i])) {
                return values[i];
            }
        }
        return defaultVal;
    }

    private static double minDefined(double[] values, int from, int to, double defaultVal) {
        final int i = firstDefinedAt(values, from, to);
        if (i == to) {
            return defaultVal;
        }
        double result = values[i];
        for (int j = i + 1; j < to; j++) {
            final double value = values[j];
            if (!Double.isNaN(value) && value < result) {
                result = value;
            }
        }
        return result;
    }

    private static double maxDefined(double[] values, int from, int to, double defaultVal) {
        final int i = firstDefinedAt(values, from, to);
        if (i == to) {
            return defaultVal;
        }
        double result = values[i];
        for (int j = i + 1; j < to; j++) {
            final double value = values[j];
            if (!Double.isNaN(value) && value > result) {
                result = value;
            }
        }
        return result;
    }

    private static double sumDefined(double[] values, int from, int to, double defaultVal) {
        final int i = firstDefinedAt(values, from, to);
        if (i == to) {
            return defaultVal;
        }
        double result = values[i];
        for (int j = i + 1; j < to; j++) {
            final double value = values[j];
            if (!Double.isNaN(value)) {
                result += value;
            }
        }
        return result;
    }

    private static int firstDefinedAt(double[] values, int from, int to) {
        for (int i = from; i < to; i++) {
            if (!Double.isNaN(values[i])) {
                return i;
            }
        }
        return to;
    }
}
