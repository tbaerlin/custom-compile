/*
 * FndSektorenvergleich.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.Min;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.RatioFieldDescription.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.ratios.frontend.BestToolRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.BestToolVisitor;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;

import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.qidSymbol;
import static de.marketmaker.istar.ratios.frontend.SearchParameterParser.SUFFIX_LOWER;
import static de.marketmaker.istar.ratios.frontend.SearchParameterParser.SUFFIX_UPPER;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CerBestTool extends EasytradeCommandController {
    private RatiosProvider ratiosProvider;
    private EasytradeInstrumentProvider instrumentProvider;

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public static class Command {
        private String[] rawparam;
        private String listid;
        private String primaryField;
        private String secondaryField;
        private String primaryFieldOperator;
        private String secondaryFieldOperator;
        private int numResults;
        private String sortField;
        private String maxSortFieldValue;
        private boolean ascending;

        public String[] getRawparam() {
            return rawparam;
        }

        public void setRawparam(String[] rawparam) {
            this.rawparam = rawparam;
        }

        /**
         * @return a {@link RatioDataRecord.Field} name.
         * @sample underlyingIsin
         */
        @NotNull
        public String getPrimaryField() {
            return primaryField;
        }

        public void setPrimaryField(String primaryField) {
            this.primaryField = primaryField;
        }

        /**
         *
         * @return an index composition list id.
         * @sample 106547.qid
         */
        @NotNull
        public String getListid() {
            return listid;
        }

        public void setListid(String listid) {
            this.listid = listid;
        }

        /**
         * @return a {@link RatioDataRecord.Field} name.
         * @sample capLevel
         */
        public String getSecondaryField() {
            return secondaryField;
        }

        public void setSecondaryField(String secondaryField) {
            this.secondaryField = secondaryField;
        }

        /**
         * @return a {@link RatioDataRecord.Field} name.
         * @sample maximumYieldRelativePerYear
         */
        @NotNull
        public String getSortField() {
            return sortField;
        }

        public void setSortField(String sortField) {
            this.sortField = sortField;
        }

        public String getMaxSortFieldValue() {
            return maxSortFieldValue;
        }

        public void setMaxSortFieldValue(String maxSortFieldValue) {
            this.maxSortFieldValue = maxSortFieldValue;
        }

        public boolean isAscending() {
            return ascending;
        }

        public void setAscending(boolean ascending) {
            this.ascending = ascending;
        }

        /**
         * @return primary field operator.
         */
        public String getPrimaryFieldOperator() {
            return primaryFieldOperator;
        }

        public void setPrimaryFieldOperator(String primaryFieldOperator) {
            this.primaryFieldOperator = primaryFieldOperator;
        }

        /**
         * @return secondary field operator.
         * @sample interval:20
         */
        public String getSecondaryFieldOperator() {
            return secondaryFieldOperator;
        }

        public void setSecondaryFieldOperator(String secondaryFieldOperator) {
            this.secondaryFieldOperator = secondaryFieldOperator;
        }

        /**
         * @return number of results.
         * @sample 1
         */
        @NotNull
        @Min(1)
        public int getNumResults() {
            return numResults;
        }

        public void setNumResults(int numResults) {
            this.numResults = numResults;
        }
    }

    public CerBestTool() {
        super(Command.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote indexQuote = this.instrumentProvider.identifyQuote(cmd.getListid(), null, null, null);

        final List<Quote> quotes = this.instrumentProvider.getIndexQuotes(qidSymbol(indexQuote.getId()));
        quotes.sort(QuoteComparator.byName(RequestContextHolder.getRequestContext().getQuoteNameStrategy()));

        final RatioSearchRequest ratioSearchRequest = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        ratioSearchRequest.setType(InstrumentTypeEnum.CER);
        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields =
            AbstractFindersuchergebnis.getFields(InstrumentTypeEnum.CER);
        ratioSearchRequest.setVisitorClass(BestToolVisitor.class);
        final Map<String, String> parameters = new HashMap<>();
        ratioSearchRequest.setParameters(parameters);
        parameters.put("n", Integer.toString(cmd.getNumResults()));

        final StringBuilder sb = new StringBuilder(quotes.size() * 10);
        for (Quote quote : quotes) {
            final String isin = quote.getInstrument().getSymbolIsin();
            if (StringUtils.hasText(isin)) {
                if (sb.length() > 0) {
                    sb.append("@");
                }
                sb.append(isin);
            }
        }
        parameters.put("underlyingisin", sb.toString());
        if (StringUtils.hasText(cmd.getMaxSortFieldValue())) {
            parameters.put(cmd.getSortField() + (cmd.isAscending() ? SUFFIX_LOWER : SUFFIX_UPPER), cmd.getMaxSortFieldValue());
        }

        final Field sortField = AbstractFindersuchergebnis.getField(fields, cmd.getSortField());
        final Field primaryField = AbstractFindersuchergebnis.getField(fields, cmd.getPrimaryField());
        final Field secondaryField = AbstractFindersuchergebnis.getField(fields, cmd.getSecondaryField());

        parameters.put(BestToolVisitor.KEY_GROUP_BY, primaryField.name() +
            (secondaryField == null ? "" : "," + secondaryField.name()));
        parameters.put(BestToolVisitor.KEY_OPERATOR, (StringUtils.hasText(cmd.getPrimaryFieldOperator()) ? cmd.getPrimaryFieldOperator() : "")
            + (secondaryField == null ? "" : ("," + (StringUtils.hasText(cmd.getSecondaryFieldOperator()) ? cmd.getSecondaryFieldOperator() : ""))));
        parameters.put(BestToolVisitor.KEY_SOURCE, sortField.name());

        if (cmd.getRawparam() != null) {
            for (String s : cmd.getRawparam()) {
                final String[] strings = s.split("\\|");
                ratioSearchRequest.addParameter(strings[0], strings[1]);
            }
        }

        final BestToolRatioSearchResponse ratioSearchResponse = (BestToolRatioSearchResponse) this.ratiosProvider.search(ratioSearchRequest);

        final Map<String, Map<Object, List<BestToolRatioSearchResponse.BestToolElement>>> result = ratioSearchResponse.getResult();

        final Map<String, Object> model = new HashMap<>();

        for (final Iterator<Quote> iterator = quotes.iterator(); iterator.hasNext();) {
            final Quote underlyingquote = iterator.next();

            final Map<Object, List<BestToolRatioSearchResponse.BestToolElement>> map = result.get(underlyingquote.getInstrument().getSymbolIsin());
            if (map == null) {
                iterator.remove();
                continue;
            }

            final List<BestElement> intervals = new ArrayList<>();
            for (Map.Entry<Object, List<BestToolRatioSearchResponse.BestToolElement>> entry : map.entrySet()) {
                final List<BestToolRatioSearchResponse.BestToolElement> elements = entry.getValue();
                intervals.add(new BestElement(PriceCoder.decode((Long)entry.getKey()), elements));
            }
            intervals.sort(null);


            model.put(underlyingquote.getInstrument().getSymbolIsin(), intervals);
        }

        model.put("quotes", quotes);
        return new ModelAndView("cerbesttool", model);
    }

    public static class BestElement implements Comparable<BestElement> {
        private final BigDecimal interval;
        private final List<BestToolRatioSearchResponse.BestToolElement> elements;

        public BestElement(BigDecimal interval, List<BestToolRatioSearchResponse.BestToolElement> elements) {
            this.interval = interval;
            this.elements = elements;
        }

        public BigDecimal getInterval() {
            return interval;
        }

        public List<BestToolRatioSearchResponse.BestToolElement> getElements() {
            return elements;
        }

        public int compareTo(BestElement o) {
            return this.interval.compareTo(o.interval);
        }
    }
}
