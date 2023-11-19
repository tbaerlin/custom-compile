/*
 * SelectListForm.java
 *
 * Created on 05.05.2008 12:55:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import de.marketmaker.iview.dmxml.WatchlistElement;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class SelectWatchlistForm extends AbstractListForm {
    public SelectWatchlistForm(Map<String, String> params) {
        super(params, "watchlistid"); // $NON-NLS-0$
        setHeaderVisible(false);        
    }

    protected String[][] initValues() {
        final List<WatchlistElement> list = SessionData.INSTANCE.getWatchlists();
        final String[][] s = new String[list.size()][];
        int i = -1;
        for (WatchlistElement element : list) {
            i++;
            s[i] = new String[]{element.getWatchlistid(), element.getName()};
        }
        return s;
    }
}
