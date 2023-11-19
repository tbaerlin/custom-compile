/*
 * PortraitChartcenterPrintController.java
 *
 * Created on Jan 28, 2009 12:44:05 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ChartcenterSnippetPrintView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;

/**
 * @author Michael Lösch
 */
public class PortraitChartcenterPrintController {
    private final String headHtml;
    private final IMGResult img;
    private final SnippetConfiguration conf;
    private final ChartcenterSnippetPrintView cspv;

    public PortraitChartcenterPrintController(IMGResult img, SnippetConfiguration config, String headHtml) {
        this.cspv = new ChartcenterSnippetPrintView();
        this.img = img;
        this.conf = config;
        this.headHtml = headHtml;
    }

    public String getPrintHtml() {
        return this.headHtml + this.cspv.getPrintHtml(this.img, this.conf);
    }
}
