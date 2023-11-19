/*
 * TickTypeCheckerImpl.java
 *
 * Created on 20.09.12 13:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author oflege
 */
public final class TickTypeCheckerByFlags implements TickTypeChecker {
    public static final TickTypeChecker INSTANCE = new TickTypeCheckerByFlags();

    private TickTypeCheckerByFlags() {
    }

    @Override
    public String toString() {
        return getClass().getName() + "#INSTANCE";
    }

    @Override
    public int getTickFlags(ParsedRecord record) {
        return record.getFlags();
    }

    @Override
    public TickTypeChecker forMarket(String marketName) {
        return this;
    }
}
