/*
 * QuoteComparator.java
 *
 * Created on 13.07.2006 15:35:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.instrument;

import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.MarketcategoryEnum;

import org.joda.time.DateTime;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Defines instances of Comparator&lt;Quote> objects.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class QuoteComparator {

    private static final Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

    private static final Collator GERMAN_COLLATOR_PRIMARY = Collator.getInstance(Locale.GERMAN);

    static {
        GERMAN_COLLATOR_PRIMARY.setStrength(Collator.PRIMARY);
    }

    /**
     * Prefers quotes that "match" a criterion specified by a subclass, i.e., a matching quote is "less"
     * than a non-matching quote and will therefore appear before the latter in sorted lists.
     */
    private abstract static class MatchComparator implements Comparator<Quote> {
        @Override
        public int compare(Quote o1, Quote o2) {
            final int m1 = matches(o1) ? -1 : 0;
            final int m2 = matches(o2) ? 1 : 0;
            return m1 + m2;
        }

        protected abstract boolean matches(Quote q);
    }


    public static final Comparator<Quote> PREFER_BOERSE = new MatchComparator() {
        @Override
        public boolean matches(Quote q) {
            return q.getMarket().getMarketcategory() == MarketcategoryEnum.BOERSE;
        }
    };

    private static Comparator<Quote> preferCountry(final String code) {
        return new MatchComparator() {
            @Override
            protected boolean matches(Quote q) {
                return code.equals(q.getMarket().getCountry().getSymbolIso());
            }
        };
    }

    private static Comparator<Quote> preferVwdMarketnames(final String... codes) {
        return new MatchComparator() {
            private final Set<String> set = new HashSet<>(Arrays.asList(codes));

            @Override
            protected boolean matches(Quote q) {
                return set.contains(q.getSymbolVwdfeedMarket());
            }
        };
    }

    public static class QuoteOrderComparator implements Comparator<Quote> {
        private final QuoteOrder order;

        public QuoteOrderComparator(QuoteOrder order) {
            this.order = order;
        }

        public int compare(Quote o1, Quote o2) {
            return o1.getOrder(this.order) - o2.getOrder(this.order);
        }
    }

    private abstract static class StringComparator implements Comparator<Quote> {
        private final Collator collator;

        protected StringComparator() {
            this(false);
        }

        protected StringComparator(boolean strengthPrimary) {
            this.collator = strengthPrimary ? GERMAN_COLLATOR_PRIMARY : GERMAN_COLLATOR;
        }

        public int compare(Quote o1, Quote o2) {
            return this.collator.compare(getString(o1), getString(o2));
        }

        protected abstract String doGetString(Quote q);

        private String getString(Quote q) {
            if (q == null) {
                return "\u7fff";
            }
            final String result = doGetString(q);
            return (result != null) ? result : "\u7fff";
        }
    }

    public static Comparator<Quote> byMarketName(final MarketNameStrategy strategy) {
        return byName(strategy, false);
    }

    public static Comparator<Quote> byName(final QuoteNameStrategy strategy) {
        return byName(strategy, false);
    }

    public static Comparator<Quote> byName(final NameStrategy<?, Quote> strategy,
            boolean usePrimaryStrengthCollator) {
        return new StringComparator(usePrimaryStrengthCollator) {
            @Override
            protected String doGetString(Quote q) {
                return strategy.getName(q);
            }
        };
    }

    public static final Comparator<Quote> BY_CURRENCY_ISO = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getCurrency().getSymbolIso();
        }
    };

    public static final Comparator<Quote> BY_MARKET_FEEDSYMBOL = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getMarket().getSymbol(KeysystemEnum.VWDFEED);
        }
    };

    public static final Comparator<Quote> BY_EXPIRES = new Comparator<Quote>() {
        public int compare(Quote o1, Quote o2) {
            final DateTime e1 = o1.getInstrument().getExpiration();
            final DateTime e2 = o2.getInstrument().getExpiration();
            if (e1 == null) {
                return (e2 == null) ? 0 : 1;
            }
            if (e2 == null) {
                return -1;
            }
            return e1.compareTo(e2);
        }
    };

    public static final Comparator<Quote> BY_INSTRUMENT_NAME = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getInstrument().getName();
        }
    };

    public static final Comparator<Quote> BY_INSTRUMENT_TYPE = new StringComparator() {
        @Override
        protected String doGetString(Quote q) {
            return q.getInstrument().getInstrumentType().toString();
        }
    };

    public static final Comparator<Quote> BY_INSTRUMENT_TYPE_DESCRIPTION
            = new StringComparator() {
        @Override
        protected String doGetString(Quote q) {
            return q.getInstrument().getInstrumentType().getDescription();
        }
    };

    public static final Comparator<Quote> BY_ISIN = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getInstrument().getSymbolIsin();
        }
    };

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> BY_MARKETNAME_LBBW = CollectionUtils.chain(
            preferVwdMarketnames("ETR", "EEU", "EUS", "STG", "FONDS", "FX"),
            PREFER_BOERSE,
            preferCountry("DE"),
            byName(MarketNameStrategies.DEFAULT.with(Locale.GERMAN, null), true)
    );

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> BY_MARKETNAME_CH = CollectionUtils.chain(
            preferVwdMarketnames("FONDS", "FX", "VX", "SWX"),
            PREFER_BOERSE,
            preferCountry("CH"),
            preferVwdMarketnames("ETR", "EEU", "EUS", "STG"),
            byName(MarketNameStrategies.DEFAULT.with(Locale.GERMAN, null), true)
    );

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> BY_MARKETNAME_IT = CollectionUtils.chain(
            preferVwdMarketnames("FONDS", "FX", "IT"),
            PREFER_BOERSE,
            preferCountry("IT"),
            preferVwdMarketnames("ETR", "EEU", "EUS", "STG"),
            byName(MarketNameStrategies.DEFAULT.with(Locale.ITALIAN, null), true)
    );

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> BY_MARKETNAME_NL = CollectionUtils.chain(
            preferVwdMarketnames("FONDS", "FX", "NL"),
            PREFER_BOERSE,
            preferCountry("NL"),
            preferVwdMarketnames("ETR", "EEU", "EUS", "STG"),
            byName(MarketNameStrategies.DEFAULT.with(new Locale("nl"), null), true)
    );

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> BY_MARKETNAME_BE = CollectionUtils.chain(
            preferVwdMarketnames("FONDS", "FX", "BL"),
            PREFER_BOERSE,
            preferCountry("BE"),
            preferVwdMarketnames("ETR", "EEU", "EUS", "STG"),
            byName(MarketNameStrategies.DEFAULT.with(Locale.FRENCH, null), true)
    );

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> BY_MARKETNAME_FR = CollectionUtils.chain(
            preferVwdMarketnames("FONDS", "FX", "FR"),
            PREFER_BOERSE,
            preferCountry("FR"),
            preferVwdMarketnames("ETR", "EEU", "EUS", "STG"),
            byName(MarketNameStrategies.DEFAULT.with(Locale.FRENCH, null), true)
    );

    public static final Comparator<Quote> BY_MARKET_NORMAL_MAKRETS_FIRST = new MatchComparator() {
        private final Set<String> blackMarkets
                = new HashSet<>(Arrays.asList("OBUS", "OBUFFM", "OBUETR"));

        @Override
        protected boolean matches(Quote q) {
            return !this.blackMarkets.contains(q.getSymbolVwdfeedMarket());
        }
    };

    public static final Comparator<Quote> BY_MMWKN = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getSymbolMmwkn();
        }
    };

    public static final Comparator<Quote> BY_VWDCODE = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getSymbolVwdcode();
        }
    };

    public static final Comparator<Quote> BY_VWDFEED = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getSymbolVwdfeed();
        }
    };

    public static final Comparator<Quote> BY_VWDFEED_MARKET = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getSymbolVwdfeedMarket();
        }
    };

    public static final Comparator<Quote> BY_VWDSYMBOL = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getSymbolVwdsymbol();
        }
    };

    public static final Comparator<Quote> BY_WKN = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getInstrument().getSymbolWkn();
        }
    };

    public static final Comparator<Quote> BY_WM_TICKER = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getSymbolWmTicker();
        }
    };

    public static final Comparator<Quote> BY_WM_WP_NAME_KURZ = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getSymbolWmWpNameKurz();
        }
    };

    public static final Comparator<Quote> BY_WM_WP_NAME_LANG = new StringComparator() {
        protected String doGetString(Quote q) {
            return q.getSymbolWmWpNameLang();
        }
    };

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> MARKET_MANAGER_SEARCH_QUOTE_COMPARATOR
            = CollectionUtils.chain(PREFER_BOERSE, preferCountry("DE"), BY_MARKET_FEEDSYMBOL);

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> SWISS_SEARCH_QUOTE_COMPARATOR = CollectionUtils.chain(
            PREFER_BOERSE, preferCountry("CH"), preferCountry("DE"), BY_MARKET_FEEDSYMBOL);

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> ITALIAN_SEARCH_QUOTE_COMPARATOR = CollectionUtils.chain(
            PREFER_BOERSE, preferCountry("IT"), BY_MARKET_FEEDSYMBOL);

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> BELGIAN_SEARCH_QUOTE_COMPARATOR = CollectionUtils.chain(
            PREFER_BOERSE, preferCountry("BE"), BY_MARKET_FEEDSYMBOL);

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> DUTCH_SEARCH_QUOTE_COMPARATOR = CollectionUtils.chain(
            PREFER_BOERSE, preferCountry("NL"), BY_MARKET_FEEDSYMBOL);

    @SuppressWarnings("unchecked")
    public static final Comparator<Quote> FRENCH_SEARCH_QUOTE_COMPARATOR = CollectionUtils.chain(
            PREFER_BOERSE, preferCountry("FR"), BY_MARKET_FEEDSYMBOL);
}
