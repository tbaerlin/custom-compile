package de.marketmaker.istar.merger.provider.pages;

import java.io.Serializable;
import java.util.Comparator;

import de.marketmaker.istar.common.util.XmlUtil;

/**
 * PageSummary.java
 * Created on 15.07.2010 15:25:33
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

/**
 * This class is returned as result of a page search and
 * represents a <em>single</em> page in the result.<br/>
 * It also contains the score of this page in a specific query, so
 * you should not try to reuse instances of this class across several queries.<br/>
 * <b>This class is immutable and has value-semantics based on page number and language.</b><br/>
 * This class does not implement the Comparable-interface, but provides
 * static fields with comparators for some sensible sorting fields.
 * @author Sebastian Wild
 */
public final class PageSummary implements Serializable {

    private static final long serialVersionUID = -8368692986426558365L;


    /**
     * Creates a new instance with the given content.
     * @param pagenumber the page number, id of the page
     * @param contentSummary the title or heading of the page
     * @param language the language of static text
     * @param score the score of this page in the corresponding Lucene query
     */
    public PageSummary(final int pagenumber, final String contentSummary,
            final DocumentFactory.PageLanguage language, final float score) {
        this.pagenumber = pagenumber;
        this.contentSummary = contentSummary;
        this.language = language;
        this.score = score;
    }

    private int pagenumber;

    private String contentSummary;

    private DocumentFactory.PageLanguage language;

    private float score;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageSummary that = (PageSummary) o;

        return pagenumber == that.pagenumber && language == that.language;

    }

    @Override
    public int hashCode() {
        int result = pagenumber;
        result = 31 * result + language.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PageSummary[" + pagenumber + "." + language + ", " + contentSummary.substring(0,
                Math.min(contentSummary.length(), 20)) + ", score=" + score + "]";
    }

    public int getPagenumber() {
        return pagenumber;
    }

    public String getContentSummary() {
        return contentSummary;
    }

    public DocumentFactory.PageLanguage getLanguage() {
        return language;
    }

    /**
     * @return a String representation of {@link #getLanguage()}.
     */
    public String getLanguageString() {
        return language.name();
    }

    /**
     * The same as {@link #getContentSummary()}, i.e. plain getter for
     * contentSummary, but this method uses {@link de.marketmaker.istar.common.util.XmlUtil#encode(String)}
     * to handle special characters correctly.
     * @return contentSummary with encoded special chars
     */
    public String getContentSummaryXml() {
        return XmlUtil.encode(getContentSummary());
    }

    public float getScore() {
        return score;
    }

    /**
     * This comparator instance can be used to sort {@link de.marketmaker.istar.merger.provider.pages.PageSummary}
     * objects according to {@link #getPagenumber()}.
     */
    public static final Comparator<PageSummary> BY_PAGENUMBER_COMPARATOR = new Comparator<PageSummary>() {

        public int compare(PageSummary o1, PageSummary o2) {
            final int nr1 = o1.getPagenumber();
            final int nr2 = o2.getPagenumber();
            return nr1 < nr2 ? -1 : (nr1 > nr2 ? 1 : 0);
        }
    };

    /**
     * This comparator instance can be used to sort {@link de.marketmaker.istar.merger.provider.pages.PageSummary}
     * objects according to {@link #getContentSummary()}. Comparisons are case-insensitive.
     */
    public static final Comparator<PageSummary> BY_CONTENT_SUMMARY_COMPARATOR = new Comparator<PageSummary>() {

        public int compare(PageSummary o1, PageSummary o2) {
            final int contComp = o1.getContentSummary().compareToIgnoreCase(o2.getContentSummary());
            return contComp == 0 ? BY_PAGENUMBER_COMPARATOR.compare(o1, o2) : contComp;
        }
    };

    /**
     * This comparator instance can be used to sort {@link de.marketmaker.istar.merger.provider.pages.PageSummary}
     * objects according to {@link #getScore()}.
     */
    public static final Comparator<PageSummary> BY_SCORE_COMPARATOR = new Comparator<PageSummary>() {

        public int compare(PageSummary o1, PageSummary o2) {
            final int scoreComp = Float.compare(o1.getScore(), o2.getScore());
            // we compare by pagenumber if score is equal
            // BUT we reverse the order, since scores will always be used to sort descendingly 
            return scoreComp == 0 ? BY_PAGENUMBER_COMPARATOR.compare(o2,o1) : scoreComp;
        }
    };

}
