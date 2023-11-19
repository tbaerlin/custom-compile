package de.marketmaker.istar.merger.web.xmlmarket;

import de.marketmaker.istar.domain.instrument.Quote;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * sort quotes by market name to reproduce the old merger's order
 *
 * Note: this comparator imposes orderings that are inconsistent with equals.
 */
public class QuoteSorter implements Comparator<Quote> {
    static final QuoteSorter MARKET_SORTER = new QuoteSorter(Arrays.asList("ETR", "BLN", "DDF", "FFM", "HBG", "HNV", "MCH", "STG", "Q", "FFMFO", "FONDS", "TRADE"));
    static final QuoteSorter DEFAULT_SORTER = new QuoteSorter(Collections.<String>emptyList());

    private final List<String> sortOrder;

    QuoteSorter(List<String> sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(Quote leftQuote, Quote rightQuote) {
        String leftMarketName = leftQuote.getSymbolVwdfeedMarket();
        String rightMarketName = rightQuote.getSymbolVwdfeedMarket();

        leftMarketName=leftMarketName==null?"":leftMarketName;
        rightMarketName=rightMarketName==null?"":rightMarketName;

        // order depends on the position in the sort array
        if (sortOrder.contains(leftMarketName) && sortOrder.contains(rightMarketName)) {
            return Integer.compare(sortOrder.indexOf(leftMarketName), sortOrder.indexOf(rightMarketName));
        }

        // whatever is in the sort array goes first
        if (sortOrder.contains(leftMarketName) ^ sortOrder.contains(rightMarketName)) {
            return sortOrder.contains(leftMarketName)?-1:1;
        }

        // fallback to normal String order
        return leftMarketName.compareTo(rightMarketName);
    }

}
