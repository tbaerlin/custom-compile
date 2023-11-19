/*
 * SuggestionSearcher.java
 *
 * Created on 17.06.2009 12:14:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.List;

import de.marketmaker.istar.domain.data.SuggestedInstrument;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SuggestionSearcher {
    List<SuggestedInstrument> query(SuggestRequest request) throws Exception;
}
