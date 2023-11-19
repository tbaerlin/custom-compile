/*
 * MarketcategoryEnum.java
 *
 * Created on 17.09.2004 11:36:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum MarketcategoryEnum {
    UNKNOWN(0),
    BOERSE(1),
    BANK(2),
    TECHNISCH(3),
    VENDOR(5),
    VWD_FREISCHALTUNG(111);

    private static final Logger logger = LoggerFactory.getLogger(MarketcategoryEnum.class);

    private final int id;

    MarketcategoryEnum(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static MarketcategoryEnum valueOf(int id) {
        switch (id) {
            case 0:
                return UNKNOWN;
            case 1:
                return BOERSE;
            case 2:
                return BANK;
            case 3:
                return TECHNISCH;
            case 5:
                return VENDOR;
            case 111:
                return VWD_FREISCHALTUNG;
            default:
                logger.warn("<valueOf> " + id + " is unknown");
                return UNKNOWN;
        }
    }

    public String toString() {
        return "MarketcategoryEnum[" + super.toString() + "]";
    }
}
