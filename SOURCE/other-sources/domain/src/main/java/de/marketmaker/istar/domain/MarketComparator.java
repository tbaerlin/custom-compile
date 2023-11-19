/*
 * MarketComparator.java
 *
 * Created on 13.07.2006 15:32:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

import java.util.Comparator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class MarketComparator {
    private abstract static class StringComparator implements Comparator<Market> {
        public int compare(Market o1, Market o2) {
            return String(o1).compareTo(String(o2));
        }

        private String String(Market m) {
            final String result = doGetString(m);
            return (result != null) ? result : "\u7fff";
        }

        protected abstract String doGetString(Market m);
    }

    public static final Comparator<Market> BY_NAME = new StringComparator() {
        protected String doGetString(Market m) {
            return m.getName();
        }
    };

}
