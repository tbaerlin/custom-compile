/*
 * EvaluatedPositionComparator.java
 *
 * Created on 19.09.2006 19:41:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class EvaluatedPositionComparator {
    private abstract static class StringComparator implements Comparator<EvaluatedPosition> {
        private static final Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);

        public int compare(EvaluatedPosition o1, EvaluatedPosition o2) {
            return GERMAN_COLLATOR.compare(getString(o1), getString(o2));
        }

        private String getString(EvaluatedPosition q) {
            final String result = doGetString(q);
            return (result != null) ? result : "\u7fff";
        }

        protected abstract String doGetString(EvaluatedPosition ep);
    }


    public static final Comparator<EvaluatedPosition> BY_NAME = new StringComparator() {
        protected String doGetString(EvaluatedPosition ep) {
            return ep.getQuote().getInstrument().getName();
        }
    };

    public static final Comparator<EvaluatedPosition> BY_CURRENCY = new StringComparator() {
        protected String doGetString(EvaluatedPosition ep) {
            return ep.getQuote().getCurrency().getSymbolIso();
        }
    };

    public static final Comparator<EvaluatedPosition> BY_VWDFEED_MARKET = new StringComparator() {
        protected String doGetString(EvaluatedPosition ep) {
            return ep.getQuote().getSymbolVwdfeedMarket();
        }
    };

    public static final Comparator<EvaluatedPosition> BY_CURRENT_CHANGE_PERCENT = new Comparator<EvaluatedPosition>() {
        public int compare(EvaluatedPosition o1, EvaluatedPosition o2) {
            return getPrice(o1).compareTo(getPrice(o2));
        }

        private BigDecimal getPrice(EvaluatedPosition ep) {
            BigDecimal result = ep.getCurrentPrice().getChangePercent();
            return (result == null) ? BigDecimal.ZERO : result;
        }
    };
    public static final Comparator<EvaluatedPosition> BY_CURRENT_LASTDATE = new Comparator<EvaluatedPosition>() {
        public int compare(EvaluatedPosition o1, EvaluatedPosition o2) {
            return getDate(o1).compareTo(getDate(o2));
        }

        private DateTime getDate(EvaluatedPosition o1) {
            final DateTime date = o1.getCurrentPrice().getPrice().getDate();
            return (date == null) ? new DateTime().withTimeAtStartOfDay() : date;
        }
    };

}
