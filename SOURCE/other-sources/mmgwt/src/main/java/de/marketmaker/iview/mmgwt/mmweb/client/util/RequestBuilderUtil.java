/*
 * RequestBuilderUtil.java
 *
 * Created on 03.03.2009 11:31:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.http.client.RequestBuilder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RequestBuilderUtil {
    public static RequestBuilder forPost(String url) {
        final RequestBuilder result = new RequestBuilder(RequestBuilder.POST, url);
        result.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"); // $NON-NLS-0$ $NON-NLS-1$
        return result;
    }
}
