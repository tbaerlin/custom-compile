package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;

/**
 * Created on 04.02.2010 11:41:01
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public interface PriceObserver {
    void update(String quote, PushPrice price);
}
