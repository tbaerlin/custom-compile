package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.OrderbookData;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OrderbookDataImpl implements Serializable, OrderbookData {
    protected static final long serialVersionUID = 1L;

    private final Quote quote;

    private final List<Item> bids;

    private final List<Item> asks;

    private final PriceQuality priceQuality;

    private final boolean pushAllowed;

    private final DateTime date;

    public OrderbookDataImpl(Quote quote, PriceQuality priceQuality, boolean pushAllowed,
            List<Item> bids, List<Item> asks, DateTime date) {
        this.quote = quote;
        this.priceQuality = priceQuality;
        this.pushAllowed = pushAllowed;
        this.bids = bids;
        this.asks = asks;
        this.date = date;
    }

    public Quote getQuote() {
        return quote;
    }

    public DateTime getDate() {
        return this.date;
    }

    public PriceQuality getPriceQuality() {
        return this.priceQuality;
    }

    public boolean isPushAllowed() {
        return this.pushAllowed;
    }

    public List<Item> getBids() {
        return this.bids;
    }

    public List<Item> getAsks() {
        return this.asks;
    }

    public static class ItemImpl implements Serializable, OrderbookData.Item {
        protected static final long serialVersionUID = 1L;

        private final BigDecimal price;

        private final long volume;

        public ItemImpl(BigDecimal price, long volume) {
            this.price = price;
            this.volume = volume;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public long getVolume() {
            return volume;
        }
    }
}
