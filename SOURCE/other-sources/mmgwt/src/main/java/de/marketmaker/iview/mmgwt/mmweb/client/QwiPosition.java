/*
* QwiPosition.java
*
* Created on 18.08.2008 11:12:01
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.QuoteData;

/**
 * @author Michael Lösch
 */
public class QwiPosition extends QuoteWithInstrument {

    public enum Type {
        WATCHLIST,
        PORTFOLIO,
        ORDER
    }

    private final String positionId;
    
    private final Type type;

    public QwiPosition(InstrumentData instrumentData, QuoteData quoteData, String name, String positionId, Type type) {
        super(instrumentData, quoteData, name);
        this.positionId = positionId;
        this.type = type;
    }

    public QwiPosition(InstrumentData instrumentData, QuoteData quoteData, String positionId, Type type) {
        this(instrumentData, quoteData, null, positionId, type);
    }

    public QwiPosition(String positionId, Type type) {
        this(NULL_INSTRUMENT_DATA, NULL_QUOTE_DATA, null, positionId, type);
    }

    public String getPositionId() {
        return positionId;
    }

    public Type getType() {
        return type;
    }
}
