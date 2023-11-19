/*
 * BndRenditekennzahlen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.qidSymbol;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.NoDataException;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.web.easytrade.HasQuote;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides yields for a given bond list, which contains a list of bonds with maturities, i.e. ranging
 * from 1 year to 10 years.
 * <p>
 * Available bond list id is <code>rex_years</code>.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BndYields extends EasytradeCommandController {
    public static class YieldElement implements HasQuote {
        private final Quote quote;

        private final BigDecimal yield;

        public YieldElement(Quote q, BigDecimal yield) {
            this.quote = q;
            this.yield = yield;
        }

        public String getQid() {
            return qidSymbol(this.quote.getId());
        }

        public String getName() {
            return this.quote.getInstrument().getName();
        }

        public BigDecimal getYield() {
            return yield;
        }

        @Override
        public Quote getQuote() {
            return quote;
        }
    }

    public static class Command {
        private String listid;

        /**
         * @return a bond list id.
         * @sample rex_years
         */
        @NotNull
        public String getListid() {
            return listid;
        }

        public void setListid(String listid) {
            this.listid = listid;
        }
    }

    private static final Pattern CYILD_MATCHER = Pattern.compile("^5\\.CYILD(.*)J\\.DZF$");

    private static final Pattern REX_MATCHER = Pattern.compile("^6\\.8469(..)\\.FFM$");

    private IntradayProvider intradayProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    public BndYields() {
        super(Command.class);
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final List<Quote> quotes = getQuotes(cmd);
        final List<PriceRecord> prices = this.intradayProvider.getPriceRecords(quotes);

        final Map<String, YieldElement> yields = new HashMap<>();
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final PriceRecord pr = prices.get(i);

            final BigDecimal yield = pr.getYield();
            if (yield == null) {
                continue;
            }

            final int year = getYear(quote.getSymbolVwdfeed());
            if (year < 0) {
                continue;
            }

            yields.put("P" + year + "Y", new YieldElement(quote, yield));
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("yields", yields);
        return new ModelAndView("bndyields", model);
    }

    private List<Quote> getQuotes(Command cmd) {
        IndexCompositionResponse response
                = this.indexCompositionProvider.getIndexCompositionByName(cmd.getListid());
        if (!response.isValid()) {
            throw new NoDataException(cmd.getListid());
        }
        final List<Long> quoteids = response.getIndexComposition().getQids();
        final List<Quote> result = this.instrumentProvider.identifyQuotes(quoteids);
        CollectionUtils.removeNulls(result);
        return result;
    }

    private int getYear(String vwdsymbol) {
        final Matcher m1 = CYILD_MATCHER.matcher(vwdsymbol);
        if (m1.matches()) {
            return Integer.parseInt(m1.group(1));
        }

        final Matcher m2 = REX_MATCHER.matcher(vwdsymbol);
        if (m2.matches()) {
            final int num = Integer.parseInt(m2.group(1));
            // 12 ==> 1, 14 ==> 2, ..., 30 ==> 10
            return (num - 10) / 2;
        }

        return -1;
    }
}
