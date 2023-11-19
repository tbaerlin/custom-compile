/*
 * AnalysisDetailsSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.analysis;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.HTML;

import de.marketmaker.iview.dmxml.RSCAnalysis;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AnalysisDetailsSnippetView extends SnippetView<AnalysisDetailsSnippet> {
    private final Panel panel;
    private final DateRenderer dateRenderer = DateRenderer.compactDateTime("--"); // $NON-NLS-0$

    public AnalysisDetailsSnippetView(final AnalysisDetailsSnippet snippet) {
        super(snippet);
        reloadTitle();

        this.panel = new FlowPanel();
        this.panel.setStyleName("mm-analysis"); // $NON-NLS-0$
    }

    @Override
    protected void onContainerAvailable() {
        this.container.setContentWidget(this.panel);
    }

    void update(RSCAnalysis analysis) {
        this.panel.clear();
        if (analysis == null) {
            getConfiguration().put("title", I18n.I.noAnalysisSelected());  // $NON-NLS-0$
            getConfiguration().put("titleSuffix", null); // $NON-NLS-0$
        }
        else {
            getConfiguration().put("title", this.dateRenderer.render(analysis.getDate())); // $NON-NLS-0$
            getConfiguration().put("titleSuffix", analysis.getHeader()); // $NON-NLS-0$
            this.panel.add(new HTML(Renderer.RSC_RECOMMENDATION.render(analysis.getRecommendation())));
            this.panel.add(new Label(analysis.getText()));
        }
        reloadTitle();
    }
}
