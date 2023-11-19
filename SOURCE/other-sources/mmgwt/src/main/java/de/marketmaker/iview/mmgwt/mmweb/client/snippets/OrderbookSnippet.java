/*
 * PriceListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.dmxml.MSCOrderbook;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ExtendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Orderbook;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NumberUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OrderbookSnippet extends AbstractSnippet<OrderbookSnippet, OrderbookSnippetView>
        implements SymbolSnippet, PushRegisterHandler {

    public static class Class extends SnippetClass {
        public Class() {
            super("Orderbook", I18n.I.orderBook()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new OrderbookSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("colSpan", 3); // $NON-NLS$
        }
    }

    private PriceSupport priceSupport = new PriceSupport(this);

    private DmxmlContext.Block<MSCOrderbook> block;

    private OrderbookSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        setView(new OrderbookSnippetView(this));
        getView().withPriceSupport(this.priceSupport);
        this.block = createBlock("MSC_Orderbook"); // $NON-NLS$
        onParametersChanged();
    }

    public void destroy() {
        deactivate();
        destroyBlock(this.block);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.block.isResponseOk() && event.addVwdcode(this.block.getResult())) {
            event.addComponentToReload(this.block, this);
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.block.isResponseOk()
                && this.priceSupport.isNewerOrderbookAvailable(this.block.getResult())) {
            updateView();
        }
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS$
    }

    public boolean isConfigurable() {
        return true;
    }

    public void configure(Widget triggerWidget) {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addSelectSymbol(null);
        configView.show();
    }

    protected void onParametersChanged() {
        this.block.setParameter("symbol", getConfiguration().getString("symbol")); // $NON-NLS$
    }

    public void setTitle(String title) {
        this.getView().setTitle(title);
    }

    private void onSymbolChange(final String title, final String symbol) {
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

    public void updateView() {
        this.priceSupport.invalidateRenderItems();
        if (!this.block.isResponseOk()) {
            if (getConfiguration().getString("symbol") == null) { // $NON-NLS-0$
                getView().setMessage(I18n.I.messagePleaseConfigureInstrument(), true);
                return;
            }

            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final MSCOrderbook data = this.block.getResult();
        final DefaultTableDataModel dtm = getTableDataModel(Orderbook.getFor(data));
        getView().update(dtm);
        this.priceSupport.activate();
    }

    private DefaultTableDataModel getTableDataModel(final Orderbook orderbook) {
        final int max = Math.max(orderbook.getAskSize(), orderbook.getBidSize());
        if (max == 0) {
            return DefaultTableDataModel.NULL;
        }
        final NumberUtil.Max maxVolume = new NumberUtil.Max(0d);
        final DefaultTableDataModel result = new DefaultTableDataModel(max, 7);
        for (int row = 0; row < max; row++) {
            if (orderbook.getBidSize() > row) {
                final Orderbook.Item item = orderbook.getBidItem(row);
                result.setValueAt(row, 2, item);
                result.setValueAt(row, 1, item);
                result.setValueAt(row, 0, new ExtendBarData(maxVolume.add(item.getVolume())));
            }
            result.setValueAt(row, 3, getRowLabel(row));
            if (orderbook.getAskSize() > row) {
                final Orderbook.Item item = orderbook.getAskItem(row);
                result.setValueAt(row, 4, item);
                result.setValueAt(row, 5, item);
                result.setValueAt(row, 6, new ExtendBarData(maxVolume.add(item.getVolume())));
            }
        }
        finishExtendBarDatas(result, maxVolume.getResult());

        return result;
    }

    private void finishExtendBarDatas(DefaultTableDataModel dtm, double maxVolume) {
        for (int row = 0; row < dtm.getRowCount(); row++) {
            finish((ExtendBarData) dtm.getValueAt(row, 0), maxVolume);
            finish((ExtendBarData) dtm.getValueAt(row, 6), maxVolume);
        }
    }

    private void finish(ExtendBarData data, double maxVolume) {
        if (data != null) {
            data.setMaxValue(maxVolume);
        }
    }

    private String getRowLabel(int row) {
        if (row >= 4 && row <= 20) {
            return I18n.I.nThBestNth(row);
        }
        else if (row == 1) {
            return I18n.I.nThBest1();
        }
        else if (row == 2) {
            return I18n.I.nThBest2();
        }
        else if (row == 3) {
            return I18n.I.nThBest3();
        }
        return I18n.I.nThBestNdot(row);
    }
}
