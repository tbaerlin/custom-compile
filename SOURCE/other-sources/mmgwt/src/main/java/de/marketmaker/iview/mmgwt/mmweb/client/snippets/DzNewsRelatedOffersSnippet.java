/*
* DzNewsRelatedOffersSnippet.java
*
* Created on 10.04.2013 12:12:00
*
* Copyright (c) vwd AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.GISFinder;
import de.marketmaker.iview.dmxml.GISFinderElement;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.InstrumentDataListChangedSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Markus Dick
 */
public class DzNewsRelatedOffersSnippet extends
        AbstractSnippet<DzNewsRelatedOffersSnippet, SnippetTableView<DzNewsRelatedOffersSnippet>>
        implements LinkListener<QuoteWithInstrument>, SymbolListSnippet, VisibilityUpdatedHandler<Widget> {
    private final TableCellRenderers.LocalLinkRenderer localLinkRenderer;
    private final InstrumentDataListChangedSupport instrumentsChangedSupport = new InstrumentDataListChangedSupport();

    public static class Class extends SnippetClass {
        public Class() {
            super("DzNewsRelatedOffers"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new DzNewsRelatedOffersSnippet(context, config);
        }
    }

    private DmxmlContext.Block<GISFinder> block;

    private List<SymbolSnippet> symbolSnippets = new ArrayList<>();
    private boolean visible = true;

    private DzNewsRelatedOffersSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        final boolean displayDzBankLink = Permutation.GIS.isActive() && Selector.PRODUCT_WITH_PIB.isAllowed();
        final VisibilityCheck dzBankLink = SimpleVisibilityCheck.valueOf(displayDzBankLink);

        this.block = createBlock("GIS_Finder"); // $NON-NLS$

        this.localLinkRenderer = new TableCellRenderers.LocalLinkRenderer(this, "mm-bestTool-link", "mm-bestTool-link selected", "", null); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        setView(new SnippetTableView<>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn("", 0.05f, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_ELEM).withVisibilityCheck(dzBankLink),
                        new TableColumn(I18n.I.name(), 120f, SessionData.INSTANCE.isAnonymous() ?
                                TableCellRenderers.STRING : TableCellRenderers.OPTIONAL_QUOTELINK_18),
                        new TableColumn("ISIN", 100f, TableCellRenderers.STRING_CENTER), // $NON-NLS$
                        new TableColumn(I18n.I.marketName(), 60f, TableCellRenderers.STRING_CENTER),
                        new TableColumn(I18n.I.gisRendite(), 45f, TableCellRenderers.PERCENT),
                        new TableColumn(I18n.I.gisExpiration(), 45f, TableCellRenderers.DATE),
                        new TableColumn(I18n.I.price(), 45f, TableCellRenderers.PRICE),
                        new TableColumn("", 15f, TableCellRenderers.STRING_10),
                        new TableColumn("+/-", 45f, TableCellRenderers.PRICE23), // $NON-NLS$
                        new TableColumn("+/-%", 45f, TableCellRenderers.CHANGE_PERCENT), // $NON-NLS$
                        new TableColumn(I18n.I.time(), 70f, TableCellRenderers.COMPACT_DATETIME),
                        new TableColumn("", 10f, this.localLinkRenderer) // $NON-NLS$
                })));
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            setVisible(false);
            return;
        }

        final GISFinder result = this.block.getResult();
        setVisible(!result.getElement().isEmpty());

        final DefaultTableDataModel tdm = DefaultTableDataModel.create(result.getElement(),
                new AbstractRowMapper<GISFinderElement>() {
                    public Object[] mapRow(final GISFinderElement e) {
                        final QuoteWithInstrument qwi = QuoteWithInstrument
                                .createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata(), e.getBezeichnung());
                        final Price p = Price.create(e);

                        return new Object[]{
                                e,
                                SessionData.INSTANCE.isAnonymous() ? qwi.getName() : qwi,
                                e.getInstrumentdata().getIsin(),
                                e.getQuotedata().getMarketName(),
                                e.getRendite(),
                                e.getExpiration(),
                                p.getLastPrice().getPrice(),
                                e.getQuotedata().getCurrencyIso(),
                                p.getChangeNet(),
                                p.getChangePercent(),
                                p.getDate(),
                                qwi
                        };
                    }
                });

        getView().update(tdm);
    }

    public void setSymbols(List<InstrumentData> symbols) {
        //this is only used as a trigger to indicate that a new offer should be selected
        // from the dependent symbols. The content of the symbol list is of no interest...
        if(symbols == null || symbols.isEmpty()) {
            this.instrumentsChangedSupport.reset();
            setVisible(false);
            setDependentSymbol(null, null, null);
            return;
        }

        if(this.block.isResponseOk() && this.instrumentsChangedSupport.hasChanged(symbols)) {
            if(this.block.getResult().getElement().isEmpty()) {
                setDependentSymbol(null, null, null);
                return;
            }

            InstrumentData instrument = this.block.getResult().getElement().get(0).getInstrumentdata();
            setDependentSymbol(InstrumentTypeEnum.valueOf(instrument.getType()), instrument.getIid(), instrument.getName());
        }
    }

    public void onClick(LinkContext<QuoteWithInstrument> context, Element e) {
        final QuoteWithInstrument qwi = context.data;
        final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(qwi.getInstrumentData().getType());
        final String qid = qwi.getQuoteData().getQid();
        final String name = qwi.getInstrumentData().getName();
        setDependentSymbol(type, qid, name);
        for(SymbolSnippet s : this.symbolSnippets) {
            if(s instanceof Snippet) {
                this.contextController.updateVisibility(((Snippet) s).getView(), true);
            }
        }
    }

    private void setDependentSymbol(InstrumentTypeEnum type, String symbol, String name) {
        for (SymbolSnippet s : this.symbolSnippets) {
            s.setSymbol(type, symbol, name);
        }
        this.localLinkRenderer.setSelectedSymbol(symbol);
        this.getView().reloadTitle();

        if(!(type == null && symbol == null && name == null)) { //avoid endless loops due to context reloads
            ackParametersChanged();
        }
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if ((getView() != null) && (getView().container != null)) {
            getView().container.setVisible(visible);
        }
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void addSymbolSnippet(SymbolSnippet s) {
        this.symbolSnippets.add(s);
    }

    @Override
    public void onControllerInitialized() {
        final String dependsOn = getConfiguration().getString("dependsOn"); //$NON-NLS$
        if(!StringUtil.hasText(dependsOn)) {
            throw new IllegalArgumentException("DependsOn must be defined in guidefs!"); //$NON-NLS$
        }

        final NewsEntrySnippet s = (NewsEntrySnippet)this.contextController.getSnippet(dependsOn);
        this.block.setDependsOn(s.getNWSNewsBlock());
    }

    @Override
    public void onVisibilityUpdated(VisibilityUpdatedEvent event) {
        for(SymbolSnippet s : this.symbolSnippets) {
            if(((Snippet) s).getView().container == event.getTarget()) {
                if(!event.isVisible()) {
                    Firebug.log("<DzNewsRelatedOffersSnippet.onVisibilityUpdated> setting renderer to empty string and updating view");
                    this.localLinkRenderer.setSelectedSymbol("");
                    updateView();
                    break;
                }
            }
        }
    }
}
