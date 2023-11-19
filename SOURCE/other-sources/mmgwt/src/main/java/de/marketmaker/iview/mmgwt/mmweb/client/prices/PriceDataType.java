/*
 * PriceDataType.java
 *
 * Created on 02.02.2010 14:22:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.prices;

/**
* @author oflege
*/
public enum PriceDataType {
    STANDARD("standard"),    //$NON-NLS$
    FUND_OTC("fund-otc"),     //$NON-NLS$
    CONTRACT_EXCHANGE("contract-exchange"),    //$NON-NLS$
    LME("lme"),     //$NON-NLS$
    INVALID(null);

    private final String value;

    private PriceDataType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static PriceDataType fromValue(String value) {
        for (PriceDataType c: PriceDataType.values()) {
            if (c.value != null && c.value.equals(value)) {
                return c;
            }
        }
        return INVALID;
    }

    public static PriceDataType fromDmxml(String value) {
        return fromValue(value);
    }
}
