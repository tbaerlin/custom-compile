/*
 * NewsRecord.java
 *
 * Created on 21.11.2005 11:21:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.frontend;

import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.instrument.Instrument;

/**
 * The standard news data structure for i* development.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface NewsRecord {

    // the vwd specific cross-agency id / hashvalue
    String getId();

    // agency/source of this news record
    String getAgency();

    // id provided by the agency that created the news
    // the agency might send updates with the same id replacing the current record
    String getNdbNewsId();

    // used to connect an analysis to the news
    String getNdbRatingId();

    /**
     * @return newsid of a former news that might get updated/replaced by this news
     */
    String getPreviousId();

    void setPreviousId(String previousId);

    DateTime getTimestamp();

    String getHeadline();

    /**
     * news content
     * @return text/plain, text/html or text/nitf formatted news content
     */
    String getText();

    /**
     * Sometimes, a news's text includes advertising; for customers who are allowed to filter those
     * ads, this methods should be used to retrieve the text rather than {@link #getText()}.
     * @return text without any advertising
     */
    String getTextWithoutAds();

    /**
     * @return html-ish encoded string containing img tags
     */
    String getGallery();

    /**
     * @return true iff news is advertisement
     */
    boolean isAd();

    boolean isHtml();

    // news industry text format
    boolean isNitf();

    String getMimetype();

    String getTeaser();

    String getLanguage();

    Integer getPriority();

    Map<NewsAttributeEnum, Set<String>> getAttributes();

    Set<Instrument> getInstruments();

    Entitlement getEntitlement();

    Set<String> getIsinReferences();

    Set<String> getWknReferences();

    Set<String> getSelectors();

    Set<String> getNumericSelectors();

    boolean isWithVwdsymbols();

    Set<String> getTopics();

    Set<String> getCategories();

    String getProductId();
}