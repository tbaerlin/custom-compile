/*
 * ProtobufPriceState.java
 *
 * Created on 01.12.2009 17:32:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.protobuf;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;

/**
 * Protobuf helper for encoding prices.
 *
 * Prices are encoded as long values with an additonal exponent field.
 * Price fields (w/ name 'xyz') need an additional field w/ name 'exponent_xyz' to encode
 * the exponent.
 *
 * Price are encoded as increment values to exploit protobuf's stop-bit-encoding,
 * exponents are encoded as literal values as the valid range is small (0..9).
 * After null values, the exponent is still sent as incremental value, whereas the first non-null
 * value for a price after a null value is sent as absolute number (or: difference to 0).
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class ProtobufPriceState {
    private static final int MAX_PRECISION = 9;

    private static final MathContext MC = new MathContext(MAX_PRECISION, RoundingMode.HALF_EVEN);

    private Descriptors.FieldDescriptor field;

    private Descriptors.FieldDescriptor exponentField;

    private int lastExponent = 0;

    private BigDecimal lastValue = BigDecimal.ZERO;

    public ProtobufPriceState(Descriptors.Descriptor descriptor, String fieldname) {
        this.field = AbstractProtobufState.findField(descriptor, fieldname);
        this.exponentField = AbstractProtobufState.findField(descriptor, "exponent_" + fieldname);
    }

    protected static BigDecimal limitToMaxScale(BigDecimal bd) {
        return bd.setScale(MAX_PRECISION, MC.getRoundingMode());
    }

    protected boolean update(GeneratedMessage.Builder element, BigDecimal value) {
        if (value == null) {
            return false;
        }
        if (value.scale() > MAX_PRECISION) {
            return update(element, limitToMaxScale(value));
        }
        final BigDecimal difference = calcDifference(value);

        if (difference.signum() != 0) {
            final int exponent = difference.scale();
            if (exponent != this.lastExponent) {
                element.setField(this.exponentField, exponent);
                this.lastExponent = exponent;
            }

            final long valueToStore = difference.movePointRight(exponent).longValue();
            this.lastValue = value;

            element.setField(this.field, valueToStore);
        }
        else {
            element.setField(this.field, 0L);
        }

        return true;
    }

    /**
     * Returns the difference between value and this.lastValue; if the result is not ZERO, its
     * scale may be different from this.lastExponent.
     * @param value reference
     * @return difference
     */
    private BigDecimal calcDifference(BigDecimal value) {
        final BigDecimal result = value.subtract(this.lastValue).stripTrailingZeros();

        if (result.signum() == 0) {
            return BigDecimal.ZERO;
        }

        final int scale = result.scale();
        if (scale < this.lastExponent) {
            return result.setScale(this.lastExponent);
        }

        return result;
    }
}
