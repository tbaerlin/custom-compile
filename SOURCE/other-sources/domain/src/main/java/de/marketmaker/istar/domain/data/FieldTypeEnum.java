/*
 * FieldTypeEnum.java
 *
 * Created on 01.03.2005 16:45:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum FieldTypeEnum {
    PRICE,
    NUMBER,
    STRING,
    DATE,
    TIME;

    public boolean isPrice() {
        return this == PRICE;
    }

    public boolean isNumber() {
        return this == NUMBER;
    }

    public boolean isString() {
        return this == STRING;
    }

    public boolean isDate() {
        return this == DATE;
    }

    public boolean isTime() {
        return this == TIME;
    }
}
