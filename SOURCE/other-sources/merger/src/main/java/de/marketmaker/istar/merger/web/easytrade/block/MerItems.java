/*
 * FndFindersuchkriterien.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.mer.MerDataProvider;
import de.marketmaker.istar.merger.provider.mer.MerItem;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;

/**
 * Provides macroeconomic data for given countries and aspects.
 * <p>
 * Available countries and aspects can be queried using {@see MER_ItemsMetadata}.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MerItems extends EasytradeCommandController {
    public static class Command extends ListCommand {
        private String[] country;

        private String[] type;

        /**
         * @return country names in German.
         * @sample Deutschland
         */
        @NotNull
        public String[] getCountry() {
            return country;
        }

        public void setCountry(String[] country) {
            this.country = country;
        }

        /**
         * @return macroeconomic aspect in German.
         * @sample BIP
         */
        @NotNull
        public String[] getType() {
            return type;
        }

        public void setType(String[] type) {
            this.type = type;
        }
    }

    private MerDataProvider merDataProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    private IntradayProvider intradayProvider;

    public MerItems() {
        super(Command.class);
    }

    public void setMerDataProvider(MerDataProvider merDataProvider) {
        this.merDataProvider = merDataProvider;
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
        final Map<String, Object> model = new HashMap<>();

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        final List<String> types = Arrays.asList(cmd.getType());
        final List<String> countries = Arrays.asList(cmd.getCountry());
        final List<MerItem> items = this.merDataProvider.getItems(profile, types, countries);
        model.put("items", items);

        final Map<String, List<String>> qidsByCountryXType = new HashMap<>();

        final List<Long> qids = new ArrayList<>(items.size());
        for (final MerItem item : items) {
            qids.add(item.getQid());
            final String key = item.getCountry() + "@" + item.getType();
            List<String> list = qidsByCountryXType.get(key);
            if (list == null) {
                list = new ArrayList<>();
                qidsByCountryXType.put(key, list);
            }
            list.add(Long.toString(item.getQid()));
        }
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);
        final List<PriceRecord> prices = this.intradayProvider.getPriceRecords(quotes);
        final Map<String, Quote> quoteByQid = new HashMap<>();
        final Map<String, PriceRecord> priceByQid = new HashMap<>();
        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            if (quote == null) {
                continue;
            }
            final PriceRecord pr = prices.get(i);
            quoteByQid.put(Long.toString(quote.getId()), quote);
            priceByQid.put(Long.toString(quote.getId()), pr);
        }

        model.put("qidsByCountryXType", qidsByCountryXType);
        model.put("quoteByQid", quoteByQid);
        model.put("priceByQid", priceByQid);
        model.put("countries", countries);
        model.put("types", types);

        return new ModelAndView("meritems", model);
    }
}
