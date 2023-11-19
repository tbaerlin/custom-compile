/*
 * SearchResponse.java
 *
 * Created on 22.12.2004 14:05:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.util.Collections;
import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.SuggestedInstrument;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SuggestResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 123545L;

    public static SuggestResponse getInvalid() {
        final SuggestResponse result = new SuggestResponse();
        result.setInvalid();
        return result;
    }

    private List<SuggestedInstrument> suggestions;

    public SuggestResponse() {
    }

    public List<SuggestedInstrument> getSuggestions() {
        return (this.suggestions != null)
                ? this.suggestions
                : Collections.<SuggestedInstrument>emptyList();
    }

    public SuggestResponse withSuggestions(List<SuggestedInstrument> suggestions) {
        this.suggestions = suggestions;
        return this;
    }
}