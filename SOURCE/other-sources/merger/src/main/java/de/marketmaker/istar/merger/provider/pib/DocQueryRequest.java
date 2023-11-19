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
 * todo: shared abstract super classes for DocRequest?
 *
 * @author zzhao
 */
public class DocQueryRequest extends AbstractIstarRequest {

    private static final long serialVersionUID = -2843349249611306640L;

    private final String client;

    private final String variant;

    private DateTime fromDateTime;

    private DateTime toDateTime;

    private final String symbol;

    private int offset;

    private int amount;

//    private Set<String> fields;

    private String orderBy;

    private String address;

    private String docType;

    public DocQueryRequest(String client, String variant, DateTime fromDateTime,
            DateTime toDateTime, String symbol, int offset, int amount, String orderBy) {
        this.client = client;
        this.variant = variant;
        this.fromDateTime = fromDateTime;
        this.toDateTime = toDateTime;
        this.symbol = (null == symbol) ? symbol : symbol.trim().toUpperCase();
        this.offset = offset;
        this.amount = amount;
        this.orderBy = orderBy;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getClient() {
        return client;
    }

    public String getVariant() {
        return variant;
    }

    public int getAmount() {
        return amount;
    }

    public DateTime getFromDateTime() {
        return fromDateTime;
    }

    public int getOffset() {
        return offset;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public DateTime getToDateTime() {
        return toDateTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }
}
