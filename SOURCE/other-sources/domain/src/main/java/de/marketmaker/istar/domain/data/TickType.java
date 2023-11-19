/*
 * TickType.java
 *
 * Created on 02.03.2005 16:07:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum TickType {
    TRADE,
    BID,
    ASK,
    SUSPEND_END,
    SUSPEND_START,
    SYNTHETIC_TRADE
}
