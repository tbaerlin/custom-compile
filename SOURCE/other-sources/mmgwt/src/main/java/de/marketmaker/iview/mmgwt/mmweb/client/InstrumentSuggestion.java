/*
 * InstrumentSuggestion.java
 *
 * Created on 17.06.2009 09:49:37
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InstrumentSuggestion extends AbstractSuggestion {
    private final InstrumentData data;

    public InstrumentSuggestion(String query, InstrumentData data) {
        this.data = data;
        final String display = getStringWithMatch(query, data.getName())
                + " | " + getStringWithMatch(query, data.getIsin()) // $NON-NLS-0$
                + " | " + getStringWithMatch(query, data.getWkn()); // $NON-NLS-0$
        setDisplay(display);
    }

    public InstrumentData getData() {
        return this.data;
    }

    public void goTo() {
        PlaceUtil.goToPortrait(this.data);
    }
}
