/*
 * OrderPresenterFuchsbriefe.java
 *
 * Created on 28.10.13 09:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log.LogBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.BrokerageModuleID;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.LookupSecurityDataResponse;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptor;

import java.util.List;

/**
 * @author Markus Dick
 */
class OrderPresenterFuchsbriefe
        extends AbstractOrderPresenter<OrderPresenterFuchsbriefe, OrderSessionFeaturesDescriptor, OrderSession.OrderSessionFuchsbriefe>
        implements DisplayFuchsbriefe.PresenterFuchsbriefe {

    private final DisplayFuchsbriefe display;

    OrderPresenterFuchsbriefe(DisplayFuchsbriefe<DisplayFuchsbriefe.PresenterFuchsbriefe> display, OrderSession.OrderSessionFuchsbriefe orderSession) {
        super(display, orderSession, "Infront Advisory Solution Order Entry Fuchsbriefe", false, new ParameterMapProcessorFuchsbriefe(display)); //$NON-NLS$
        this.display = display;
        validateDisplay();
    }

    @Override
    protected void validateDisplay() {
        this.display.isValid();
    }

    @Override
    protected DisplayAbstract getDisplay() {
        return this.display;
    }

    @Override
    protected void validateOrder() {
        boolean valid = this.display.isValid();

        if(!valid) {
            OrderMethods.INSTANCE.showValidationErrorMessage(SafeHtmlUtils.fromTrustedString(I18n.I.orderEntryFormValidationErrors()));
            return;
        }

        validateOrder(createOrderDataType());
    }

    @Override
    protected List<OrderConfirmationDisplay.Section> createVerificationModel(OrderDataType order) {
        final LogBuilder log = new LogBuilder("OrderPresenterFuchsbriefe.createVerificationModel"); //$NON-NLS$

        final OrderConfirmationModelBuilderFuchsbriefe builder = createOrderConfirmationBaseModel(order, log);

        return builder.getSections();
    }

    @Override
    protected List<OrderConfirmationDisplay.Section> createAckOfReceiptModel(OrderDataType order) {
        final LogBuilder log = new LogBuilder("OrderPresenterFuchsbriefe.createAckOfReceiptModel"); //$NON-NLS$

        OrderConfirmationModelBuilderFuchsbriefe builder = createOrderConfirmationBaseModel(order, log);
        builder.addOrderSuccessfullyCreated();

        return builder.getSections();
    }

    private OrderConfirmationModelBuilderFuchsbriefe createOrderConfirmationBaseModel(OrderDataType order, LogBuilder log) {
        final OrderConfirmationModelBuilderFuchsbriefe builder = new OrderConfirmationModelBuilderFuchsbriefe();
        final LookupSecurityInfo security = getCurrentSecurityInfo();

        builder.addTransactionSection(order);
        builder.addPortfolioSection(order, log);
        builder.addSecuritySection(order, log);
        builder.addExchangeAndCurrencySection(order, security, log);
        builder.addOrderSection(order);

        return builder;
    }

    private OrderDataType createOrderDataType() {
        final OrderDataType order = new OrderDataType();
        final OrderSession sessionRaw = getCurrentOrderSession();
        final OrderSession.OrderSessionFuchsbriefe session = (OrderSession.OrderSessionFuchsbriefe)sessionRaw;

        final OrderLogBuilder log = new OrderLogBuilder("OrderPresenterFuchsbriefe.createOrderDataType", //$NON-NLS$
                getCurrentOrderSession());

        final BrokerageModuleID brokerageModuleID = session.getBrokerageModuleID();
        if(!BrokerageModuleID.BM_FUCHSBRIEFE.equals(brokerageModuleID)) {
            final String message = "<OrderPresenterFuchsbriefe.createOrderDataType> brokerage module of session does not correspond to presenter!"; //$NON-NLS$
            Firebug.debug(message);
            DebugUtil.logToServer(message);
            throw new IllegalStateException(message);
        }
        order.setBM(session.getBrokerageModuleID());
        log.addEnum("order.bm", session.getBrokerageModuleID()); //$NON-NLS$

        final String activityInstanceId = getActivityInstanceId();
        order.setActivityId(activityInstanceId);
        log.addActivityInstanceId(order, activityInstanceId);

        final String activityListEntryId = getActivityListEntryId();
        order.setOrderSuggestionRef(activityListEntryId);
        log.addActivityListEntryId(order, activityListEntryId);

        final OrderActionType orderActionType = getCurrentOrderAction().getValue();
        order.setTyp(OrderUtils.toOrderTransactionType(orderActionType));
        log.addOrderTransactionType(order, orderActionType);

        order.setIsShort(false);
        log.addIsShort(order);

        order.setInhaberData(session.getOwner());
        log.addInvestor(order, session);

        order.setDepotData(session.getAccountInfo().getSecurityAccount());
        log.addDepot(order);

        final AccountData accountData = session.getAccountList().get(this.display.getAccountsSelectedItem());
        order.setKontoData(accountData);
        log.addAccountData(order, accountData);

        order.setLagerstelleData(null);
        log.addLagerstelle(order);

        final OrderSecurityInfo orderSecurityInfo = getCurrentSecurityInfo().getSecurity();
        order.setSecurityData(orderSecurityInfo);
        log.addOrderSecurityInfo(order, orderSecurityInfo);

        final OrderExchangeInfo orderExchangeInfo = getCurrentSecurityInfo().getExchangeList().get(this.display.getSelectedExchangeChoice());
        order.setExchangeData(orderExchangeInfo);
        log.addExchangeInfo(order, orderExchangeInfo);

        final double quantity = NumberFormat.getDecimalFormat().parse(this.display.getQuantity());
        order.setQuantity(Double.toString(quantity));
        log.add("order.quantity", order.getQuantity()); //$NON-NLS$
        log.add("display.quantity", this.display.getQuantity()); //$NON-NLS$

        final CurrencyAnnotated exchangeCurrency = getCurrentSecurityInfo().getCurrencyList().get(this.display.getSelectedExchangeCurrency());
        order.setOrderCurrencyData(exchangeCurrency.getCurrency());
        log.addOrderCurrencyData(order, exchangeCurrency);

        DebugUtil.logToServer(log.toString());

        return order;
    }

    @Override
    public void onExchangeCurrencyChangedHandler(ChangeEvent event) {
        super.onExchangeCurrencyChangedHandler(event);
        updateExpectedMarketValue();
    }

    @Override
    public void onExchangeChangedHandler(ChangeEvent event) {
        super.onExchangeChangedHandler(event);
        updateExpectedMarketValue();
    }

    @Override
    protected void updateExpectedMarketValue() {
        final NumberFormat nf = NumberFormat.getDecimalFormat();
        final LookupSecurityInfo securityInfo = getCurrentSecurityInfo();
        final int currentQuoteIndex = getCurrentQuoteIndex();

        Firebug.info("<updateExpectedMarketValue> securityInfo == null?" + (securityInfo == null) + " currentQuoteIndex=" + currentQuoteIndex);

        if(securityInfo == null || currentQuoteIndex < 0) {
            this.display.setExpectedMarketValue("");
            return;
        }

        final String amountStr = this.display.getQuantity();
        if(!this.display.isValid()) {
            Firebug.warn("<OrderPresenterFuchsbriefe.updateExpectedMarketValue> amount field not valid.");
            this.display.setExpectedMarketValue("");
            return;
        }

        final double amount;
        final double price;
        final double conversionFactor;

        try {
            amount = nf.parse(OeRenderers.UNAMBIGUOUS_NUMBER_RENDERER.render(amountStr));
            price = Double.parseDouble(securityInfo.getQuoteList().get(currentQuoteIndex).getValue());
            conversionFactor = Double.parseDouble(securityInfo.getSecurity().getKursfaktor());
        }
        catch(Exception e) {
            Firebug.warn("<OrderPresenterFuchsbriefe.updateExpectedMarketValue> Converting numbers failed", e);
            this.display.setExpectedMarketValue("");
            return;
        }

        final double value = OrderUtils.calculateExpectedMarketValue(amount, price, conversionFactor);
        if(Double.isInfinite(value)) {
            OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromSafeConstant(I18n.I.orderEntryNoConversionFactor()));
            this.display.setExpectedMarketValue("");
            return;
        }

        this.display.setExpectedMarketValue(Renderer.PRICE23.render(Double.toString(value)));
    }

    @Override
    public void onQuantityValueChanged(ValueChangeEvent<String> event) {
        updateExpectedMarketValue();
    }

    @Override
    public void onQuantityFieldKeyUp(KeyUpEvent event) {
        updateExpectedMarketValue();
    }

    @Override
    public void onValidation(ValidationEvent event) {
        getOrderViewContainerDisplay().setExecuteOrderButtonEnabled(event.isValid());
    }

    @Override
    protected void doOnLookupSecuritySuccess(LookupSecurityDataResponse result, LookupSecurityConfig config) {
        super.doOnLookupSecuritySuccess(result, config);
        if(LookupSecurityConfig.PROCESS_PARAMETER_MAP != config) {
            resetOrderSection();
        }
    }

    @Override
    protected void doOnLookupSecurityFailure(String isin, LookupSecurityConfig config, Throwable caught) {
        super.doOnLookupSecurityFailure(isin, config, caught);
        if(LookupSecurityConfig.PROCESS_PARAMETER_MAP != config) {
            resetOrderSection();
        }
    }

    @Override
    protected void initDisplay() {
        super.initDisplay();
        resetOrderSection();
    }

    private void resetOrderSection() {
        this.display.setQuantity("");
        this.display.setExpectedMarketValue("");
        validateDisplay();
    }
}