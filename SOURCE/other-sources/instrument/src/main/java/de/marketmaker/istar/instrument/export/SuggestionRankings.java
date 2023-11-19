/*
 * SuggestionRankings.java
 *
 * Created on 22.04.2010 11:04:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;

import de.marketmaker.istar.common.lifecycle.Initializable;
import de.marketmaker.istar.domain.instrument.Instrument;

/**
 * Extracted from {@link SuggestionRankingsImpl}.
 *
 * @author zzhao
 * @since 1.2
 */
public interface SuggestionRankings extends Initializable {

    void setIndexDir(File indexDir);

    void setInstrumentDao(InstrumentDao idf);

    short getOrder(Instrument instrument, int i);

    int getIntOrder(Instrument instrument, int i);

    String[] getStrategyNames();

    boolean hasFundRank(Long id);
}
