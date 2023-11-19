/*
 * SymbolSortResponse.java
 *
 * Created on 17.02.2005 13:48:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TypedVendorkeysResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 7L;

    private final Map<String, String> result = new HashMap<>();

    public TypedVendorkeysResponse() {
    }

    public TypedVendorkeysResponse(Map<String, String> mappings) {
        this.result.putAll(mappings);
    }

    public Map<String, String> getResult() {
        return this.result;
    }

    public String getTyped(String vwdcode) {
        return this.result.get(vwdcode);
    }

    public List<String> getTypedKeys() {
        return new ArrayList<String>(this.result.values());
    }

    public void add(String vwdcode, String typedVwdcode) {
        this.result.put(vwdcode, typedVwdcode);
    }
}
