/*
 * AnalysisListSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.analysis;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.dmxml.RSCFinder;
import de.marketmaker.iview.dmxml.RSCFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.*;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PagingPanel;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AnalysisListSnippetView extends SnippetView<AnalysisListSnippet> {
    private PagingPanel pp;

    private SnippetTableWidget tw;

    private final TableColumnModel columnModel;

    private final boolean withAnalyst;

    public AnalysisListSnippetView(final AnalysisListSnippet snippet,
            final SnippetConfiguration config) {
        super(snippet);
        reloadTitle();
        this.withAnalyst = config.getBoolean("withAnalyst", true); // $NON-NLS-0$

        final TableCellRenderer analysisLinkRenderer = new TableCellRenderers.MaxLengthStringRenderer(40, "--") { // $NON-NLS-0$
            public void render(Object data, StringBuffer sb, Context context) {
                LinkContext lc = (LinkContext) data;
                RSCFinderElement e = (RSCFinderElement) lc.data;
                final String display = getMaxLengthText(e.getHeader());
                context.appendLink(lc, display, display != e.getHeader() ? e.getHeader() : null, sb);
            }
        };

        if (this.withAnalyst) {
            this.columnModel = new DefaultTableColumnModel(new TableColumn[]{
                    new TableColumn(I18n.I.date(), -1f, DATE, "date").alignRight()  // $NON-NLS-0$
                    , new TableColumn(I18n.I.analysis(), -1f, analysisLinkRenderer) 
                    , new TableColumn(I18n.I.value(), 220, QUOTELINK_32)
                    , new TableColumn(I18n.I.recommendation(), -1f, RSC_RECOMMENDATION) 
                    , new TableColumn(I18n.I.analystsCompany(), -1f, STRING_RIGHT, "analyst")  // $NON-NLS-0$
            });
        }
        else {
            this.columnModel = new DefaultTableColumnModel(new TableColumn[]{
                    new TableColumn(I18n.I.date(), -1f, DATE, "date").alignRight()  // $NON-NLS-0$
                    , new TableColumn(I18n.I.analysis(), -1f, analysisLinkRenderer) 
                    , new TableColumn(I18n.I.value(), 220, QUOTELINK_32)
                    , new TableColumn(I18n.I.recommendation(), -1f, RSC_RECOMMENDATION) 
            });
        }
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        int pageSize = getConfiguration().getInt("count", AnalysisListSnippet.DEFAULT_COUNT); // $NON-NLS-0$
        this.pp = new PagingPanel(new PagingPanel.Config(this.container)
                .withPageSize(pageSize));
        this.pp.setHandler(this.snippet);
    }

    void update(RSCFinder rscFinder) {
        reloadTitle();
        if (rscFinder == null) {
            this.pp.update(0, 0, 0);
            if (this.tw != null) {
                this.tw.updateData(DefaultTableDataModel.NULL);
            }
        }
        else {
            if (this.tw == null) {
                this.tw = SnippetTableWidget.create(this.columnModel);
                this.container.setContentWidget(this.tw);
                this.container.layout();
            }

            final List<RSCFinderElement> listElements = rscFinder.getElement();
            final List<Object[]> list = new ArrayList<>(listElements.size());
            for (RSCFinderElement e : listElements) {
                if (this.withAnalyst) {
                    list.add(new Object[]{e.getDate(),
                            new LinkContext<>(this.snippet, e),
                            createQuoteWithInstrument(e),
                            e.getRecommendation(),
                            e.getAnalyst()
                    });
                }
                else {
                    list.add(new Object[]{e.getDate(),
                            new LinkContext<>(this.snippet, e),
                            createQuoteWithInstrument(e),
                            e.getRecommendation()
                    });
                }
            }
            this.tw.updateData(DefaultTableDataModel.create(list));
            this.pp.update(Integer.parseInt(rscFinder.getOffset()), Integer.parseInt(rscFinder.getCount()), Integer.parseInt(rscFinder.getTotal()));
        }
    }

    /**
     * @see QuoteWithInstrument#createQuoteWithInstrument(de.marketmaker.iview.dmxml.InstrumentData, de.marketmaker.iview.dmxml.QuoteData, String)
     * @see de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.QuoteLinkRenderer#render(Object, StringBuffer, de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer.Context)
     */
    private QuoteWithInstrument createQuoteWithInstrument(RSCFinderElement e) {
        return QuoteWithInstrument.createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata(), e.getCompanyName());
    }
}
