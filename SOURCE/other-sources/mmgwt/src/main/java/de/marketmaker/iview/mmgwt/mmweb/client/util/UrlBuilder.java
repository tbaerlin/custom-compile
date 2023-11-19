/*
 * QueryBuilder.java
 *
 * Created on 07.08.2009 16:18:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.http.client.URL;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.Settings;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;

import java.util.Map;

/**
 * Builder for urls that possibly come with a jsessionid and query parameters
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UrlBuilder {
    public static final String MODULE_NAME = GuiDefsLoader.getModuleName();

    private final StringBuilder sb;

    private char separator = '?';

    public static UrlBuilder byName(String name) {
        if (name.endsWith(".pdf")) { // $NON-NLS$
            return forPdf(name);
        }
        if (name.endsWith(".csv")) { // $NON-NLS$
            return forCsv(name);
        }
        if (name.endsWith(".xls")) { // $NON-NLS$
            return forCsv(name);
        }
        if (name.endsWith(".png")) { // $NON-NLS$
            return forPng(name);
        }
        throw new IllegalStateException("Unknown type: " + name); // $NON-NLS$
    }

    public static UrlBuilder forPdf(String name) {
        final String pdfBaseUri = Settings.INSTANCE.pdfBaseUri();
        return forFop(pdfBaseUri, name);
    }

    public static UrlBuilder forPng(String name) {
        final String pngBaseUri = Settings.INSTANCE.pngBaseUri();
        return forFop(pngBaseUri, name);
    }

    private static UrlBuilder forFop(String baseUri, String name) {
        final UrlBuilder urlBuilder = new UrlBuilder(baseUri + name, true).addPageOrientation();
        if (baseUri.startsWith("/")) { // $NON-NLS-0$
            urlBuilder.addBaseUrl().addZone();
        }
        return urlBuilder;
    }

    public static UrlBuilder forCsv(String name) {
        return new UrlBuilder(Settings.INSTANCE.csvBaseUri() + name, true);
    }

    public static UrlBuilder forDmxml(String name) {
        return new UrlBuilder(MainController.INSTANCE.contextPath + "/" + MODULE_NAME + "/" + name, false); // $NON-NLS$
    }

    public static UrlBuilder forPmReport(String name) {
        return new UrlBuilder(MainController.INSTANCE.contextPath + "/" + name, false); // $NON-NLS$
    }

    public static UrlBuilder forAsDocman(String url) {
        if(url != null && !url.trim().startsWith("/")) {   // $NON-NLS$s
            url = "/" + url;  // $NON-NLS$
        }
        return new UrlBuilder(MainController.INSTANCE.contextPath + "/as-docman" + url, false); // $NON-NLS$
    }

    public static UrlBuilder forCharts(String name) {
        final StringBuilder sb = new StringBuilder();
        ChartUrlFactory.addUrl(sb, name);
        return new UrlBuilder(sb, false);
    }

    public static String getServerPrefix() {
        return getServerPrefix(false);
    }

    public static String getServerPrefix(boolean includeZone) {
        final String serverPrefix = JsUtil.getServerSetting("serverPrefix"); // $NON-NLS$

        if (includeZone) {
            return serverPrefix + "/" + GuiDefsLoader.getModuleName();
        } else {
            return serverPrefix;
        }
    }

    public static String ensureServerPrefix(String uri, boolean includeZone) {
        if (!uri.startsWith(getServerPrefix(false))) {
            uri = getServerPrefix(includeZone) + '/' + uri;
        }

        return uri;
    }

    public UrlBuilder(String baseUri, boolean withJsessionId) {
        this(new StringBuilder().append(baseUri), withJsessionId);
    }

    public UrlBuilder(StringBuilder sb, boolean withJsessionId) {
        this.sb = sb;

        if (sb.indexOf("?") >= 0) {
            this.separator = '&';
        }

        if (withJsessionId) {
            final String credentials = SessionData.INSTANCE.getCredentials();
            if (credentials != null) {
                add("credential", credentials); // $NON-NLS-0$
            }
            else {
                add(';', "jsessionid", SessionData.INSTANCE.getJsessionID()); // $NON-NLS-0$
            }
        }
    }

    public UrlBuilder addStyleSuffix() {
        return add("styleVariant", SessionData.INSTANCE.getStyleSuffix()); // $NON-NLS-0$
    }

    public UrlBuilder add(SnippetConfiguration config, String key) {
        add(key, config.getString(key));
        return this;
    }

    public UrlBuilder addAll(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public UrlBuilder add(String key, String value) {
        if (add(this.separator, key, value)) {
            this.separator = '&';
        }
        return this;
    }

    private boolean add(char sep, String key, String value) {
        if (StringUtil.hasText(key) && value != null) {
            this.sb.append(sep).append(key).append('=').append(URL.encodeQueryString(value));
            return true;
        }
        return false;
    }

    private UrlBuilder addPageOrientation() {
        if (SessionData.INSTANCE.getUser().getAppConfig().getBooleanProperty(AppConfig.PDF_ORIENTATION_PORTRAIT, false)) {
            add("pageOrientation", "portrait"); // $NON-NLS-0$ $NON-NLS-1$
        }
        return this;
    }

    private UrlBuilder addBaseUrl() {
        add("baseUrl", GWT.getModuleBaseURL()); // $NON-NLS-0$
        return this;
    }

    private UrlBuilder addZone() {
        add("zone", GuiDefsLoader.getModuleName()); // $NON-NLS-0$
        return this;
    }

    public String toURL() {
        return sb.toString();
    }

    @Override
    public String toString() {
        return toURL();
    }

    public static String getAsyncPmUrl(String sessionId) {
        return getWebSocketUrl("/pmweb/asyncWs/" + sessionId); // $NON-NLS$
    }

    public static String getWebSocketUrl(String urlSuffix) {
        final String serverPrefix = getServerPrefix();
        final String url = serverPrefix == null
                ? (MainController.INSTANCE.contextPath + urlSuffix)
                : (serverPrefix + MainController.INSTANCE.contextPath + urlSuffix);
        if (url.startsWith("http://")) { // $NON-NLS$
            return url.replace("http://", "ws://"); // $NON-NLS$
        }
        else if (url.startsWith("https://")) { // $NON-NLS$
            return url.replace("https://", "wss://"); // $NON-NLS$
        }
        else {
            final String hostUrl = Document.get().getURL();
            if (!hostUrl.matches("https{0,1}://[^/]+/.*")) { // $NON-NLS$
                throw new RuntimeException("cannot extract server base from url (not http[s]?): " + hostUrl); // $NON-NLS$
            }
            final int pos = hostUrl.indexOf('/', 8);
            if (pos == -1) {
                throw new RuntimeException("cannot extract server base from url (no trailing slash): " + hostUrl); // $NON-NLS$
            }
            return "ws" + hostUrl.substring(4, pos) + url; // $NON-NLS$

        }
    }
}
