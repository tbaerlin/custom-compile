/*
 * UpdateWatchlistMenuMethod.java
 *
 * Created on 18.02.2016 08:18
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import de.marketmaker.iview.dmxml.WatchlistElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * Must be processed before the PlaceChangeEvent is handled by the MainController!
 * @author mdick
 */
public class UpdateWatchlistMenuMethod extends AbstractUpdateMenuMethod<UpdateWatchlistMenuMethod, WatchlistElement> {
    public UpdateWatchlistMenuMethod() {
        super("B_WS", "B_W", I18n.I.watchlist(), "B_W", "B_W", SessionData.INSTANCE::getWatchlists); // $NON-NLS$
    }

    @Override
    protected String getId(WatchlistElement element) {
        return element.getWatchlistid();
    }

    @Override
    protected String getName(WatchlistElement element) {
        return element.getName();
    }
}
