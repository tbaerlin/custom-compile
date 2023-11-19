package de.marketmaker.istar.domain.data;

import de.marketmaker.istar.domain.instrument.Quote;

import java.math.BigDecimal;
import java.util.List;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface OrderbookData extends HasPriceQuality {
    Quote getQuote();

    DateTime getDate();

    List<Item> getBids();

    List<Item> getAsks();

    public interface Item {
        BigDecimal getPrice();

        long getVolume();
    }
}
