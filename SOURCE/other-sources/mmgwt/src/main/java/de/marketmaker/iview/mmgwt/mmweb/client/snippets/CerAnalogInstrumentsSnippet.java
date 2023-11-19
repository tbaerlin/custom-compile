package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.CERAnalogInstruments;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiAndPricedata;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created on 10.08.11 09:28
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class CerAnalogInstrumentsSnippet extends AbstractMiniPortraitsSnippet<CERAnalogInstruments> {

    static final int INSTRUMENT_COUNT = 5;

    public static class Class extends SnippetClass {
        public Class() {
            super("CerAnalogInstruments"); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new CerAnalogInstrumentsSnippet(context, config);
        }
    }

    public CerAnalogInstrumentsSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
    }

    protected void updateView(CERAnalogInstruments result) {
        if (result == null) {
            update(null);
        }
        else if (getView().getViewMode() == MiniPortraitsSnippetView.ViewMode.CHART) {
            issueChartContext(new ChartBlocksCallback() {
                @Override
                void onSuccess(List<IMGResult> chartResults) {
                    update(chartResults);
                }
            });
        } else {
            final List<QwiAndPricedata> qwiAndPricedatas = getQwiAndPricedatas();
            update(new String[qwiAndPricedatas.size()], null, qwiAndPricedatas);
        }
    }

    @Override
    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        final String symbol = config.getString("symbol"); // $NON-NLS$
        this.block.setParameter("symbol", symbol); // $NON-NLS$
    }

    @Override
    protected void initBlock() {
        this.block = createBlock("CER_AnalogInstruments"); // $NON-NLS$
        this.block.disableRefreshOnRequest();
        this.block.setParameter("issuername", GuiDefsLoader.getIssuerName()); // $NON-NLS$
    }

    @Override
    protected MiniPortraitsSnippetView<CERAnalogInstruments> createView() {
        return new MiniPortraitsSnippetView<CERAnalogInstruments>(this);
    }

    @Override
    protected int getInstrumentMaxCount() {
        return INSTRUMENT_COUNT;
    }

    @Override
    protected List<QwiAndPricedata> getQwiAndPricedatas() {
        if (!this.block.isResponseOk()) {
            return Collections.emptyList();
        }
        List<MSCPriceData> elements = this.block.getResult().getElement();
        ArrayList<QwiAndPricedata> result = new ArrayList<QwiAndPricedata>(elements.size());
        for (MSCPriceData element : elements) {
            if (element.getInstrumentdata() != null) {
                result.add(new QwiAndPricedata(element));
            }
        }
        return result;
    }
}