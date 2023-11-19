/*
 * News2Document.java
 *
 * Created on 15.03.2007 12:31:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsIndexConstants;

import static de.marketmaker.istar.news.frontend.NewsIndexConstants.*;
import static org.joda.time.DateTimeConstants.MILLIS_PER_SECOND;

/**
 * Transforms NewsItem objects into lucene Document objects. In order to be able to use the
 * FastVectorHighlighter, the instance has to be configured calling
 * {@link #setTextTermVector(org.apache.lucene.document.Field.TermVector)} with parameter
 * {@link org.apache.lucene.document.Field.TermVector#WITH_POSITIONS}
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class News2Document {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    static final Collection<String> UPDATING_AGENCIES = Arrays.asList("dpa");

    private Field.TermVector textTermVector = Field.TermVector.NO;

    private boolean tokenizeHeadlineAndTeaserAsText = false;

    public void setTextTermVector(Field.TermVector textTermVector) {
        this.textTermVector = textTermVector;
    }

    public void setTokenizeHeadlineAndTeaserAsText(boolean tokenizeHeadlineAndTeaserAsText) {
        this.tokenizeHeadlineAndTeaserAsText = tokenizeHeadlineAndTeaserAsText;
    }

    /**
     * turning the NewsRecord from feed into a Lucene Document
     */
    public Document toDocument(NewsRecordImpl item) {
        final Document d = new Document();
        d.add(field(FIELD_ID, item.getId(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));

        d.add(new NumericField(FIELD_TIMESTAMP).setIntValue(encodeTimestamp(item.getTimestamp())));
        //noinspection deprecation
        noNorms(d, FIELD_SHORTID, encodeShortid(item.getShortId()));
        noNorms(d, FIELD_SEQ_NO, encodeSequenceNo(item));

        final String agency = item.getAgency();
        if (StringUtils.hasText(agency)) {
            noNorms(d, FIELD_AGENCY, agency);
        }

        final String agencyProvidedId = item.getNdbNewsId();
        if (StringUtils.hasText(agencyProvidedId)) {
            noNorms(d, FIELD_AGENCY_PROVIDED_ID, agencyProvidedId);
        }

        final String ratingId = item.getNdbRatingId();
        if (StringUtils.hasText(ratingId)) {
            noNorms(d, FIELD_RATING_ID, ratingId);
        }

        for (Instrument instrument : item.getInstruments()) {
            noNorms(d, FIELD_IID, Long.toString(instrument.getId()));
            final Set<String> symbols = getSymbols(instrument);
            for (String s : symbols) {
                addSymbol(d, s);
            }
        }

        final Map<NewsAttributeEnum, Set<String>> attributes = item.getAttributes();

        final Set<String> rawSelectors = attributes.remove(NewsAttributeEnum.SELECTOR);
        if (rawSelectors != null) {
            final Set<String> selectors = normalize(rawSelectors);
            noNorms(d, NewsIndexConstants.FIELD_SELECTOR, selectors);
        }

        noNorms(d, NewsIndexConstants.FIELD_TOPIC, item.getTopics());

        // index all attributes
        for (NewsAttributeEnum elem : attributes.keySet()) {
            if (elem == NewsAttributeEnum.IID) {
                continue;
            }
            final Set<String> values = attributes.get(elem);
            noNorms(d, ATTRIBUTE_2_FIELDNAME.get(elem), values);

            // index the providercodes, the client wants to perform a search with "providercode:bla"
            // refering a subset of the attributes
            if (NewsAttributeEnum.PROVIDER_CODE_ATTRIBUTES.contains(elem)) {
                noNorms(d, FIELD_PROVIDERCODE, values);
            }
        }

        final String headline = decode(item.getHeadline());
        tokenized(d, FIELD_HEADLINE, headline);
        if (this.tokenizeHeadlineAndTeaserAsText) {
            tokenized(d, FIELD_TEXT, headline);
        }

        if (item.getStory() != null) {
            tokenized(d, FIELD_TEXT, getText(item));
        }

        final String teaser = item.getTeaser();
        if (teaser != null) {
            final String rawTeaser = (item.isHtml() || item.isNitf()) ? stripTags(item, teaser) : decode(teaser);
            tokenized(d, FIELD_TEASER, rawTeaser);
            if (this.tokenizeHeadlineAndTeaserAsText) {
                tokenized(d, FIELD_TEXT, rawTeaser);
            }
        }

        noNorms(d, FIELD_MIMETYPE, item.getMimetype());

        final String language = item.getLanguage();
        if (language != null) {
            noNorms(d, FIELD_LANGUAGE, language);
        }
        else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<toDocument> no language for news " + item.getId() + " " + item.getAgency());
            }
        }

        final String supplier = item.getSupplier();
        if (supplier != null) {
            noNorms(d, FIELD_SUPPLIER, supplier);
        }

        final Integer priority = item.getPriority();
        if (priority != null && priority != 0) {
            noNorms(d, NewsIndexConstants.FIELD_PRIORITY, priority.toString());
        }
        
        if (item.isAd()) {
            noNorms(d, FIELD_AD, "1"); // advertisement flag
        }

        return d;
    }

    String getText(NewsRecordImpl item) {
        final String result = item.getTextWithoutAds();
        if (item.isHtml() || item.isNitf()) {
            return stripTags(item, result);
        }
        return decode(result);
    }

    /**
     * Removes all tags from html or xml
     * @param item news record
     * @param html to be stripped
     * @return stripped
     */
    private String stripTags(NewsRecordImpl item, String html) {
        final StringBuilder result = new StringBuilder(html.length());
        try {
            new ParserDelegator().parse(new StringReader(html), new HTMLEditorKit.ParserCallback() {
                @Override
                public void handleText(char[] data, int pos) {
                    result.append(' ').append(data);
                }
            }, true);
            return result.toString();
        } catch (IOException e) {
            this.logger.warn("<stripTags> failed to strip tags from " + item, e);
            return html;
        }
    }

    private Set<String> normalize(Set<String> rawSelectors) {
        final Set<String> result = new HashSet<>();
        for (String selector : rawSelectors) {
            final String normalized = normalize(selector);
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String normalize(String selector) {
        try {
            return EntitlementsVwd.normalize(selector);
        } catch (Exception e) {
            return null;
        }
    }

    private static String encodeSequenceNo(NewsRecordImpl item) {
        final SnapField field = item.getField(VwdFieldDescription.NDB_Story_Number.id());
        if (field.isDefined()) {
            return field.getValue().toString();
        }
        return "0";
    }

    private static String decode(String s) {
        return StringEscapeUtils.unescapeHtml4(s);
    }

    private static void noNorms(Document d, final String name, final Set<String> values) {
        for (String value : values) {
            noNorms(d, name, value);
        }
    }

    private static void noNorms(Document d, String name, String value) {
        d.add(field(name, value.toLowerCase(), Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS));
    }

    private static Field field(String name, String value, Field.Store store, Field.Index index) {
        final Field result = new Field(name, value, store, index);
        result.setOmitTermFreqAndPositions(true);
        return result;
    }

    private void tokenized(Document d, String field, String value) {
        d.add(new Field(field, value, Field.Store.NO, Field.Index.ANALYZED, this.textTermVector));
    }

    private static Set<String> getSymbols(Instrument instrument) {
        final Set<String> result = new HashSet<>();
        addSymbol(instrument, KeysystemEnum.ISIN, result);
        addSymbol(instrument, KeysystemEnum.WKN, result);
        addQuoteSymbols(instrument, KeysystemEnum.VWDSYMBOL, result);
        addQuoteSymbols(instrument, KeysystemEnum.WM_TICKER, result);
        return result;
    }

    private static void addSymbol(Instrument instrument, final KeysystemEnum keysystem,
            Set<String> symbols) {
        final String symbol = instrument.getSymbol(keysystem);
        if (symbol != null) {
            symbols.add(symbol);
        }
    }

    private static void addQuoteSymbols(Instrument instrument, final KeysystemEnum keysystem,
            Set<String> symbols) {
        for (Quote quote : instrument.getQuotes()) {
            final String symbol = quote.getSymbol(keysystem);
            if (symbol != null) {
                symbols.add(symbol);
            }
        }
    }

    private static void addSymbol(Document result, String symbol) {
        if (symbol != null) {
            noNorms(result, FIELD_SYMBOL, symbol);
        }
    }

    public static String encodeShortid(final int shortId) {
        // MAX_RADIX -> shorter string, less space required, faster search
        return Integer.toString(shortId, Character.MAX_RADIX);
    }

    /**
     * Encode timestamp for indexing. An encoded timestamp can be decoded using
     * {@link #decodeTimestamp(long)}.
     * @param timestamp to be encoded
     * @return encoded timestamp or 0 if timestamp is null
     */
    public static int encodeTimestamp(DateTime timestamp) {
        return encodeTimestamp(timestamp, 0);
    }

    public static int encodeTimestamp(DateTime timestamp, int defaultValue) {
        if (timestamp == null) {
            return defaultValue;
        }
        // todo: change before overflow happens (at 03:14:07 UTC 2038-01-19)
        return (int) (timestamp.getMillis() / MILLIS_PER_SECOND);
    }

    /**
     * Reverses the effect of encodeTimestamp
     * @param ts an encoded timestamp as obtained by calling
     * {@link #encodeTimestamp(org.joda.time.DateTime)}.
     * @return decoded timestamp
     */
    public static DateTime decodeTimestamp(long ts) {
        // if ts' type were int, multiplying it with the int MILLIS_PER_SECOND would cause on overflow!
        return new DateTime(ts * MILLIS_PER_SECOND);
    }
}
