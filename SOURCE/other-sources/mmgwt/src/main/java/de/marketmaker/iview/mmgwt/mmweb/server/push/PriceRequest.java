package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;
import de.marketmaker.itools.gwtcomet.comet.server.CometSession;

import java.util.List;

/**
 * Created on 04.02.2010 11:45:55
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class PriceRequest {
    private final List<String> vwdCodes;
    private final CometSession session;

    public PriceRequest(List<String> vwdCodes, CometSession session) {
        this.vwdCodes = vwdCodes;
        this.session = session;
    }

    public List<String> getVwdCodes() {
        return vwdCodes;
    }

    public void setPrice(PushPrice price) {
        synchronized (this.session) {
            this.session.enqueue(price);
        }
    }

}
