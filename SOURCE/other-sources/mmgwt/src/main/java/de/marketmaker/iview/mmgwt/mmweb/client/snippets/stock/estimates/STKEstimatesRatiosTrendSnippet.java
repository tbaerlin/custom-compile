/*
 * STKEstimatesTrendSnippet.java
 *
 * Created on 17.09.2008 13:14:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.estimates;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.EstimatesField;
import de.marketmaker.iview.dmxml.EstimatesFields;
import de.marketmaker.iview.dmxml.MSCCrossRate;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.dmxml.STKEstimates;
import de.marketmaker.iview.dmxml.STKHistoricEstimates;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PriceWithCurrency;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.math.BigDecimal;
import java.util.ArrayList;

import static java.math.MathContext.DECIMAL32;

/**
 * @author Ulrich Maurer
 */
public class STKEstimatesRatiosTrendSnippet extends
        AbstractSnippet<STKEstimatesRatiosTrendSnippet, STKEstimatesRatiosTrendSnippetView> implements
        PushRegisterHandler, SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("STKEstimatesRatiosTrend", I18n.I.ratiosTrend()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new STKEstimatesRatiosTrendSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("width", DEFAULT_SNIPPET_WIDTH); // $NON-NLS$
        }
    }

    private DmxmlContext.Block<STKEstimates> blockEstimates;

    private DmxmlContext.Block<STKHistoricEstimates> blockHistoricEstimates;

    private DmxmlContext.Block<MSCPriceData> blockPriceData;

    private DmxmlContext.Block<MSCCrossRate> blockCrossRateCurrency;

    private MSCPriceData mscPriceData;

    private PriceWithCurrency crossRate;

    private STKEstimates stkEstimates;

    private STKHistoricEstimates stkHistoricEstimates;

    private Price pushedPrice = Price.NULL_PRICE;

    private PriceSupport priceSupport = new PriceSupport(this);

    protected STKEstimatesRatiosTrendSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);

        this.blockEstimates = context.addBlock("STK_Estimates"); // $NON-NLS$
        this.blockEstimates.setParameters("year", new String[]{"fy0", "fy1", "fy2"}); // $NON-NLS$
        this.blockHistoricEstimates = context.addBlock("STK_HistoricEstimates"); // $NON-NLS$
        this.blockPriceData = context.addBlock("MSC_PriceData"); // $NON-NLS$
        this.blockPriceData.setParameter("marketStrategy", configuration.getString("marketStrategy", null)); // $NON-NLS$

        this.blockCrossRateCurrency = new DmxmlContext().addBlock("MSC_CrossRate"); // $NON-NLS$
        this.blockCrossRateCurrency.setParameter("betrag", "1"); // $NON-NLS$

        this.setView(new STKEstimatesRatiosTrendSnippetView(this));
    }

    @Override
    public void destroy() {
        destroyBlock(this.blockEstimates);
        destroyBlock(this.blockHistoricEstimates);
        destroyBlock(this.blockPriceData);
        destroyBlock(this.blockCrossRateCurrency);
        this.clearAll();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
        this.clearAll();
    }

    private void clearAll() {
        this.pushedPrice = Price.NULL_PRICE;
        this.mscPriceData = null;
        this.stkEstimates = null;
        this.stkHistoricEstimates = null;
    }

    @Override
    public void onPricesUpdated(PricesUpdatedEvent event) {
        this.pushedPrice = Price.create(this.mscPriceData);
        if (!Price.NULL_PRICE.equals(this.pushedPrice)) {
            onPush();
        }
    }

    @Override
    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.mscPriceData != null) {
            event.addVwdcode(this.mscPriceData);
        }
        return null;
    }

    @Override
    public void updateView() {
        this.getView().invalidate();

        if (!isReady(this.blockPriceData, this.blockEstimates, this.blockHistoricEstimates)) {
            return;
        }

        this.mscPriceData = this.blockPriceData.getResult();
        this.stkEstimates = this.blockEstimates.getResult();
        this.stkHistoricEstimates = this.blockHistoricEstimates.getResult();

        // Safe now since MSC_PriceData has returned, which provides the vwdcode for the push handler
        this.priceSupport.activate();

        String priceCurrency = mscPriceData.getQuotedata().getCurrencyIso();

        final EstimatesFields estimatesFields = getYear(stkEstimates);
        final String estimatesCurrency = estimatesFields.getCurrency();

        this.crossRate = null;
        if (!priceCurrency.equals(estimatesCurrency)) {
            this.blockCrossRateCurrency.setParameter("isocodeFrom", priceCurrency); // $NON-NLS$
            this.blockCrossRateCurrency.setParameter("isocodeTo", estimatesCurrency); // $NON-NLS$
            this.blockCrossRateCurrency.issueRequest(new AsyncCallback<ResponseType>() {
                @Override
                public void onFailure(Throwable throwable) {
                    Firebug.error("STKEstimatesRatiosTrendSnippet: MSC_CrossRate callback failed!", throwable);
                }

                @Override
                public void onSuccess(ResponseType responseType) {
                    if (blockCrossRateCurrency.isResponseOk()) {
                        crossRate = new PriceWithCurrency(blockCrossRateCurrency.getResult().getResult(), estimatesCurrency);
                        onPush();
                    }
                }
            });
            return;
        }
        onPush();
    }

    private void onPush() {
        // All three needed for the update of the view, except MSC_CrossRates is not mandatory
        if (!isReady(this.mscPriceData, this.stkEstimates, this.stkHistoricEstimates)) {
            getView().invalidate();
            return;
        }

        final EstimatesFields estimatesFields = getYear(this.stkEstimates);
        final EstimatesField priceTarget = estimatesFields.getPriceTarget();
        final String currency = estimatesFields.getCurrency();

        if (priceTarget == null || currency == null) {
            getView().invalidate();
            return;
        }

        final PriceWithCurrency targetPrice = new PriceWithCurrency(priceTarget.getValue(), currency);
        final BigDecimal targetChange = calculateTargetChange(priceTarget);

        getView().update(this.stkHistoricEstimates, estimatesFields, targetPrice, targetChange, this.crossRate);
    }

    private boolean isReady(DmxmlContext.Block... blocks) {
        for (DmxmlContext.Block block : blocks) {
            if (block == null || !block.isResponseOk() || block.getResult() == null) {
                return false;
            }
        }
        return true;
    }

    private boolean isReady(Object... objects) {
        for (Object o : objects) {
            if (o == null)
                return false;
        }
        return true;
    }

    private BigDecimal calculateTargetChange(final EstimatesField priceTarget) {
        final BigDecimal currentPrice;
        if (Price.NULL_PRICE.equals(this.pushedPrice) || !isReady(this.pushedPrice)) {
            currentPrice = new BigDecimal(this.mscPriceData.getPricedata().getPrice());
        } else {
            currentPrice = new BigDecimal(this.pushedPrice.getLastPrice().getPrice());
            this.pushedPrice = Price.NULL_PRICE; // invalidate
        }

        final BigDecimal targetPrice = new BigDecimal(priceTarget.getValue());
        if (this.crossRate == null) {
            return (targetPrice.divide(currentPrice, DECIMAL32)).subtract(BigDecimal.ONE);
        } else {
            final BigDecimal crossRate = new BigDecimal(this.crossRate.getPrice());
            return (targetPrice.divide(currentPrice.multiply(crossRate), DECIMAL32)).subtract(BigDecimal.ONE);
        }
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.blockEstimates.setParameter("symbol", symbol); // $NON-NLS$
        this.blockHistoricEstimates.setParameter("symbol", symbol); // $NON-NLS$
        this.blockPriceData.setParameter("symbol", symbol); // $NON-NLS$
    }

    private EstimatesFields getYear(STKEstimates stkEstimates) {
        EstimatesFields fy1 = null;
        for (EstimatesFields ef : stkEstimates.getFields()) {
            if ("fy1".equals(ef.getYear())) { // $NON-NLS$
                fy1 = ef;
            }
            if ("fy2".equals(ef.getYear())) { // $NON-NLS$
                return ef;
            }
        }
        return fy1;
    }
}
