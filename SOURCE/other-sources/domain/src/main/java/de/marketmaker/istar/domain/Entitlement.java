/*
 * Issuer.java
 *
 * Created on 17.12.2004 16:28:20
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Entitlement {
    String[] getEntitlements(KeysystemEnum id);
}
