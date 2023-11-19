/*
 * PriceType.java
 *
 * Created on(24),.01.13(10),:04
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zzhao
 */
public enum PriceType {
    ADF,
    OPEN,
    HIGH,
    LOW,
    CLOSE,
    KASSA,
    CONTRACT,
    VOLUME,
    OPENINTEREST,
//    ISSUEPRICE,
//    REDEMPTIONPRICE,
    ;

    private static final Map<String, PriceType> map;

    static {
        map = new HashMap<>();
//        map.put("Rücknahme", REDEMPTIONPRICE);
//        map.put("Ausgabe", ISSUEPRICE);
        map.put("Open", OPEN);
        map.put("High", HIGH);
        map.put("Low", LOW);
        map.put("Close", CLOSE);
        map.put("Kassa", KASSA);
        map.put("Kontrakt", CONTRACT);
        map.put("Volume", VOLUME);
        map.put("OpenInterest", OPENINTEREST);
    }

    public static PriceType fromFormula(String formula) {
        if (formula.toUpperCase().startsWith("ADF")) {
            return ADF;
        }
        else {
            try {
                return valueOf(formula);
            } catch (IllegalArgumentException e) {
                final String prefix = formula.substring(0, formula.indexOf('['));
                if (map.containsKey(prefix)) {
                    return map.get(prefix);
                }
            }
        }

        throw new IllegalArgumentException("no price type found for: " + formula);
    }
}
