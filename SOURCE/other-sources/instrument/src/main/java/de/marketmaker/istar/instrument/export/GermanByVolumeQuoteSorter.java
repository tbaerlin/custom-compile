/*
 * GermanByVolumeQuoteSorter.java
 *
 * Created on 02.09.2009 13:57:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.util.List;

import org.apache.lucene.search.SortField;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.IndexConstants;

/**
 * A ByVolumeQuoteSorter that prefers quotes with volume at german markets. The order of the
 * quotes for an instrument is:
 * <ol>
 * <li>Quotes at german markets with volume ordered by volume
 * <li>Quotes at non-german markets with volume ordered by volume
 * <li>Quotes without volume, unordered
 * </ol>
 * <b>Note</b> that the order for funds is the same as provided by the superclass
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GermanByVolumeQuoteSorter extends ByVolumeQuoteSorter {
    protected static class MyQuoteWrapper extends QuoteWrapper {
        private final int german; // 0 for german, 1 otherwise

        protected MyQuoteWrapper(Quote quote, Integer order) {
            super(quote, order);
            this.german = ((order != null) && isGerman(quote)) ? 0 : 1;
        }

        @Override
        public int compareTo(QuoteWrapper o) {
            final int cmp = this.german - ((MyQuoteWrapper) o).german;
            if (cmp != 0) {
                return cmp;
            }
            return super.compareTo(o);
        }
    }

    public static final SortField SORT_FIELD
            = new SortField(IndexConstants.FIELDNAME_SORT_QUOTE_VOLUME_PREFER_DE, SortField.BYTE);

    private static boolean isGerman(Quote quote) {
        return "DE".equals(quote.getMarket().getCountry().getSymbolIso());
    }

    private boolean createMyWrappers = false;

    @Override
    public SortField getSortField() {
        return SORT_FIELD;
    }

    @Override
    public void prepare(Instrument instrument) {
        this.createMyWrappers = (instrument.getInstrumentType() != InstrumentTypeEnum.FND)
                && hasGermanAndNonGermanQuotes(instrument);
        super.prepare(instrument);
    }

    @Override
    protected QuoteWrapper createWrapper(Quote quote, Integer order) {
        if (this.createMyWrappers) {
            return new MyQuoteWrapper(quote, order);
        }
        return super.createWrapper(quote, order);
    }

    private boolean hasGermanAndNonGermanQuotes(Instrument instrument) {
        final List<Quote> quotes = instrument.getQuotes();
        final boolean first = isGerman(quotes.get(0));
        for (int i = 1; i < quotes.size(); i++) {
            if (first != isGerman(quotes.get(i))) {
                return true;
            }
        }
        return false;
    }
}
