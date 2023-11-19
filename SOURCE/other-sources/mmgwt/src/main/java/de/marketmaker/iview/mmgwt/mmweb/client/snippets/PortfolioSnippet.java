/*
 * PriceListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.dmxml.PFEvaluation;
import de.marketmaker.iview.dmxml.PortfolioElement;
import de.marketmaker.iview.dmxml.PortfolioPositionElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CurrentTrendBar;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortfolioSnippet extends AbstractSnippet<PortfolioSnippet, SnippetTableView<PortfolioSnippet>> implements PushRegisterHandler {
    public static class Class extends SnippetClass {
        public Class() {
            super("Portfolio", SessionData.isAsDesign() ? I18n.I.portfolioSample() : I18n.I.portfolio()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PortfolioSnippet(context, config);
        }
    }

    private DmxmlContext.Block<PFEvaluation> block;
    private final PriceSupport priceSupport = new PriceSupport(this);
    private DefaultTableDataModel dtm;


    private PortfolioSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock("PF_Evaluation"); // $NON-NLS-0$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("sortBy", "name"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("userid", SessionData.INSTANCE.getUser().getVwdId()); // $NON-NLS-0$
        onParametersChanged();

        this.setView(new SnippetTableView<PortfolioSnippet>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.name(), 0.4f, TableCellRenderers.QUOTELINK_22), 
                        new TableColumn(I18n.I.price(), 0.2f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH), 
                        new TableColumn(I18n.I.trend(), 0.2f, TableCellRenderers.TRENDBAR).withCellClass("mm-middle"),  // $NON-NLS-0$
                        new TableColumn("+/-%", 0.2f, TableCellRenderers.CHANGE_PERCENT_PUSH) // $NON-NLS-0$
                })
        ));
    }


    public void destroy() {
        this.priceSupport.deactivate();
        destroyBlock(this.block);
    }

    public boolean isConfigurable() {
        return true;
    }

    public void configure(Widget triggerWidget) {
        final Menu menu = new Menu();
        for (final PortfolioElement pe : SessionData.INSTANCE.getPortfolios()) {
            final MenuItem menuItem = new MenuItem(pe.getName());
            menuItem.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    if (!menu.isNoSelectionItem(menuItem)) {
                        getConfiguration().put("portfolioid", pe.getPortfolioid()); // $NON-NLS$
                        getConfiguration().put("titleSuffix", pe.getName()); // $NON-NLS$
                        ackParametersChanged();
                    }
                }
            });
            menu.add(menuItem);
        }
        menu.show(triggerWidget);
    }

    protected void onParametersChanged() {
        this.block.setParameter("portfolioid", getConfiguration().getString("portfolioid", null)); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void updateView() {
        this.priceSupport.invalidateRenderItems();

        if (!this.block.blockChanged()) {
            return;
        }
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final PFEvaluation data = this.block.getResult();
        final List<PortfolioPositionElement> list = data.getElements().getPosition();
        if (list.isEmpty()) {
            getView().setMessage(I18n.I.portfolioHasNoValues(), true); 
            return;
        }
        this.dtm = getTableDataModel(list);
        getView().update(this.dtm);
        this.priceSupport.activate();
    }

    private DefaultTableDataModel getTableDataModel(List<PortfolioPositionElement> elements) {
        final TrendBarData tbd = TrendBarData.createGuV(this.block.getResult());

        final List<Object[]> list = new ArrayList<Object[]>(elements.size());
        for (PortfolioPositionElement e : elements) {
            Price p = Price.create(e);
            final String change = e.getPositionChangePercentInPortfolioCurrency();
            list.add(new Object[]{
                    new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata()),
                    p,
                    new CurrentTrendBar(change, tbd),
                    p
            });
        }
        return DefaultTableDataModel.create(list);
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            if (numAdded == this.block.getResult().getElements().getPosition().size()) {
                event.addComponentToReload(this.block, this);
            }
            if (numAdded > 0) {
                return getView().getRenderItems(this.dtm);
            }
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.block.isResponseOk() && !this.priceSupport.isLatestPriceGeneration() && !event.isPushedUpdate()) {
            updateView();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }

}
