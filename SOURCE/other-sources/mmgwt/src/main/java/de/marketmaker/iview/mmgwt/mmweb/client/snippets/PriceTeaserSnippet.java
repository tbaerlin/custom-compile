/*
 * PriceTeaserSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.dmxml.GISReports;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.MSCPriceDatasElement;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ReportType;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.DataAvailableEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.DataAvailableHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.HasDataAvailableHandlers;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.EdgUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PriceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentHeaderProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceTeaserSnippet extends AbstractSnippet<PriceTeaserSnippet, PriceTeaserSnippetView<PriceTeaserSnippet>>
        implements SymbolSnippet, PushRegisterHandler, ContentHeaderProvider, HasDataAvailableHandlers {

    private String currentSymbol;

    public static class Class extends SnippetClass {
        public Class() {
            super("PriceTeaser"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PriceTeaserSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("colSpan", 3); // $NON-NLS$
        }
    }

    /**
     * Always request arbitrage data: the market selection button indicates for each market
     * whether it's up or down, so we need price data for all markets
     */
    private DmxmlContext.Block<MSCPriceDatas> blockPriceDatas;

    private DmxmlContext.Block<MSCPriceDataExtended> blockPriceDataExtended;

    private DmxmlContext.Block<IMGResult> blockEdgChart;
    private DmxmlContext.Block<EDGData> blockEdgData;

    private DmxmlContext.Block<GISReports> blockGisReports;

    private MSCPriceDatasElement data;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private HandlerManager manager;

    private PriceTeaserSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        boolean withLimits = !isAnonymous() && config.getBoolean("withLimits", true); // $NON-NLS$
        boolean withPin = !isAnonymous() && config.getBoolean("withPin", true); // $NON-NLS$
        boolean withSimpleWatchlist = !withPin && config.getBoolean("withSimpleWatchlist", false); // $NON-NLS$
        boolean withEdg = isWithEdg();

        setView(new PriceTeaserSnippetView<>(this, withLimits, withPin, withSimpleWatchlist, withEdg));

        this.blockPriceDatas = createBlock("MSC_PriceDatas"); // $NON-NLS$
        this.blockPriceDatas.setParameter("disablePaging", "true"); // $NON-NLS$
        this.blockPriceDatas.setParameter("marketStrategy", config.getString("marketStrategy", null)); // $NON-NLS$

        this.blockPriceDataExtended = createBlock("MSC_PriceDataExtended"); // $NON-NLS-0$
        this.blockPriceDataExtended.setParameter("marketStrategy", config.getString("marketStrategy", null)); // $NON-NLS$

        this.blockEdgChart = createBlock("IMG_ChartEDG"); // $NON-NLS$
        this.blockEdgChart.disable();

        this.blockEdgData = null;

        if (Selector.PRODUCT_WITH_PIB.isAllowed()) {
            this.blockGisReports = createBlock("GIS_Reports"); //$NON-NLS$
            this.blockGisReports.setParameter("filterStrategy", "DZBANK-PIB"); //$NON-NLS$
        }
    }

    public void destroy() {
        destroyBlock(this.blockPriceDatas);
        destroyBlock(this.blockPriceDataExtended);
        destroyBlock(this.blockEdgChart);
        destroyBlock(this.blockGisReports);
        destroyBlock(this.blockEdgData);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.data = null;
        this.priceSupport.deactivate();
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.blockPriceDatas.isResponseOk() && this.data != null
                && this.priceSupport.isNewerPriceAvailable(this.data.getQuotedata())) {
            doUpdateView();
        }
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.data != null) {
            if (event.addVwdcode(this.data)) {
                event.addComponentToReload(this.blockPriceDatas, this);
            }
        }
        return null;
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        if(isObjectInfo() && !StringUtil.equals(this.blockPriceDatas.getParameter("symbol"), symbol)) {  // $NON-NLS$
            this.currentSymbol = symbol;
            getView().asWidget().setVisible(false);
        }

        this.blockPriceDatas.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockPriceDataExtended.setParameter("symbol", symbol); // $NON-NLS-0$
        if (blockGisReports != null) {
            blockGisReports.setParameter("symbol", symbol); //$NON-NLS$
        }
        if (enableEdgFor(type)) {
            this.blockEdgChart.setParameter("symbol", symbol); // $NON-NLS-0$
            this.blockEdgChart.enable();
            //edg data block is only necessary for AS
            if(SessionData.isAsDesign()) {
                this.context.removeBlock(this.blockEdgData);
                this.blockEdgData = (EdgUtil.createBlock(type, this.context));
                this.blockEdgData.setParameter("symbol", symbol); // $NON-NLS-0$
                this.blockEdgData.setEnabled(true);
            }
        }
        else {
            this.blockEdgChart.disable();
            if(this.blockEdgData != null) {
                this.blockEdgData.disable();
            }
        }
    }

    private boolean isObjectInfo() {
        return getConfiguration().getBoolean("isObjectInfo", false); // $NON-NLS$
    }

    private boolean enableEdgFor(InstrumentTypeEnum type) {
        return EdgUtil.isTypeWithEdgData(type) && isWithEdg();
    }

    public void updateView() {
        this.priceSupport.activate();

        this.data = PriceUtil.getSelectedElement(this.blockPriceDatas);

        if (this.data == null) {
            getView().update(getInstrumentData(), getQuoteData(), null, null, null, null, null);
            return;
        }
        doUpdateView();
        if(isObjectInfo()) {
            getView().asWidget().setVisible(this.blockPriceDatas.isResponseOk() && StringUtil.equals(this.blockPriceDatas.getParameter("symbol"), this.currentSymbol));  // $NON-NLS$
        }
    }

    private void doUpdateView() {
        final QuoteData quoteData = getQuoteData();
        final InstrumentData instrumentData = getInstrumentData();

        if (SessionData.isAsDesign() && instrumentData != null) {
            MainController.INSTANCE.getView().setContentHeader(instrumentData.getName());
        }

        getView().update(instrumentData, quoteData,
                this.blockPriceDatas.getResult(),
                this.priceSupport.getCurrentPrice(quoteData),
                this.blockEdgChart.isEnabled() ? this.blockEdgChart.getResult() : null,
                this.getPibReportUrl(),
                this.blockEdgData != null && this.blockEdgData.isEnabled() ? this.blockEdgData.getResult() : null);

        fireEvent(new DataAvailableEvent());
    }

    private QuoteData getQuoteData() {
        if (this.blockPriceDataExtended.isResponseOk()) {
            return this.blockPriceDataExtended.getResult().getElement().get(0).getQuotedata();
        }
        if (this.data != null) {
            return this.data.getQuotedata();
        }
        return null;
    }

    private InstrumentData getInstrumentData() {
        if (this.blockPriceDataExtended.isResponseOk()) {
            return this.blockPriceDataExtended.getResult().getElement().get(0).getInstrumentdata();
        }
        if (this.blockPriceDatas.isResponseOk()) {
            return this.blockPriceDatas.getResult().getInstrumentdata();
        }
        return null;
    }

    private String getPibReportUrl() {
        if (blockGisReports != null && blockGisReports.isResponseOk()) {
            final List<ReportType> reports = blockGisReports.getResult().getReport();
            if (reports != null && reports.size() > 0) {
                return reports.get(0).getUrl();
            }
        }
        return null;
    }

    // TODO: remove this: it depends on the fact that this snippet gets its result before
    // TODO: any snippet that uses this method -- does not make sense, each snippet is supposed
    // TODO: to request any information it needs by itself.
    public boolean isEndOfDay() {
        return PriceUtil.isEndOfDay(this.data);
    }

    public boolean isWithEdg() {
        return Selector.EDG_RATING.isAllowed();
    }

    boolean isAnonymous() {
        return SessionData.INSTANCE.isAnonymous();
    }

    public HandlerRegistration addDataAvailableHandler(DataAvailableHandler handler) {
        if (this.manager == null) {
            this.manager = new HandlerManager(this);
        }
        return this.manager.addHandler(DataAvailableEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (this.manager != null) {
            this.manager.fireEvent(event);
        }
    }
}
