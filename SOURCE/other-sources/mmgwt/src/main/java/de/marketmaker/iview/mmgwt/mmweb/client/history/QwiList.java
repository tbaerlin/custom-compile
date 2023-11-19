/*
 * QwiList.java
 *
 * Created on 07.12.12 10:36
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.history;

import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;

import java.util.List;

/**
 * @author Michael Lösch
 */
public class QwiList extends ContextList<QuoteWithInstrument> {

    public QwiList(List<QuoteWithInstrument> list) {
        super(list);
    }

    @Override
    protected String getIdOf(QuoteWithInstrument item) {
        if (item == null || item.getQuoteData() == null) {
            return null;
        }
        return item.getQuoteData().getQid();
    }
}
