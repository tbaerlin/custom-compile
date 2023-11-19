/*
 * IdentityHistoryContextProducer.java
 *
 * Created on 08.07.2014 11:53
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.history;

/**
 * @author Markus Dick
 */
public class IdentityHistoryContextProducer implements HistoryContextProducer {
    private final HistoryContext hc;

    public IdentityHistoryContextProducer(HistoryContext hc) {
        this.hc = hc;
    }

    @Override
    public HistoryContext produce() {
        return this.hc;
    }
}
