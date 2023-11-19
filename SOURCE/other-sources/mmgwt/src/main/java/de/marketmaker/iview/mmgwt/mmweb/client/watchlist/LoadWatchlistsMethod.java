/*
 * LoadWatchlistsMethod.java
 *
 * Created on 18.02.2016 08:11
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.iview.dmxml.WLWatchlist;

/**
 * @author mdick
 */
public class LoadWatchlistsMethod extends AbstractLoadElementsMethod<LoadWatchlistsMethod, WLWatchlist> {
    public LoadWatchlistsMethod(AsyncCallback<WLWatchlist> callback) {
        super("WL_Watchlist", callback); // $NON-NLS$
    }
}
