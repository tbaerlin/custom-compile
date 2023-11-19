/*
 * PriceListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.FundPriceData;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CurrentTrendBar;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.ItemListContext;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectListMenu;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DatePickerHeaderRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PagingPanel;

import java.util.ArrayList;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.ErrorMessageUtil.getMessage;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceListSnippet extends AbstractSnippet<PriceListSnippet, PriceListSnippetView> implements PushRegisterHandler,
        SymbolSnippet, PagingPanel.Handler {
    private DefaultTableDataModel dtm;

    public static class Class extends SnippetClass {
        public Class() {
            super("PriceList", I18n.I.pricelist()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PriceListSnippet(context, config);
        }
    }

    public enum Mode {
        BONDS, FUNDS_LONG, BID_ASK, BID_ASK_LONG, ONLY_NAME, INDEX, DEFAULT
    }

    private enum BlockType {
        MSC_PriceDataMulti,
        MSC_List_Details
    }

    private static final TableCellRenderers.QuoteLinkRenderer NAME_RENDERER = new TableCellRenderers.QuoteLinkRenderer(30, ""); // $NON-NLS-0$
    private static final TableCellRenderers.QuoteLinkRenderer NAME_CURRENCY_RENDERER = new TableCellRenderers.QuoteLinkRendererWithCurrency(30, ""); // $NON-NLS-0$

    private DmxmlContext.Block<MSCListDetails> block;
    private Mode mode;
    private final BlockType blockType;
    private boolean compactDateOrTime;
    private final boolean reduceCurrencyName;
    private final boolean nameWithCurrency;
    private boolean withDetailLink = false;
    private boolean withPartitions;
    private final String priceColumnHeader;
    private List<SymbolSnippet> symbolSnippets = new ArrayList<>();
    private String selectedSymbol;
    private int columnCount;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private PriceListSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        if (config.containsKey("symbols")) { // $NON-NLS-0$
            this.block = createBlock("MSC_PriceDataMulti"); // $NON-NLS-0$
            this.blockType = BlockType.MSC_PriceDataMulti;
        }
        else {
            this.block = createBlock("MSC_List_Details"); // $NON-NLS-0$
            this.blockType = BlockType.MSC_List_Details;
        }
        this.mode = Mode.valueOf(config.getString("mode", Mode.DEFAULT.name())); // $NON-NLS-0$
        this.compactDateOrTime = config.getBoolean("compactDateOrTime", false); // $NON-NLS-0$
        this.reduceCurrencyName = config.getBoolean("reduceCurrencyName", false); // $NON-NLS-0$
        this.nameWithCurrency = config.getBoolean("nameWithCurrency", false); // $NON-NLS-0$
        this.withPartitions = config.getBoolean("withPartitions", false); // $NON-NLS-0$
        final String selectedSymbol = config.getString("selectedSymbol", null); // $NON-NLS-0$
        if (selectedSymbol != null) {
            this.selectedSymbol = selectedSymbol;
        }
        this.priceColumnHeader = config.getString("priceColumnHeader", I18n.I.yield()); // $NON-NLS-0$

        final PriceListSnippetView view = new PriceListSnippetView(this
                , createColumnModel(), "mm-snippetTable mm-snippet-priceList"); // $NON-NLS-0$
        this.setView(view);

        onParametersChanged();
    }

    private DefaultTableColumnModel createColumnModel() {
        final List<TableColumn> columns = getColumnList();
        this.columnCount = columns.size();
        return new DefaultTableColumnModel(columns.toArray(new TableColumn[columns.size()]));
    }

    private List<TableColumn> getColumnList() {
        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());
        final List<TableColumn> result = new ArrayList<>(10);
        if (this.mode == Mode.BID_ASK) {
            result.add(new TableColumn(I18n.I.name(), 0.52f, getNameRenderer()));
            result.add(new TableColumn(I18n.I.bid(), 0.16f, TableCellRenderers.BID_PUSH));
            result.add(new TableColumn(I18n.I.ask(), 0.16f, TableCellRenderers.ASK_PUSH));
            result.add(new TableColumn(I18n.I.time(), 0.16f, TableCellRenderers.DATE_OR_TIME_PUSH));
        }
        else if (this.mode == Mode.BID_ASK_LONG) {
            result.add(new TableColumn(I18n.I.name(), -1f, getNameRenderer()));
            result.add(new TableColumn(I18n.I.priceValue(), -1f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH));
            result.add(new TableColumn(I18n.I.bid(), -1f, TableCellRenderers.BID_PUSH));
            result.add(new TableColumn(I18n.I.ask(), -1f, TableCellRenderers.ASK_PUSH));
            result.add(new TableColumn("+/-%", -1f, TableCellRenderers.CHANGE_PERCENT_PUSH)); // $NON-NLS-0$
            result.add(new TableColumn(I18n.I.time(), -1f, TableCellRenderers.DATE_OR_TIME_PUSH));
            result.add(new TableColumn(I18n.I.high(), -1f, TableCellRenderers.HIGH_PUSH, "high")); // $NON-NLS$
            result.add(new TableColumn(I18n.I.low(), -1f, TableCellRenderers.LOW_PUSH, "low")); // $NON-NLS$
            result.add(new TableColumn(I18n.I.close(), -1f, TableCellRenderers.PRICE_WITH_SUPPLEMENT, "close")); // $NON-NLS$
        }
        else if (this.mode == Mode.BONDS) {
            result.add(new TableColumn(I18n.I.name(), 0.60f, getNameRenderer()));
            result.add(new TableColumn(this.priceColumnHeader, 0.25f, TableCellRenderers.PRICE_PERCENT_PUSH));
            result.add(new TableColumn(I18n.I.date(), 0.15f, this.compactDateOrTime
                    ? TableCellRenderers.DATE_OR_TIME_COMPACT_PUSH
                    : TableCellRenderers.DATE_PUSH));
        }
        else if (this.mode == Mode.FUNDS_LONG) {
            final TableColumn changePercentColumn = new TableColumn(I18n.I.nYears(1), -1f, TableCellRenderers.CHANGE_PERCENT, "changePercent"); // $NON-NLS$
            changePercentColumn.setHeaderRenderer(DatePickerHeaderRenderer.create((period, title) -> {
                getConfiguration().put("period", period); // $NON-NLS$
                changePercentColumn.setTitle(title);
                ackParametersChanged();
            }));

            result.add(new TableColumn("WKN", -1f, TableCellRenderers.DEFAULT).withVisibilityCheck(showWknCheck)); // $NON-NLS$
            result.add(new TableColumn("ISIN", -1f, TableCellRenderers.DEFAULT).withVisibilityCheck(showIsinCheck)); // $NON-NLS$
            result.add(new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_32, "name"));  // $NON-NLS$
            result.add(new TableColumn(I18n.I.redemption(), -1f, TableCellRenderers.PRICE, "priceValue"));  // $NON-NLS$
            result.add(new TableColumn(I18n.I.issuePrice2(), -1f, TableCellRenderers.PRICE, "issueprice"));  // $NON-NLS$
            result.add(new TableColumn(I18n.I.currency(), -1f, TableCellRenderers.DEFAULT_CENTER, "currency"));  // $NON-NLS$
            result.add(new TableColumn(I18n.I.date(), -1f, TableCellRenderers.COMPACT_DATE_OR_TIME, "date").alignCenter());  // $NON-NLS$
            result.add(changePercentColumn);
            result.add(new TableColumn(I18n.I.low52wAbbr(), 0.06f, TableCellRenderers.PRICE, "low")); // $NON-NLS$
            result.add(new TableColumn(I18n.I.high52wAbbr(), 0.06f, TableCellRenderers.PRICE, "high")); // $NON-NLS$
        }
        else if (this.mode == Mode.ONLY_NAME) {
            result.add(new TableColumn(I18n.I.name(), 1f, getNameRenderer()));
        }
        else if (this.mode == Mode.INDEX) {
            result.add(new TableColumn(I18n.I.name(), 0.2f, new TableCellRenderers.QuoteLinkRenderer(20, ""))); // $NON-NLS-0$
            result.add(new TableColumn("WKN", 0.05f, TableCellRenderers.DEFAULT, "wkn").withVisibilityCheck(showWknCheck)); // $NON-NLS$
            result.add(new TableColumn("ISIN", 0.05f, TableCellRenderers.DEFAULT, "isin").withVisibilityCheck(showIsinCheck));  // $NON-NLS$
            result.add(new TableColumn(I18n.I.price(), 0.05f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH));
            result.add(new TableColumn(I18n.I.market(), 0.05f, TableCellRenderers.STRING_CENTER));
            result.add(new TableColumn("+/-", 0.05f, TableCellRenderers.CHANGE_PERCENT_PUSH, "changePercent")); // $NON-NLS-0$ $NON-NLS-1$
            result.add(new TableColumn(I18n.I.trend(), 0.05f, TableCellRenderers.TRENDBAR, "changePercent").withCellClass("mm-middle")); // $NON-NLS-0$ $NON-NLS-1$
            result.add(new TableColumn(I18n.I.time(), 0.07f, TableCellRenderers.DATE_OR_TIME_PUSH, "date").withCellClass("mm-center")); // $NON-NLS-0$ $NON-NLS-1$
            result.add(new TableColumn(I18n.I.bid(), 0.06f, TableCellRenderers.BID_PUSH));
            result.add(new TableColumn(I18n.I.ask(), 0.06f, TableCellRenderers.ASK_PUSH));
            result.add(new TableColumn(I18n.I.bidVolumeAbbr(), 0.06f, TableCellRenderers.BID_VOLUME_PUSH, "bidVolume")); // $NON-NLS-0$
            result.add(new TableColumn(I18n.I.askVolumeAbbr(), 0.06f, TableCellRenderers.ASK_VOLUME_PUSH, "askVolume")); // $NON-NLS-0$
            result.add(new TableColumn(I18n.I.previousClose(), 0.07f, TableCellRenderers.PRICE_WITH_SUPPLEMENT, "previousClose"));  // $NON-NLS-0$
        }
        else {
            result.add(new TableColumn(I18n.I.name(), 0.52f, getNameRenderer()));
            result.add(new TableColumn(I18n.I.priceValue(), 0.16f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH));
            result.add(new TableColumn("+/-%", 0.16f, TableCellRenderers.CHANGE_PERCENT_PUSH)); // $NON-NLS-0$
            result.add(new TableColumn(I18n.I.time(), 0.16f, TableCellRenderers.DATE_OR_TIME_PUSH));
        }

        final String[] detailIds = getConfiguration().getArray("detailIds"); // $NON-NLS-0$
        if (detailIds != null && detailIds.length > 0) {
            this.withDetailLink = true;
            result.add(new TableColumn("", 10f, TableCellRenderers.DEFAULT)); // $NON-NLS-0$
        }

        return result;
    }

    private TableCellRenderers.QuoteLinkRenderer getNameRenderer() {
        return this.nameWithCurrency ? NAME_CURRENCY_RENDERER : NAME_RENDERER;
    }

    @Override
    public void onControllerInitialized() {
        final String[] detailIds = getConfiguration().getArray("detailIds"); // $NON-NLS-0$
        if (detailIds != null) {
            for (String detailId : detailIds) {
                this.symbolSnippets.add((SymbolSnippet) this.contextController.getSnippet(detailId));
            }
        }
    }

    void setSelectedSymbol(String symbol, final String name) {
        this.selectedSymbol = symbol;
        for (SymbolSnippet symbolSnippet : symbolSnippets) {
            symbolSnippet.setSymbol(null, symbol, name);
        }
        if (symbol != null) {
            ackParametersChanged();
        }
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        if (this.blockType == BlockType.MSC_List_Details) {
            getConfiguration().put("listid", symbol); // $NON-NLS$
            getConfiguration().put("title", name); // $NON-NLS$
            ackParametersChanged();
        }
        else {
            throw new RuntimeException("cannot use setSymbol for " + this.blockType); // $NON-NLS$
        }
    }

    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        if (this.blockType == BlockType.MSC_PriceDataMulti) {
            this.block.setParameters("symbol", config.getArray("symbols")); // $NON-NLS-0$ $NON-NLS-1$
            this.block.setParameter("symbolStrategy", config.getString("symbolStrategy", "auto")); // $NON-NLS$
            if (config.getString("period") != null) { // $NON-NLS$
                Firebug.log("PriceListSnippet.onParametersChanged(): config parameter 'period' is not evaluated when 'symbol' is specified -> use 'listid' instead");
            }
        }
        else {
            this.block.setParameter("offset", config.getInt("offset", 0)); // $NON-NLS$
            this.block.setParameter("disablePaging", !this.withPartitions); // $NON-NLS-0$
            this.block.setParameter("withHitCount", config.getBoolean("withHitCount", false)); // $NON-NLS$
            this.block.setParameter("listid", config.getString("listid", "")); // $NON-NLS$
            this.block.setParameter("period", config.getString("period", null)); // $NON-NLS$
            this.block.setParameter("partition", this.withPartitions); // $NON-NLS$
        }
        this.block.setParameter("sortBy", config.getString("sortBy", "name")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.block.setParameter("marketStrategy", config.getString("marketStrategy", null)); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void destroy() {
        this.priceSupport.deactivate();
        destroyBlock(this.block);
    }

    public boolean isConfigurable() {
        return true;
    }

    public void configure(Widget triggerWidget) {
        SelectListMenu.configure(this, triggerWidget, this::ackParametersChanged);
    }

    public void updateView() {
        this.priceSupport.invalidateRenderItems();

        if (!this.block.isResponseOk()) {
            getView().update(DefaultTableDataModel.create(getMessage(block.getError())), null);
            return;
        }

        final ArrayList<MSCListDetailElement> listElements = new ArrayList<>();
        for (MSCListDetailElement e : this.block.getResult().getElement()) {
            if(e.getInstrumentdata() == null || e.getQuotedata() == null) {
                continue;
            }
            listElements.add(e);
        }

        final TrendBarData tbd = TrendBarData.create(listElements);

        final int rows = listElements.size();

        if (this.selectedSymbol != null) {
            boolean symbolExists = false;
            for (MSCListDetailElement e : listElements) {
                if (this.selectedSymbol.equals(e.getQuotedata().getQid())) {
                    symbolExists = true;
                }
            }
            if (!symbolExists) {
                this.selectedSymbol = null;
            }
        }

        this.dtm = new DefaultTableDataModel(rows, this.columnCount).withRowClasses(DefaultTableDataModel.ROW_CLASSES);
        int row = 0;

        for (final MSCListDetailElement e : listElements) {
            final QuoteWithInstrument qwi = QuoteWithInstrument.createQuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata(), e.getItemname());
            if(qwi != null && !qwi.isNullQuoteOrNullInstrument()) {
                qwi.withHistoryContext(ItemListContext.createForPortrait(e, listElements, getView().getTitle()));
            }
            else {
                continue;
            }

            if (this.reduceCurrencyName) {
                StringUtil.reduceCurrencyNameLength(e.getInstrumentdata());
            }

            int col = -1;

            final Price price = Price.create(e);
            final CurrentTrendBar currentTrendBar = new CurrentTrendBar(price.getChangePercent(), tbd);

            if (this.mode == Mode.FUNDS_LONG) {
                final FundPriceData fundpricedata = e.getFundpricedata();
                this.dtm.setValueAt(row, ++col, qwi.getInstrumentData().getWkn());
                this.dtm.setValueAt(row, ++col, qwi.getInstrumentData().getIsin());
                this.dtm.setValueAt(row, ++col, qwi);
                this.dtm.setValueAt(row, ++col, fundpricedata.getRepurchasingprice());
                this.dtm.setValueAt(row, ++col, fundpricedata.getIssueprice());
                this.dtm.setValueAt(row, ++col, qwi.getQuoteData().getCurrencyIso());
                this.dtm.setValueAt(row, ++col, fundpricedata.getDate());
                this.dtm.setValueAt(row, ++col, fundpricedata.getChangePercent());
                this.dtm.setValueAt(row, ++col, fundpricedata.getLow1Y());
                this.dtm.setValueAt(row, ++col, fundpricedata.getHigh1Y());
            }
            else if (this.mode == Mode.BID_ASK) {
                this.dtm.setValueAt(row, ++col, qwi);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
            }
            else if (this.mode == Mode.BID_ASK_LONG) {
                this.dtm.setValueAt(row, ++col, qwi);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price.getPreviousPrice());
            }
            else if (this.mode == Mode.ONLY_NAME) {
                this.dtm.setValueAt(row, ++col, qwi);
            }
            else if (this.mode == Mode.INDEX) {
                this.dtm.setValueAt(row, ++col, qwi);
                this.dtm.setValueAt(row, ++col, qwi.getInstrumentData().getWkn());
                this.dtm.setValueAt(row, ++col, qwi.getInstrumentData().getIsin());
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, qwi.getQuoteData().getMarketVwd());
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, currentTrendBar);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price);
                this.dtm.setValueAt(row, ++col, price.getPreviousPrice());
            }
            else {
                this.dtm.setValueAt(row, ++col, qwi);
                this.dtm.setValueAt(row, ++col, price);
                if (this.mode != Mode.BONDS) {
                    this.dtm.setValueAt(row, ++col, price);
                }
                this.dtm.setValueAt(row, ++col, price);
            }
            if (this.withDetailLink) {
                final String symbol = e.getQuotedata().getQid();
                final String styleName = isSelected(symbol, qwi.getName()) ? "mm-bestTool-link selected" : "mm-bestTool-link"; // $NON-NLS-0$ $NON-NLS-1$
                final String linkContent = "<div class=\"" + styleName + "-content\"></div>"; // $NON-NLS-0$ $NON-NLS-1$
                final Link link = new Link((linkLinkContext, e1) -> setSelectedSymbol(symbol, qwi.getName()), linkContent, null);
                this.dtm.setValueAt(row, ++col, link);
            }
            row++;
        }

        getView().update(this.dtm, this.withPartitions ? this.block.getResult().getPartition() : null);

        this.priceSupport.activate();
    }

    private boolean isSelected(String symbol, String name) {
        if (this.selectedSymbol == null) {
            setSelectedSymbol(symbol, name);
            return true;
        }
        return this.selectedSymbol.equals(symbol);
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.block.getResult());
            if (numAdded == this.block.getResult().getElement().size()) {
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

    @Override
    public void ackNewOffset(int offset) {
        getConfiguration().put("offset", Integer.toString(offset)); // $NON-NLS-0$
        ackParametersChanged();
    }
}
