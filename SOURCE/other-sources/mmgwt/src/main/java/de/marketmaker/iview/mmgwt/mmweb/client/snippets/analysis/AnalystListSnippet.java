/*
 * AnalystListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.analysis;

import com.google.gwt.dom.client.Element;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.RSCFinderMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AnalystListSnippet extends AbstractSnippet<AnalystListSnippet, SnippetTableView<AnalystListSnippet>>
        implements LinkListener<Link> {
    public static class Class extends SnippetClass {

        public Class() {
            super("AnalystList", I18n.I.analysts()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new AnalystListSnippet(context, config);
        }
    }

    private final DmxmlContext.Block<RSCFinderMetadata> block;
    private String listid = null;
    private AnalysisListSnippet analysisListSnippet;

    private AnalystListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        final DefaultTableColumnModel columnModel = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.value(), 1f, TableCellRenderers.DEFAULT) 
        }, false);
        this.setView(new SnippetTableView<>(this, columnModel));

        this.listid = config.getString("list", null); // $NON-NLS$

        this.block = createBlock("RSC_FinderMetadata"); // $NON-NLS$
    }

    public AnalysisListSnippet getAnalysisListSnippet() {
        if (this.listid != null) {
            this.analysisListSnippet = (AnalysisListSnippet) this.contextController.getSnippet(this.listid);
            this.listid = null;
        }
        return this.analysisListSnippet;
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (this.block.isResponseOk()) {
            final RSCFinderMetadata metadata = this.block.getResult();
            final TableDataModelBuilder builder =
                    new TableDataModelBuilder(1 + metadata.getAnalyst().getElement().size(), 1);
            final Link all = new Link(this, I18n.I.all(), null); 
            builder.addRow(all);
            for (final FinderMetaList.Element e : metadata.getAnalyst().getElement()) {
                final Link link = new Link(this, e.getName(), null).withData(e.getKey());
                builder.addRow(link);
            }
            getView().update(builder.getResult());
        }
        else {
            final ErrorType error = this.block.getError();
            if (error != null && "data.missing".equals(error.getCode())) { // $NON-NLS$
                getView().setMessage(I18n.I.noDataAvailable(), false);
            }
            else {
                getView().setMessage(I18n.I.internalError(), false);
            }
        }
    }

    public void onClick(LinkContext<Link> context, Element e) {
        final String analystKey = (String) context.data.getData();
        final String analystName = context.data.getText();
        requestAnalysis(analystKey, analystName);
    }

    private void requestAnalysis(String analystKey, String analystName) {
        final AnalysisListSnippet listSnippet = getAnalysisListSnippet();
        if (listSnippet != null) {
            listSnippet.setAnalyst(analystKey, analystName);
            ackParametersChanged();
        }
    }
}
