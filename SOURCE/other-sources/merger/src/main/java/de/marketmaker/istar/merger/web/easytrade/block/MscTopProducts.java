/**
 * Created on 06.06.12 13:44
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */


package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.web.easytrade.ListHelper;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.merger.web.easytrade.MultiListSorter;
import de.marketmaker.istar.merger.web.easytrade.MultiSymbolListCommand;
import de.marketmaker.istar.ratios.frontend.BestToolRatioSearchResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Returns the top products of a given product type for a certain issuer.
 * What a top product means depends on the definitions, usually made together with the customer.
 *
 * It searches for products of an given issuer which have the underlying defined by the given symbol(s).
 * Further search restrictions depend on the ProductType.
 *
 * MscTopProducts is a very specific and simpel variant of @see MscBestTool.
 * It returns the same type of data, enhanced with pricedatas.
 *
 */

public class MscTopProducts extends EasytradeCommandController {

    public static class TopCommand extends MultiSymbolListCommand {
        private String issuername;

        private MscTopProductsCommandBuilder.ProductType productType;

        /**
         * @return issuer of the top products
         * @sample Commerzbank
         */
        @NotNull
        public String getIssuername() {
            return issuername;
        }

        public void setIssuername(String issuername) {
            this.issuername = issuername;
        }

        @NotNull
        public MscTopProductsCommandBuilder.ProductType getProductType() {
            return productType;
        }

        public void setProductType(MscTopProductsCommandBuilder.ProductType productType) {
            this.productType = productType;
        }

        @Size(min = 1, max = 100)
        public String[] getSymbol() {
            return super.getSymbol();
        }
    }

    private final static Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

    private EasytradeInstrumentProvider instrumentProvider;

    private RatiosProvider ratiosProvider;

    private IntradayProvider intradayProvider;

    private HighLowProvider highLowProvider;

