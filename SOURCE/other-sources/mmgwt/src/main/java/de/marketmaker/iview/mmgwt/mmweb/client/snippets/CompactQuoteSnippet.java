/*
 * NewsHeadlinesSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.LMEPriceData;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.dmxml.MSCQuotes;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompactQuoteSnippet extends AbstractSnippet<CompactQuoteSnippet, CompactQuoteSnippetView> implements PushRegisterHandler {

    public static class Class extends SnippetClass {
        public Class() {
            super("CompactQuote", I18n.I.compactQuote()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new CompactQuoteSnippet(context, config);
        }
    }

    private DmxmlContext.Block<MSCQuotes> quotesBlock;
    protected DmxmlContext.Block<IMGResult> idChartBlock;
    protected DmxmlContext.Block<IMGResult> hChartBlock;
    protected DmxmlContext.Block<MSCPriceData> priceDataBlock;


    private final PriceSnippetFieldConfig tabConfig = new PriceSnippetFieldConfig();
    private TableColumnAndData<PriceDataContainer> tableColAndData;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private TableDataModel tableDataModel;


    private CompactQuoteSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.quotesBlock = createBlock("MSC_Quotes"); // $NON-NLS$
        this.quotesBlock.setParameter("disablePaging", true); // $NON-NLS$

        this.idChartBlock = createBlock("IMG_Compact"); // $NON-NLS$
        this.idChartBlock.setParameter("period", "P1D"); // $NON-NLS$
        this.idChartBlock.setParameter("withPrice", "true"); // $NON-NLS$

        this.hChartBlock = createBlock("IMG_Compact"); // $NON-NLS$
        this.hChartBlock.setParameter("period", "P1Y"); // $NON-NLS$
        this.hChartBlock.setParameter("withPrice", "false"); // $NON-NLS$

        this.priceDataBlock = createBlock("MSC_PriceData"); // $NON-NLS$

        PriceSnippetFieldConfig.Mode displayMode = findDisplayMode();
        this.tableColAndData = tabConfig.getTableColumnAndData(displayMode.getId());
        setView(new CompactQuoteSnippetView(this));

        onParametersChanged();
    }

    private PriceSnippetFieldConfig.Mode findDisplayMode() {
        PriceSnippetFieldConfig.Mode displayMode;
        boolean displayVolume = getConfiguration().getBoolean("displayVolume", true); // $NON-NLS-0$
        if (displayVolume) {
            displayMode = PriceSnippetFieldConfig.Mode.REDUCED_CONTENT_WITH_VOLUME;
        }
        else {
            displayMode = PriceSnippetFieldConfig.Mode.REDUCED_CONTENT;
        }
        // override old display mode if we got a LME price
        if (priceDataBlock.isEnabled()
                && priceDataBlock.isResponseOk()
                && "lme".equals(priceDataBlock.getResult().getPricedatatype())) { // $NON-NLS$
            displayMode = PriceSnippetFieldConfig.Mode.LME_CONTENT;
        }
        return displayMode;
    }

    public void configure(Widget triggerWidget) {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addSelectSymbol(null);
        configView.show();
    }

    public void destroy() {
        this.priceSupport.deactivate();
        destroyBlock(this.quotesBlock);
        destroyBlock(this.idChartBlock);
        destroyBlock(this.hChartBlock);
        destroyBlock(this.priceDataBlock);
    }

    public String getDropTargetGroup() {
        return DROP_TARGET_GROUP_INSTRUMENT;
    }

    public boolean isConfigurable() {
        return true;
    }

    public boolean notifyDrop(QuoteWithInstrument qwi) {
        if (qwi == null) {
            return false;
        }
        onSymbolChange(qwi.getName(), qwi.getId());
        return true;
    }

    private void onSymbolChange(final String title, final String symbol) {
        this.quotesBlock.enable();
        this.priceDataBlock.enable();
        getConfiguration().put("title", title); // $NON-NLS-0$
        getConfiguration().put("symbol", symbol); // $NON-NLS-0$
        ackParametersChanged();
    }

    public void setParameters(HashMap<String, String> params) {
        final QuoteWithInstrument qwi = QuoteWithInstrument.getLastSelected();
        if (qwi != null) {
            onSymbolChange(qwi.getName(), qwi.getId());
        }
    }

    @Override
    public void updateView() {
        this.priceSupport.invalidateRenderItems();

        if (!idChartBlock.isResponseOk()) {
            if (getConfiguration().getString("symbol") == null) { // $NON-NLS-0$
                getView().update(I18n.I.messagePleaseConfigureInstrument()); 
                return;
            }
            getView().update(I18n.I.noData());
            return;
        }

        final IMGResult imgResult = this.idChartBlock.getResult();

        if (this.quotesBlock.isEnabled()) {
            getView().reloadTitle();
            if (this.quotesBlock.isResponseOk()) {
                final MSCQuotes result = this.quotesBlock.getResult();
                getView().updateQuotesMenu(result.getQuotedata(), imgResult.getQuotedata());
                getView().updateSymbols(result.getInstrumentdata().getWkn(), result.getInstrumentdata().getIsin());
            }
            else {
                getView().disableQuotesMenu();
            }
            this.quotesBlock.disable();
        }

        LMEPriceData lmePriceData = null;
        if (this.priceDataBlock.isEnabled()) {
            if (this.priceDataBlock.isResponseOk()) {
                final MSCPriceData prices = this.priceDataBlock.getResult();
                lmePriceData = prices.getLmepricedata();
            }
        }

        final Price price = Price.create(imgResult);

        // we might need to change the view if someone dropped a new instrument which requires different table setup
        final PriceSnippetFieldConfig.Mode newMode = findDisplayMode();

        getView().update("switching instruments"); // $NON-NLS$ this is a hack, we needed to reset tw in view
        this.tableColAndData = tabConfig.getTableColumnAndData(newMode.getId());

        PriceDataContainer priceDataContainer = new PriceDataContainer(imgResult.getQuotedata(), imgResult.getPricedata(), null, null, lmePriceData, price);
        final List<RowData> rowDatas = this.tableColAndData.getRowData(priceDataContainer);
        this.tableDataModel = DefaultTableDataModel.createWithRowData(rowDatas);

        getView().update(this.tableDataModel, getTableColumnModel(), getIntradayRequest(imgResult), getHistoricRequest(this.hChartBlock.getResult()));
        this.priceSupport.activate();
    }

    private String getHistoricRequest(IMGResult imgResult) {
        return imgResult == null ? null : imgResult.getRequest();
    }

    public TableColumnModel getTableColumnModel() {
        return this.tableColAndData.getTableColumnModel();
    }

    private String getIntradayRequest(IMGResult pr) {
        if (pr != null && pr.getPricedata() != null
                && !"END_OF_DAY".equals(pr.getPricedata().getPriceQuality())) { // $NON-NLS-0$
            return pr.getRequest();
        }
        return null;
    }

    public void goToPortrait() {
        if (!this.idChartBlock.isResponseOk()) {
            return;
        }
        PlaceUtil.goToPortrait(this.idChartBlock.getResult().getInstrumentdata(),
                               this.idChartBlock.getResult().getQuotedata());
    }

    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        final String symbol = config.getString("symbol", null); // $NON-NLS-0$
        this.idChartBlock.setParameter("symbol", symbol); // $NON-NLS-0$
        this.hChartBlock.setParameter("symbol", symbol); // $NON-NLS-0$
        if (this.quotesBlock.isEnabled()) {
            this.quotesBlock.setParameter("symbol", symbol); // $NON-NLS-0$
        }
        if (this.priceDataBlock.isEnabled()) {
            this.priceDataBlock.setParameter("symbol", symbol);  // $NON-NLS-0$
        }
    }

    void updateQuote(String qid) {
        // called whenever a different market has been selected, no need to enable quotesBlock
        getConfiguration().put("symbol", qid); // $NON-NLS-0$
        ackParametersChanged();
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.idChartBlock.isResponseOk()) {
            if (event.addVwdcode(this.idChartBlock.getResult())) {
                event.addComponentToReload(this.idChartBlock, this);
                return getView().getRenderItems(this.tableDataModel);
            }
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.idChartBlock.isResponseOk()
                && !this.priceSupport.isLatestPriceGeneration()
                && !event.isPushedUpdate()) {
            updateView();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }

}
