package de.marketmaker.iview.mmgwt.mmweb.server.push;

import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created on 04.02.2010 11:40:28
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class PushClientHandler implements PriceObserver {

    // SessionID, PriceRequest(QuoteList, CometResponse)
    private final ConcurrentMap<String, PriceRequest> clientRequests = new ConcurrentHashMap<>();

    // Quote, List(SessionID)
    private final ConcurrentMap<String, List<String>> quoteToClients = new ConcurrentHashMap<>();

    public final static PushClientHandler INSTANCE = new PushClientHandler();
    final Log logger = LogFactory.getLog(getClass());

    private PushClientHandler() {
    }

    public void addClientRequest(String id, PriceRequest request) {
        this.logger.info("PushClientHandler: addClientRequest for id " + id);
        this.clientRequests.put(id, request);
        for (String vwdCode : request.getVwdCodes()) {
            if (this.quoteToClients.containsKey(vwdCode)) {
                this.quoteToClients.get(vwdCode).add(id);
            }
            else {
                List<String> list = Collections.synchronizedList(new ArrayList<String>());
                list.add(id);
                this.quoteToClients.put(vwdCode, list);
            }
            DummyPriceSource.INSTANCE.registerForUpdates(vwdCode, this);
        }
    }

    public void removeClientRequest(String id) {
        this.logger.info("PushClientHandler: removeClientRequest for id " + id);
        PriceRequest request = this.clientRequests.remove(id);
        for (String vwdCode : request.getVwdCodes()) {
            if (this.quoteToClients.containsKey(vwdCode)) {
                this.quoteToClients.get(vwdCode).remove(id);
            }
            DummyPriceSource.INSTANCE.unregisterForUpdates(vwdCode);
        }
    }

    public void update(String quote, PushPrice price) {
        this.logger.info("PushClientHandler update price " + price.getPrice() + " for vwdCode " + price.getVwdCode());
        if (this.quoteToClients.containsKey(quote)) {
            for (String id : this.quoteToClients.get(quote)) {
                PriceRequest priceRequest = this.clientRequests.get(id);
                if (priceRequest != null) {
                    priceRequest.setPrice(price);
                }
                else {
                    logger.info("!!!priceRequest == null");
                }
            }
        }
    }
}

