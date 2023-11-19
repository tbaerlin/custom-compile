package de.marketmaker.istar.domain.data;

import de.marketmaker.istar.domain.instrument.Quote;

import java.io.Serializable;
import java.util.List;
import java.util.Collections;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NullOrderbookData implements Serializable, OrderbookData {
    protected static final long serialVersionUID = 1L;

    public static final OrderbookData INSTANCE = new NullOrderbookData();

    public PriceQuality getPriceQuality() {
        return PriceQuality.NONE;
    }

    public boolean isPushAllowed() {
        return false;
    }

    public Quote getQuote() {
        return null;  
    }

    public DateTime getDate() {
        return null;
    }

    public List<Item> getBids() {
        return null;
    }

    public List<Item> getAsks() {
        return null; 
    }
}
