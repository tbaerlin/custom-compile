/*
 * BackendUpdateReceiver.java
 *
 * Created on 26.10.2005 10:35:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface BackendUpdateReceiver {
    void update(byte[] bytes);
}