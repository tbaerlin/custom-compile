/*
 * EntitlementProvider.java
 *
 * Created on 21.02.2008 13:16:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl;

import java.util.BitSet;

import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface EntitlementProvider {

    Entitlement getEntitlement(String key);

    Entitlement getEntitlement(Market market);
}