    protected MscTopProducts() {
        super(TopCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) throws Exception {
        final TopCommand topCmd = (TopCommand) o;

        final BestToolRatioSearchMethod searchMethod = createSearchMethod(topCmd);
        final BestToolRatioSearchResponse ratioSearchResponse = searchMethod.ratioSearch();
        final Map<String, Map<Object, List<BestToolRatioSearchResponse.BestToolElement>>> responseMap
                = ratioSearchResponse.getResult();

        final Set<Object> columns = getColumns(topCmd, responseMap);

        final List<String> rows = new ArrayList<>(responseMap.keySet());
        final List<Quote> underlyingQuotes = getUnderlyingQuotes(searchMethod, rows);

        final ListResult listResult = ListResult.create(topCmd,
                Collections.singletonList("fieldname"), "fieldname", rows.size());
        ListHelper.clipPage(topCmd, rows, underlyingQuotes);
        listResult.setCount(rows.size());

        final List<Long> qids = new ArrayList<>();

        final Map<String, List<List<BestToolRatioSearchResponse.BestToolElement>>> table
                = new LinkedHashMap<>();
        for (final String row : rows) {
            final Map<Object, List<BestToolRatioSearchResponse.BestToolElement>> map
                    = responseMap.get(row);
            final List<List<BestToolRatioSearchResponse.BestToolElement>> bteElements
                    = new ArrayList<>(columns.size());

            for (final Object column : columns) {
                final List<BestToolRatioSearchResponse.BestToolElement> elements = map.get(column);
                bteElements.add(elements);

                if (elements == null) {
                    continue;
                }

                for (final BestToolRatioSearchResponse.BestToolElement element : elements) {
                    qids.add(element.getQid());
                }
            }

            table.put(row, bteElements);
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("rows", rows);
        model.put("columns", columns);
        model.put("table", table);
        model.put("listinfo", listResult);

        addQuotesAndPrices(qids, model);
        addUnderlyingQuotesAndPrices(underlyingQuotes, model);

        return new ModelAndView("msctopproducts", model);
    }

    private List<Quote> getUnderlyingQuotes(BestToolRatioSearchMethod searchMethod,
                                            List<String> rows) {
        final List<Quote> result
                = MscFinderGroups.getUnderlyingQuotes(searchMethod.getPrimaryField(), null,
                rows, null, this.instrumentProvider);
        if (result == null) {
            throw new NoDataException("no underlyings");
        }

        for (int i = 0; i < result.size(); i++) {
            if (result.get(i) != null && result.get(i).isNullQuote()) {
                result.set(i, null);
            }
        }

        CollectionUtils.removeNulls(result, rows);
        if (result.size() > 1) {
            sortUnderlyingsByName(result, rows);
        }

        return result;
    }

    private Set<Object> getColumns(TopCommand topCmd,
                                   Map<String, Map<Object, List<BestToolRatioSearchResponse.BestToolElement>>> responseMap) {
        final Set<Object> result = new TreeSet<>();
        final List<String> validColumns = topCmd.getProductType().getColumns();
        for (final Map<Object, List<BestToolRatioSearchResponse.BestToolElement>> map
                : responseMap.values()) {
            for (final Object col : map.keySet()) {
                final String colStr = String.valueOf(col);
                if (validColumns.contains(colStr)) {
                    result.add(col);
                }
            }
        }
        return result;
    }

    private void addUnderlyingQuotesAndPrices(List<Quote> underlyingQuotes,
                                              Map<String, Object> model) {
        model.put("underlyingQuotes", underlyingQuotes);
        final List<PriceRecord> underlyingPriceRecords
                = this.intradayProvider.getPriceRecords(underlyingQuotes);
        model.put("underlyingPriceRecords", underlyingPriceRecords);
        model.put("underlyingHighLows",
                this.highLowProvider.getHighLows52W(underlyingQuotes, underlyingPriceRecords));
    }

    private void addQuotesAndPrices(List<Long> qids, Map<String, Object> model) {
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);
        CollectionUtils.removeNulls(quotes);
        for (final Quote quote : quotes) {
            model.put(Long.toString(quote.getId()), quote);
        }
        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);
        for (int i = 0, priceRecordsSize = priceRecords.size(); i < priceRecordsSize; i++) {
            final Quote quote = quotes.get(i);
            final PriceRecord price = priceRecords.get(i);
            final HighLow highLow52W = this.highLowProvider.getHighLow52W(quote, price);
            model.put(quote.getId() + "-p", price);
            model.put(quote.getId() + "-hl", highLow52W);
        }
    }

    private void sortUnderlyingsByName(List<Quote> underlyingQuotes, List<String> rows) {
        final QuoteNameStrategy nameStrategy
                = RequestContextHolder.getRequestContext().getQuoteNameStrategy();
        final MultiListSorter sorter = new MultiListSorter(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return GERMAN_COLLATOR.compare(
                        nameStrategy.getName((Quote) o1), nameStrategy.getName((Quote) o2)
                );
            }
        }, false);
        sorter.sort(underlyingQuotes, rows);
    }

    private BestToolRatioSearchMethod createSearchMethod(TopCommand topCmd) {
        final List<Quote> underlyings = getUnderlyings(topCmd);
        CollectionUtils.removeNulls(underlyings);
        if (topCmd.getSymbol() != null && underlyings.isEmpty()) {
            throw new NoDataException("no underlyings");
        }

        final BestToolCommand bestCmd
                = MscTopProductsCommandBuilder.buildCommand(topCmd, underlyings);
        return BestToolRatioSearchMethod.create(bestCmd, this.ratiosProvider);
    }

    private List<Quote> getUnderlyings(TopCommand topCmd) {
        if (topCmd.getSymbol() == null) {
            return Collections.emptyList();
        }
        return this.instrumentProvider.identifyQuotes(Arrays.asList(topCmd.getSymbol()),
                topCmd.getSymbolStrategy(), new MarketStrategies(topCmd));
    }
}