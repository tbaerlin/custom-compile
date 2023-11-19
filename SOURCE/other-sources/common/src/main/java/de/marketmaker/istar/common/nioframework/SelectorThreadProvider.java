/*
 * SelectorThreadProvider.java
 *
 * Created on 30.06.2006 07:17:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;

/**
 * Provides access to the SelectorThread.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SelectorThreadProvider {
    SelectorThread getSelectorThread();
}
