package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.QuoteData;

/**
 * @author Ulrich Maurer
 *         Date: 03.11.11
 */
public class QwiAndValue<V> extends QuoteWithInstrument {
    private V value;


    public QwiAndValue(InstrumentData instrumentData, QuoteData quoteData, String name, V value) {
        super(instrumentData, quoteData, name);
        this.value = value;
    }


    public QwiAndValue(QuoteWithInstrument qwi, V value) {
        this(qwi.getInstrumentData(), qwi.getQuoteData(), qwi.getName(), value);
    }


    public V getValue() {
        return this.value;
    }
}
