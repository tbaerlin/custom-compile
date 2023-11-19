/*
 * StkFinderMetadata.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteNameStrategy;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.util.NameUtil;
import de.marketmaker.istar.domainimpl.data.IndexComposition;
import de.marketmaker.istar.instrument.IndexCompositionRequest;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.web.PermissionDeniedException;
import de.marketmaker.istar.merger.web.easytrade.BaseMultiSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;

import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkFinderMetadata extends AbstractFinderMetadata {
    static final Map<String, String> BY_YEARS_FIELDS = Collections.synchronizedMap(new LinkedHashMap<String, String>());

    static final Map<String, String> NO_YEARS_FIELDS = Collections.synchronizedMap(new LinkedHashMap<String, String>());

    static {
        BY_YEARS_FIELDS.put("Dividend", "Dividende");
        BY_YEARS_FIELDS.put("Dividendyield", "Dividendenrendite");
        BY_YEARS_FIELDS.put("Profit", "Gewinn");
        BY_YEARS_FIELDS.put("Eps", "Ergebnis pro Aktie (EPS)");
        BY_YEARS_FIELDS.put("PriceEarningRatio", "KGV");

        NO_YEARS_FIELDS.put("averageVolume1m", "Ø-Volumen (1 Monat)");
        NO_YEARS_FIELDS.put("averageVolume1y", "Ø-Volumen (1 Jahr)");
        NO_YEARS_FIELDS.put("volatility1m", "Volatilität (1 Monat)");
        NO_YEARS_FIELDS.put("volatility1y", "Volatilität (1 Jahr)");
        NO_YEARS_FIELDS.put("beta1m", "Beta (1 Monat)");
        NO_YEARS_FIELDS.put("beta1y", "Beta (1 Jahr)");
        NO_YEARS_FIELDS.put("correlation1m", "Korrelation (1 Jahr)");
        NO_YEARS_FIELDS.put("correlation1y", "Korrelation (1 Jahr)");
        NO_YEARS_FIELDS.put("changePercent", "Kursänderung (1 Tag)");
        NO_YEARS_FIELDS.put("performance1w", "Kursänderung (1 Woche)");
        NO_YEARS_FIELDS.put("performance1m", "Kursänderung (1 Monat)");
        NO_YEARS_FIELDS.put("performance1y", "Kursänderung (1 Jahr)");
        NO_YEARS_FIELDS.put("performanceToBenchmark1w", "Kursänderung zu Benchmark (1 Woche)");
        NO_YEARS_FIELDS.put("performanceToBenchmark1m", "Kursänderung zu Benchmark (1 Monat)");
        NO_YEARS_FIELDS.put("performanceToBenchmark1y", "Kursänderung zu Benchmark (1 Jahr)");
        NO_YEARS_FIELDS.put("performanceToBenchmark1y", "Kursänderung zu Benchmark (1 Jahr)");
        NO_YEARS_FIELDS.put("factsetRecommendation", "Analystenbewertung");
    }

    private static final Set<String> ALLOWED_LIST_NAMES = new HashSet<>(Arrays.asList(
            "indizes-deutschland",
            "indizes-europa",
            "indizes-usa",
            "indizes-asien",
            "indizes-international")
    );

    private static final Comparator<Quote> QUOTE_BY_NAME_COMPARATOR = new Comparator<Quote>() {
        public int compare(Quote o1, Quote o2) {
            return NameUtil.getDisplayName1(o1).compareTo(NameUtil.getDisplayName1(o2));
        }
    };

    public static class Command extends BaseMultiSymbolCommand implements ProviderSelectionCommand {
        private boolean profileIndexConstituents;

        /**
         * if set to true: filter index list to contain only indices for which all constituents are visible (with the given authentication).
         */
        public boolean isProfileIndexConstituents() {
            return profileIndexConstituents;
        }

        public void setProfileIndexConstituents(boolean profileIndexConstituents) {
            this.profileIndexConstituents = profileIndexConstituents;
        }

        public String getProviderPreference() {
            return null;
        }
    }

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    public void setIndexCompositionProvider(
            ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public StkFinderMetadata() {
        super(Command.class, InstrumentTypeEnum.STK,
                RatioDataRecord.Field.sector,
                RatioDataRecord.Field.country,
                RatioDataRecord.Field.gicsSectorKey,
                RatioDataRecord.Field.gicsIndustryGroupKey,
                RatioDataRecord.Field.gicsIndustryKey,
                RatioDataRecord.Field.gicsSubIndustryKey,
                RatioDataRecord.Field.gicsSector,
                RatioDataRecord.Field.gicsIndustryGroup,
                RatioDataRecord.Field.gicsIndustry,
                RatioDataRecord.Field.gicsSubIndustry);
    }

    protected void onDoHandle(Object o, Map<String, Object> model) {
        final Command cmd = (Command) o;

        addIndexList(cmd, model);
        model.put("byYears", BY_YEARS_FIELDS);
        model.put("noYears", NO_YEARS_FIELDS);

        final int year = new LocalDate().getYear();
        model.put("years", Arrays.asList(year - 2, year - 1, year, year + 1));

        localize(model, RatioDataRecord.Field.gicsSectorKey.name());
        localize(model, RatioDataRecord.Field.gicsIndustryGroupKey.name());
        localize(model, RatioDataRecord.Field.gicsIndustryKey.name());
        localize(model, RatioDataRecord.Field.gicsSubIndustryKey.name());
    }

    private void addIndexList(Command cmd, Map<String, Object> model) {
        final boolean symbolBased = cmd.getSymbol() == null;
        model.put("symbolBased", symbolBased);

        if (symbolBased) {
            addDefaultIndexList(model, cmd.isProfileIndexConstituents());
        }
        else {
            addSymbolList(cmd, model);
        }
    }

    private void addSymbolList(Command cmd, Map<String, Object> model) {
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(Arrays.asList(cmd.getSymbol()),
                cmd.getSymbolStrategy(), cmd.getMarket(), cmd.getMarketStrategy());

        applyProfile(quotes, cmd.isProfileIndexConstituents());

        final LinkedHashMap<Quote, String> quote2name = new LinkedHashMap<>();

        final QuoteNameStrategy qns
                = RequestContextHolder.getRequestContext().getQuoteNameStrategy();

        final Language language = Language.valueOf(RequestContextHolder.getRequestContext().getLocale());
        for (final Quote quote : quotes) {
            final String country = quote.getInstrument().getCountry().getNameOrDefault(language);
            final String name = qns.getName(quote);
            final String displayname = name.replaceAll(" \\(.*\\)", "")
                    + (StringUtils.hasText(country) ? " (" + country + ")" : "");
            quote2name.put(quote, displayname);
        }

        model.put("symbollist", quote2name);
    }

    private List<Long> getQuoteIds(String listid) {
        IndexCompositionResponse response
                = this.indexCompositionProvider.getIndexCompositionByName(listid);
        if (!response.isValid()) {
            return Collections.emptyList();
        }
        return response.getIndexComposition().getQids();
    }

    private void addDefaultIndexList(Map<String, Object> model, boolean profileIndexConstituents) {
        final Set<Long> qids = new HashSet<>();
        for (final String listid : ALLOWED_LIST_NAMES) {
            qids.addAll(getQuoteIds(listid));
        }

        final List<Quote> list = this.instrumentProvider.identifyQuotes(qids);
        applyProfile(list, profileIndexConstituents);

        list.sort(QUOTE_BY_NAME_COMPARATOR);

        model.put("indexliste", list);
    }

    private void applyProfile(List<Quote> list, boolean profileIndexConstituents) {
        CollectionUtils.removeNulls(list);
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        for (final Iterator<Quote> it = list.iterator(); it.hasNext(); ) {
            final Quote quote = it.next();
            if (profile.getPriceQuality(quote) == PriceQuality.NONE) {
                it.remove();
                continue;
            }
            try {
                final IndexCompositionResponse ic = this.indexCompositionProvider.getIndexComposition(new IndexCompositionRequest(quote.getId()));
                if (!ic.isValid()) {
                    it.remove();
                    continue;
                }


                if (profileIndexConstituents && !constituentsAllowed(profile, ic.getIndexComposition())) {
                    it.remove();
                }
            } catch (PermissionDeniedException e) {
                it.remove();
            }
        }
    }

    private boolean constituentsAllowed(Profile profile, IndexComposition indexComposition) {
        final List<Long> qids = indexComposition.getQids();
        final List<Quote> quotes = this.instrumentProvider.identifyQuotes(qids);
        for (final Quote quote : quotes) {
            if (quote != null && profile.getPriceQuality(quote) == PriceQuality.NONE) {
                return false;
            }
        }
        return true;
    }
}