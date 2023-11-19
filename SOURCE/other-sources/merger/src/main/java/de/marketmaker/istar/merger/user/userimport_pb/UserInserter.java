/*
 * UserInserter.java
 *
 * Created on 23.11.2006 17:13:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user.userimport_pb;

import java.util.List;
import java.util.Map;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface UserInserter {
    void insertUsers(List<String> usernames, Map<String, List<ImportPortfolio>> map);

    void setInstrumentsByWkn(Map<String, Instrument> instrumentsByWkn);

    void setIsoCurrencyConversionProvider(IsoCurrencyConversionProvider isoCurrencyConversionProvider);
}
