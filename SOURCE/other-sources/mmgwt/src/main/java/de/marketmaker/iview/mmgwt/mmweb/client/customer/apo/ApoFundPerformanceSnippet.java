/*
 * ApoFundPerformanceSnippet.java
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.apo;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import de.marketmaker.iview.dmxml.FNDFinder;
import de.marketmaker.iview.dmxml.FNDFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.GenericListSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.GenericListSnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.JsonListDetailsSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author umaurer
 */
@NonNLS
public class ApoFundPerformanceSnippet extends GenericListSnippet<ApoFundPerformanceSnippet>
        implements JsonListDetailsSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("ApoFundPerformance");
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new ApoFundPerformanceSnippet(context, config);
        }
    }

    private String currentPerformance = "bviperformance1m";

    private ToggleButton selectedPerformanceButton = null;

    protected ApoFundPerformanceSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config, "FND_Finder", "bviperformance1y");

        final VisibilityCheck vc = new VisibilityCheck() {
            public boolean isVisible(TableColumn tc) {
                return currentPerformance.equals(tc.getSortKey());
            }
        };

        SnippetTableView<ApoFundPerformanceSnippet> snippetTableView = new SnippetTableView<ApoFundPerformanceSnippet>(this, new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.name(), -1f, TableCellRenderers.DEFAULT, "name"),
                new TableColumn(I18n.I.redemption(), -1f, TableCellRenderers.PRICE, "priceValue"),
                new TableColumn(I18n.I.currency(), -1f, TableCellRenderers.DEFAULT, "currency"),
                new TableColumn(I18n.I.currentYearAbbr(), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformanceCurrentYear"),
                new TableColumn(I18n.I.nMonths(1), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance1m").withVisibilityCheck(vc),
                new TableColumn(I18n.I.nMonths(3), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance3m").withVisibilityCheck(vc),
                new TableColumn(I18n.I.nMonths(6), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance6m").withVisibilityCheck(vc),
                new TableColumn(I18n.I.nYears(1), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance1y"),
                new TableColumn(I18n.I.nYears(3), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance3y"),
                new TableColumn(I18n.I.nYears(5), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance5y"),
                new TableColumn(I18n.I.nYears(10), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance10y").withVisibilityCheck(vc)
        }));

        final GenericListSnippetView<ApoFundPerformanceSnippet> view = new GenericListSnippetView<>(this, snippetTableView);
        final FloatingToolbar toolbar = view.getOrCreateTopToolbar();
        toolbar.add(createPerformanceButton(I18n.I.nMonths(1), "bviperformance1m"));
        toolbar.add(createPerformanceButton(I18n.I.nMonths(3), "bviperformance3m"));
        toolbar.add(createPerformanceButton(I18n.I.nMonths(6), "bviperformance6m"));
        toolbar.add(createPerformanceButton(I18n.I.nYears(10), "bviperformance10y"));
        
        this.setView(view);
    }

    private Button createPerformanceButton(final String text, final String sortKey) {
        final ToggleButton button = new ToggleButton(text);
        final boolean current = this.currentPerformance.equals(sortKey);
        button.toggle(current);
        if (current) {
            this.selectedPerformanceButton = button;
        }
        button.addListener(Events.Select, new Listener<ButtonEvent>() {
            public void handleEvent(ButtonEvent baseEvent) {
                currentPerformance = sortKey;
                if (selectedPerformanceButton != null) {
                    selectedPerformanceButton.toggle(false);
                }
                selectedPerformanceButton = button;
                button.toggle(true);
                updateView();
            }
        });
        return button;
    }

    @Override
    protected boolean isResponseOk() {
        return true;
    }

    @Override
    protected boolean isMetaDataEnabled() {
        return false;
    }

    @Override
    protected void enableMetaData(boolean enabled) {
        // no metadata
    }

    @Override
    protected void updateTableData() {
        final FNDFinder fndFinder = (FNDFinder) this.block.getResult();
        final List<FNDFinderElement> listElements = fndFinder.getElement();
        final List<Object[]> list = new ArrayList<Object[]>(listElements.size());

        for (FNDFinderElement e : listElements) {
            list.add(new Object[]{
                    new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata()),
                    e.getPrice(),
                    e.getQuotedata().getCurrencyIso(),
                    e.getBviperformanceCurrentYear(),
                    e.getBviperformance1M(),
                    e.getBviperformance3M(),
                    e.getBviperformance6M(),
                    e.getBviperformance1Y(),
                    e.getBviperformance3Y(),
                    e.getBviperformance5Y(),
                    e.getBviperformance10Y()
            });
        }
        final TableDataModel tdm = DefaultTableDataModel.create(list).withSort(fndFinder.getSort());
        this.getView().update(tdm, Integer.parseInt(fndFinder.getOffset()), Integer.parseInt(fndFinder.getCount()), Integer.parseInt(fndFinder.getTotal()));
    }

    @Override
    protected boolean hasSymbol(String qid) {
        if (qid == null) {
            return false;
        }
        final FNDFinder fndFinder = (FNDFinder) this.block.getResult();
        final List<FNDFinderElement> listElements = fndFinder.getElement();
        for (FNDFinderElement e : listElements) {
            if (qid.equals(e.getQuotedata().getQid())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setList(String name, String jsonListKey) {
        getConfiguration().put("titleSuffix", name);
        getView().reloadTitle();
        String finderQuery = SessionData.INSTANCE.getListAsFinderQuery(jsonListKey);
        if (finderQuery == null) {
            getView().update(DefaultTableDataModel.NULL, 0, 0, 0);
            return;
        }
        this.block.setParameter("query", finderQuery);
        ackParametersChanged();
    }

    @Override
    public void setInitialQuery(String name, String query) {
        setQueryParameters(name, query);
    }
}
