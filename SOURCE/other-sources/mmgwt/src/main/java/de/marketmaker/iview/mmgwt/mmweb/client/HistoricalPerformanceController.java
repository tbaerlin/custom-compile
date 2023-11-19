package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.HashMap;

import com.extjs.gxt.ui.client.util.DateWrapper;

import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.MSCHistoricalRatios;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * HistoricalPerformanceController.java
 * Created on Nov 26, 2008 11:09:44 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class HistoricalPerformanceController extends AbstractPageController implements ConfigurableSnippet, SymbolSnippet {

    private HistoricalPerformanceView view = null;
    private DmxmlContext.Block<IMGResult> chartBlock;
    private DmxmlContext.Block<MSCHistoricalRatios> ratiosBlock;
    private SnippetConfiguration config;

    protected HistoricalPerformanceController(ContentContainer contentContainer) {
        super(contentContainer);
    }

    private void init() {
        if (this.view == null) {
//            DebugUtil.logToFirebugConsole("HistoricalPerformanceController init");
            this.chartBlock = this.context.addBlock("IMG_Chartcenter"); // $NON-NLS-0$
            this.ratiosBlock = this.context.addBlock("MSC_HistoricalRatios"); // $NON-NLS-0$
            this.config = new SnippetConfiguration()
                    .with("symbol", SessionData.INSTANCE.getGuiDefValue("defaultIndex", "symbol")); // set initial quote // $NON-NLS$
            this.view = new HistoricalPerformanceView(this, config);
        }
        reload();
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        init();
    }

    protected void onResult() {
        if (this.chartBlock.isResponseOk() && this.ratiosBlock.isResponseOk()) {
            this.view.update(this.chartBlock.getResult(), this.ratiosBlock.getResult());
        }
    }

    public HashMap<String, String> getCopyOfParameters() {
        return new HashMap<>();
    }

    public void setParameters(HashMap<String, String> params) {
        final QuoteWithInstrument qwi = QuoteWithInstrument.getLastSelected();
        if (qwi != null) {
            setSymbol(null, qwi.getId(), null);
        }
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.config.put("symbol", symbol); // $NON-NLS-0$
        reload();
    }

    public void reload() {
        final DateWrapper now = new DateWrapper();
        final String from = this.config.getString("from", // $NON-NLS-0$
                Formatter.formatDateAsISODay(now.add(DateWrapper.Unit.YEAR, -1).asDate()));
        final String to = this.config.getString("to", Formatter.formatDateAsISODay(now.asDate())); // $NON-NLS-0$

//        DebugUtil.logToFirebugConsole("HistoricalPerformanceController.reload() symbol: " + this.config.getString("symbol", "394623.qid"));

        this.chartBlock.setParameter("symbol", this.config.getString("symbol")); // $NON-NLS-0$ $NON-NLS-1$
        this.chartBlock.setParameter("from", from); // $NON-NLS-0$
        this.chartBlock.setParameter("to", to); // $NON-NLS-0$
        this.chartBlock.setParameter("width", this.config.getString("chartwidth", "600")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.chartBlock.setParameter("height", this.config.getString("chartheight", "260")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.ratiosBlock.setParameter("symbol", this.config.getString("symbol")); // $NON-NLS-0$ $NON-NLS-1$
        this.ratiosBlock.setParameter("from", from); // $NON-NLS-0$
        this.ratiosBlock.setParameter("to", to); // $NON-NLS-0$
        this.context.issueRequest(this);
    }
}
