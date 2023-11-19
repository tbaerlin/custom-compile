/*
 * ScreenerField.java
 *
 * Created on 04.04.2007 08:38:20
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.screener;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ScreenerField {
    int getId();

    Object getValue();

    boolean isDecimal();

    boolean isDate();

    boolean isString();

    boolean isInteger();

    String getShortText();

    String getLongText();

    String getImageName();

    Boolean isStar();

    String getName();

    String getHeadline();
}
