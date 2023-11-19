/*
 * MockUtil.java
 *
 * Created on 16.12.13 13:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock;

/**
 * @author Markus Dick
 */
public class MockUtil {
    public static <T> T fill(T my, T other) {
        if(other != null) {
            return other;
        }
        return my;
    }

    public static <E extends Enum> E fillEnum(E naValue, E newValue) {
        if(newValue != null) {
            return newValue;
        }
        return naValue;
    }
}
