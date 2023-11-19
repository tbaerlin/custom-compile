/*
 * AnalysisListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.analysis;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.RSCFinder;
import de.marketmaker.iview.dmxml.RSCFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PagingPanel;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AnalysisListSnippet extends AbstractSnippet<AnalysisListSnippet, AnalysisListSnippetView>
        implements SymbolSnippet, LinkListener<RSCFinderElement>, PagingPanel.Handler {
    public static class Class extends SnippetClass {

        public Class() {
            super("AnalysisList", I18n.I.analyses()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new AnalysisListSnippet(context, config);
        }
    }

    static final int DEFAULT_COUNT = 15;

    private final DmxmlContext.Block<RSCFinder> block;
    private String detailsid = null;
    private AnalysisDetailsSnippet analysisDetailsSnippet;

    private AnalysisListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new AnalysisListSnippetView(this, config));
        this.detailsid = config.getString("details", null); // $NON-NLS-0$

        this.block = createBlock("RSC_Finder"); // $NON-NLS-0$
        this.block.setParameter("sortBy", "datum"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("ascending", "false"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("count", config.getString("count", String.valueOf(DEFAULT_COUNT))); // $NON-NLS-0$ $NON-NLS-1$
    }

    public AnalysisDetailsSnippet getAnalysisDetailsSnippet() {
        if (this.detailsid != null) {
            this.analysisDetailsSnippet = (AnalysisDetailsSnippet) this.contextController.getSnippet(this.detailsid);
            this.detailsid = null;
        }
        return this.analysisDetailsSnippet;
    }

    public void setAnalysisDetailsSnippet(AnalysisDetailsSnippet analysisDetailsSnippet) {
        this.analysisDetailsSnippet = analysisDetailsSnippet;
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("query", symbol == null ? null : "symbol==" + symbol); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void setAnalyst(String key, String name) {
        this.block.setParameter("query", key == null ? null : "analyst==" + key); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("offset", "0"); // $NON-NLS-0$ $NON-NLS-1$
        getConfiguration().put("titleSuffix", name); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (this.block.isResponseOk()) {
            final RSCFinder finder = this.block.getResult();
            getView().update(finder);
            final List<RSCFinderElement> listElements = finder.getElement();
            if (listElements.isEmpty()) {
                requestAnalysis(null);
            }
            else {
                final RSCFinderElement e = listElements.get(0);
                requestAnalysis(e.getAnalysisid());
            }
        }
        else {
            getView().update(null);
            requestAnalysis(null);
        }
    }

    public void onClick(LinkContext<RSCFinderElement> context, Element e) {
        final String analysisid = context.data.getAnalysisid();
        requestAnalysis(analysisid);
    }

    private void requestAnalysis(String analysisid) {
        final AnalysisDetailsSnippet detailsSnippet = getAnalysisDetailsSnippet();
        if (detailsSnippet != null) {
            detailsSnippet.requestAnalysis(analysisid);
        }
    }

    public void ackNewOffset(int offset) {
        this.block.setParameter("offset", offset); // $NON-NLS-0$
        ackParametersChanged();
    }
}
