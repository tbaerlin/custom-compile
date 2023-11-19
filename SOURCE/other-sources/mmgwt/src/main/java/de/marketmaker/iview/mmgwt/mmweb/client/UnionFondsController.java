package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.List;

import com.google.gwt.user.client.Command;
import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.marketmaker.iview.dmxml.FNDFinder;
import de.marketmaker.iview.dmxml.FNDFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFNDView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * UnionFondsController.java
 * Created on Feb 3, 2009 10:44:24 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class UnionFondsController extends AbstractPageController {
    protected final DmxmlContext.Block<FNDFinder> block;

    private ContentPanel panel;
    private SnippetTableWidget table;

    public UnionFondsController() {
        this.block = context.addBlock("FND_Finder"); // $NON-NLS-0$
        this.block.setParameter("query", "vrIssuer=='true'"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("count", "500"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("sortBy", "name"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("ascending", "true"); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        refresh();
    }

    @Override
    protected void onResult() {
        if (this.panel == null) {
            initView();
        }
        getContentContainer().setContent(this.panel);
        this.table.updateData(createDataModel());
    }

    private void initView() {
        this.panel = new ContentPanel();
        this.panel.setBorders(false);
        this.panel.setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
        this.panel.setStyleName("mm-contentData"); // $NON-NLS-0$

        this.table = new SnippetTableWidget(FinderFNDView.BASE_COLUMN_MODEL);
        this.table.setWidth("100%"); // $NON-NLS-0$
        this.table.setSortLinkListener(new SortLinkSupport(this.block, new Command() {
            public void execute() {
                refresh();
            }
        }));
        this.panel.add(this.table);
    }

    @Override
    public String getPrintHtml() {
        return this.table.getElement().getInnerHTML();
    }

    private TableDataModel createDataModel() {
        List<FNDFinderElement> elements = this.block.getResult().getElement();
        if (elements.isEmpty()) {
            return DefaultTableDataModel.NULL;
        }
        return DefaultTableDataModel.create(elements, new AbstractRowMapper<FNDFinderElement>() {
            public Object[] mapRow(FNDFinderElement e) {
                return new Object[]{
                        e.getInstrumentdata().getWkn(),
                        e.getInstrumentdata().getIsin(),
                        new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata()),
                        e.getFundtype(),
                        e.getWmInvestmentAssetPoolClass(),
                        e.getIssuePrice(),
                        e.getRepurchasingPrice(),
                        e.getDate(),
                        e.getQuotedata().getMarketName(),
                        e.getChangeNet(),
                        e.getChangePercent()
                };
            }
        }).withSort(this.block.getResult().getSort());
    }
}
