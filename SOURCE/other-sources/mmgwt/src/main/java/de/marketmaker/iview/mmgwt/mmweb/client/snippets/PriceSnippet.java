/*
 * NewsHeadlinesSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.FundPriceData;
import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDataExtendedElement;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.dmxml.PriceData;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.portrait.MetadataAware;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceDataType;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceSnippet extends AbstractSnippet<PriceSnippet, PriceSnippetView>
        implements SymbolSnippet, PushRegisterHandler, MetadataAware {

    public static class Class extends SnippetClass {

        public Class() {
            super("Price"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PriceSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("width", DEFAULT_SNIPPET_WIDTH); // $NON-NLS$
        }
    }

    private final DmxmlContext.Block<MSCPriceDataExtended> block;

    private final boolean displayVolume;
    private final boolean showUnderlying;
    private final boolean reducedContent;
    private boolean isContributorQuote;

    private final PriceSupport priceSupport = new PriceSupport(this);
    private final PriceSnippetFieldConfig config;

    private TableDataModel tableDataModel;

    public PriceSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.displayVolume = config.getBoolean("displayVolume", true); // $NON-NLS-0$
        this.showUnderlying = SymbolSnippet.SYMBOL_UNDERLYING.equals(config.getString("symbol")); // $NON-NLS-0$
        this.reducedContent = config.getBoolean("reducedContent", false); // $NON-NLS-0$
        config.putDefault("title", this.showUnderlying ? I18n.I.pricesUnderlying() : I18n.I.prices());  // $NON-NLS-0$
        this.setView(new PriceSnippetView(this));
        this.config = new PriceSnippetFieldConfig(getView());
        this.block = createBlock("MSC_PriceDataExtended"); // $NON-NLS-0$
    }

    public MSCPriceDataExtendedElement getResult() {
        return block.getResult().getElement().get(0);
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", resolveSymbol(symbol)); // $NON-NLS-0$
    }

    private String resolveSymbol(String s) {
        return this.showUnderlying ? ("underlying(" + s + ")") : s; // $NON-NLS-0$ $NON-NLS-1$
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        this.priceSupport.invalidateRenderItems();

        if (!block.isResponseOk()) {
            getView().update(new DefaultTableColumnModel(new TableColumn[] {}, false),
                    DefaultTableDataModel.NULL);
            return;
        }

        final MSCPriceDataExtendedElement data = getResult();
        PriceDataType priceDataType = PriceDataType.fromDmxml(data.getPricedatatype());
        final PriceSnippetFieldConfig.Mode mode = getMode(priceDataType);

        Price p = Price.create(data);

        PriceData priceData = null;
        FundPriceData fundpriceData = null;
        switch (priceDataType) {
            case STANDARD:
                priceData = Price.nonNullPriceData(data.getPricedata());
                break;
            case FUND_OTC:
                fundpriceData = Price.nonNullFundPriceData(data.getFundpricedata());
                break;
            case CONTRACT_EXCHANGE:
                priceData = Price.nonNullContractPriceData(data.getContractpricedata());
                break;
            case LME:
                priceData = Price.nonNullLMEPriceData(data.getLmepricedata());
                break;
        }

        final TableColumnAndData<PriceDataContainer> tableColAndData
                = this.config.getTableColumnAndData(mode.getId());
        TableColumnModel tcm = tableColAndData.getTableColumnModel();

        PriceDataContainer pc = new PriceDataContainer(data.getQuotedata(), priceData, fundpriceData,
                data.getPricedataExtended(), data.getLmepricedata(), p);
        final List<RowData> rowDatas = tableColAndData.getRowData(pc);
        this.tableDataModel = DefaultTableDataModel.createWithRowData(rowDatas);

        getView().update(tcm, this.tableDataModel);
        
        this.priceSupport.activate();
        this.priceSupport.updatePriceGeneration();
    }

    private PriceSnippetFieldConfig.Mode getMode(PriceDataType type) {
        if (type == PriceDataType.FUND_OTC) {
            return PriceSnippetFieldConfig.Mode.FUND_OTC;
        }
        if (type == PriceDataType.LME) {
            return PriceSnippetFieldConfig.Mode.LME_CONTENT;
        }
        if (this.reducedContent && this.displayVolume) {
            return PriceSnippetFieldConfig.Mode.REDUCED_CONTENT_WITH_VOLUME;
        }
        if (this.reducedContent) {
            return PriceSnippetFieldConfig.Mode.REDUCED_CONTENT;
        }
        if (!this.displayVolume) {
            return PriceSnippetFieldConfig.Mode.WITHOUT_VOLUME;
        }
        if (this.isContributorQuote) {
            return PriceSnippetFieldConfig.Mode.CONTRIBUTOR_CONTENT;
        }
        return PriceSnippetFieldConfig.Mode.FULL_CONTENT;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.block.isResponseOk() && !this.priceSupport.isLatestPriceGeneration()
                && !event.isPushedUpdate()) {
            updateView();
            this.priceSupport.updatePriceGeneration();            
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            if (event.addVwdcode(getResult())) {
                event.addComponentToReload(this.block, this);
                return getView().getRenderItems(this.tableDataModel);
            }
        }
        return null;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }

    @Override
    public boolean isMetadataNeeded() {
        return true;
    }

    @Override
    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        if (FeatureFlags.Feature.CONTRIBUTOR_PORTRAIT.isEnabled()
                && SessionData.INSTANCE.getGuiDef("showContributorPortrait").booleanValue()) { // $NON-NLS-0$
            this.isContributorQuote = metadata.isContributorQuote();
        }
    }
}
