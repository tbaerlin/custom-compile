/*
 * DpPricesInputHandler.java
 *
 * Created on 29.04.2005 15:51:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp.web;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * Handler for parsing a request sent to a DpPricesServlet, the result is stored in a DpPricesQuery
 * object.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class DpPricesInputHandler extends DefaultHandler {
    private final StringBuilder stb = new StringBuilder();

    private final DpPricesQuery query = new DpPricesQuery();

    private final FeedDataRepository feedDataRepository;

    /**
     * attributes of the current xml tag
     */
    private Attributes attributes;

    private Locator locator;

    private StringBuilder filter;

    /**
     * If every filter contains a market name, we do not need to scan all markets for matching
     * vendorkeys but just the specified ones. If there is a single filter without a market, it
     * will set this
     */
    private HashSet<ByteString> marketNames = new HashSet<>();


    DpPricesInputHandler(FeedDataRepository feedDataRepository) {
        this.feedDataRepository = feedDataRepository;
    }

    DpPricesQuery getQuery() {
        return query;
    }

    public void setDocumentLocator(Locator locator) {
        super.setDocumentLocator(locator);
        this.locator = locator;
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        this.stb.append(ch, start, length);
    }

    private String getString() {
        return this.stb.toString().trim();
    }

    private int getLine() {
        return (this.locator != null) ? this.locator.getLineNumber() : -1;
    }

    public void endDocument() throws SAXException {
        if (this.filter != null) {
            final List<FeedData> result = new ArrayList<>();
            final VendorkeyFilter vendorkeyFilter =
                    VendorkeyFilterFactory.create(this.filter.toString());

            final List<FeedMarket> markets = getMarkets();
            for (final FeedMarket market : markets) {
                result.addAll(market.collect(fd ->
                        !fd.isDeleted() && vendorkeyFilter.test(fd.getVendorkey())));
            }

            this.query.addQuotes(result);
        }
        super.endDocument();
    }

    private List<FeedMarket> getMarkets() {
        if (this.marketNames == null) {
            return this.feedDataRepository.getMarkets();
        }
        final List<FeedMarket> result = new ArrayList<>();
        for (ByteString name : marketNames) {
            final FeedMarket market = this.feedDataRepository.getMarket(name);
            if (market != null) {
                result.add(market);
            }
        }
        return result;
    }

    public void startElement(String uri, String localName, String tagName,
            Attributes attributes) throws SAXException {
        this.stb.setLength(0);
        this.attributes = attributes;
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("isZipped".equals(qName)) {
            this.query.setZipped("true".equals(getString()));
        }
        else if ("isRealtime".equals(qName)) {
            this.query.setRealtime("true".equals(getString()));
        }
        else if ("escapeNulls".equals(qName)) {
            this.query.setEscapeNulls(getString());
        }
        else if ("useFieldids".equals(qName)) {
            this.query.setUseFieldids("true".equals(getString()));
        }
        else if ("encoding".equals(qName)) {
            final String encoding = getString();
            if (!Charset.isSupported(encoding)) {
                throw new SAXException("Unsupported encoding on line " + getLine());
            }
            this.query.setEncoding(Charset.forName(encoding));
        }
        else if ("rootElement".equals(qName)) {
            this.query.setRootElement(getString());
        }
        else if ("field".equals(qName)) {
            final String fieldName = getString();
            final VwdFieldDescription.Field field = VwdFieldDescription.getFieldByName(fieldName);
            if (field != null) {
                this.query.addFieldById(field.id());
            }
            else {
                throw new SAXException("Unknown field '" + fieldName + "' in line " + getLine());
            }
        }
        else if ("fieldid".equals(qName)) {
            this.query.addFieldById(Integer.parseInt(getString()));
        }
        else if ("filter".equals(qName)) {
            if (this.attributes == null) {
                throw new SAXException("Missing attribute in filter ending in line " + getLine());
            }
            final String quote = this.attributes.getValue("quote");
            if (quote != null) {
                final FeedData fd = dataDataForQuote(quote);
                if (fd != null) {
                    this.query.addQuote(fd);
                }
                return;
            }
            final String date = this.attributes.getValue("date");
            if (date != null) {
                final DateTime dt;
                try {
                    dt = DateTimeFormat.forPattern("dd.MM.yyyy").parseDateTime(date);
                } catch (IllegalArgumentException e) {
                    throw new SAXException("Illegal date '" + date + "' in line " + getLine());
                }
                this.query.addDate(DateUtil.toYyyyMmDd(dt));
                return;
            }
            addFilter(this.attributes.getValue("endsWith"),
                    this.attributes.getValue("type"),
                    this.attributes.getValue("market"),
                    this.attributes.getValue("partition"));
        }
    }

    private FeedData dataDataForQuote(String quote) {
        ByteString key = new ByteString(quote);
        if (VendorkeyVwd.isKeyWithTypePrefix(key)) {
            key = key.substring(key.indexOf('.') + 1);
        }
        return this.feedDataRepository.get(key);
    }

    private void addFilter(String endsWith, String type, String market, String partition) {
        if (this.filter == null) {
            this.filter = new StringBuilder();
        }
        else {
            this.filter.append(" || ");
        }

        boolean putAnd = false;
        if (StringUtils.hasText(endsWith)) {
            this.filter.append(endsWith + "$");
            putAnd = true;
        }
        if (StringUtils.hasText(type)) {
            if (putAnd) {
                this.filter.append("&&");
            }
            this.filter.append("t:" + type);
            putAnd = true;
        }
        if (StringUtils.hasText(market)) {
            if (putAnd) {
                this.filter.append("&&");
            }
            this.filter.append("^m:" + market + "$");
            if (this.marketNames != null) {
                this.marketNames.add(new ByteString(market));
            }
            putAnd=true;
        }
        else {
            this.marketNames = null;
        }
        if (StringUtils.hasText(partition)) {
            if (putAnd) {
                this.filter.append("&&");
            }
            this.filter.append("p:"+partition);
        }
    }
}
