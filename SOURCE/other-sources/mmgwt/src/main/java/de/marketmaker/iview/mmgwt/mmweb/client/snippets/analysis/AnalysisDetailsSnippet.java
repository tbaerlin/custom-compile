/*
 * AnalysisDetailsSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.analysis;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.iview.dmxml.RSCAnalysis;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AnalysisDetailsSnippet extends AbstractSnippet<AnalysisDetailsSnippet, AnalysisDetailsSnippetView> {
    public static class Class extends SnippetClass {

        public Class() {
            super("AnalysisDetails", I18n.I.analysis()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new AnalysisDetailsSnippet(context, config);
        }
    }

    private final DmxmlContext context;
    private final DmxmlContext.Block<RSCAnalysis> block;

    private AnalysisDetailsSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.setView(new AnalysisDetailsSnippetView(this));

        this.context = new DmxmlContext();
        this.block = this.context.addBlock("RSC_Analysis"); // $NON-NLS-0$
    }

    public void requestAnalysis(String analysisid) {
        this.block.setParameter("analysisid", analysisid); // $NON-NLS-0$
        if (analysisid == null) {
            this.block.disable();
            getView().update(null);
        }
        else {
            this.block.enable();
            this.context.issueRequest(new AsyncCallback<ResponseType>() {
                public void onFailure(Throwable caught) {
                    updateView();
                }

                public void onSuccess(ResponseType result) {
                    updateView();
                }
            });
        }
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        getView().update(this.block.getResult());
    }
}
