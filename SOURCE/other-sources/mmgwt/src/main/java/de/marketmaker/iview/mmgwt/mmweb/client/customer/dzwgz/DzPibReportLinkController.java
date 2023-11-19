/*
 * DzPibReportLinkController.java
 *
 * Created on 28.09.2012 10:52:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz;

import com.google.gwt.core.client.JavaScriptObject;

import de.marketmaker.iview.dmxml.GISReports;
import de.marketmaker.iview.dmxml.GISStaticData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ReportType;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContentFlagsEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.DzPibMarginDialog;

import java.util.List;

/**
 * @author Markus Dick
 * @author Michael Wohlfart
 */
public final class DzPibReportLinkController {
    public static final DzPibReportLinkController INSTANCE = new DzPibReportLinkController();

    protected DmxmlContext context = new DmxmlContext();

    private final DmxmlContext.Block<GISReports> blockGisReports;

    private final DmxmlContext.Block<GISStaticData> blockGisStaticData;

    private DzPibReportLinkController() {
        blockGisReports = context.addBlock("GIS_Reports"); //$NON-NLS$
        blockGisStaticData = context.addBlock("GIS_StaticData"); //$NON-NLS$
    }

    // if possible use the QuoteData version of this method to save some electrons
    public void openDialogOrReport(String symbol) {
        if (symbol == null) {
            return;
        }

        blockGisStaticData.setParameter("symbol", symbol); //$NON-NLS$
        blockGisStaticData.isToBeRequested();

        context.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (blockGisStaticData.isResponseOk()) {
                    final GISStaticData staticData = blockGisStaticData.getResult();
                    final QuoteData quoteData = staticData.getQuotedata();
                    openDialogOrReport(quoteData);
                }
            }
        });
    }

    public void openDialogOrReport(final QuoteData quoteData) {
        if ((quoteData == null) || quoteData.getQid() == null) {
            return;
        }
        if (ContentFlagsEnum.DzMarginDialogRequired.isAvailableFor(quoteData)) {
            DzPibMarginDialog.INSTANCE.showDialog(quoteData.getVwdcode());
            return;
        }

        // Need to open window before additional events are thrown/processed to make links work on
        // iOS devices. See http://jira.vwdkl.net/browse/ISTAR-592 for details
        final JavaScriptObject pibWindow = openWindow("", "_blank", ""); //$NON-NLS$

        blockGisReports.setParameter("symbol", quoteData.getQid()); //$NON-NLS$
        blockGisReports.setParameter("filterStrategy", "DZBANK-PIB"); //$NON-NLS$
        blockGisReports.isToBeRequested();

        context.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (blockGisReports.isResponseOk()) {
                    handleResponse(blockGisReports, quoteData, pibWindow);
                }
            }
        });
    }

    private void handleResponse(DmxmlContext.Block<GISReports> blockGisReports, QuoteData quoteData,
            JavaScriptObject pibWindow) {
        final GISReports reports = blockGisReports.getResult();
        final List<ReportType> reportTypes = reports.getReport();
        if (reportTypes == null || reportTypes.isEmpty()) {
            return;
        }
        final String url = reportTypes.get(0).getUrl();
        replaceWindowUrl(pibWindow, DzPibMarginDialog.DYN_PIB_BASE_URL + url);
    }

    private native JavaScriptObject openWindow(String url, String name, String features) /*-{
        var pibWindow = $wnd.open(url, name, features);
        return pibWindow;
    }-*/;

    private native void replaceWindowUrl(JavaScriptObject pibWindow, String url) /*-{
        pibWindow.location.replace(url);
    }-*/;

}
