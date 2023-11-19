/*
 * DerivativeIpoProviderDzbank.java
 *
 * Created on 26.10.2008 18:00:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.domain.data.DerivativeIpoData;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface DerivativeIpoProviderDzbank {
    List<DerivativeIpoData> getDerivateIposDzbank(String type);
}
