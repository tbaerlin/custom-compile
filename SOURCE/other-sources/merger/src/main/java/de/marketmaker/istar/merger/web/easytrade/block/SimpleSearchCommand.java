package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collection;
import java.util.EnumSet;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.provider.InstrumentProvider;

/**
 * A simple search command contains criteria used to query financial instruments. Specifically:
 * <p>
 * <dl>
 * <dt><b>search string</b></dt><dd>an arbitrary string, normally an instrument's name or part of it</dd>
 * <dt><b>search fields</b></dt><dd>the instrument fields that should be queried. If not set the following
 * fields are returned: <tt>name, wm_wp_name, wm_wp_name_kurz, wm_wp_name_zusatz, isin, wkn, vwdcode, ticker</tt>.
 * For a list of available fields, please refer to {@link de.marketmaker.istar.instrument.IndexConstants}</dd>
 * <dt><b>search strategy</b></dt><dd>the strategy to be used to perform the search, defined in
 * {@link de.marketmaker.istar.merger.provider.InstrumentProvider.StrategyEnum}. If not set,
 * StrategyEnum.DEFAULT would be taken</dd>
 * <dt><b>types</b></dt><dd>constrains the instrument types to be searched. If not given, all instrument types
 * would be searched</dd>
 * <dt><b>count types</b></dt><dd>the instrument types in the search result that should be counted.
 * Should be a sub set of the above types</dd>
 * <dt><b>markets</b></dt><dd>constraints the markets to be searched, if not given all markets would
 * be searched</dd>
 * <dt><b>currencies</b></dt><dd>constraints the currencies to be searched. If not given all currencies
 * would be searched</dd>
 * <dt><b>offset</b></dt><dd>paging support, the index from which the results are returned</dd>
 * <dt><b>count</b></dt><dd>paging support, how many results this query should return</dd>
 * <dt><b>max size</b></dt><dd>paging support, the maxinum results this query should return</dd>
 * <dt><b>count instrument results</b></dt><dd>paging support, if set to true, the instrument count
 * information would be set in the response</dd>
 * <dt><b>sort fields</b></dt><dd>the instrument fields according to which the results are sorted. Don't
 * have to appear in the set of search fields</dd>
 * </dl>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SimpleSearchCommand {
    private final String searchstring;

    private final Collection<String> searchfields;

    private final Collection<String> additionalSearchfields;

    private final InstrumentProvider.StrategyEnum strategy;

    private final EnumSet<InstrumentTypeEnum> countTypes;

    private final EnumSet<InstrumentTypeEnum> types;

    private final Collection<String> markets;

    private final Collection<String> currencies;

    private final int offset;

    private final int count;

    private final int maxSize;

    private final boolean countInstrumentResults;

    private String[] sortFields;

    private boolean filterOpra = true;

    public SimpleSearchCommand(String searchstring, int offset, int count, int maxSize,
            boolean countInstrumentResults) {
        this(searchstring, null, null, null, null, null, null, null, offset, count, maxSize, countInstrumentResults);
    }

    public SimpleSearchCommand(String searchstring, EnumSet<InstrumentTypeEnum> types, int offset, int count, int maxSize,
                               boolean countInstrumentResults) {
        this(searchstring, null, null, null, null, types, null, null, offset, count, maxSize, countInstrumentResults);
    }

    public SimpleSearchCommand(String searchstring, Collection<String> searchfields,
            Collection<String> additionalSearchfields, InstrumentProvider.StrategyEnum strategy,
            EnumSet<InstrumentTypeEnum> countTypes, EnumSet<InstrumentTypeEnum> types,
            Collection<String> markets, Collection<String> currencies,
            int offset, int count, int maxSize, boolean countInstrumentResults) {
        this.searchstring = searchstring;
        this.searchfields = searchfields;
        this.additionalSearchfields = additionalSearchfields;
        this.strategy = strategy;
        this.countTypes = countTypes;
        this.types = types;
        this.markets = markets;
        this.currencies = currencies;
        this.offset = offset;
        this.count = count;
        this.maxSize = maxSize;
        this.countInstrumentResults = countInstrumentResults;
    }

    public boolean isFilterOpra() {
        return filterOpra;
    }

    public void setFilterOpra(boolean filterOpra) {
        this.filterOpra = filterOpra;
    }

    public String getSearchstring() {
        return searchstring;
    }

    public Collection<String> getAdditionalSearchfields() {
        return additionalSearchfields;
    }

    public Collection<String> getSearchfields() {
        return searchfields;
    }

    public InstrumentProvider.StrategyEnum getStrategy() {
        return strategy;
    }

    public EnumSet<InstrumentTypeEnum> getCountTypes() {
        return countTypes;
    }

    public EnumSet<InstrumentTypeEnum> getTypes() {
        return types;
    }

    public Collection<String> getMarkets() {
        return markets;
    }

    public Collection<String> getCurrencies() {
        return currencies;
    }

    public int getOffset() {
        return offset;
    }

    public int getCount() {
        return count;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isCountInstrumentResults() {
        return countInstrumentResults;
    }

    public void setSortFields(String[] sortFields) {
        this.sortFields = sortFields;
    }

    public String[] getSortFields() {
        return this.sortFields;
    }

    public boolean isSearchForSortedQuotes() {
        if (this.sortFields == null) {
            return false;
        }
        for (String sortField : sortFields) {
            if (sortField.startsWith("q")) {
                return true;
            }
        }
        return false;
    }
}
