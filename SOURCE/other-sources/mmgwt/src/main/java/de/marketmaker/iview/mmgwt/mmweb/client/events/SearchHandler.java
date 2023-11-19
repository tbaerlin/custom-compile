package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created on 13.02.13 07:41
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface SearchHandler extends EventHandler {
    void onSearchResult(SearchResultEvent event);
}
