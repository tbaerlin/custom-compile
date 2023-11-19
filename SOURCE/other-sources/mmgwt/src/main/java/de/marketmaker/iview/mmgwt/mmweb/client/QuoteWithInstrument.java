/*
 * QuoteWithInstrument.java
 *
 * Created on 24.03.2008 10:47:34
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContentFlagsEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class QuoteWithInstrument implements ContextItem {
    private static QuoteWithInstrument lastSelected = null;

    private final InstrumentData instrumentData;

    private final QuoteData quoteData;

    private final String name;
    private final String realName;

    public static final InstrumentData NULL_INSTRUMENT_DATA = new InstrumentData();

    public static final QuoteData NULL_QUOTE_DATA = new QuoteData();
    private HistoryContext historyContext;

    public QuoteWithInstrument(InstrumentData instrumentData, QuoteData quoteData) {
        this(instrumentData, quoteData, null, null);
    }

    public QuoteWithInstrument(InstrumentData instrumentData, QuoteData quoteData, String name) {
        this(instrumentData, quoteData, name, null);
    }

    public QuoteWithInstrument(InstrumentData instrumentData, QuoteData quoteData, String name, String realName) {
        assert instrumentData != null;
        this.instrumentData = instrumentData;
        this.quoteData = quoteData;
        this.name = name;
        this.realName = realName;
    }

    public QuoteWithInstrument withHistoryContext(HistoryContext context) {
        this.historyContext = context;
        return this;
    }

    /**
     * Factory method, which creates a new empty instance of InstrumentData and QuoteData which only hold the qid and the name.
     * This method should only be used, if no instrumentData and quoteData is available.  
     * @param qid .
     * @param name .
     * @param type .
     * @return .
     */
    public static QuoteWithInstrument createFromQid(String qid, String name, String realName, String type) {
        final InstrumentData instrumentData = new InstrumentData();
        instrumentData.setName(name);
        instrumentData.setType(type);
        final QuoteData quoteData = new QuoteData();
        quoteData.setQid(qid);
        return new QuoteWithInstrument(instrumentData, quoteData, name, realName);
    }

    /**
     * If <code>companyName</code>, <code>id</code>, and <code>qd</code> are {@value null}, {@value null} is returned.
     * Otherwise an instance of  {@link QuoteWithInstrument} is returned where <code>id</code> and <code>qd</code> are
     * empty (but not {@value null} and the {@link QuoteWithInstrument#name} and {@link QuoteWithInstrument#realName}
     * fields are intitialized {@link QuoteWithInstrument#name} with the <code>companyName</code>.
     *
     * @see de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.QuoteLinkRenderer#render(Object, StringBuffer, de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer.Context)
     *
     * @param id .
     * @param qd .
     * @param companyName .
     * @return .
     */
    @SuppressWarnings("JavadocReference")
    public static QuoteWithInstrument createQuoteWithInstrument(InstrumentData id, QuoteData qd, String companyName) {
        if (id == null || qd == null) {
            if(companyName != null) {
                return QuoteWithInstrument.createNullInstrumentAndQuote(companyName);
            }
            return null;
        }
        return new QuoteWithInstrument(id, qd);
    }

    /**
     * Returns a new instance of <code>QuoteWithInstrument</code> initialized with {@see NULL_INSTRUMENT_DATA} and {@see NULL_QUOTE_DATA}, using the given company name as the name and real name.
     * This factory method can be used in conjunction with {@see isNullQuoteOrNullInstrument} to determine whether any data is available.
     * Such a &quot;<i>NullQuoteOrNullInstrument</i>&quot;-QwI's <code>instrumentData</code> is set to <code>NULL_INSTRUMENT_DATA</code> and <code>quoteData</code> is set to <code>NULL_QUOTE_DATA</code>.
     * Only <code>getName</code> returns the <code>companyName</code> as a usable value.
     * This is useful if analysis data is available, but without instrument or quote data.
     *
     * @param companyName used as the name and real name.
     * @return a &quot;<i>NullQuoteOrNullInstrument</i>&quot; initialized with <code>companyName</code>.
     */
    public static QuoteWithInstrument createNullInstrumentAndQuote(String companyName) {
        return new QuoteWithInstrument(NULL_INSTRUMENT_DATA, NULL_QUOTE_DATA, companyName, companyName);
    }

    @Override
    public String toString() {
        return "QwI[" + getIid(true) + ", " + (this.quoteData != null ? this.quoteData.getQid() : "_") + "]"; // $NON-NLS$
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return getId().equals(((QuoteWithInstrument)o).getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public InstrumentData getInstrumentData() {
        return instrumentData;
    }

    public QuoteData getQuoteData() {
        return quoteData;
    }

    public String getName() {
        return this.name != null ? this.name : this.instrumentData.getName();
    }

    public String getRealName() {
        return this.realName == null ? getName() : this.realName;
    }

    @SuppressWarnings("unused")
    public boolean hasFlag(ContentFlagsEnum cf) {
        return cf.isAvailableFor(this.quoteData);
    }

    public String getId() {
        if (this.quoteData != null) {
            return this.quoteData.getQid();
        }
        return this.instrumentData.getIid();
    }

    public String getIid(boolean withSuffix) {
        if (withSuffix) {
            return this.instrumentData.getIid();
        }
        String s = this.instrumentData.getIid();
        return s.substring(0, s.length() - 4);
    }

    public void goToPortrait() {
        PlaceUtil.goTo(getTokenPortrait());
    }

    public void goToPortrait(HistoryContext context) {
        PlaceUtil.goTo(getTokenPortrait(), context);
    }

    public String getTokenPortrait() {
        return getToken("U"); // $NON-NLS-0$
    }

    public void goToChartcenter() {
        PlaceUtil.goTo(getTokenChartcenter());
    }

    public void goToChartcenter(HistoryContext context) {
        PlaceUtil.goTo(getTokenChartcenter(), context);
    }

    public String getTokenChartcenter() {
        return getToken("C"); // $NON-NLS-0$
    }

    public void goToScreener() {
        PlaceUtil.goTo(getTokenScreener());
    }

    public String getTokenScreener() {
        return getToken("S"); // $NON-NLS-0$
    }

    public void goToReports() {
        PlaceUtil.goTo(getTokenReports());
    }

    public String getTokenReports() {
        return getToken("P"); // $NON-NLS-0$
    }

    public String getToken(String view) {
        return PlaceUtil.getPortraitPlace(this, view);
    }

    /**
     * This is used to determine if the QwI is from an analysis that does not have a valid instrument or quote.
     * @return true if instrumentData's identity is {@link #NULL_INSTRUMENT_DATA} or if quoteData's identity is {@link #NULL_QUOTE_DATA}
     */
    public boolean isNullQuoteOrNullInstrument() {
        return instrumentData == NULL_INSTRUMENT_DATA || quoteData == NULL_QUOTE_DATA;
    }

    public static QuoteWithInstrument getLastSelected() {
        return lastSelected;
    }

    public static void setLastSelected(QuoteWithInstrument qwi) {
        lastSelected = qwi;
//        DebugUtil.logToFirebugConsole("lastSelected = " + qwi);
    }

    public HistoryContext getHistoryContext() {
        return this.historyContext;
    }

    @SuppressWarnings("UnusedParameters")
    public String getName(String name) {
        return this.getInstrumentData().getName();
    }
}