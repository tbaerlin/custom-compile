package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.dmxml.HasPricedata;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;

/**
 * Created on 18.07.12 08:43
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class QwiAndPricedata extends QuoteWithInstrument {

    private final HasPricedata hasPricedata;

    public QwiAndPricedata(InstrumentData instrumentData, QuoteData quoteData, HasPricedata hasPricedata) {
        super(instrumentData, quoteData);
        this.hasPricedata = hasPricedata;
    }

    public QwiAndPricedata(MSCPriceData element) {
        this(element.getInstrumentdata(), element.getQuotedata(), element);
    }

    public QwiAndPricedata(MSCListDetailElement listDetailElement) {
        this(listDetailElement.getInstrumentdata(), listDetailElement.getQuotedata(), listDetailElement);
    }

    public HasPricedata getHasPricedata() {
        return hasPricedata;
    }
}
