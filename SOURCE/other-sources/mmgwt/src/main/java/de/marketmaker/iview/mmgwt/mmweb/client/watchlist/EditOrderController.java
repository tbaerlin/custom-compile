package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import de.marketmaker.iview.dmxml.MSCCrossRate;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.PFOrder;
import de.marketmaker.iview.dmxml.PortfolioElement;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiPosition;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NumberUtil;

import java.util.Date;
import java.util.HashMap;

/**
 * OrderEditController
 * <p>
 * Created on 01.10.2008 10:38:01
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

class EditOrderController implements ConfigurableSnippet {

    public enum Operation {
        BUY(I18n.I.addOrder(), I18n.I.executeBuyOrder()),
        SELL(I18n.I.addOrder(), I18n.I.executeSellOrder()),
        EDIT(I18n.I.changeOrderInDepot(), I18n.I.changeOrder());

        private final String title;

        private final String action;

        Operation(String title, String action) {
            this.title = title;
            this.action = action;
        }

        String getTitle() {
            return this.title;
        }

        public String getAction() {
            return action;
        }
    }

    private DmxmlContext.Block<MSCPriceDatas> priceBlock;

    private final DmxmlContext.Block<MSCCrossRate> crossRateBlock;

    private final HashMap<String, String> params = new HashMap<>();

    private final Operation operation;

    private final PortfolioElement element;

    private final PFOrder order;

    private String symbol;

    private final QwiPosition position;

    private boolean hasInvalidResponse = false;

    private boolean sellFromOtherMarkets = false;

    public static EditOrderController sell(final QwiPosition position, PortfolioElement element, boolean sellFromOtherMarkets) {
        final PFOrder order = new PFOrder();
        order.setOrdertype(Operation.SELL.name());
        return new EditOrderController(Operation.SELL, position, position.getQuoteData().getQid(), order, element, sellFromOtherMarkets);
    }

    public static EditOrderController buy(String symbol, PortfolioElement element) {
        final PFOrder order = new PFOrder();
        order.setOrdertype(Operation.BUY.name());
        return new EditOrderController(Operation.BUY, symbol, order, element);
    }

    public static EditOrderController edit(String symbol, PFOrder order, PortfolioElement element) {
        return new EditOrderController(Operation.EDIT, symbol, order, element);
    }

    private EditOrderController(Operation operation, String symbol, PFOrder order,
                                PortfolioElement element) {
        this(operation, null, symbol, order, element, false);
    }

    private EditOrderController(Operation operation, QwiPosition position, String symbol, PFOrder order,
                                PortfolioElement element, boolean sellFromOtherMarkets) {
        this.operation = operation;
        this.order = order;
        this.element = element;
        this.position = position;
        this.sellFromOtherMarkets = sellFromOtherMarkets;

        this.priceBlock = new DmxmlContext().addBlock("MSC_PriceDatas"); // $NON-NLS-0$
        this.priceBlock.setParameter("disablePaging", "true"); // $NON-NLS-0$ $NON-NLS-1$
        if (symbol != null) {
            this.priceBlock.setParameter("symbol", symbol); // $NON-NLS-0$
            issueRequest();
        }
        else {
            openSearchDialog();
        }
        this.crossRateBlock = new DmxmlContext().addBlock("MSC_CrossRate"); // $NON-NLS-0$
    }

    Operation getOperation() {
        return this.operation;
    }

    PortfolioElement getElement() {
        return this.element;
    }

    private void issueRequest() {
        this.priceBlock.issueRequest(new ResponseTypeCallback() {
            protected void onResult() {
                onPrice();
            }
        });
    }

    protected void onPrice() {
        if (!this.priceBlock.isResponseOk()) {
            onInvalidResponse();
            return;
        }
        if (this.operation == Operation.EDIT) {
            updateView(priceBlock.getResult(), Formatter.parseISODate(this.order.getDate()), this.order);
        }
        else {
            updateView(priceBlock.getResult(), new Date());
        }
    }

    private void onInvalidResponse() {
        if (this.operation == Operation.SELL) {
            this.hasInvalidResponse = true;
            EditOrderView.show(this, this.position, new Date(), order.getPrice(), order.getAmount(), order.getCharge());
        }
    }

    private void openSearchDialog() {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addSelectSymbol(null);
        configView.show();
    }

    private void updateView(MSCPriceDatas priceDatas, Date date) {
        EditOrderView.show(this, priceDatas, date, null, null, null);
    }

    private void updateView(MSCPriceDatas priceDatas, Date date, PFOrder order) {
        EditOrderView.show(this, priceDatas, date, order.getPrice(), order.getAmount(), order.getCharge());
    }

    public HashMap<String, String> getCopyOfParameters() {
        return this.params;
    }

    public void setParameters(HashMap<String, String> params) {
        final QuoteWithInstrument qwi = QuoteWithInstrument.getLastSelected();
        if (qwi != null) {
            this.priceBlock.setParameter("symbol", qwi.getId()); // $NON-NLS-0$
            issueRequest();
        }
    }

    public void onEditDone(String symbol, String amount, String price, String currency, String charge, Date date) {
        this.symbol = symbol;
        this.order.setAmount(toDoubleString(amount));
        this.order.setPrice(toDoubleString(price));
        if (charge != null) {
            this.order.setCharge(toDoubleString(charge));
        }
        this.order.setDate(Formatter.formatDateAsISODate(date));

        if (this.element.getPortfolioCurrency().equals(currency)) {
            this.order.setExchangeRate("1"); // $NON-NLS-0$
        }
        else {
            this.order.setExchangeRate(String.valueOf(getCrossRate()));
        }
        doEditAction();
    }

    private String toDoubleString(String value) {
        return (value != null) ? NumberUtil.toDoubleString(value) : null;
    }

    public void requestCrossrate(String currency, ResponseTypeCallback callback) {
        this.crossRateBlock.setParameter("isocodeFrom", currency); // $NON-NLS-0$
        this.crossRateBlock.setParameter("isocodeTo", this.element.getPortfolioCurrency()); // $NON-NLS-0$
        this.crossRateBlock.issueRequest(callback);
    }

    private void doEditAction() {
        if (this.order.getOrderid() != null) {
            PortfolioActions.INSTANCE.updateOrder(this.element.getPortfolioid(), this.order);
        }
        else {
            PortfolioActions.INSTANCE.createOrder(this.element.getPortfolioid(), this.symbol, this.order, sellFromOtherMarkets);
        }
    }

    public double getCrossRate() {
        if (this.crossRateBlock.isResponseOk()) {
            final MSCCrossRate crossRate = this.crossRateBlock.getResult();
            return Integer.parseInt(crossRate.getFactor()) * Double.parseDouble(crossRate.getRate());
        }
        else {
            if (hasInvalidResponse) {
                return 1;
            }
            DebugUtil.logToServer(getClass().getSimpleName() + " <getCrossRate> MSCCrossRate.(state = " // $NON-NLS$
                    + this.crossRateBlock.getState() + ", isocodeFrom = " + this.crossRateBlock.getParameter("isocodeFrom") // $NON-NLS$
                    + ", isocodeTo = " + this.crossRateBlock.getParameter("isocodeTo") + ")"); // $NON-NLS$
            AbstractMainController.INSTANCE.showError(I18n.I.internalError());
            return 0;
        }
    }

    public String calcCharge(String price, String amount, String exchangeRate) {
        return String.valueOf(Charge.getInstance().computeCharge(toDoubleString(price),
                toDoubleString(amount), Double.valueOf(exchangeRate)));
    }

    public String calcBidAskMidRate(final Price p, String exchangeRate) {
        try {
            return String.valueOf((Double.valueOf(p.getBid()) + Double.valueOf(p.getAsk())) / 2 * Double.valueOf(exchangeRate));
        } catch (NumberFormatException e) {
            DebugUtil.logToServer(getClass().getSimpleName() + " <updatePriceForms> bid ask middle rate failed");
            return null;
        }
    }
}
