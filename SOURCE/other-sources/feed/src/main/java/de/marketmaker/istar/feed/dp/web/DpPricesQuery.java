/*
 * DpPricesQuery.java
 *
 * Created on 29.04.2005 15:38:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp.web;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ordered.BufferFieldData;
import de.marketmaker.istar.feed.ordered.SnapFieldIteratorFactory;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * Contains all the data that makes up a request sent to a DpPricesServlet.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class DpPricesQuery {

    private boolean zipped = false;

    private boolean realtime = false;

    private Charset encoding = Charset.forName("ISO-8859-1");

    private String rootElement = "data";

    private final BitSet fieldIds = new BitSet();

    private final List<FeedData> feedDatas = new ArrayList<>();

    private boolean useFieldids = false;

    private BitSet escapeNulls = null;

    private boolean withFields = false;

    private boolean withDynamicFields = false;

    private boolean withNonDynamicFields = false;

    private boolean withTimeOfArrival = false;

    private boolean withDateOfArrival = false;

    private List<Integer> dates;

    private SnapFieldIteratorFactory iteratorFactory;

    private BufferFieldData fieldData;

    public boolean isZipped() {
        return zipped;
    }

    public void setZipped(boolean zipped) {
        this.zipped = zipped;
    }

    public boolean isRealtime() {
        return realtime;
    }

    public void setRealtime(boolean realtime) {
        this.realtime = realtime;
    }

    boolean isEscapeNulls() {
        return escapeNulls != null;
    }

    BitSet getEscapeNulls() {
        return escapeNulls;
    }

    public void setEscapeNulls(String escapeNulls) {
        if ("false".equalsIgnoreCase(escapeNulls)) {
            this.escapeNulls = null;
        }
        else if ("true".equalsIgnoreCase(escapeNulls)) {
            this.escapeNulls = DpPricesFormatter.DEFAULT_ESCAPE_NULLS;
        }
        else {
            this.escapeNulls = new BitSet();
            for (String s: escapeNulls.split(",")) {
                this.escapeNulls.set(VwdFieldDescription.getFieldByName(s.trim()).id());
            }
        }
    }

    public Charset getEncoding() {
        return encoding;
    }

    public void setEncoding(Charset encoding) {
        this.encoding = encoding;
    }

    public String getRootElement() {
        return rootElement;
    }

    public void setRootElement(String rootElement) {
        this.rootElement = rootElement;
    }

    void addFieldById(int fieldid) {
        this.fieldIds.set(fieldid);
        this.withFields = true;
        boolean isDynamic = VwdFieldOrder.isDynamic(VwdFieldOrder.getOrder(fieldid));
        this.withDynamicFields |= isDynamic;
        this.withNonDynamicFields |= !isDynamic;
        this.withTimeOfArrival |= (fieldid == VwdFieldDescription.ADF_TIMEOFARR.id());
        this.withDateOfArrival |= (fieldid == VwdFieldDescription.ADF_DATEOFARR.id());
    }

    public boolean isWithTimeOfArrival() {

        return withTimeOfArrival;
    }

    public boolean isWithDateOfArrival() {
        return withDateOfArrival;
    }

    public boolean isWithFields() {
        return withFields;
    }

    public boolean isWithDynamicFields() {
        return withDynamicFields;
    }

    public boolean isWithNonDynamicFields() {
        return withNonDynamicFields;
    }

    int[] getFieldIds() {
        final int[] result = new int[this.fieldIds.cardinality()];
        int n = 0;
        for (int i = this.fieldIds.nextSetBit(0); i >= 0; i = this.fieldIds.nextSetBit(i + 1)) {
            result[n++] = i;
        }
        return result;
    }

    public SnapFieldIteratorFactory getIteratorFactory() {
        if (this.iteratorFactory == null) {
            this.iteratorFactory = SnapFieldIteratorFactory.create(this.escapeNulls, getFieldIds());
        }
        return iteratorFactory;
    }

    public Iterator<SnapField> getFieldsForQuery(byte[] orderedSnap) {
        if (orderedSnap == null) {
            return Collections.emptyIterator();
        }
        if (this.fieldData == null) {
            this.fieldData = new BufferFieldData();
        }
        return getIteratorFactory().iterator(this.fieldData.reset(orderedSnap));
    }

    void addQuote(FeedData data) {
        this.feedDatas.add(data);
    }

    void addQuotes(List<FeedData> datas) {
        this.feedDatas.addAll(datas);
    }

    public List<FeedData> getFeedDatas() {
        return feedDatas;
    }

    public void setUseFieldids(boolean useFieldids) {
        this.useFieldids = useFieldids;
    }

    public boolean isUseFieldids() {
        return useFieldids;
    }

    public void addDate(int date) {
        if (this.dates == null) {
            this.dates = new ArrayList<>();
        }
        this.dates.add(date);
    }

    public List<Integer> getDates() {
        return this.dates != null ? this.dates : Collections.<Integer>emptyList();
    }
}
