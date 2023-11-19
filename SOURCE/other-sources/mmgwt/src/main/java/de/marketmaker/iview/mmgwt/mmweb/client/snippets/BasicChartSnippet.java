/*
 * BasicChartSnippet.java
 *
 * Created on 14.05.2008 15:12:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import java.util.ArrayList;

import com.extjs.gxt.ui.client.util.DateWrapper;
import de.marketmaker.iview.dmxml.ErrorType;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class BasicChartSnippet<S extends BasicChartSnippet<S, V>, V extends BasicChartSnippetView<S, V>>
        extends AbstractSnippet<S, V> implements PushRegisterHandler {

    protected DmxmlContext.Block<IMGResult> block;

    private InstrumentTypeEnum type;
    private IMGResult ipr;
    private final PriceSupport priceSupport = new PriceSupport(this);

    public BasicChartSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.block = createBlock(getChartBlockName());

        this.block.setParameter("period", config.getString("period", "P1D")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.block.setParameter("width", config.getString("chartwidth", "260")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.block.setParameter("height", config.getString("chartheight", "200")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.block.setParameter("chartlayout", config.getString("layout", "basic")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            if (getConfiguration().getString("symbol") == null) { // $NON-NLS-0$
                getView().update(getEmptyMessage(), true);
                return;
            }
            final ErrorType et = block.getError();
            if (et == null) {
                getView().update(block.toString(), false);
            }
            else {
                getView().update(et.getCode() + ": " + et.getDescription() + " / " + block.toString(), false); // $NON-NLS-0$ $NON-NLS-1$
            }
            return;
        }
        this.ipr = this.block.getResult();
        getView().update(this.ipr);
        this.priceSupport.activate();
    }

    protected String getEmptyMessage() {
        return I18n.I.messagePleaseSelectInstrument(); 
    }

    protected String getTitle() {
        return getConfiguration().getString("title", I18n.I.chart());  // $NON-NLS-0$
    }

    protected String getChartBlockName() {
        return "IMG_Chart_Analysis"; // $NON-NLS-0$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.type = type;
        getConfiguration().put("symbol", symbol); // $NON-NLS-0$
        onParametersChanged();
    }

    public void setPeriod(String period) {
        final SnippetConfiguration config = getConfiguration();
        if (period == null || DateTimeUtil.PERIOD_KEY_ALL.equals(period)) {
            config.remove("period"); // $NON-NLS-0$
            config.put("from", "start"); // $NON-NLS-0$ $NON-NLS-1$
            config.put("to", "today"); // $NON-NLS-0$ $NON-NLS-1$
        }
        else if (DateTimeUtil.PERIOD_KEY_YEAR_TO_DATE.equals(period)) {
            config.remove("period"); // $NON-NLS-0$
            config.put("from", "01.01." + new DateWrapper().getFullYear()); // $NON-NLS-0$ $NON-NLS-1$
            config.put("to", "today"); // $NON-NLS-0$ $NON-NLS-1$
        }
        else {
            config.put("period", period); // $NON-NLS-0$
            config.remove("from"); // $NON-NLS-0$
            config.remove("to"); // $NON-NLS-0$
            if (!period.endsWith("D")) { // $NON-NLS-0$
                config.remove("ask"); // $NON-NLS-0$
                config.remove("bid"); // $NON-NLS-0$
            }
        }
        ackParametersChanged();

        if (this.type != null && getConfiguration().getBoolean("periodForChartcenter", false)) { // $NON-NLS-0$
            ChartcenterSnippet.PERIODS.put(this.type, period);
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk()) {
            if (event.addVwdcode(this.ipr)) {
                event.addComponentToReload(this.block, this);
            }
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.priceSupport.isNewerPriceAvailable(this.ipr.getQuotedata())) {
            getView().push(this.ipr);
        }
    }
}

