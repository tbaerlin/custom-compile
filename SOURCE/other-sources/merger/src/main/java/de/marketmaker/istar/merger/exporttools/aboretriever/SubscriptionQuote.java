/*
 * SubscriptionQuote.java
 *
 * Created on 21.04.2010 16:53:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.exporttools.aboretriever;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SubscriptionQuote {
    private final short id;

    private final QuoteData quote;

    public SubscriptionQuote(short id, QuoteData quote) {
        this.id = id;
        this.quote = quote;
    }

    public short getId() {
        return id;
    }

    public QuoteData getQuote() {
        return quote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscriptionQuote quote1 = (SubscriptionQuote) o;

        if (!quote.equals(quote1.quote)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return quote.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SubscriptionQuote");
        sb.append("{id='").append(id).append('\'');
        sb.append(", quote='").append(quote).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
