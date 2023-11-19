/*
 * InstrumentSuggestOracle.java
 *
 * Created on 17.06.2009 09:07:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCInstrumentSuggestion;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Markus Dick
 */
public class InstrumentSuggestOracle extends AbstractBlockBasedSuggestOracle<InstrumentSuggestion, MSCInstrumentSuggestion> {
    public InstrumentSuggestOracle() {
        super(new DmxmlContext().<MSCInstrumentSuggestion>addBlock("MSC_InstrumentSuggestion")); //$NON-NLS$
    }

    @Override
    protected boolean isQueryAcceptable(String query) {
        return true;
    }

    @Override
    public void setBlockParameters(String query, int limit) {
        final DmxmlContext.Block<MSCInstrumentSuggestion> block = getBlock();
        block.setParameter("limit", Integer.toString(limit)); // $NON-NLS$
        block.setParameter("query", query); // $NON-NLS$
    }

    @Override
    protected List<InstrumentSuggestion> getSuggestions(String query) {
        final List<InstrumentSuggestion> suggestions = new ArrayList<InstrumentSuggestion>();
        final String equery = SafeHtmlUtils.htmlEscape(query);
        for (InstrumentData data : this.getBlock().getResult().getInstrumentdata()) {
            suggestions.add(new InstrumentSuggestion(equery, data));
        }
        return suggestions;
    }
}
