package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;

import java.util.Date;
import java.util.Map;

/**
 * Created on 15.11.2010 14:13:36
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class ExportUtil {


    public static void exportChartcenterPng(Map<String, String> params) {
        ActionPerformedEvent.fire("X_CC_PNG"); // $NON-NLS-0$
        final String url = UrlBuilder.byName("chartcenterexport.png").addAll(params).toURL(); // $NON-NLS-0$
        Window.open(url, "_blank", "");  // $NON-NLS$
    }

    public static void exportCsvTimeseries(Map<String, String> params) {
        ActionPerformedEvent.fire("X_TS_CSV"); // $NON-NLS-0$
        Window.open(buildTimeseriesUrl("timeseries.csv", params.get("symbol"), params.get("period"), params.get("from"), params.get("to")), "_blank", ""); // $NON-NLS$
    }

    public static String buildTimeseriesUrl(String fileName, String symbol, String period, String from, String to) {
        Date fromdate = null;
        Date todate = null;

        if (period == null && (from != null && to != null)) {
            fromdate = DateTimeUtil.DATE_FORMAT_DMY.parse(from);
            if ("today".equals(to)) { // $NON-NLS$   
                to = DateTimeUtil.DATE_FORMAT_DMY.format(new Date()); 
            }
            todate = DateTimeUtil.DATE_FORMAT_DMY.parse(to);
        }
        else if (period != null) {
            fromdate = DateTimeUtil.nowMinus(period);
            todate = new Date();
        }

        final UrlBuilder builder = UrlBuilder.byName(fileName).add("symbol", symbol); // $NON-NLS$
        if (fromdate != null && todate != null) {
            final DateTimeFormat format = DateTimeFormat.getFormat("yyyy-MM-dd"); // $NON-NLS$
            builder.add("from", format.format(fromdate)); // $NON-NLS-0$)
            builder.add("to", format.format(todate)); // $NON-NLS-0$))
        }
        Firebug.log("url: " + builder.toURL()); // $NON-NLS$
        return builder.toURL();
    }


}
