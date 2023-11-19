/*
 * TermWithFreq.java
 *
 * Created on 16.11.2007 13:41:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

/**
 * Number of documents for a particular value of some search field.
 */
class TermWithFreq implements Comparable<TermWithFreq> {
    private final int freq;

    private final String field;

    private final String text; // fieldname:value

    public TermWithFreq(String field, String text, int freq) {
        this.field = field;
        this.freq = freq;
        this.text = text;
    }

    public String getField() {
        return field;
    }

    public String getText() {
        return text;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final TermWithFreq that = (TermWithFreq) o;

        return field.equals(that.field) && text.equals(that.text);
    }

    public int hashCode() {
        int result;
        result = field.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }

    public int compareTo(TermWithFreq o) {
        return o.freq - this.freq;
    }

    public String toString() {
        return this.field + ":" + this.text + "(#" + this.freq + ")";
    }
}
