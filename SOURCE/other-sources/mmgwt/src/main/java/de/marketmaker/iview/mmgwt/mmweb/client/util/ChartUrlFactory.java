/*
 * ChartUrlFactory.java
 *
 * Created on 04.04.2008 11:19:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.core.client.Duration;
import com.google.gwt.http.client.URL;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ChartUrlFactory {
    private static String suffix;

    private static double nextSuffixUpdate = 0;

    private static String authentication = null;

    private static String chartUrlPrefix = MainController.INSTANCE.contextPath + "/" + UrlBuilder.MODULE_NAME + "/";

    public static void setAuthentication(String authentication) {
        ChartUrlFactory.authentication = "&authentication=" + URL.encodeQueryString(authentication); // $NON-NLS$
    }

    public static void setCredentials(String credentials) {
        ChartUrlFactory.authentication = "&credential=" + URL.encodeQueryString(credentials); // $NON-NLS$
    }

    public static void setChartUrlPrefix(String prefix) {
        if (StringUtil.hasText(prefix)) {
            ChartUrlFactory.chartUrlPrefix = prefix.endsWith("/") ? prefix : (prefix + "/");
        }
    }

    private static String getSuffix() {
        final double now = Duration.currentTimeMillis();
        if (now > nextSuffixUpdate) {
            nextSuffixUpdate = now + 10000;
            final String s = Double.toString(now / 10000);
            suffix = s.substring(0, s.indexOf('.'));
        }
        return suffix;
    }

    public static String getUrl(String s) {
        return addUrl(new StringBuilder(100), s).toString();
    }

    public static StringBuilder addUrl(StringBuilder sb, String s) {
        if (!s.startsWith("http")) { // $NON-NLS$
            String serverPrefix = UrlBuilder.getServerPrefix();

            if (serverPrefix != null) {
                sb.append(serverPrefix + "/"); // $NON-NLS$
            }
        }

        if (!s.startsWith("http") && !s.startsWith("/")) { // $NON-NLS$
            sb.append(chartUrlPrefix);
        }
        sb.append(s);

        if (!s.endsWith("?")) {
            sb.append("&");
        }

        // force image reload by appending a parameter that changes frequently
        sb.append("zz=").append(getSuffix()); // $NON-NLS$
        if (authentication != null && !s.contains(authentication)) {
            sb.append(authentication);
        }

        return sb;
    }
}
