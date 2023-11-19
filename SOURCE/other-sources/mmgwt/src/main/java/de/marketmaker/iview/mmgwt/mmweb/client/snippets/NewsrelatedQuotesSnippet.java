/*
* NewsrelatedQuotesSnippet.java
*
* Created on 02.09.2008 14:22:13
*
* Copyright (c) vwd AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Lösch
 */
public class NewsrelatedQuotesSnippet extends
        AbstractSnippet<NewsrelatedQuotesSnippet, SnippetTableView<NewsrelatedQuotesSnippet>>
        implements LinkListener<QuoteWithInstrument>, SymbolListSnippet, VisibilityUpdatedHandler<Widget> {
    private final TableCellRenderers.LocalLinkRenderer localLinkRenderer;
    private final InstrumentDataListChangedSupport instrumentsChangedSupport = new InstrumentDataListChangedSupport();

    public static class Class extends SnippetClass {
        public Class() {
            super("NewsrelatedQuotes"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new NewsrelatedQuotesSnippet(context, config);
        }
    }

    private DmxmlContext.Block<MSCListDetails> block;

    private List<SymbolSnippet> symbolSnippets = new ArrayList<>();
    private boolean visible = true;
    private boolean updateVisibilityBecauseOfNewSymbols = false;

    private NewsrelatedQuotesSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        final boolean isGis = Permutation.GIS.isActive();
        final VisibilityCheck dzBankCheck = SimpleVisibilityCheck.valueOf(isGis);
        final VisibilityCheck notDzBankCheck = SimpleVisibilityCheck.valueOf(!isGis);

        this.block = createBlock("MSC_PriceDataMulti"); // $NON-NLS-0$
        this.block.setParameter("symbolStrategy", "auto"); // $NON-NLS-0$ $NON-NLS-1$

        this.localLinkRenderer = new TableCellRenderers.LocalLinkRenderer(this, "mm-bestTool-link", "mm-bestTool-link selected", "", null); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        setView(new SnippetTableView<>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn("", 0.05f, TableCellRenderers.VR_ICON_LINK).withVisibilityCheck(dzBankCheck),
                        new TableColumn(I18n.I.name(), 120f, SessionData.INSTANCE.isAnonymous() ? 
                                TableCellRenderers.STRING : TableCellRenderers.QUOTELINK_18),
                        new TableColumn("ISIN", 100f, TableCellRenderers.STRING_CENTER), // $NON-NLS-0$
                        new TableColumn(I18n.I.marketName(), 60f, TableCellRenderers.STRING_CENTER),
                        new TableColumn(I18n.I.price(), 45f, TableCellRenderers.PRICE),
                        new TableColumn("", 15f, TableCellRenderers.STRING_10), // $NON-NLS-0$
                        new TableColumn("+/-", 45f, TableCellRenderers.PRICE23).withVisibilityCheck(dzBankCheck), // $NON-NLS$
                        new TableColumn("+/-%", 45f, TableCellRenderers.CHANGE_PERCENT), // $NON-NLS-0$
                        new TableColumn(I18n.I.volume(), 45f, TableCellRenderers.VOLUME_LONG).withVisibilityCheck(notDzBankCheck),
                        new TableColumn(I18n.I.time(), 70f, TableCellRenderers.COMPACT_DATETIME),
                        new TableColumn("", 10f, this.localLinkRenderer) // $NON-NLS-0$
                })));
    }

    public void addSymbolSnippet(SymbolSnippet s) {
        this.symbolSnippets.add(s);
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }
        final MSCListDetails result = this.block.getResult();

        final DefaultTableDataModel tdm = DefaultTableDataModel.create(result.getElement(),
                new AbstractRowMapper<MSCListDetailElement>() {
                    public Object[] mapRow(MSCListDetailElement e) {
                        final QuoteWithInstrument qwi = new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata());
                        final Price p = Price.create(e);
                        return new Object[]{
                                qwi,
                                SessionData.INSTANCE.isAnonymous() ? qwi.getName() : qwi,
                                e.getInstrumentdata().getIsin(),
                                e.getQuotedata().getMarketName(),
                                p.getLastPrice().getPrice(),
                                e.getQuotedata().getCurrencyIso(),
                                p.getChangeNet(),
                                p.getChangePercent(),
                                p.getVolume(),
                                p.getDate(),
                                qwi
                        };
                    }
                });

        getView().update(tdm);

        //updating visibility is necessary to bring the PortraitChart snippet "to the front"
        //in a scenario where a cell of the FlexSnippetView has more than one snippet,
        //especially if the second snippet can be invisible due to unavailable data, e.g.
        //"DZ-Bank Offerten" with "GisStaticData".
        if(this.updateVisibilityBecauseOfNewSymbols) {
            this.updateVisibilityBecauseOfNewSymbols = false;
            updateVisibilityOfDependentSymbolsSnippets();
        }
    }

    public void onClick(LinkContext<QuoteWithInstrument> context, Element e) {
        final QuoteWithInstrument qwi = context.data;
        final InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(qwi.getInstrumentData().getType());
        final String qid = qwi.getQuoteData().getQid();
        this.localLinkRenderer.setSelectedSymbol(qid);
        setDependentSymbol(type, qid);
        updateVisibilityOfDependentSymbolsSnippets();
    }

    private void updateVisibilityOfDependentSymbolsSnippets() {
        for(SymbolSnippet s : this.symbolSnippets) {
            if(s instanceof Snippet) {
                this.contextController.updateVisibility(((Snippet) s).getView(), true);
            }
        }
    }

    private void setDependentSymbol(InstrumentTypeEnum type, String symbol) {
        for (SymbolSnippet s : this.symbolSnippets) {
            s.setSymbol(type, symbol, null);
        }
        this.localLinkRenderer.setSelectedSymbol(symbol);
        ackParametersChanged();
    }

    protected void onParametersChanged() {
        List<String> sl = getConfiguration().getList("symbol"); // $NON-NLS-0$
        if (sl != null) {
            this.block.setParameters("symbol", sl.toArray(new String[sl.size()])); // $NON-NLS-0$
        }
    }

    public void setSymbols(List<InstrumentData> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            this.instrumentsChangedSupport.reset();
            setVisible(false);
            return;
        }
        setVisible(true);
        getConfiguration().put("symbol", "[" + join(symbols) + "]"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        final InstrumentData first = symbols.get(0);
        setDependentSymbol(InstrumentTypeEnum.valueOf(first.getType()), first.getIid());
        this.updateVisibilityBecauseOfNewSymbols = this.instrumentsChangedSupport.hasChanged(symbols);
    }

    @Override
    public void onVisibilityUpdated(VisibilityUpdatedEvent event) {
        for(SymbolSnippet s : this.symbolSnippets) {
            if(((Snippet) s).getView().container == event.getTarget()) {
                if(!event.isVisible()) {
                    Firebug.log("<NewsrelatedQuotesSnippet.onVisibilityUpdated> setting renderer to empty string and updating view");

                    this.localLinkRenderer.setSelectedSymbol("");
                    updateView();
                    break;
                }
            }
        }
    }

    private String join(List<InstrumentData> list) {
        final StringBuilder sb = new StringBuilder();
        for (InstrumentData id : list) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(id.getIid());
        }
        return sb.toString();
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
}
