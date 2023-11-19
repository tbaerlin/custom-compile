/*
 * QuotedPer.java
 *
 * Created on 13.08.06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.istar.domain.instrument;

/**
 * @author Martin Wilke
*/

public enum DerivativeTypeEnum {
    // order is important, do not change
    NONE(),
    CALL(),
    PUT(),
    OTHER();

    public int getId() {
        return ordinal();
    }

    public static DerivativeTypeEnum valueOf(int id) {
        return values()[id];
    }

}
