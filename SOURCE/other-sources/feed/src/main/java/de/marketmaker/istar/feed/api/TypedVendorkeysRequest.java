/*
 * SymbolSortRequest.java
 *
 * Created on 17.02.2005 13:48:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TypedVendorkeysRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 7L;

    private final Set<String> vwdcodes;

    public TypedVendorkeysRequest() {
        this.vwdcodes = new HashSet<>();
    }

    public TypedVendorkeysRequest(String... vwdcodes) {
        this.vwdcodes = new HashSet<>(Arrays.asList(vwdcodes));
    }

    public TypedVendorkeysRequest(Collection<String> vwdcodes) {
        this.vwdcodes = new HashSet<>(vwdcodes);
    }

    public void add(String vwdcode) {
        this.vwdcodes.add(vwdcode);
    }

    public Iterable<String> getVwdcodes() {
        return this.vwdcodes;
    }

    public boolean isEmpty() {
        return vwdcodes.isEmpty();
    }

    @Override
    public String toString() {
        return "TypedVendorkeysRequest[" +
                "vwdcodes=" + vwdcodes +
                ']';
    }
}
