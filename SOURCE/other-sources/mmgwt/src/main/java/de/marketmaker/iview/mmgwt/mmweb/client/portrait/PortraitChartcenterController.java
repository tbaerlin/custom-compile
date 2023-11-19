/*
 * PortraitChartcenterController.java
 *
 * Created on Jan 22, 2009 1:13:18 PM
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ChartcenterSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceTeaserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.List;

/**
 * @author Michael Lösch
 */
public class PortraitChartcenterController extends PortraitOverviewController {

    public PortraitChartcenterController(ContentContainer contentContainer, String def) {
        super(contentContainer, def);
    }

    @Override
    public String getPrintHtml() {
        final ChartcenterSnippet chartCenter = (ChartcenterSnippet) this.delegate.getSnippet("cc"); // $NON-NLS-0$
        final PriceTeaserSnippet priceTeaser = (PriceTeaserSnippet) this.delegate.getSnippet("pt"); // $NON-NLS-0$
        final StringBuilder sbHeadHtml = new StringBuilder();

        if(!priceTeaser.getConfiguration().getBoolean("isObjectInfo", false)) { //$NON-NLS$
            sbHeadHtml.append("<table class=\"mm-priceTeaser\">") // $NON-NLS-0$
                    .append(priceTeaser.getView().getElement().getInnerHTML())
                    .append("</table>"); // $NON-NLS-0$
        }

        final List<String> listBenchmarkNames = chartCenter.getBenchmarkNames();
        if (!listBenchmarkNames.isEmpty()) {
            boolean first = true;
            for (String benchmarkName : listBenchmarkNames) {
                if (first) {
                    sbHeadHtml.append("<div class=\"mm-printchartcenter-meta\" style=\"text-align: center; margin-top: 20px;\">") // $NON-NLS$
                            .append(I18n.I.comparisonValues(listBenchmarkNames.size()))
                            .append(": ");
                    first = false;
                }
                else {
                    sbHeadHtml.append(", ");
                }
                sbHeadHtml.append(benchmarkName);
            }
            sbHeadHtml.append("</div>"); // $NON-NLS$
        }

        return new PortraitChartcenterPrintController(chartCenter.getIMG(), chartCenter.getConfiguration(), sbHeadHtml.toString())
                .getPrintHtml();
    }
}