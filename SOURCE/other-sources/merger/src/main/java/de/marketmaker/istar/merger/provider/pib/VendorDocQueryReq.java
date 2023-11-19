/*
 * DocQueryRequest.java
 *
 * Created on 06.07.11 15:58
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author zzhao
 */
public class VendorDocQueryReq extends AbstractIstarRequest {

    private static final long serialVersionUID = 2901288352609234239L;

    private final String client;

    private final String variant;

    private DateTime fromDateTime;

    private DateTime toDateTime;

    private int docId;

    private String symbol;

    private int offset;

    private int amount;

    public VendorDocQueryReq(String client, String variant) {
        this.client = client;
        this.variant = variant;
    }

    public String getClient() {
        return client;
    }

    public String getVariant() {
        return variant;
    }

    public DateTime getFromDateTime() {
        return fromDateTime;
    }

    public void setFromDateTime(DateTime fromDateTime) {
        this.fromDateTime = fromDateTime;
    }

    public DateTime getToDateTime() {
        return toDateTime;
    }

    public void setToDateTime(DateTime toDateTime) {
        this.toDateTime = toDateTime;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
