/*
 * PmReportSnippetView.java
 *
 * Created on 05.04.2011
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.AnalysisDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasIsPrintable;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;

/**
 * @author Ulrich Maurer
 * @author Markus Dick
 */
public class PmReportSnippetView extends SnippetView<PmReportSnippet> implements HasIsPrintable {
    private final AnalysisDisplay view;

    protected PmReportSnippetView(PmReportSnippet snippet) {
        super(snippet);
        this.view = snippet.getAnalysisController().getView();
    }

    @Override
    public void reloadTitle() {
        // do nothing
    }

    @Override
    protected void onContainerAvailable() {
        this.container.setContentWidget(this.view.asWidget());
    }

    public int getChartHeight() {
        return this.container != null
                ? this.container.asWidget().getOffsetHeight()
                : -1;
    }

    public int getChartWidth() {
        return this.container != null
                ? this.container.asWidget().getOffsetWidth()
                : -1;
    }

    @Override
    public boolean isPrintable() {
        return false;
    }

    @Override
    public String getPrintHtml() {
        return I18n.I.notPrintable();
    }
}