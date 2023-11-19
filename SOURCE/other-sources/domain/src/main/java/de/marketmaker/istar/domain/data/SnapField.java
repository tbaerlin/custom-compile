/*
 * SnapField.java
 *
 * Created on 01.03.2005 15:32:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SnapField {
    boolean isDefined();

    int getId();

    String getName();

    FieldTypeEnum getType();

    Object getValue();

    /**
     * returns the price encoded in this field as BigDecimal; results are undefined if
     * {@link #getType()} does not return {@link FieldTypeEnum#PRICE} for this field.
     * @return price
     */
    BigDecimal getPrice();

    /**
     * @return Iff {@link #getPrice()} returns {@link java.math.BigDecimal#ZERO} and the snap field somehow
     * knows the last non-zero value of this field, that value is returned. Otherwise, this
     * method returns the same value as <tt>getPrice()</tt>
     */
    BigDecimal getLastPrice();
}
