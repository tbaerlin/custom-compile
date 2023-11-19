/*
 * VendorDocRequest.java
 *
 * Created on 05.03.2015 15:36
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author jkirchg
 */
public class VendorDocRequest extends AbstractIstarRequest {

    private static final long serialVersionUID = 408857959160790135L;

    private final int docId;

    private final String isin;

    private final String wkn;

    private final String name;

    private final byte[] data;

    private final String vwdId;

    private final String address;

    private final String userInfo;

    private final String client;

    private final String variant;

    private VendorDocRequest(int docId, String isin, String wkn, String name, byte[] data,
            String vwdId, String address, String userInfo, String client, String variant) {
        this.isin = isin;
        this.wkn = wkn;
        this.name = name;
        this.data = data;
        this.vwdId = vwdId;
        this.address = address;
        this.userInfo = userInfo;
        this.client = client;
        this.variant = variant;
        this.docId = docId;
    }

    public static VendorDocRequest forCreate(String isin, String wkn, String name, byte[] data,
            String vwdId, String address, String userInfo, String client, String variant) {
        return new VendorDocRequest(-1, isin, wkn, name, data, vwdId, address, userInfo, client, variant);
    }

    public static VendorDocRequest forUpdate(int docId, String name, byte[] data,
            String vwdId, String address, String userInfo, String client, String variant) {
        return new VendorDocRequest(docId, null, null, name, data, vwdId, address, userInfo, client, variant);
    }

    public static VendorDocRequest forDelete(int docId, String vwdId, String address,
            String userInfo, String client, String variant) {
        return new VendorDocRequest(docId, null, null, null, null, vwdId, address, userInfo, client, variant);
    }

    public static VendorDocRequest forRead(int docId, String client, String variant) {
        return new VendorDocRequest(docId, null, null, null, null, null, null, null, client, variant);
    }

    public String getIsin() {
        return this.isin;
    }

    public String getWkn() {
        return wkn;
    }

    public String getName() {
        return this.name;
    }

    public byte[] getData() {
        return this.data;
    }

    public String getVwdId() {
        return this.vwdId;
    }

    public String getAddress() {
        return this.address;
    }

    public String getUserInfo() {
        return this.userInfo;
    }

    public int getDocId() {
        return docId;
    }

    public String getClient() {
        return client;
    }

    public String getVariant() {
        return variant;
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(", docId").append(this.docId)
                .append(", isin=").append(this.isin)
                .append(", wkn=").append(this.wkn)
                .append(", name").append(this.name)
                .append(", dataLength").append(this.data == null ? 0 : data.length)
                .append(", vwdId").append(this.vwdId)
                .append(", ipAddress").append(this.address)
                .append(", userInfo").append(this.userInfo)
                .append(", client").append(this.client)
                .append(", variant").append(this.variant);
    }
}
