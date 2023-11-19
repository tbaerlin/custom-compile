/*
 * MarketManagerQuoteSorter.java
 *
 * Created on 20.08.2009 11:58:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.SortField;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.IndexConstants;

/**
 * Orders quotes by volume, with the exception of fund quotes, where the KAG quotes appear in
 * front (EUR first, the others in alphabetical order); quotes w/o a known trade volume
 * will be given the same order, so this class only creates a partial order and it is recommended
 * to add the SortField of another class after this class's SortField to ensures a total order.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ByVolumeQuoteSorter implements QuoteSorter {
    public static final SortField SORT_FIELD
            = new SortField(IndexConstants.FIELDNAME_SORT_QUOTE_VOLUME, SortField.BYTE);

    protected static class QuoteWrapper implements Comparable<QuoteWrapper> {
        private static final String FIRST = "A";

        private static final String LAST = "Z";

        private final Quote quote;

        private final int order;

        // "A" for FONDS.EUR, "FONDS.XXX" for FONDS in XXX currency, "Z" for all others
        private final String compareStr;

        protected QuoteWrapper(Quote quote, Integer order) {
            this.quote = quote;
            this.order = (order != null) ? order : Integer.MAX_VALUE;
            this.compareStr = getCompareString(quote);
        }

        public static String getCompareString(Quote q) {
            if (q.getInstrument().getInstrumentType() == InstrumentTypeEnum.FND) {
                final String code = q.getSymbolVwdcode();
                if (code != null) {
                    final int p = code.indexOf("FONDS.");
                    if (p > 0) {
                        if (code.startsWith("FONDS.EUR", p)) {
                            return FIRST;
                        }
                        return code.substring(p, Math.min(code.length(), p + 9));
                    }
                }
            }
            return LAST;
        }

        public int compareTo(QuoteWrapper o) {
            final int cmp = this.compareStr.compareTo(o.compareStr);
            if (cmp != 0){
                return cmp;
            }
            return this.order - o.order;
        }

        boolean isUnordered() {
            return this.order == Integer.MAX_VALUE && this.compareStr == LAST;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private QuoteVolumeOrderProvider orderProvider;

    // avoid expensive equals(..) calls by using IdentityHashMap
    private final Map<Quote, Integer> orders = new IdentityHashMap<>();

    public void setOrderProvider(QuoteVolumeOrderProvider orderProvider) {
        this.orderProvider = orderProvider;
    }

    public void prepare(Instrument instrument) {
        if (instrument.getQuotes().size() > 127) {
            // if this is encountered, we must use SortField.SHORT in this class and its subclasses
            this.logger.error("<prepare> too many quotes in " + instrument);
        }

        this.orders.clear();

        final List<QuoteWrapper> wrappers = getSortedWrappers(instrument);
        if (wrappers.get(0).isUnordered()) {
            return;
        }

        int noOrder = 0;
        for (int i = 0; i < wrappers.size(); i++) {
            final QuoteWrapper wrapper = wrappers.get(i);
            final int order = (noOrder != 0 ? noOrder : Math.min(i + 1, 127));
            this.orders.put(wrapper.quote, order);
            if (noOrder == 0 && wrapper.isUnordered()) {
                noOrder = order;
            }
        }
    }

    private List<QuoteWrapper> getSortedWrappers(Instrument instrument) {
        final List<QuoteWrapper> result = new ArrayList<>();
        for (Quote quote : instrument.getQuotes()) {
            result.add(createWrapper(quote, getQuoteOrder(quote)));
        }
        result.sort(null);
        return result;
    }

    protected QuoteWrapper createWrapper(Quote quote, Integer order) {
        return new QuoteWrapper(quote, order);
    }

    private Integer getQuoteOrder(Quote q) {
        final String market = q.getSymbolVwdfeedMarket();
        if (market == null || market.startsWith("OBU")) {
            return null;
        }
        return this.orderProvider.getQuoteOrder(q);
    }

    public int getOrder(Quote quote) {
        if (this.orders.isEmpty()) {
            return 0;
        }
        return this.orders.get(quote);
    }

    public SortField getSortField() {
        return SORT_FIELD;
    }

    public static void main(String[] args) throws Exception {
        QuoteVolumeOrderProvider orderProvider = new QuoteVolumeOrderProvider();
        orderProvider.setFile(new File("d:/produktion/var/data/buildindex/istar-quote-volume.xml.gz"));
        orderProvider.afterPropertiesSet();

        final ByVolumeQuoteSorter sorter = new GermanByVolumeQuoteSorter();
        sorter.setOrderProvider(orderProvider);

        final InstrumentDirDao idf = new InstrumentDirDao(new File("d:/produktion/var/data/instrument/work0/data/instruments/"));

        final Instrument dcx = idf.getInstrument(13139);
        sorter.prepare(dcx);
        for (Quote quote : dcx.getQuotes()) {
            System.out.println(sorter.getOrder(quote) + ": " + quote + "  " + orderProvider.getQuoteOrder(quote));
        }
    }
}
