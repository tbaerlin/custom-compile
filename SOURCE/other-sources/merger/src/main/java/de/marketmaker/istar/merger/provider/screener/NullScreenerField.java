/*
 * NullScreenerField.java
 *
 * Created on 04.04.2007 08:43:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.screener;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullScreenerField implements ScreenerField {

    public int getId() {
        return Integer.MIN_VALUE;
    }

    public Object getValue() {
        return null;
    }

    public String getName() {
        return null;
    }

    public String getHeadline() {
        return null;
    }

    public String getShortText() {
        return null;
    }

    public String getLongText() {
        return null;
    }

    public String getImageName() {
        return null;
    }

    public Boolean isStar() {
        return false;
    }

    public boolean isDecimal() {
        return false;
    }

    public boolean isDate() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isInteger() {
        return false;
    }
}
