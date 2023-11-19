/*
 * AbstractDeserializerState.java
 *
 * Created on 17.09.2010 12:23:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

/**
 * @author oflege
 */
abstract class AbstractDeserializerState {
    /**
     * value is a decimal that uses oldExponent and has to be rescaled so that it uses newExponent.
     * @param value to be rescaled
     * @param oldExponent currently used for value
     * @param newExponent to be used for value in result
     * @return rescaled value
     */
    protected long rescale(long value, int oldExponent, int newExponent) {
        if (oldExponent == newExponent) {
            return value;
        }
        long result = value;
        if (oldExponent < newExponent) {
            for (int i = oldExponent; i < newExponent; i++) {
                result *= 10;
            }
        }
        else {
            for (int i = oldExponent; i > newExponent; i--) {
                result /= 10;
            }
        }
        return result;
    }
}
