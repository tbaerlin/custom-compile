/*
 * OrderPresenterHA.java
 *
 * Created on 30.10.12 08:53
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.history.OrderEntryHistorySupport;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidatorGroup;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log.LogBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.BrokerageModuleID;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.LookupSecurityDataResponse;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderExpirationType;
import de.marketmaker.iview.pmxml.OrderLimitType;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptor;
import de.marketmaker.iview.pmxml.OrderSingleQuote;
import de.marketmaker.iview.pmxml.PmxmlConstants;

import java.util.Date;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderConfirmationDisplay.*;
import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderMethods.INSTANCE;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
class OrderPresenterHA extends AbstractOrderPresenter<OrderPresenterHA, OrderSessionFeaturesDescriptor, OrderSession.OrderSessionHA>
        implements DisplayHA.PresenterHA {
//    private static final String HISTORY_AMOUNT = "amount"; //$NON-NLS$
    private static final String HISTORY_LIMIT = "limit"; //$NON-NLS$
    private static final String HISTORY_STOP_BUY_MARKET = "stopBuyMarket"; //$NON-NLS$
    private static final String HISTORY_STOP_LOSS_MARKET = "stopLossMarket"; //$NON-NLS$
    private static final String HISTORY_VALID_UNTIL_CHOICE_NAME = "validUntilChoice.name"; //$NON-NLS$
    private static final String HISTORY_VALID_UNTIL_CHOICE_DATE_TIME = "validUntilChoiceDate.time"; //$NON-NLS$

    private final DisplayHA display;
    private boolean limitManuallyChecked = false;
    private boolean limitChoiceChecked = false;

    OrderPresenterHA(DisplayHA<DisplayHA.PresenterHA> view, OrderSession.OrderSessionHA orderSession) {
        super(view, orderSession, I18n.I.orderEntryHAWindowTitle(), new ParameterMapProcessorHA(view));
        this.display = view;

        final OrderEntryHistorySupport hs = getHistorySupport();
        hs.addProcessStep(new OrderEntryHistorySupport.ProcessStep() {
            @Override
            public void process(OrderEntryHistorySupport.Item historyItem) {
                processHaHistoryItem(historyItem);
            }
        });

        if(orderSession == null) throw new IllegalArgumentException("OrderSession must not be null!"); // $NON-NLS$
    }

    @Override
    public void onLimitFocus(FocusEvent event) {
        if(!this.limitManuallyChecked) {
            checkLimitChoice(true);
        }
    }

    @Override
    public void onLimitBlur(BlurEvent event) {
        if(!this.display.isMouseOverLimitCheck() && !this.limitManuallyChecked && !StringUtil.hasText(this.display.getLimit())) {
            checkLimitChoice(false);
        }
    }

    @Override
    public void onLimitCheckValueChange(ValueChangeEvent<Boolean> event) {
        final boolean checked = event.getValue();

        this.limitManuallyChecked = checked;
        this.limitChoiceChecked = checked;
        limitChoiceEnabled(checked);
    }

    @Override
    public void onValidUntilRadioGroupValueChanged(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
        initValidUntilChoiceDate();
    }

    private void initValidUntilChoiceDate() {
        final DisplayHA d = this.display;

        if(d.getValidUntilChoice() == DisplayHA.ValidUntilChoice.DATE) {
            d.setValidUntilChoiceDate(new MmJsDate());
        }
        else {
            d.setValidUntilChoiceDate(null);
        }
        d.setValidUntilChoice(d.getValidUntilChoice());
    }

    private void changeValidUntilChoice(DisplayHA.ValidUntilChoice choice) {
        if(!this.display.getValidUntilChoice().equals(choice)) {
            this.display.setValidUntilChoice(choice);
            initValidUntilChoiceDate();
        }
    }

    @Override
    public void onOrderActionChanged(ChangeEvent event) {
        super.onOrderActionChanged(event);
        updateStopChoice(this.display.getSelectedOrderAction());
    }

    @Override
    public void onValidationEventJoint(ValidationEvent event) {
        getOrderViewContainerDisplay().setExecuteOrderButtonEnabled(this.getCurrentSecurityInfo() != null && !this.getCurrentSecurityInfo().getQuoteList().isEmpty() && event.isValid());
    }

    @Override
    public void onValidDateClick(ClickEvent event) {
        changeValidUntilChoice(DisplayHA.ValidUntilChoice.DATE);
    }

    @Override
    public void onAmountKeyUp(KeyUpEvent event) {
        updateExpectedMarketValue();
    }

    @Override
    public void onAmountValueChange(ValueChangeEvent<String> event) {
        updateExpectedMarketValue();
    }

    @Override
    public void onExchangeChangedHandler(ChangeEvent event) {
        /* do nothing */
    }

    @Override
    public void onExchangeCurrencyChangedHandler(ChangeEvent event) {
        super.onExchangeCurrencyChangedHandler(event);
        updateExpectedMarketValue();
    }

    @Override
    protected OrderSingleQuote findQuoteForExchangeAndExchangeCurrency(OrderExchangeInfo exchange, CurrencyAnnotated currencyAnnotated) {
        if(currencyAnnotated == null || currencyAnnotated.getCurrency() == null) {
            Firebug.debug("<OrderPresenterHA.findQuoteForExchangeAndExchangeCurrency> no quote found: currencyAnnotated or currencyAnnotated.currency are null!");
            return null;
        }

        final OrderCurrency currency = currencyAnnotated.getCurrency();

        final List<OrderSingleQuote> quotes = getCurrentSecurityInfo().getQuoteList();
        for (int i = 0; i < quotes.size(); i++) {
            Firebug.debug("Quote " + i + " value=" + quotes.get(i).getValue());
            final OrderSingleQuote quote = quotes.get(i);
            if (currency.getId().equals(quote.getCurrency().getId())) {
                setCurrentQuoteIndex(i);
                return quote;
            }
        }
        setCurrentQuoteIndex(-1);

        //TODO: improve i18n for null value
        final String escapedCurrencyParam = SafeHtmlUtils.htmlEscape(currency.getKuerzel());
        final String message = I18n.I.orderEntryNoPriceForExchangeCurrency(escapedCurrencyParam);
        INSTANCE.showFailureMessage(SafeHtmlUtils.fromTrustedString(message));
        return null;
    }

    protected void updateExpectedMarketValue() {
        final NumberFormat nf = NumberFormat.getDecimalFormat();
        final LookupSecurityInfo securityInfo = getCurrentSecurityInfo();
        final int currentQuoteIndex = getCurrentQuoteIndex();

        if(securityInfo == null || currentQuoteIndex < 0) {
            this.display.setExpectedMarketValue("");
            return;
        }

        final String amountStr = this.display.getAmount();
        if(!this.display.getAmountFieldValidator().isValid()) {
            Firebug.log("<OrderPresenterHA.updateExpectedMarketValue> amount field not valid.");
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
            Firebug.error("<OrderPresenterHA.updateExpectedMarketValue> Converting numbers failed", e);
            this.display.setExpectedMarketValue("");
            return;
        }

        final double value = OrderUtils.calculateExpectedMarketValue(amount, price, conversionFactor);
        if(Double.isInfinite(value)) {
            OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromSafeConstant(I18n.I.orderEntryNoConversionFactor()));
            this.display.setExpectedMarketValue("");
            return;
        }

        displayExpectedMarketValue(value);
    }

    private void displayExpectedMarketValue(double value) {
        final String valueStr = Renderer.PRICE23.render(Double.toString(value));
        this.display.setExpectedMarketValue(valueStr);
    }

    @Override
    protected void validateOrder() {
        ValidatorGroup vg = this.display.getValidatorGroup();
        boolean valid = vg.isValid();

        if(!valid) {
            OrderMethods.INSTANCE.showValidationErrorMessage(SafeHtmlUtils.fromTrustedString(I18n.I.orderEntryFormValidationErrors()));
            return;
        }

        validateOrder(createOrderDataType());
    }

    @Override
    protected List<Section> createVerificationModel(OrderDataType order) {
        final LogBuilder log = new LogBuilder("OrderPresenterHA.createVerificationModel"); //$NON-NLS$

        OrderConfirmationModelBuilderHA builder = createOrderConfirmationBaseModel(order, log);

        builder.addHAHintSection(0);

        return builder.getSections();
    }

    @Override
    protected List<Section> createAckOfReceiptModel(OrderDataType order) {
        final LogBuilder log = new LogBuilder("OrderPresenterHA.createAckOfReceiptModel"); //$NON-NLS$

        OrderConfirmationModelBuilderHA builder = createOrderConfirmationBaseModel(order, log);
        builder.addOrderNumberSection(0, order);

        return builder.getSections();
    }

    @Override
    protected void validateDisplay() {
        this.display.getValidatorGroup().isValid();
    }

    private OrderConfirmationModelBuilderHA createOrderConfirmationBaseModel(OrderDataType order, LogBuilder log) {
        final OrderConfirmationModelBuilderHA builder = new OrderConfirmationModelBuilderHA();
        final LookupSecurityInfo security = getCurrentSecurityInfo();

        builder.addTransactionSection(order);
        builder.addPortfolioSection(order, log);
        builder.addSecuritySection(order, log);
        builder.addOrderSection(order, security, log);

        return builder;
    }

    private OrderDataType createOrderDataType() {
        final OrderDataType order = new OrderDataType();
        final OrderSession.OrderSessionHA session = getCurrentOrderSession();

        final OrderLogBuilder log = new OrderLogBuilder("OrderPresenterHA.createOrderDataType", //$NON-NLS$
                getCurrentOrderSession());

        final BrokerageModuleID brokerageModuleID = session.getBrokerageModuleID();
        if(!BrokerageModuleID.BM_HA.equals(brokerageModuleID)) {
            final String message = "<OrderPresenterHA.createOrderDataType> brokerage module of session does not correspond to presenter!"; //$NON-NLS$
            Firebug.log(message);
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

        final double quantity = NumberFormat.getDecimalFormat().parse(this.display.getAmount());
        order.setQuantity(Double.toString(quantity));
        log.add("order.quantity", order.getQuantity()); //$NON-NLS$
        log.add("display.amount", this.display.getAmount()); //$NON-NLS$

        final CurrencyAnnotated exchangeCurrency = getCurrentSecurityInfo().getCurrencyList().get(this.display.getSelectedExchangeCurrency());
        order.setOrderCurrencyData(exchangeCurrency.getCurrency());
        log.addOrderCurrencyData(order, exchangeCurrency);

        switch (this.display.getValidUntilChoice()) {
            case DATE:
                final Date date = this.display.getValidUntilChoiceDate().atMidnight().getJavaDate();
                order.setExpirationType(OrderExpirationType.OET_DATE);
                order.setExpirationDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(date));
                break;
            case ULTIMO:
                order.setExpirationType(OrderExpirationType.OET_ULTIMO);
                order.setExpirationDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(PmxmlConstants.ZERO_DATE));
                break;
            case GOOD_FOR_THE_DAY:
            default:
                order.setExpirationType(OrderExpirationType.OET_DAY);
                order.setExpirationDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(PmxmlConstants.ZERO_DATE));
        }
        log.addEnum("order.expirationType", order.getExpirationType()); //$NON-NLS$
        log.add("order.expirationDate", order.getExpirationDate()); //$NON-NLS$
        log.addEnum("display.validUntilChoice", this.display.getValidUntilChoice()); //$NON-NLS$
        log.add("display.validUntilChoice.gmtString", this.display.getValidUntilChoiceDate()); //$NON-NLS$

        final boolean limitFlag = this.display.isLimitChecked();
        final boolean stopFlag;
        if(OrderActionType.AT_BUY.equals(getCurrentOrderAction().getValue())) {
            stopFlag = this.display.isStopBuyMarketChecked();
        }
        else {
            stopFlag = this.display.isStopLossMarketChecked();
        }

        if(!limitFlag && !stopFlag) {
            order.setLimitType(OrderLimitType.OLT_MARKET);
            order.setLimit(null);
        }
        else if(!limitFlag) {
            order.setLimitType(OrderLimitType.OLT_STOP);
            order.setLimit(null);
        }
        else if(StringUtil.hasText(this.display.getLimit())) {
            if(!stopFlag) {
                order.setLimitType(OrderLimitType.OLT_LIMIT);
            }
            else {
                order.setLimitType(OrderLimitType.OLT_STOP_LIMIT);
            }

            final double limit = NumberFormat.getDecimalFormat().parse(this.display.getLimit());
            order.setLimit(Double.toString(limit));
        }
        else {
            final String message = "<OrderPresenterHA.createOrderDataType> undefined limit state reached!"; //$NON-NLS$
            Firebug.log(message);
            DebugUtil.logToServer(message);
            throw new IllegalStateException(message);
        }

        log.addEnum("order.limitType", order.getLimitType()); //$NON-NLS$
        log.add("order.limit", order.getLimit()); //$NON-NLS$
        log.add("display.limitChecked", this.display.isLimitChecked()); //$NON-NLS$
        log.add("display.stopBuyMarketChecked", this.display.isStopBuyMarketChecked()); //$NON-NLS$
        log.add("display.stopLossMarketChecked", this.display.isStopLossMarketChecked()); //$NON-NLS$
        log.add("limitFlag", limitFlag); //$NON-NLS$
        log.add("stopFlag", stopFlag); //$NON-NLS$
        log.add("display.limit", this.display.getLimit()); //$NON-NLS$

        DebugUtil.logToServer(log.toString());

        return order;
    }

    protected void checkLimitChoice(boolean checked) {
        if(this.limitChoiceChecked != checked) {
            this.limitChoiceChecked = checked;
            this.display.setLimitChecked(checked);
            limitChoiceEnabled(checked);
        }
    }

    private void limitChoiceEnabled(boolean value) {
        final DisplayHA d = this.display;

        if (value) {
            d.setValidUntilChoiceEnabled(
                    DisplayHA.ValidUntilChoice.DATE,
                    DisplayHA.ValidUntilChoice.ULTIMO
            );
            if(!(DisplayHA.ValidUntilChoice.DATE == d.getValidUntilChoice())) {
                d.setValidUntilChoice(DisplayHA.ValidUntilChoice.ULTIMO);
                d.setValidUntilChoiceDate(null);
            }
            d.setLimit("");
            d.setLimitPseudoEnabled(true);
        }
        else {
            d.setValidUntilChoiceEnabled(
                    DisplayHA.ValidUntilChoice.GOOD_FOR_THE_DAY,
                    DisplayHA.ValidUntilChoice.DATE
            );
            if(!(DisplayHA.ValidUntilChoice.DATE == d.getValidUntilChoice())) {
                d.setValidUntilChoice(DisplayHA.ValidUntilChoice.GOOD_FOR_THE_DAY);
                d.setValidUntilChoiceDate(null);
            }
            d.setLimitPseudoEnabled(false);
            d.setLimit("");
        }
    }

    private boolean isValidUntilChoiceAllowed(DisplayHA.ValidUntilChoice choice) {
        if(DisplayHA.ValidUntilChoice.DATE == choice) {
            return true;
        }

        if(this.limitChoiceChecked) {
            if(DisplayHA.ValidUntilChoice.ULTIMO == choice) {
                return true;
            }
        }
        else {
            if(DisplayHA.ValidUntilChoice.GOOD_FOR_THE_DAY == choice) {
                return true;
            }
        }

        return false;
    }

    protected void initDisplay() {
        Firebug.log("<OrderPresenterHA.initDisplay>");
        super.initDisplay();
        resetHaSpecifics();
        updateStopChoice(getCurrentOrderAction());
    }

    private void updateStopChoice(OrderAction orderAction) {
        Firebug.log("<OrderPresenterHA.updateStopChoice> orderAction=" + orderAction.getValue());
        this.display.setStopBuyMarketChecked(false);
        this.display.setStopLossMarketChecked(false);

        switch(orderAction.getValue()) {
            case AT_BUY:
                this.display.setShowStopChoice(DisplayHA.ShowStopChoice.STOP_BUY);
                break;
            case AT_SELL:
                this.display.setShowStopChoice(DisplayHA.ShowStopChoice.STOP_LOSS);
                break;
        }
    }

    @Override
    protected void doOnLookupSecuritySuccess(LookupSecurityDataResponse result, LookupSecurityConfig config) {
        super.doOnLookupSecuritySuccess(result, config);
        if(LookupSecurityConfig.PROCESS_PARAMETER_MAP == config) {
            validateDisplay();
        }
        else {
            resetHaSpecifics();
        }
    }

    @Override
    protected void doOnLookupSecurityFailure(String isin, LookupSecurityConfig config, Throwable caught) {
        super.doOnLookupSecurityFailure(isin, config, caught);
        if(LookupSecurityConfig.PROCESS_PARAMETER_MAP == config) {
            validateDisplay();
        }
        else {
            resetHaSpecifics();
        }
    }

    private void resetHaDisplay() {
        Firebug.log("<OrderPresenterHA.resetHaDisplay>");

        this.display.getAmountFieldValidator().clear();
        this.display.getLimitFieldValidator().clear();

        this.display.setAmount("");
        this.display.setLimitChecked(false);
        this.display.setLimitPseudoEnabled(false);
        this.display.setLimit("");
        this.display.setStopBuyMarketChecked(false);
        this.display.setStopLossMarketChecked(false);
        this.display.setExpectedMarketValue("");
        this.display.setValidUntilChoiceEnabled(DisplayHA.ValidUntilChoice.GOOD_FOR_THE_DAY, DisplayHA.ValidUntilChoice.DATE);
        this.display.setValidUntilChoice(DisplayHA.ValidUntilChoice.GOOD_FOR_THE_DAY);
        this.display.setValidUntilChoiceDate(null);

        validateDisplay();
    }

    private void resetHaPresenterState() {
        Firebug.log("<OrderPresenterHA.resetHaPresenterState>");

        this.limitManuallyChecked = false;
        this.limitChoiceChecked = false;
    }

    private void resetHaSpecifics() {
        Firebug.log("<OrderPresenterHA.resetHaSpecifics>");

        resetHaPresenterState();
        resetHaDisplay();
    }

    @Override
    protected OrderEntryHistorySupport.Item createOrderEntryHistoryItem() {
        OrderEntryHistorySupport.Item item = super.createOrderEntryHistoryItem();
        if(item == null) {
            item = new OrderEntryHistorySupport.Item();
        }

        final String oldLabel = item.getLabel();
        final StringBuilder label;
        if(StringUtil.hasText(oldLabel)) {
            label = new StringBuilder(oldLabel);
//            label.append(", "); //$NON-NLS$
        }
        else {
            label = new StringBuilder();
        }

//        final String amount = this.display.getAmount();
//        item.put(HISTORY_AMOUNT, amount);
//        label.append(I18n.I.orderEntryAmountNominal()).append(" "); //$NON-NLS$
//        label.append(amount);

        final String limit = this.display.getLimit();
        if(this.limitChoiceChecked && StringUtil.hasText(limit)) {
            item.put(HISTORY_LIMIT, limit);

            CurrencyAnnotated ca = getCurrentSecurityInfo().getCurrencyList().get(this.display.getSelectedExchangeCurrency());
            if(label.length() > 0) {
                label.append(", ");
            }
            label.append(I18n.I.orderEntryLimit()).append(" "); //$NON-NLS$
            label.append(limit).append(" ").append(ca.getCurrency().getKuerzel()); //$NON-NLS$
        }

        final OrderActionType orderActionType = getCurrentOrderAction().getValue();

        if(OrderActionType.AT_BUY.equals(orderActionType)) {
            final boolean stopBuyMarket = this.display.isStopBuyMarketChecked();
            item.put(HISTORY_STOP_BUY_MARKET, Boolean.toString(stopBuyMarket));
            if(stopBuyMarket) {
                if(label.length() > 0) {
                    label.append(", "); //$NON-NLS$
                }
                label.append(I18n.I.orderEntryStopBuyMarket());
            }
        }

        if(OrderActionType.AT_SELL.equals(orderActionType)) {
            final boolean stopLossMarket = this.display.isStopLossMarketChecked();
            item.put(HISTORY_STOP_LOSS_MARKET, Boolean.toString(stopLossMarket));
            if(stopLossMarket) {
                if(label.length() > 0) {
                    label.append(", "); //$NON-NLS$
                }
                label.append(I18n.I.orderEntryStopLossMarket());
            }
        }

        final DisplayHA.ValidUntilChoice validUntilChoice = this.display.getValidUntilChoice();
        item.put(HISTORY_VALID_UNTIL_CHOICE_NAME, validUntilChoice.name());
        if(label.length() > 0) {
            label.append(", "); //$NON-NLS$
        }
        label.append(I18n.I.orderEntryValidity()).append(": ");  //$NON-NLS$

        if(!DisplayHA.ValidUntilChoice.DATE.equals(validUntilChoice)) {
            label.append(OeRenderers.HA_VALID_UNTIL_CHOICE_RENDERER.render(validUntilChoice));
        }
        else {
            final MmJsDate date = this.display.getValidUntilChoiceDate().atMidnight();
            item.put(HISTORY_VALID_UNTIL_CHOICE_DATE_TIME, Long.toString(date.getTime()));

            label.append(Formatter.LF.formatDate(date.getJavaDate()));
        }

        item.setLabel(label.toString());

        return item;
    }

    private void processHaHistoryItem(OrderEntryHistorySupport.Item item) {
        resetHaSpecifics();

//        final String amount = item.get(HISTORY_AMOUNT);
//        if(StringUtil.hasText(amount)) {
//            this.display.setAmount(amount);
//            this.display.getAmountFieldValidator().formatAndValidate();
//            updateExpectedMarketValue();
//        }

        final String limit = item.get(HISTORY_LIMIT);
        if(StringUtil.hasText(limit)) {
            checkLimitChoice(true);
            this.display.setLimit(limit);
            this.display.getLimitFieldValidator().formatAndValidate();
        }

        final OrderActionType orderActionType = this.display.getSelectedOrderAction().getValue();
        if(OrderActionType.AT_BUY.equals(orderActionType)) {
            final String stopBuyMarket = item.get(HISTORY_STOP_BUY_MARKET);
            if(StringUtil.hasText(stopBuyMarket)) {
                this.display.setStopBuyMarketChecked(Boolean.parseBoolean(stopBuyMarket));
            }
        }

        if(OrderActionType.AT_SELL.equals(orderActionType)) {
            final String stopLossMarket = item.get(HISTORY_STOP_LOSS_MARKET);
            if(StringUtil.hasText(stopLossMarket)) {
                this.display.setStopLossMarketChecked(Boolean.parseBoolean(stopLossMarket));
            }
        }

        final String validUntilChoiceName = item.get(HISTORY_VALID_UNTIL_CHOICE_NAME);
        if(StringUtil.hasText(validUntilChoiceName)) {
            try {
                final DisplayHA.ValidUntilChoice validUntilChoice = DisplayHA.ValidUntilChoice.valueOf(validUntilChoiceName);
                if(isValidUntilChoiceAllowed(validUntilChoice)) {
                    changeValidUntilChoice(validUntilChoice);

                    if(DisplayHA.ValidUntilChoice.DATE.equals(validUntilChoice)) {
                        final String validUntilChoiceDateTime = item.get(HISTORY_VALID_UNTIL_CHOICE_DATE_TIME);
                        if(StringUtil.hasText(validUntilChoiceDateTime)) {
                            try {
                                final MmJsDate date = new MmJsDate(Long.parseLong(validUntilChoiceDateTime));
                                this.display.setValidUntilChoiceDate(date);
                            }
                            catch(NumberFormatException e) {
                                Firebug.error("Date of ValidUntilChoice.DATE not valid", e);
                            }
                        }
                    }
                }
            }
            catch(IllegalArgumentException e) {
                Firebug.error("ValidUntilChoice of history item not valid", e);
            }
        }
    }
}