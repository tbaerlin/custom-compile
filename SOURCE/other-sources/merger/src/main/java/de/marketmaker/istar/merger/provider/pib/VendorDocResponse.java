/*
 * PibResponse.java
 *
 * Created on 28.03.11 14:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pib;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

public class VendorDocResponse extends AbstractIstarResponse {

    public static final VendorDocResponse INVALID = new VendorDocResponse();

    private static final long serialVersionUID = 5776572740248280153L;

    private final int docId;

    private final String isin;

    private final String wkn;

    private final String name;

    private VendorDocResponse() {
        this.docId = -1;
        this.isin = null;
        this.wkn = null;
        this.name = null;
        setInvalid();
    }

    public VendorDocResponse(int id, String isin, String wkn, String name) {
        this.docId = id;
        this.isin = isin;
        this.wkn = wkn;
        this.name = name;
    }

    public int getDocId() {
        return docId;
    }

    public String getIsin() {
        return isin;
    }

    public String getWkn() {
        return wkn;
    }

    public String getName() {
        return name;
    }
}
