/*
 * PriceEarningsChartSnippet.java
 *
 * Created on 19.02.2009 13:34:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.HashMap;
import java.util.List;

/**
 * @author Ulrich Maurer
 */
public class PriceEarningsChartSnippet extends
        BasicChartSnippet<PriceEarningsChartSnippet, PriceEarningsChartSnippetView>
        implements SymbolSnippet, SymbolListSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("PriceEarningsChart"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PriceEarningsChartSnippet(context, config);
        }
    }

    private DmxmlContext.Block<MSCQuoteMetadata> blockQuoteMetadata = null;

    private PriceEarningsChartSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        if (getConfiguration().getString("period", null) == null // $NON-NLS-0$
                && getConfiguration().getString("start", null) == null) { // $NON-NLS-0$
            getConfiguration().put("period", "P1Y"); // $NON-NLS-0$ $NON-NLS-1$
        }

        this.blockQuoteMetadata = createBlock("MSC_QuoteMetadata"); // $NON-NLS-0$

        this.setView(new PriceEarningsChartSnippetView(this));

        onParametersChanged();
    }

    public void configure(Widget triggerWidget) {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addSelectSymbol(null);
        configView.show();
    }

    public void destroy() {
        super.destroy();
        destroyBlock(this.blockQuoteMetadata);
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
        DebugUtil.logToServer("PriceEarningsChartSnippet '" + title + "', '" + symbol + "'"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
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

    public void setSymbols(List<InstrumentData> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            setVisible(false);
        }
        else {
            setVisible(true);
            final InstrumentData id = symbols.get(0);
            setSymbol(InstrumentTypeEnum.valueOf(id.getType()), id.getIid(), null);
        }
    }

    public void setVisible(boolean value) {
        if ((getView() != null) && (getView().container != null)) {
            getView().container.setVisible(value);
        }
    }

    public void updateView() {
        if (!this.block.isResponseOk()
                || !this.blockQuoteMetadata.isResponseOk()
                || this.blockQuoteMetadata.getResult().getKgvQuote() == null
                ) {
            getView().showMessage(I18n.I.chartNotAvailable());
            return;
        }

        final IMGResult imgResult = this.block.getResult();
        final IdentifierData peQuote = this.blockQuoteMetadata.getResult().getKgvQuote();
        final String url = imgResult.getRequest();
        final String result =
                url.replaceFirst("symbol=(\\d+).qid", "symbol=" + peQuote.getQuotedata().getQid()); // $NON-NLS-0$ $NON-NLS-1$

        imgResult.setRequest(result);
        getView().update(imgResult);
    }

    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        final String symbol = config.getString("symbol", null); // $NON-NLS-0$
        this.block.setEnabled(symbol != null);
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
        this.block.setParameter("period", config.getString("period", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("from", config.getString("from", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("to", config.getString("to", null)); // $NON-NLS-0$ $NON-NLS-1$
        this.blockQuoteMetadata.setParameter("symbol", symbol); // $NON-NLS-0$
    }


    void updateQuote(String qid) {
        // called whenever a different market has been selected, no need to enable blockQuoteMetadata
        getConfiguration().put("symbol", qid); // $NON-NLS-0$
        ackParametersChanged();
    }
}
