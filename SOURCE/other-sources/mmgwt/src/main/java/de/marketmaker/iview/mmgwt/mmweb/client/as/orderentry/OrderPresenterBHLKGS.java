/*
 * OrderPresenterBHLKGS.java
 *
 * Created on 10.07.13 12:43
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log.LogBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ParameterMap;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.search.SelectIpoSymbolControllerBHLKGS;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.search.SelectIpoSymbolFormBHLKGS;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PendingRequestsEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PendingRequestsHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SearchMethods;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SelectPmSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.ConfigurationPresenter;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.TabbedSnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SymbolUtil;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.AccountRef;
import de.marketmaker.iview.pmxml.AuthorizedRepresentative;
import de.marketmaker.iview.pmxml.BrokerageModuleID;
import de.marketmaker.iview.pmxml.ChangeOrderDataResponse;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.ExternExchangeType;
import de.marketmaker.iview.pmxml.LookupSecurityDataResponse;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderDataTypeBHL;
import de.marketmaker.iview.pmxml.OrderDepository;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderExpirationType;
import de.marketmaker.iview.pmxml.OrderLock;
import de.marketmaker.iview.pmxml.OrderSecurityFeatureDescriptor;
import de.marketmaker.iview.pmxml.OrderSecurityFeatureDescriptorBHL;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptorBHL;
import de.marketmaker.iview.pmxml.OrderStock;
import de.marketmaker.iview.pmxml.OrderTransaktionType;
import de.marketmaker.iview.pmxml.ProvisionType;
import de.marketmaker.iview.pmxml.SendOrderDataResponse;
import de.marketmaker.iview.pmxml.SessionState;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.TextType;
import de.marketmaker.iview.pmxml.TextWithKey;
import de.marketmaker.iview.pmxml.TextWithTyp;
import de.marketmaker.iview.pmxml.ValidateChangeOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidateOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidationMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderMethods.INSTANCE;

/**
 * @author Markus Dick
 */
public class OrderPresenterBHLKGS
        extends AbstractOrderViewContainerPresenter
        implements DisplayBHLKGS.PresenterBHLKGS, OrderMethods.OrderValidationCallback<OrderDataTypeBHL>,
        ConfigurableSnippet, LoadDepotsAndInvestorsMethod.DepotsCallback, PendingRequestsHandler,
        HasReturnParameterMap, PresenterDisposedHandler, IsActivityAware {

    public enum LookupSecurityConfig { DEFAULT, PROCESS_PARAMETER_MAP }

    private static final String ZERO = "0"; //$NON-NLS$
    /*
     * NOTE: MMBroking delivers "0" although TABEX table defines "00" as the key of the empty element.
     * The key is defined as CHAR(2) and not as NUMOVZ. So it is semantically wrong to convert it to an integer number.
     */
    private static final HashSet<String> EMPTY_LIMIT_CLAUSE_KEYS = new HashSet<>(Arrays.asList("00", "0")); //$NON-NLS$
    public static final String ENABLES_AUTHORIZED_REPRESENTATIVES_FLAG = "A"; //$NON-NLS$

    private final String CLASS_NAME = this.getClass().getName();
    private final String SIMPLE_CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf('.') + 1);
    private final String LOG_PREFIX = "<" + SIMPLE_CLASS_NAME; //$NON-NLS$

    private final String windowTitle;
    private final DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> display;
    protected final OrderSession.OrderSessionBHLKGS orderSession;
    protected final OrderStrategy orderStrategy;

    private HandlerRegistration loadingIndicatorHandlerRegistration;

    private OrderActionType currentOrderActionType;
    private List<ValidationMessage> lookupSecurityValidationMessages = null;
    private LookupSecurityInfo securityInfo = null;
    private OrderSecurityFeatureDescriptorBHL securityFeatureDescriptor = null;

    private final Set<String> limitOptionsKeysThatEnableLimitAfterStopLimit = new HashSet<>();

    private final ConfigurationPresenter selectInstrumentView;
    private final ConfigurationPresenter selectIpoInstrumentView;
    private final SelectIpoSymbolControllerBHLKGS selectIpoSymbolController;
    private final OrderValidationMessagePresenter orderValidationMessagePresenter;
    private final ArbitragePresenter arbitragePresenter;

    private OrderEntryContext orderEntryContext;
    private final ParameterMap returnParameterMap;
    protected final ParameterMapProcessorBHLKGS parameterMapProcessor;

    private String activityInstanceId;
    private String activityListEntryId;

    OrderPresenterBHLKGS(DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> display, OrderSession.OrderSessionBHLKGS orderSession, OrderStrategy orderStrategy) {
        super(new OrderViewContainerView(I18n.I.orderEntryBHLKGSWindowTitle()));

        this.orderStrategy = orderStrategy;
        this.orderStrategy.setPresenter(this);

        this.parameterMapProcessor = new ParameterMapProcessorBHLKGS(display);
        this.parameterMapProcessor.setPresenter(this);

        final OrderViewContainerDisplay containerDisplay = this.getOrderViewContainerDisplay();
        containerDisplay.setPresenter(this);
        containerDisplay.setExecuteOrderButtonText(I18n.I.orderEntryBHLKGSValidateOrder());

        this.display = display;
        this.display.setPresenter(this);

        this.windowTitle = getOrderViewContainerDisplay().getTitle();
        this.orderSession = orderSession;

        this.returnParameterMap = new ParameterMap(this.orderSession.getSecurityAccount().getId());

        if(OrderUtils.isWithDmXmlSymbolSearch()) {
            this.selectInstrumentView = new TabbedSnippetConfigurationView(this, SnippetConfigurationView.SymbolParameterType.ISIN);
        }
        else {
            this.selectInstrumentView = new SnippetConfigurationView(this, SnippetConfigurationView.SymbolParameterType.ISIN);
        }

        this.selectIpoInstrumentView = new SnippetConfigurationView(this, SnippetConfigurationView.SymbolParameterType.ISIN);
        this.selectIpoSymbolController = new SelectIpoSymbolControllerBHLKGS();

        this.orderValidationMessagePresenter = new OrderValidationMessagePresenter(this.orderSession, new OrderValidationMessageView());

        this.arbitragePresenter = new ArbitragePresenter();

        init();
    }

    private void init() {
        initSelectInstrumentView();
        initSelectIpoInstrumentView();

        //set feature specific defaults
        final DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> display = this.display;
        final OrderSessionFeaturesDescriptorBHL features = this.orderSession.getFeatures();

        for(TextWithKey limitOption : features.getLimitRequiredByLimitOption()) {
            this.limitOptionsKeysThatEnableLimitAfterStopLimit.add(limitOption.getKey());
        }

        display.setExchangesDomestic(Collections.<OrderExchangeInfo>emptyList());
        display.setExchangesForeign(Collections.<OrderExchangeInfo>emptyList());
        display.setExchangesOther(Collections.<OrderExchangeInfo>emptyList());

        final List<TextWithKey> tradingIndicators = features.getTradingIndicator();
        display.setTradingIndicators(tradingIndicators);
        display.setTradingIndicatorsSelectedItem(OrderUtils.findDefaultTextWithKey(tradingIndicators, true));

        final List<TextWithKey> orderers = features.getOrderer();
        display.setOrderers(orderers);
        display.setOrderersSelectedItem(null);
        setOrdererNameAndCustomerNumberEnabled(false);

        final List<TextWithKey> minutesOfTheConsultationTypes = features.getMinutesOfTheConsultation();
        display.setMinutesOfTheConsultationTypes(minutesOfTheConsultationTypes);
        final TextWithKey minutesOfTheConsultationTypeToSelect = OrderUtils.findDefaultTextWithKey(minutesOfTheConsultationTypes, true);
        display.setMinutesOfTheConsultationTypesSelectedItem(minutesOfTheConsultationTypeToSelect);

        final List<TextWithKey> settlementTypes = features.getSettlementTypes();
        display.setSettlementTypes(settlementTypes);
        display.setSettlementTypesSelectedItem(OrderUtils.findDefaultTextWithKey(settlementTypes, true));

        final List<TextWithKey> businessSegments = features.getBusinessSegments();
        display.setBusinessSegments(businessSegments);
        display.setBusinessSegmentsSelectedItem(OrderUtils.findDefaultTextWithKey(settlementTypes, true));
        display.setBusinessSegmentsEnabled(true);

        final List<TextWithKey> orderPlacementVias = features.getOrderPlacementVia();
        display.setPlacingOfOrderVias(orderPlacementVias);
        display.setPlacingOfOrderViasSelectedItem(OrderUtils.findDefaultTextWithKey(orderPlacementVias, true));

        final List<TextWithKey> cannedTextForBillingReceipts = features.getTextLibrariesBillingDocument();
        display.setCannedTextForBillingReceipts1(cannedTextForBillingReceipts);
        display.setCannedTextForBillingReceipts2(cannedTextForBillingReceipts);
        display.setCannedTextForBillingReceipts3(cannedTextForBillingReceipts);
        display.setCannedTextForOrderConfirmations(features.getTextLibrariesOrderConfirmation());

        MmJsDate date = new MmJsDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.parse(features.getAuftragsDateTime()));
        display.setContractDateTime(date);

        //set commission
        display.setCommission(Arrays.asList(ProvisionType.values()));
        final ProvisionType defaultCommissionType = ProvisionType.PT_NO_DIFFERING_PROVISION;
        display.setCommissionSelectedItem(defaultCommissionType);
        setDifferentCommissionEnabled(defaultCommissionType);

        //set static defaults
        display.setValidUntil(DisplayBHLKGS.ValidUntil.DEFAULT);
        display.setLimitOrStopLimit(null);

        this.display.setLimitClauses(this.orderSession.getFeatures().getLimitOptions());
        resetLimitClausesSelectedItemToDefault();
        display.setLimitCurrenciesValidatorEnabled(false);
        display.setLimitFee(true);

        setInstrumentPanelsVisibility(null);

        //disable all fields that can hold valid values only after successful security lookup
        display.setInstrumentDependingValuesEnabled(false);
        display.resetAmountNominalMaxValue();
    }

    private void resetLimitClausesSelectedItemToDefault() {
        final OrderSessionFeaturesDescriptorBHL features = this.orderSession.getFeatures();
        final TextWithKey defaultLimitOption = OrderUtils.findDefaultTextWithKey(features.getLimitOptions(), true);
        setLimitClausesSelectedItem(defaultLimitOption);
        setLimitNotEmptyValidatorEnabledDependingOnCurrencyAndLimitClause(null, defaultLimitOption);
    }

    private void initSelectInstrumentView() {
        final Set<ShellMMType> tradableSecurityTypes = this.orderSession.getTradableSecurityTypes();

        if(OrderUtils.isWithDmXmlSymbolSearch()) {
            final SelectPmSymbolForm dmxmlSymbolForm = SelectPmSymbolForm.createDmWithOrderEntryAvail(
                    this.selectInstrumentView.getParams(),
                    tradableSecurityTypes);
            dmxmlSymbolForm.setHeaderVisible(false);
            dmxmlSymbolForm.setFooter(false);
            this.selectInstrumentView.addConfigurationWidget(dmxmlSymbolForm, IconImage.get("mm-icon-16"), I18n.I.orderEntryInstrumentSearchMmTab()); //$NON-NLS$
        }

        final SelectPmSymbolForm pmxmlSymbolForm = SelectPmSymbolForm.createPmWithOrderEntryAvail(
                this.selectInstrumentView.getParams(),
                tradableSecurityTypes);
        pmxmlSymbolForm.setHeaderVisible(false);
        pmxmlSymbolForm.setFooter(false);
        this.selectInstrumentView.addConfigurationWidget(pmxmlSymbolForm, IconImage.get("pm-icon-16"), I18n.I.orderEntryInstrumentSearchPmTab()); //$NON-NLS$

        this.selectInstrumentView.addActionPerformedHandler(new ActionPerformedHandler() {
            @Override
            public void onAction(ActionPerformedEvent event) {
                if (ConfigurationPresenter.Actions.CANCEL.name().equals(event.getKey())) {
                    onSymbolSearchDialogCanceled();
                }
            }
        });
    }

    private void initSelectIpoInstrumentView() {
        final SelectIpoSymbolFormBHLKGS selectIpoSymbolFormBHLKGS = new SelectIpoSymbolFormBHLKGS(
                this.selectIpoInstrumentView.getParams(), this.selectIpoSymbolController);

        selectIpoSymbolFormBHLKGS.setHeaderVisible(false);
        selectIpoSymbolFormBHLKGS.setFooter(false);
        this.selectIpoInstrumentView.addConfigurationWidget(selectIpoSymbolFormBHLKGS, null,
                I18n.I.orderEntryBHLKGSActiveIPOinWKN());

        this.selectIpoInstrumentView.addActionPerformedHandler(new ActionPerformedHandler() {
            @Override
            public void onAction(ActionPerformedEvent event) {
                if (ConfigurationPresenter.Actions.CANCEL.name().equals(event.getKey())) {
                    onSymbolSearchDialogCanceled();
                }
            }
        });
    }

    private void setOrdererNameAndCustomerNumberEnabled(boolean ordererNameAndCustomerNumberEnabled) {
        this.display.setOrdererIdentifierEnabled(ordererNameAndCustomerNumberEnabled);
        this.display.setOrdererCustomerNumberEnabled(ordererNameAndCustomerNumberEnabled);
    }

    private void fillOrderExchangeChoices(LookupSecurityInfo lookupSecurityInfo) {
        final List<OrderExchangeInfo> exchangesDomestic = new ArrayList<>();
        final List<OrderExchangeInfo> exchangesForeign = new ArrayList<>();
        final List<OrderExchangeInfo> exchangesOther = new ArrayList<>();

        OrderExchangeInfo defaultExchange = null;

        for(OrderExchangeInfo oei : lookupSecurityInfo.getExchangeList()) {
            switch(oei.getExternExchangeTyp()) {
                case EET_DOMESTIC:
                    exchangesDomestic.add(oei);
                    break;
                case EET_FOREIGN:
                    exchangesForeign.add(oei);
                    break;
                case EET_OTHER:
                default:
                    exchangesOther.add(oei);
            }
            if(oei.isIsDefault()) {
                defaultExchange = oei;
            }
        }

        this.display.setExchangesDomestic(exchangesDomestic);
        this.display.setExchangesForeign(exchangesForeign);
        this.display.setExchangesOther(exchangesOther);

        //set exchange selected item (applies also for subscribe/IPOs)
        setExchangesSelectedItem(defaultExchange);

        //revalidate
        validateDisplay();
    }

    @Override
    public void setActivityInstanceId(String id) {
        this.activityInstanceId = id;
    }

    @Override
    public String getActivityInstanceId() {
        return this.activityInstanceId;
    }

    @Override
    public void setActivityListEntryId(String id) {
        this.activityListEntryId = id;
    }

    @Override
    public String getActivityListEntryId() {
        return this.activityListEntryId;
    }

    private void setLimitClausesSelectedItem(TextWithKey selectedItem) {
        this.display.setLimitClausesSelectedItem(selectedItem);
        setLimitAfterStopLimitEnabledDependingOnLimitClause(selectedItem);
    }

    private void setLimitNotEmptyValidatorEnabledDependingOnCurrencyAndLimitClause(CurrencyAnnotated selectedCurrency, TextWithKey selectedLimitClause) {
        final boolean enabled = selectedCurrency != null
                || selectedLimitClause != null
                && this.limitOptionsKeysThatEnableLimitAfterStopLimit.contains(selectedLimitClause.getKey());

        this.display.setLimitOrStopLimitNotEmptyValidatorEnabled(enabled);
    }

    private void setLimitAfterStopLimitEnabledDependingOnLimitClause(TextWithKey limitClauseItem) {
        final boolean enabled = (limitClauseItem != null) && !EMPTY_LIMIT_CLAUSE_KEYS.contains(limitClauseItem.getKey());

        this.display.setLimitTrailingAmountOrPercentEnabled(enabled);
        this.display.setLimitPeakSizeQuantityEnabled(enabled);

        if(!enabled) {
            this.display.setLimitTrailingAmountOrPercent(null);
            this.display.setLimitPeakSizeQuantity(null);
        }

        final boolean enableLimitAfterStopLimit = enabled && this.limitOptionsKeysThatEnableLimitAfterStopLimit.contains(limitClauseItem.getKey());
        this.display.setLimitAfterStopLimitEnabled(enableLimitAfterStopLimit);
        if(!enableLimitAfterStopLimit) {
            this.display.setLimitAfterStopLimit(null);
        }
    }

    private void setDifferentCommissionEnabled(ProvisionType type) {
        final boolean enabled = !(ProvisionType.PT_NONE == type || ProvisionType.PT_NO_DIFFERING_PROVISION == type);
        this.display.setDifferentCommissionEnabled(enabled);
        if(!enabled) {
            this.display.setDifferentCommission(null);
        }
    }

    protected void validateOrder() {
        if(!this.display.getValidatorGroup().isValid()) {
            getOrderViewContainerDisplay().setButtonsLocked(false);
            OrderMethods.INSTANCE.showValidationErrorMessage(SafeHtmlUtils.fromTrustedString(I18n.I.orderEntryFormValidationErrors()));
            return;
        }

        try {
            validateOrder(createOrderDataType());
        }
        catch(Exception e) {
            getOrderViewContainerDisplay().setButtonsLocked(false);
            final String raw = SafeHtmlUtils.htmlEscape(e.getMessage());
            final SafeHtml message = SafeHtmlUtils.fromTrustedString(I18n.I.orderEntryFailedToCreateOrderDataType(raw));
            OrderMethods.INSTANCE.showFailureMessage(message);
        }
    }

    protected OrderDataType createOrderDataType() {
        final OrderDataTypeBHL order = new OrderDataTypeBHL();
        final OrderSession.OrderSessionBHLKGS session = this.orderSession;

        final OrderLogBuilder log = new OrderLogBuilder("OrderPresenterBHLKGS.createOrderDataType", this.orderSession); //$NON-NLS$

        final BrokerageModuleID brokerageModuleID = session.getBrokerageModuleID();
        if(!BrokerageModuleID.BM_BHLKGS.equals(brokerageModuleID)) {
            final String message = "<OrderPresenterBHLKGS.createOrderDataType> brokerage module of session does not correspond to presenter!"; //$NON-NLS$
            Firebug.log(message);
            DebugUtil.logToServer(message);
            throw new IllegalStateException(message);
        }
        order.setBM(session.getBrokerageModuleID());
        log.addEnum("order.bm", session.getBrokerageModuleID()); //$NON-NLS$

        this.orderStrategy.initOrderDataType(order, log);

        final OrderTransaktionType orderTransactionType = this.orderStrategy.getOrderTransactionType();
        assertNotNull(orderTransactionType, "orderTransactionType"); //$NON-NLS$
        order.setTyp(orderTransactionType);
        log.addEnum("order.typ", order.getTyp()); //$NON-NLS$
        log.addEnum(this.orderStrategy.toLogStrategyRootNodeName("orderTransactionType"), orderTransactionType); //$NON-NLS$

        order.setInhaberData(session.getOwner());
        log.addInvestor(order, session);

        order.setDepotData(session.getAccountInfo().getSecurityAccount());
        log.addDepot(order);

        final AccountData accountData = this.display.getAccountsSelecedItem();
        assertNotNull(accountData, "accountsSelecedItem"); //$NON-NLS$
        order.setKontoData(accountData);
        log.addAccountData(order, accountData);

        final OrderCurrency currencyOfOrder = accountData.getCurrency();
        assertNotNull(currencyOfOrder, "selectedAccount.currency|order.orderCurrencyData"); //$NON-NLS$
        order.setOrderCurrencyData(currencyOfOrder);
        log.addExternMMDBRef("order.orderCurrency", order.getOrderCurrencyData()); //$NON-NLS$
        log.addCurrency("display.selectedAccountChoice", currencyOfOrder); //$NON-NLS$

        final OrderSecurityInfo orderSecurityInfo = this.securityInfo.getSecurity();
        order.setSecurityData(orderSecurityInfo);
        log.addOrderSecurityInfo(order, orderSecurityInfo);

        OrderExchangeInfo orderExchangeInfo = this.display.getExchangesDomesticSelectedItem();
        String fieldNameForLog = "exchangesDomesticSelectedItem"; //$NON-NLS$
        if(orderExchangeInfo == null) {
            orderExchangeInfo = this.display.getExchangesForeignSelectedItem();
            fieldNameForLog = "exchangesForeignSelectedItem"; //$NON-NLS$
        }
        if(orderExchangeInfo == null) {
            orderExchangeInfo = this.display.getExchangesOtherSelectedItem();
            fieldNameForLog = "exchangesOtherSelectedItem"; //$NON-NLS$
        }
        if(this.securityFeatureDescriptor.isExchangeIsMandatory()) {
            assertNotNull(orderExchangeInfo, "orderExchangeInfoDomestic|Foreign|Other"); //$NON-NLS$
        }
        if(orderExchangeInfo != null) {
            order.setExchangeData(orderExchangeInfo);
        }
        else {
            order.setExchangeData(null);
        }
        log.addExchangeInfo(order, fieldNameForLog, orderExchangeInfo);

        final TextWithKey tradingIndicator = this.display.getTradingIndicatorsSelectedItem();
        if(tradingIndicator != null) {
            order.setTradingIdentifier(tradingIndicator.getKey());
        }
        log.add("order.tradingIdentifier", order.getTradingIdentifier()); //$NON-NLS$
        log.addTextWithKey("display.tradingIndicatorsSelectedItem", tradingIndicator); //$NON-NLS$

        final boolean exchangeAccordingToCustomerRequest = this.display.isExchangeAccordingToCustomer();
        order.setExchangeCustomerRequest(exchangeAccordingToCustomerRequest);
        log.add("order.exchangeCustomerRequest", exchangeAccordingToCustomerRequest); //$NON-NLS$
        log.add("display.exchangeAccordingToCustomerRequest", exchangeAccordingToCustomerRequest); //$NON-NLS$

        /* Set the so called KGS depot currency (OOODEPWHR) which is in our terms more the quote currency (KGS supports
         * only one single quote per instrument). Be aware that it contains either a valid ISO currency code
         * or "ST" for "Stück". OrderSecurityInfo.isQuotedPerUnit reflects that accordingly.
         */
        final OrderCurrency depotCurrencyData = this.orderStrategy.getDepotCurrencyData();
        order.setDepotCurrencyData(depotCurrencyData);
        log.addCurrency("order.depotCurrencyData", order.getDepotCurrencyData()); //$NON-NLS$
        log.addCurrency(this.orderStrategy.toLogStrategyRootNodeName("depotCurrencyData"), depotCurrencyData); //$NON-NLS$

        final boolean isQuotedPerUnit = this.orderStrategy.isQuotedPerUnit();
        order.setIsQuotedPerUnit(isQuotedPerUnit);
        final String amountNominal = this.display.getAmountNominal();
        order.setQuantity(formattedStringToDoubleString(amountNominal));
        log.add("order.isQuotedPerUnit", order.isIsQuotedPerUnit()); //$NON-NLS$
        log.add("order.quantity", order.getQuantity()); //$NON-NLS$
        log.add(this.orderStrategy.toLogStrategyRootNodeName("isQuotedPerUnit"), isQuotedPerUnit); //$NON-NLS$
        log.add("display.amountNominal", amountNominal); //$NON-NLS$
        log.add(this.orderStrategy.toLogStrategyRootNodeName("amountNominalCurrencyOrUnit"), this.orderStrategy.getAmountNominalCurrencyOrUnit()); //$NON-NLS$

        if(OrderTransaktionType.TT_SELL.equals(order.getTyp())) {
            log.add("depositoryMandatory", true); //$NON-NLS$

            final OrderStock depository = this.display.getDepositoriesSelectedItem();
            assertNotNull(depository, "depositoriesSelectedItem"); //$NON-NLS$

            final OrderDepository orderDepository = depository.getDepositoryData();
            final OrderLock orderLock = depository.getLockData();

            order.setLagerstelleData(orderDepository);
            order.setSperreData(orderLock);

            log.add("display.depositoriesSelectedItem.stockId", depository.getStockID()); //$NON-NLS$
            log.add("display.depositoriesSelectedItem.stockString", depository.getStockString()); //$NON-NLS$
            log.addExternMMDBRef("display.depositoriesSelectedItem.depositoryData", orderDepository); //$NON-NLS$
            log.addExternMMDBRef("display.depositoriesSelectedItem.lockData", orderLock); //$NON-NLS$
            log.addExternMMDBRef("order.lagerstelleData", order.getLagerstelleData()); //$NON-NLS$
            log.addExternMMDBRef("order.sperreData", order.getSperreData()); //$NON-NLS$
        }
        else {
            log.add("depositoryMandatory", false); //$NON-NLS$
        }

        switch (this.display.getValidUntil()) {
            case TODAY:
                order.setExpirationType(OrderExpirationType.OET_DAY);
                break;
            case DATE:
                final Date date = this.display.getValidUntilDate().atMidnight().getJavaDate();
                order.setExpirationType(OrderExpirationType.OET_DATE);
                order.setExpirationDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(date));
                break;
            case ULTIMO:
                order.setExpirationType(OrderExpirationType.OET_ULTIMO);
                break;
            case DEFAULT:
            default:
                order.setExpirationType(OrderExpirationType.OET_NA);
        }
        log.addEnum("order.expirationType", order.getExpirationType()); //$NON-NLS$
        log.add("order.expirationDate", order.getExpirationDate()); //$NON-NLS$
        log.addEnum("display.validUntil", this.display.getValidUntil()); //$NON-NLS$
        log.add("display.validUntilDate.gmtString", this.display.getValidUntilDate()); //$NON-NLS$

        final String limitOrStopLimit = this.display.getLimitOrStopLimit();
        if(StringUtil.hasText(this.display.getLimitOrStopLimit())) {
            order.setLimit(formattedStringToDoubleString(limitOrStopLimit));
        }
        log.add("order.limit", order.getLimit()); //$NON-NLS$
        log.add("display.limitOrStopLimit", limitOrStopLimit); //$NON-NLS$

        final CurrencyAnnotated selectedLimitCurrency = this.display.getLimitCurrenciesSelectedItem();
        if(StringUtil.hasText(order.getLimit())) {
            assertNotNull(selectedLimitCurrency, "limitCurrenciesSelectedItem"); //$NON-NLS$
            order.setLimitCurrencyData(selectedLimitCurrency.getCurrency());
        }
        log.addCurrency("order.limitCurrencyData", order.getLimitCurrencyData()); //$NON-NLS$
        log.addCurrencyAnnotated("display.limitCurrency", selectedLimitCurrency); //$NON-NLS$

        final TextWithKey limitClause = this.display.getLimitClausesSelectedItem();
        if(limitClause != null) {
            order.setLimitOptions(limitClause.getKey());
        }
        log.add("order.limitOptions", order.getLimitOptions()); //$NON-NLS$
        log.addTextWithKey("display.limitClausesSelectedItem", limitClause); //$NON-NLS$

        final String limitAfterStopLimit = this.display.getLimitAfterStopLimit();
        if(StringUtil.hasText(limitAfterStopLimit)) {
            order.setStop(formattedStringToDoubleString(limitAfterStopLimit));
        }
        log.add("order.stop", order.getStop()); //$NON-NLS$
        log.add("display.limitAfterStopLimit", limitAfterStopLimit); //$NON-NLS$

        final String limitTrailingAmountOrPercent = this.display.getLimitTrailingAmountOrPercent();
        if(StringUtil.hasText(limitTrailingAmountOrPercent)) {
            order.setTrailingPercent(formattedStringToDoubleString(limitTrailingAmountOrPercent));
        }
        log.add("order.trailingPercent", order.getTrailingPercent()); //$NON-NLS$
        log.add("display.limitTrailingAmountOrPercent", limitTrailingAmountOrPercent); //$NON-NLS$

        final String limitPeakSizeQuantity = this.display.getLimitPeakSizeQuantity();
        if(StringUtil.hasText(limitPeakSizeQuantity)) {
            order.setPeakSizeQuantity(formattedStringToDoubleString(limitPeakSizeQuantity));
        }
        log.add("order.peakSizeQuantity", order.getPeakSizeQuantity()); //$NON-NLS$
        log.add("display.limitPeakSizeQuantity", limitPeakSizeQuantity); //$NON-NLS$

        order.setLimitFee(this.display.isLimitFee());
        log.add("order.limitFee", order.isLimitFee()); //$NON-NLS$
        log.add("display.limitFee", this.display.isLimitFee()); //$NON-NLS$

        final TextWithKey settlementType = this.display.getSettlementTypesSelectedItem();
        assertNotNull(settlementType, "settlementTypesSelectedItem"); //$NON-NLS$
        order.setSettlementType(settlementType.getKey());
        log.add("order.settlementType", order.getSettlementType()); //$NON-NLS$
        log.addTextWithKey("display.settlementTypesSelectedItem", settlementType); //$NON-NLS$

        final TextWithKey businessSegment = this.display.getBusinessSegmentsSelectedItem();
        assertNotNull(businessSegment, "businessSegmentsSelectedItem"); //$NON-NLS$
        order.setBusinessSegment(businessSegment.getKey());
        log.add("order.businessSegment", order.getBusinessSegment()); //$NON-NLS$
        log.addTextWithKey("display.businessSegmentsSelectedItem", businessSegment); //$NON-NLS$

        final TextWithKey placingOfOrderVia = this.display.getPlacingOfOrderViasSelectedItem();
        if(placingOfOrderVia != null) {
            order.setOrderPlacementVia(placingOfOrderVia.getKey());
        }
        log.add("order.orderPlacementVia", order.getOrderPlacementVia()); //$NON-NLS$
        log.addTextWithKey("display.placingOfOrderViasSelectedItem", placingOfOrderVia); //$NON-NLS$

        final TextWithKey orderer = this.display.getOrderersSelectedItem();
        assertNotNull(orderer, "orderersSelectedItem"); //$NON-NLS$
        order.setOrderer(orderer.getKey());
        log.add("order.orderer", order.getOrderer()); //$NON-NLS$
        log.addTextWithKey("display.orderersSelectedItem", orderer); //$NON-NLS$

        final String ordererIdentifier = this.display.getOrdererIdentifier();
        final String ordererCustomerNumber = this.display.getOrdererCustomerNumber();
        order.setOrdererIdentifier(ordererIdentifier);
        order.setOrdererCustomerNumber(ordererCustomerNumber);
        log.add("display.ordererIdentifier", ordererIdentifier); //$NON-NLS$
        log.add("display.ordererCustomerNumber", ordererCustomerNumber); //$NON-NLS$
        log.add("order.ordererIdentifier", order.getOrdererIdentifier()); //$NON-NLS$
        log.add("order.ordererCustomerNumber", order.getOrdererCustomerNumber()); //$NON-NLS$

        final TextWithKey consultationType = this.display.getMinutesOfTheConsultationTypesSelectedItem();
        final boolean minutesOfTheConsultationTypesSelected =
                consultationType != null && StringUtil.hasText(consultationType.getKey());
        log.add("minutesOfTheConsultationTypesSelectedItem", minutesOfTheConsultationTypesSelected); //$NON-NLS$
        if(minutesOfTheConsultationTypesSelected) {
            order.setMinutesOfTheConsultation(consultationType.getKey());
            order.setMinutesOfTheConsultationNumber(this.display.getMinutesOfTheConsultationNumber());

            final MmJsDate date = this.display.getMinutesOfTheConsultationDate();
            if(date != null) {
                order.setConsultationDate(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(date.atMidnight().getJavaDate()));
            }

            log.addTextWithKey("display.minutesOfTheConsultationTypesSelectedItem", consultationType); //$NON-NLS$
            log.add("order.minutesOfTheConsultation", order.getMinutesOfTheConsultation()); //$NON-NLS$
            log.add("display.minutesOfTheConsultationNumber", this.display.getMinutesOfTheConsultationNumber()); //$NON-NLS$
            log.add("order.minutesOfTheConsultationNumber", order.getMinutesOfTheConsultationNumber()); //$NON-NLS$
            log.add("display.minutesOfTheConsultationDate", this.display.getMinutesOfTheConsultationDate()); //$NON-NLS$
            log.add("order.consultationDate", order.getConsultationDate()); //$NON-NLS$
        }

        final ProvisionType commission = this.display.getCommissionSelectedItem();
        order.setProvisionType(commission);
        final String differentCommission = this.display.getDifferentCommission();
        if(StringUtil.hasText(differentCommission)) {
            order.setProvisionValue(formattedStringToDoubleString(differentCommission));
        }
        log.addEnum("order.provisionType", order.getProvisionType()); //$NON-NLS$
        log.add("order.provisionValue", order.getProvisionValue()); //$NON-NLS$
        log.addEnum("display.commissionSelectedItem", commission); //$NON-NLS$
        log.add("display.differentCommission", differentCommission); //$NON-NLS$

        final String externalTypist = this.display.getExternalTypist();
        if(StringUtil.hasText(externalTypist)) {
            order.setOrdererExtern(externalTypist.trim());
        }
        log.add("order.ordererExtern", order.getOrdererExtern()); //$NON-NLS$
        log.add("display.externalTypist", externalTypist); //$NON-NLS$

        final TextWithKey cannedTextForBillingReceipts1 = this.display.getCannedTextForBillingReceipts1SelectedItem();
        if(cannedTextForBillingReceipts1 != null) {
            order.setBillingDocument1(cannedTextForBillingReceipts1.getKey());
        }
        log.add("order.billingDocument1", order.getBillingDocument1()); //$NON-NLS$
        log.addTextWithKey("display.cannedTextForBillingReceipts1SelectedItem", cannedTextForBillingReceipts1); //$NON-NLS$

        final TextWithKey cannedTextForBillingReceipts2 = this.display.getCannedTextForBillingReceipts2SelectedItem();
        if(cannedTextForBillingReceipts2 != null) {
            order.setBillingDocument2(cannedTextForBillingReceipts2.getKey());
        }
        log.add("order.billingDocument2", order.getBillingDocument2()); //$NON-NLS$
        log.addTextWithKey("display.cannedTextForBillingReceipts2SelectedItem", cannedTextForBillingReceipts2); //$NON-NLS$

        final TextWithKey cannedTextForBillingReceipts3 = this.display.getCannedTextForBillingReceipts3SelectedItem();
        if(cannedTextForBillingReceipts3 != null) {
            order.setBillingDocument3(cannedTextForBillingReceipts3.getKey());
        }
        log.add("order.billingDocument3", order.getBillingDocument3()); //$NON-NLS$
        log.addTextWithKey("display.cannedTextForBillingReceipts3SelectedItem", cannedTextForBillingReceipts3); //$NON-NLS$

        final TextWithKey cannedTextForOrderConfirmation = this.display.getCannedTextForOrderConfirmationsSelectedItem();
        if(cannedTextForOrderConfirmation != null) {
            order.setOrderConfirmation(cannedTextForOrderConfirmation.getKey());
        }
        log.add("order.orderConfirmation", order.getOrderConfirmation()); //$NON-NLS$
        log.addTextWithKey("display.cannedTextForOrderConfirmationsSelectedItem", cannedTextForOrderConfirmation); //$NON-NLS$

        final List<TextWithTyp> freeText = order.getFreeText();
        final String textForOrderReceipt1 = this.display.getTextForOrderReceipt1();
        addTextWithType(freeText, TextType.TT_FREE_TEXT_ORDER_DOCUMENT_1, textForOrderReceipt1);
        final String textForOrderReceipt2 = this.display.getTextForOrderReceipt2();
        addTextWithType(freeText, TextType.TT_FREE_TEXT_ORDER_DOCUMENT_2, textForOrderReceipt2);
        final String textForInternalUse1 = this.display.getTextForInternalUse1();
        addTextWithType(freeText, TextType.TT_INTERNAL_TEXT_1, textForInternalUse1);
        final String textForInternalUse2 = this.display.getTextForInternalUse2();
        addTextWithType(freeText, TextType.TT_INTERNAL_TEXT_2, textForInternalUse2);
        log.addTextWithTypeList("order.freeText", order.getFreeText()); //$NON-NLS$
        log.add("display.textForOrderReceipt1", textForOrderReceipt1); //$NON-NLS$
        log.add("display.textForOrderReceipt2", textForOrderReceipt2); //$NON-NLS$
        log.add("display.textForInternalUse1", textForInternalUse1); //$NON-NLS$
        log.add("display.textForInternalUse2", textForInternalUse2); //$NON-NLS$

        final Date contractDateDime = this.display.getContractDateTime().getJavaDate();
        order.setOrderDateTime(Formatter.PM_DATE_TIME_FORMAT_MMTALK.format(contractDateDime));
        log.add("order.orderDateTime", order.getOrderDateTime()); //$NON-NLS$
        log.add("display.contractDateTime.gmtString", contractDateDime); //$NON-NLS$

        final String logString = log.toString();
        Firebug.debug(logString);
        DebugUtil.logToServer(logString);

        return order;
    }

    private void addTextWithType(List<TextWithTyp> list, TextType textType, String text) {
        list.add(OrderUtils.newTextWithType(textType, text));
    }

    private void assertNotNull(Object object, String displayFieldName) throws IllegalStateException {
        if(object == null) {
            final String message = "display." + displayFieldName + " is null!"; //$NON-NLS$
            Firebug.log(message);
            DebugUtil.logToServer(message);
            throw new IllegalStateException(message);
        }
    }

    protected List<OrderConfirmationDisplay.Section> createVerificationModel(OrderDataType order) {
        final LogBuilder logBuilder = new LogBuilder("OrderPresenterBHLKGS.createVerificationModel"); //$NON-NLS$
        return createOrderConfirmationModel((OrderDataTypeBHL)order, logBuilder, false, null);
    }

    protected List<OrderConfirmationDisplay.Section> createAckOfReceiptModel(OrderDataType order, String externalMessage) {
        final LogBuilder logBuilder = new LogBuilder("OrderPresenterBHLKGS.createAckOfReceiptModel"); //$NON-NLS$
        return createOrderConfirmationModel((OrderDataTypeBHL)order, logBuilder, true, externalMessage);
    }

    private List<OrderConfirmationDisplay.Section> createOrderConfirmationModel(OrderDataTypeBHL order, LogBuilder logBuilder, boolean orderCreated, String externalMessage) {
        final OrderConfirmationModelBuilderBHLKGS builder = new OrderConfirmationModelBuilderBHLKGS();
        final OrderSessionFeaturesDescriptorBHL features = this.orderSession.getFeatures();

        if(orderCreated) {
            builder.addOrderNumberSection(this.orderStrategy.getSendOrderSuccessfulMessage(order), externalMessage);
        }
        else if(this.orderStrategy.isDisplayOrderNumberInConfirmationView()) {
            builder.addOrderNumberSectionForOrderDetails(order);
        }

        builder.addTransactionSection(order);
        builder.addPortfolioSection(order, logBuilder);
        builder.addSecuritySection(order, logBuilder);
        builder.addSecuritySection2(order, features, this.securityFeatureDescriptor, logBuilder);
        builder.addOrderSection(order, logBuilder);
        builder.addOrderTypesSection(order, this.orderSession.getFeatures(), logBuilder);
        builder.addOrdererSection(order, features, logBuilder);
        builder.addOthersSection(order, features, logBuilder);
        builder.addMinutesOfTheConsultationSection(order, features);
        builder.addCommissionSection(order);
        builder.addTextSections(order, features, logBuilder);
        if(orderCreated) {
            builder.addStatusSections(order, features, false);
        }

        return builder.getSections();
    }

    protected void onSymbolSearchDialogCanceled() {
        reinitInstrumentName();
    }

    @Override
    public void onSymbolSearchTextBoxValueChanged(ValueChangeEvent<String> event) {
        final String rawValue = event.getValue();
        if(!StringUtil.hasText(rawValue)) {
            reinitInstrumentName();
            return;
        }
        final String value = rawValue.trim().toUpperCase();

        if(SymbolUtil.isIsin(value)) {
            lookupSecurity(value, LookupSecurityConfig.DEFAULT);
            return;
        }
        if(SymbolUtil.isWkn(value)) {
            SearchMethods.INSTANCE.instrumentSearchWkn(value, this.orderSession.getTradableSecurityTypes(), new AsyncCallback<ShellMMInfo>() {
                @Override
                public void onFailure(Throwable caught) {
                    Firebug.error("WKN search failed. Showing symbol search dialog", caught);
                    OrderPresenterBHLKGS.this.selectInstrumentView.show(value);
                }

                @Override
                public void onSuccess(ShellMMInfo result) {
                    Firebug.debug("ISIN for WKN " + value + " found " + result.getISIN());
                    lookupSecurity(result.getISIN(), LookupSecurityConfig.DEFAULT);
                }
            });
            return;
        }

        OrderPresenterBHLKGS.this.selectInstrumentView.show(rawValue);
    }

    private String extractNameOfCurrentSecurityInfo() {
        if(this.securityInfo != null && this.securityInfo.getSecurity() != null) {
            return this.securityInfo.getSecurity().getBezeichnung();
        }
        return null;
    }

    @Override
    public void onCancelOrderClicked() {
        cancelOrder();
    }

    @Override
    public void onExecuteOrderClicked() {
        getOrderViewContainerDisplay().setButtonsLocked(true);
        validateOrder();
    }

    @Override
    public void onOrderActionTypeChanged(ChangeEvent event) {
        Firebug.debug(LOG_PREFIX + ".onOrderActionTypeChanged>");

        final OrderActionType newOrderActionType = this.orderStrategy.getOrderActionType();
        final OrderActionType oldOrderActionType = this.currentOrderActionType;

        if (OrderActionType.AT_SUBSCRIBE.equals(newOrderActionType)
                || OrderActionType.AT_SUBSCRIBE.equals(oldOrderActionType)) {
            resetDisplaySecuritySection();
            resetSecurityDependingValuesToDefaults();
        }
        else if(newOrderActionType == null) {
            resetSecurityDependingValuesToDefaults();
        }
        setInstrumentPanelsVisibility(newOrderActionType);

        this.currentOrderActionType = newOrderActionType;

        validateDisplay();

        /* looking up the security is here necessary, because it may already produce validation messages,
         * which we have to send along with our first validateOrder request. So we have to throw away any validation
         * messages from previous validateOrder/sendOrder requests, because validation messages of order type
         * ttSell must not be sent along with an order of type ttBuy.
         */
        if(this.securityInfo != null) {
            final String isin = this.securityInfo.getSecurity().getISIN();
            Firebug.debug(LOG_PREFIX + ".onOrderActionTypeChanged> looking up security: isin=" + isin);
            lookupSecurity(isin);
        }
    }

    private void setInstrumentPanelsVisibility(OrderActionType type) {
        if(this.orderStrategy.isInstrumentChangeable()) {
            final boolean withIpo = OrderActionType.AT_SUBSCRIBE == type;
            this.display.setSelectIpoInstrumentPanelVisible(withIpo);
            this.display.setSelectIpoInstrumentPanelEnabled(withIpo);
            this.display.setSelectInstrumentPanelVisible(!withIpo);
            this.display.setInstrumentNamePanelVisible(false);
        }
        else {
            this.display.setInstrumentNamePanelVisible(true);
            this.display.setSelectInstrumentPanelVisible(false);
            this.display.setSelectIpoInstrumentPanelVisible(false);
            this.display.setSelectIpoInstrumentPanelEnabled(false);
        }
    }

    @Override
    public void onDepotChanged(ChangeEvent changeEvent) {
        final Depot depot = this.display.getDepotsSelectedItem();
        if(depot == null) {
            Firebug.warn(LOG_PREFIX + ".onDepotsSelectedItemChanged> selected depot is null. nothing to do!");
            return;
        }

        final String depotId = depot.getId();
        final OrderActionType orderActionType = this.orderStrategy.getOrderActionType();
        final String isin = extractIsinOfCurrentSecurityInfo();

        Firebug.debug(LOG_PREFIX + ".onDepotsSelectedItemChanged> depotId=" + depotId + " orderActionType=" + orderActionType + " isin=" + isin);

        if(StringUtil.hasText(depotId)) {
            cancelOrder();

            final ParameterMap parameterMap = this.orderEntryContext.getParameterMap();
            parameterMap.setDepotId(depotId);
            parameterMap.setOrderActionType(orderActionType);
            parameterMap.setIsin(isin);

            OrderModule.showByDepotId(this.orderEntryContext);
        }
    }

    private String extractIsinOfCurrentSecurityInfo() {
        if(this.securityInfo != null && this.securityInfo.getSecurity() != null) {
            return this.securityInfo.getSecurity().getISIN();
        }
        return null;
    }

    @Override
    public void onLimitChanged(ValueChangeEvent<String> valueChangeEvent) {
        doOnLimitChanged(valueChangeEvent.getValue());
    }

    protected void doOnLimitChanged(String limit) {
        final boolean hasLimit = StringUtil.hasText(limit);
        if(hasLimit) {
            if(this.display.getLimitCurrenciesSelectedItem() == null) {
                final CurrencyAnnotated currency = (this.securityInfo != null) ?
                        OrderUtils.findDefaultCurrencyAnnotated(this.securityInfo.getCurrencyList()) : null;
                this.display.setLimitCurrenciesSelectedItem(currency);
            }
        }
        else {
            this.display.setLimitCurrenciesSelectedItem(null);
        }
        this.display.setLimitCurrenciesValidatorEnabled(hasLimit);

        setLimitNotEmptyValidatorEnabledDependingOnCurrencyAndLimitClause(
                this.display.getLimitCurrenciesSelectedItem(),
                this.display.getLimitClausesSelectedItem()
        );
    }

    @Override
    public void onLimitCurrenciesChanged(ChangeEvent changeEvent) {
        doOnLimitCurrenciesChanged();
    }

    protected void doOnLimitCurrenciesChanged() {
        final CurrencyAnnotated selectedCurrency = this.display.getLimitCurrenciesSelectedItem();
        final TextWithKey selectedLimitClauses = this.display.getLimitClausesSelectedItem();

        setLimitNotEmptyValidatorEnabledDependingOnCurrencyAndLimitClause(selectedCurrency, selectedLimitClauses);
    }

    @Override
    public void onLimitClausesChanged(ChangeEvent changeEvent) {
        final CurrencyAnnotated selectedCurrency = this.display.getLimitCurrenciesSelectedItem();
        final TextWithKey selectedLimitClauses = this.display.getLimitClausesSelectedItem();

        setLimitAfterStopLimitEnabledDependingOnLimitClause(selectedLimitClauses);
        setLimitNotEmptyValidatorEnabledDependingOnCurrencyAndLimitClause(selectedCurrency, selectedLimitClauses);
    }

    @Override
    public void onSelectAuthorizedRepresentativeSelected(SelectionEvent<AuthorizedRepresentative> selectionEvent) {
        doOnSelectAuthorizedRepresentative(selectionEvent.getSelectedItem());
    }

    protected void doOnSelectAuthorizedRepresentative(AuthorizedRepresentative representative) {
        if(representative == null) {
            return;
        }

        final List<TextWithKey> orderers = this.orderSession.getFeatures().getOrderer();
        final TextWithKey item = OrderUtils.findTextWithKey(orderers, representative.getOrdererIdentifier());
        if(item == null) {
            OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromString("List entry for key '" + representative.getOrdererIdentifier() + "' not found!")); //$NON-NLS$
            return;
        }

        this.display.setOrderersSelectedItem(item);
        this.display.setOrdererIdentifier(representative.getName());
        this.display.setOrdererCustomerNumber(representative.getNumber());

        validateDisplay();
    }

    @Override
    public void onOrdererChanged(ChangeEvent changeEvent) {
        final TextWithKey selectedOrderer = this.display.getOrderersSelectedItem();

        if(selectedOrderer != null && this.securityFeatureDescriptor != null) {
            for(AuthorizedRepresentative ar : this.securityFeatureDescriptor.getAuthorizedRepresentatives()) {
                if(selectedOrderer.getKey().equals(ar.getOrdererIdentifier())) {
                    this.display.setOrdererIdentifier(ar.getName());
                    this.display.setOrdererCustomerNumber(ar.getNumber());
                    break;
                }
            }
        }
        else {
            this.display.setOrdererIdentifier(null);
            this.display.setOrdererCustomerNumber(null);
        }

        //revalidate
        validateDisplay();
    }

    @Override
    public void onCommissionChanged(ChangeEvent changeEvent) {
        setDifferentCommissionEnabled(this.display.getCommissionSelectedItem());
        validateDisplay();
    }

    private void validateDisplay() {
        this.display.getValidatorGroup().isValid();
    }

    @Override
    public void onBusinessSegmentChanged(ChangeEvent changeEvent) {
        this.orderStrategy.onBusinessSegmentChanged();
    }

    @Override
    public void onJointValidationEvent(ValidationEvent validationEvent) {
        this.getOrderViewContainerDisplay().setExecuteOrderButtonEnabled(validationEvent.isValid());
    }

    @Override
    public void onExchangesDomesticChanged(ChangeEvent changeEvent) {
        if(null == this.display.getExchangesDomesticSelectedItem()) {
            setAllExchangesValidatorsEnabledDependingOnMandatory(true);
        }
        else {
            this.display.setExchangesForeignValidatorEnabled(false);
            this.display.setExchangesForeignSelectedItem(null);
            this.display.setExchangesOtherValidatorEnabled(false);
            this.display.setExchangesOtherSelectedItem(null);
        }
    }

    @Override
    public void onExchangesForeignChanged(ChangeEvent changeEvent) {
        if(null == this.display.getExchangesForeignSelectedItem()) {
            setAllExchangesValidatorsEnabledDependingOnMandatory(true);
        }
        else {
            this.display.setExchangesDomesticValidatorEnabled(false);
            this.display.setExchangesDomesticSelectedItem(null);
            this.display.setExchangesOtherValidatorEnabled(false);
            this.display.setExchangesOtherSelectedItem(null);
        }
    }

    @Override
    public void onExchangesOtherChanged(ChangeEvent changeEvent) {
        if(null == this.display.getExchangesOtherSelectedItem()) {
            setAllExchangesValidatorsEnabledDependingOnMandatory(true);
        }
        else {
            this.display.setExchangesDomesticValidatorEnabled(false);
            this.display.setExchangesDomesticSelectedItem(null);
            this.display.setExchangesForeignValidatorEnabled(false);
            this.display.setExchangesForeignSelectedItem(null);
        }
    }

    private void setAllExchangesValidatorsEnabledDependingOnMandatory(boolean enabled) {
        final boolean mandatory = this.securityFeatureDescriptor.isExchangeIsMandatory();
        setAllExchangesValidatorsEnabled(mandatory && enabled);
    }

    private void setAllExchangesValidatorsEnabled(boolean enabled) {
        this.display.setExchangesDomesticValidatorEnabled(enabled);
        this.display.setExchangesForeignValidatorEnabled(enabled);
        this.display.setExchangesOtherValidatorEnabled(enabled);
    }

    @Override
    public void onAccountChanged(ChangeEvent event) {
        final DisplayBHLKGS display = this.display;
        final AccountData accountData = display.getAccountsSelecedItem();

        setAccountData(accountData);
    }

    private void setAccountData(AccountData accountData) {
        if(accountData != null) {
            this.display.setAccountNo(accountData.getNumber());
            this.display.setAccountBalance(accountData.getBalance(), accountData.getCurrency());
        }
        else {
            this.display.setAccountNo(null);
            this.display.setAccountBalance(null, null);
        }
    }

    @Override
    public void onSymbolSearchButtonClicked(ClickEvent event) {
        this.selectInstrumentView.getParams().remove(SelectSymbolForm.PRESET_SEARCH_STRING);
        this.selectInstrumentView.show();
    }

    @Override
    public void onSelectSymbolFromDepotSelected(SelectionEvent<OrderSecurityInfo> securityDataSelectionEvent) {
        final OrderSecurityInfo item = securityDataSelectionEvent.getSelectedItem();
        final String isin = item.getISIN();
        lookupSecurity(isin);
    }

    @Override
    public void onIpoSearchButtonClicked(ClickEvent clickEvent) {
        //order matters, otherwise the glass panel will not be displayed!
        this.selectIpoInstrumentView.show();
        this.selectIpoSymbolController.search(this.orderSession);
    }

    @Override
    public void onShowArbitrageButtonClicked(ClickEvent clickEvent) {
        this.arbitragePresenter.setSymbol(this.securityInfo.getSecurity().getMMSecurityID() + ".iid"); //$NON-NLS$
        this.arbitragePresenter.show();
    }

    protected void cancelOrder() {
        Firebug.debug(LOG_PREFIX + ".cancelOrder>");
        dispose();
    }

    @Override
    public void show(OrderEntryContext presenterContext) {
        this.orderEntryContext = presenterContext;

        final PresenterDisposedHandler disposedHandler = this.orderEntryContext.getPresenterDisposedHandler();
        if(disposedHandler != null) {
            addPresenterDisposedHandler(disposedHandler);
        }

        show();
    }

    private void initOrderActionTypes() {
        final DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> d = this.display;
        final OrderSession.OrderSessionBHLKGS os = this.orderSession;

        final boolean changeable = this.orderStrategy.isOrderActionChangeable();
        final OrderActionType selectedOrderActionType = OrderUtils.toOrderActionType(this.orderStrategy.getOrderTransactionType());

        d.setOrderActionTypesVisible(changeable);
        d.setOrderActionTypesEnabled(changeable);
        d.setOrderActionTypeNameVisible(!changeable);

        if(changeable) {
            final List<OrderAction> orderActions = os.getFeatures().getOrderActions();
            final List<OrderActionType> orderActionTypes = new ArrayList<>(orderActions.size());
            for(final OrderAction orderAction : orderActions) {
                final OrderActionType orderActionType = orderAction.getValue();
                if(orderActionType != null) {
                    orderActionTypes.add(orderActionType);
                }
            }
            d.setOrderActionTypes(orderActionTypes);
        }
        else {
            d.setOrderActionTypes(Collections.singletonList(selectedOrderActionType));
            d.setOrderActionTypeName(selectedOrderActionType);
        }
        d.setOrderActionTypesSelectedItem(selectedOrderActionType);
    }

    private void initDepots() {
        final DisplayBHLKGS d = this.display;
        final boolean changeable = this.orderStrategy.isDepotChangeable();

        final AccountRef depot = this.orderSession.getSecurityAccount();
        d.setDepotsEnabled(changeable);
        d.setDepotsVisible(changeable);
        d.setDepotName(depot.getName());
        d.setDepotNameVisible(!changeable);
        d.setDepotNo(depot.getNumber());
    }

    private void initInstrumentChoicePanel() {
        final DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> d = this.display;
        final OrderStrategy strategy = this.orderStrategy;
        final boolean changeable = strategy.isInstrumentChangeable();

        d.setSelectInstrumentPanelVisible(changeable);
        d.setSelectSymbolFromDepotEnabled(false);
        d.setSelectIpoInstrumentPanelVisible(false);

        if(changeable) {
            final OrderSession.OrderSessionBHLKGS os = this.orderSession;
            d.setSymbolsOfDepot(os.getSecurityList());
            d.setSelectSymbolFromDepotEnabled(!(os.getSecurityList() == null || os.getSecurityList().isEmpty()));
        }
        else {
            d.setInstrumentName(strategy.getInstrumentName());
        }
    }

    private void initDisplayToChangeOrder(OrderDataTypeBHL o) {
        final OrderSessionFeaturesDescriptorBHL sessionFeatures = this.orderSession.getFeatures();

        //bank account
        final List<AccountData> accountDatas = this.orderSession.getAccountList();

        AccountData accountToSelect = null;
        for (AccountData accountData : accountDatas) {
            if (o.getKontoData().getId().equals(accountData.getId())) {
                accountToSelect = accountData;
            }
        }
        if(accountToSelect == null) {
            Firebug.warn("<OrderPresenterBHLKGS.initDisplayToChangeOrder> account not found in order session. Adding account to order session account list");

            accountToSelect = OrderUtils.toAccountData(o.getKontoData());
            accountDatas.add(accountToSelect);
            this.display.setAccounts(accountDatas);
        }
        this.display.setAccountsSelectedItem(accountToSelect);
        setAccountData(accountToSelect);

        //instrument
        this.display.setExchangeAccordingToCustomer(o.isExchangeCustomerRequest());
        this.display.setTradingIndicatorsSelectedItem(OrderUtils.findTextWithKey(
                sessionFeatures.getTradingIndicator(),
                o.getTradingIdentifier()
        ));

        for(OrderExchangeInfo e : this.securityInfo.getExchangeList()) {
            if(o.getExchangeData().getID().equals(e.getID())) {
                setExchangesSelectedItem(e);
                break;
            }
        }

        //order
        initAmountNominal(o.getQuantity());

        if(o.getLagerstelleData() != null) {
            for(OrderStock orderStock : this.securityInfo.getStockBalance()) {
                if(o.getLagerstelleData().getId().equals(orderStock.getDepositoryData().getId())) {
                    this.display.setDepositoriesSelectedItem(orderStock);
                }
            }
        }

        //validity
        final DisplayBHLKGS.ValidUntil validUntil = toValidUntil(o.getExpirationType());
        this.display.setValidUntil(validUntil);
        if(DisplayBHLKGS.ValidUntil.DATE.equals(validUntil)) {
            this.display.setValidUntilDate(OrderUtils.toMmJsDate(o.getExpirationDate()));
        }

        //limit clauses
        initLimit(o.getLimit());

        CurrencyAnnotated selectedLimitCurrency = null;
        if(o.getLimitCurrencyData() != null){
            final OrderCurrency orderLimitCurrency = o.getLimitCurrencyData();
            for (CurrencyAnnotated limitCurrency : this.securityInfo.getCurrencyList()) {
                if (orderLimitCurrency.getId().equals(limitCurrency.getCurrency().getId())) {
                    selectedLimitCurrency = limitCurrency;
                    this.display.setLimitCurrenciesSelectedItem(selectedLimitCurrency);
                    break;
                }
            }
        }

        final TextWithKey selectedLimitClause = OrderUtils.findTextWithKey(
                sessionFeatures.getLimitOptions(), o.getLimitOptions()
        );
        this.display.setLimitClausesSelectedItem(selectedLimitClause);

        if(!ZERO.equals(o.getStop())) {
            this.display.setLimitAfterStopLimit(OeRenderers.UNAMBIGUOUS_NUMBER_RENDERER_0_5.render(o.getStop()));
        }
        if(!ZERO.equals(o.getTrailingPercent())) {
            this.display.setLimitTrailingAmountOrPercent(OeRenderers.UNAMBIGUOUS_NUMBER_RENDERER_0_5.render(o.getTrailingPercent()));
        }
        if(!ZERO.equals(o.getPeakSizeQuantity())) {
            this.display.setLimitPeakSizeQuantity(OeRenderers.UNAMBIGUOUS_NUMBER_RENDERER_0_5.render(o.getPeakSizeQuantity()));
        }

        this.display.setLimitFee(o.isLimitFee());

        setLimitAfterStopLimitEnabledDependingOnLimitClause(selectedLimitClause);
        setLimitNotEmptyValidatorEnabledDependingOnCurrencyAndLimitClause(selectedLimitCurrency, selectedLimitClause);

        //Orderer
        this.display.setOrderersSelectedItem(
                OrderUtils.findTextWithKey(sessionFeatures.getOrderer(), o.getOrderer()));
        this.display.setOrdererIdentifier(o.getOrdererIdentifier());
        this.display.setOrdererCustomerNumber(o.getOrdererCustomerNumber());

        //minutes of the consultation
        this.display.setMinutesOfTheConsultationTypesSelectedItem(OrderUtils.findTextWithKey(
                sessionFeatures.getMinutesOfTheConsultation(),
                o.getMinutesOfTheConsultation()
        ));

        this.display.setMinutesOfTheConsultationNumber(o.getMinutesOfTheConsultationNumber());

        final String minutesOfTheConsultationDateStr = o.getConsultationDate();
        if(StringUtil.hasText(minutesOfTheConsultationDateStr)) {
            this.display.setMinutesOfTheConsultationDate(OrderUtils.toMmJsDate(minutesOfTheConsultationDateStr));
        }

        //different commission
        final ProvisionType commissionType = o.getProvisionType();
        final String commissionValue = o.getProvisionValue();
        this.display.setCommissionSelectedItem(commissionType);
        if(!ZERO.equals(commissionValue)) {
            this.display.setDifferentCommission(OeRenderers.UNAMBIGUOUS_NUMBER_RENDERER_0_5.render(commissionValue));
        }
        setDifferentCommissionEnabled(commissionType);

        //other
        this.display.setSettlementTypesSelectedItem(OrderUtils.findTextWithKey(
                sessionFeatures.getSettlementTypes(), o.getSettlementType()
        ));

        this.display.setBusinessSegmentsSelectedItem(OrderUtils.findTextWithKey(
                sessionFeatures.getBusinessSegments(), o.getBusinessSegment()
        ));

        this.display.setPlacingOfOrderViasSelectedItem(OrderUtils.findTextWithKey(
                sessionFeatures.getOrderPlacementVia(), o.getOrderPlacementVia()
        ));

        this.display.setExternalTypist(o.getOrdererExtern());

        final String orderDateTime = o.getOrderDateTime();
        if(StringUtil.hasText(orderDateTime)) {
            this.display.setContractDateTime(OrderUtils.toMmJsDate(orderDateTime));
        }

        //texts
        this.display.setCannedTextForBillingReceipts1SelectedItem(OrderUtils.findTextWithKey(
                sessionFeatures.getTextLibrariesBillingDocument(), o.getBillingDocument1()
        ));
        this.display.setCannedTextForBillingReceipts2SelectedItem(OrderUtils.findTextWithKey(
                sessionFeatures.getTextLibrariesBillingDocument(), o.getBillingDocument2()
        ));
        this.display.setCannedTextForBillingReceipts3SelectedItem(OrderUtils.findTextWithKey(
                sessionFeatures.getTextLibrariesBillingDocument(), o.getBillingDocument3()
        ));

        this.display.setCannedTextForOrderConfirmationsSelectedItem(OrderUtils.findTextWithKey(
                sessionFeatures.getTextLibrariesOrderConfirmation(), o.getOrderConfirmation()
        ));

        for(TextWithTyp tt : o.getFreeText()) {
            switch(tt.getTyp()) {
                case TT_FREE_TEXT_ORDER_DOCUMENT_1:
                    this.display.setTextForOrderReceipt1(tt.getText());
                    break;
                case TT_FREE_TEXT_ORDER_DOCUMENT_2:
                    this.display.setTextForOrderReceipt2(tt.getText());
                    break;
                case TT_INTERNAL_TEXT_1:
                    this.display.setTextForInternalUse1(tt.getText());
                    break;
                case TT_INTERNAL_TEXT_2:
                    this.display.setTextForInternalUse2(tt.getText());
            }
        }
    }

    protected void initLimit(String limit) {
        if(!ZERO.equals(limit)) {
            this.display.setLimitOrStopLimit(OeRenderers.UNAMBIGUOUS_NUMBER_RENDERER_0_5.render(limit));
        }
    }

    protected void initAmountNominal(String amountNominal) {
        this.display.setAmountNominal(OeRenderers.UNAMBIGUOUS_NUMBER_RENDERER_0_5.render(amountNominal));
    }

    private void setExchangesSelectedItem(OrderExchangeInfo e) {
        if(e == null) {
            this.display.setExchangesDomesticSelectedItem(null);
            this.display.setExchangesForeignSelectedItem(null);
            this.display.setExchangesOtherSelectedItem(null);

            this.display.setExchangesDomesticValidatorEnabled(true);
            this.display.setExchangesForeignValidatorEnabled(true);
            this.display.setExchangesOtherValidatorEnabled(true);

            return;
        }

        final ExternExchangeType type = e.getExternExchangeTyp();

        if(type != null) {
            this.display.setExchangesDomesticValidatorEnabled(false);
            this.display.setExchangesForeignValidatorEnabled(false);
            this.display.setExchangesOtherValidatorEnabled(false);

            this.display.setExchangesDomesticSelectedItem(null);
            this.display.setExchangesForeignSelectedItem(null);
            this.display.setExchangesOtherSelectedItem(null);

            switch(type) {
                case EET_DOMESTIC:
                    this.display.setExchangesDomesticSelectedItem(e);
                    this.display.setExchangesDomesticValidatorEnabled(true);
                    break;
                case EET_FOREIGN:
                    this.display.setExchangesForeignSelectedItem(e);
                    this.display.setExchangesForeignValidatorEnabled(true);
                    break;
                case EET_OTHER:
                    this.display.setExchangesOtherSelectedItem(e);
                    this.display.setExchangesOtherValidatorEnabled(true);
                    break;
            }
        }
    }

    public void show() {
        Firebug.debug(LOG_PREFIX + ".show>");

        final OrderSession session = this.orderSession;
        final SessionState sessionState = session.getSessionState();

        if(SessionState.SS_INITIALISED.equals(sessionState)) {
            final IllegalStateException ise = new IllegalStateException("Order session must be in state SS_AUTHENTICATED before show can be called!"); //$NON-NLS$
            Firebug.error(LOG_PREFIX + ".show>", ise);
            throw ise;
        }

        showDialog();
    }

    private void showDialog() {
        addPresenterDisposedHandler(this);
        registerLoadingIndicatorHandler();

        final OrderViewContainerDisplay containerDisplay = getOrderViewContainerDisplay();
        final SafeHtmlBuilder messages = new SafeHtmlBuilder();
        containerDisplay.setContent(this.display.getOrderView());

        this.orderStrategy.init();

        containerDisplay.show();

        final boolean noMessages = processParameterMap(messages);
        validateDisplay();

        if(!noMessages) {
            Dialog.warning(messages.toSafeHtml());
        }
    }

    private void registerLoadingIndicatorHandler() {
        if(this.loadingIndicatorHandlerRegistration == null) {
            this.loadingIndicatorHandlerRegistration =
                    EventBusRegistry.get().addHandler(PendingRequestsEvent.getType(), this);
        }
    }

    protected boolean processParameterMap(SafeHtmlBuilder messages) {
        return this.parameterMapProcessor.process(getParameterMap(), messages, ParameterMapProcessor.State.ON_INIT);
    }

    @Override
    public void onPendingRequestsUpdate(PendingRequestsEvent event) {
        try {
            this.display.setLoadingIndicatorVisible(event.getNumPmPending() > 0);
        }
        catch (Exception e) {
            Firebug.error("<OrderPresenterBHLKGS.onPendingRequestsUpdate> updating loading indicator failed", e);
        }
    }

    private void initDisplayToCreateOrder() {
        if(this.orderStrategy.isDepotChangeable()) {
            new LoadDepotsAndInvestorsMethod(this.orderSession, LoadDepotsAndInvestorsMethod.Type.STANDALONE, PrivacyMode.isActive(), this, null).invoke();
        }
    }

    protected void confirmOrder(final OrderDataType order, final List<ValidationMessage> validationMessages) {
        Firebug.debug(LOG_PREFIX + ".confirmOrder>");
        final List<OrderConfirmationDisplay.Section> model;
        try {
            model = createVerificationModel(order);
        }
        catch(Exception e) {
            getOrderViewContainerDisplay().setButtonsLocked(false);
            OrderMethods.INSTANCE.showFailureMessage(e);
            return;
        }

        if(model == null) {
            sendOrder(order, validationMessages);
            return;
        }

        final OrderConfirmationPresenter.Callback callback = new OrderConfirmationPresenter.AbstractCallback() {
            @Override
            public void onExecute() {
                onConfirmOrderProceed(order, validationMessages);
            }

            @Override
            public void onCancel() {
                onConfirmOrderCanceled();
            }

            @Override
            public void onBack() {
                onConfirmOrderCanceled();
            }
        };

        final OrderConfirmationPresenter ocp = new OrderConfirmationPresenter(this.windowTitle, callback);
        ocp.setColumns(2);
        ocp.setExecuteButtonText(this.orderStrategy.getConfirmButtonText(order.getTyp()));
        ocp.setExecuteButtonVisible(true);
        ocp.setCancelButtonVisible(false);
        ocp.setBackButtonVisible(true);
        ocp.setPrintDateVisible(true);
        ocp.show(model);
    }

    protected void onConfirmOrderCanceled() {
        DebugUtil.logToServer(new OrderLogBuilder(LOG_PREFIX + ".onConfirmOrderCanceled", this.orderSession).toString());

        getOrderViewContainerDisplay().setButtonsLocked(false);
    }

    protected void onConfirmOrderProceed(OrderDataType order, List<ValidationMessage> validationMessages) {
        DebugUtil.logToServer(new OrderLogBuilder(LOG_PREFIX + ".onConfirmOrderProceed", this.orderSession).toString());

        sendOrder(order, validationMessages);
    }

    protected void lookupSecurity(final String isin) {
        lookupSecurity(isin, LookupSecurityConfig.DEFAULT);
    }

    protected void lookupSecurity(final String isin, final LookupSecurityConfig config) {
        final OrderTransaktionType type = this.orderStrategy.getOrderTransactionType();

        Firebug.debug(LOG_PREFIX + ".lookupSecurity> isin=" + isin + " orderTransactionType=" + type + " config=" + config);

        INSTANCE.lookupSecurity(this.orderSession, type, isin, new AsyncCallback<LookupSecurityDataResponse>() {
            @Override
            public void onSuccess(LookupSecurityDataResponse result) {
                onLookupSecuritySuccess(result, config);
            }

            @Override
            public void onFailure(Throwable caught) {
                onLookupSecurityFailure(isin, caught, config);
            }
        });
    }

    protected void onLookupSecuritySuccess(LookupSecurityDataResponse result, LookupSecurityConfig config) {
        Firebug.debug(LOG_PREFIX + ".onLookupSecuritySuccess> config="+config);
        this.securityInfo = result.getSecurityInfo();

        //Checks that the returned security feature descriptor is of BHL type.
        if(!(result.getFeatures() instanceof OrderSecurityFeatureDescriptorBHL)) {
            final OrderSecurityFeatureDescriptor descriptor = result.getFeatures();
            final String actualName = (descriptor != null ? descriptor.getClass().getName() : "null"); //$NON-NLS$
            final String expectedName = OrderSecurityFeatureDescriptorBHL.class.getName();
            final String plainMessage = "Expected type " + expectedName + " but was " + actualName; //$NON-NLS$
            final String logMessage = LOG_PREFIX + ".onLookupSecuritySuccess>" + plainMessage; //$NON-NLS$
            DebugUtil.logToServer(logMessage);
            Firebug.error(logMessage);
            INSTANCE.showFailureMessage(SafeHtmlUtils.fromTrustedString(I18n.I.orderEntryError(plainMessage)));
            return;
        }
        this.securityFeatureDescriptor = (OrderSecurityFeatureDescriptorBHL)result.getFeatures();

        if(LookupSecurityConfig.PROCESS_PARAMETER_MAP != config) {
            resetSecurityDependingValuesToDefaults();
        }

        initSecurityDependingValues();

        //limit currencies are just available if lookupSecurity was successful
        if(LookupSecurityConfig.PROCESS_PARAMETER_MAP == config) {
            final SafeHtmlBuilder sb = new SafeHtmlBuilder();
            if(!this.parameterMapProcessor.process(getParameterMap(), sb,
                    ParameterMapProcessor.State.ON_SECURITY_LOOKUP_SUCCESSFUL)) {
                Dialog.warning(sb.toSafeHtml());
            }
        }

        //initially validate all reset fields
        validateDisplay();

        this.lookupSecurityValidationMessages = result.getValidationMsgList();
        if (!this.lookupSecurityValidationMessages.isEmpty()) {
            this.orderValidationMessagePresenter.show(this.lookupSecurityValidationMessages, null);
        }
    }

    protected LookupSecurityInfo getCurrentSecurityInfo() {
        return this.securityInfo;
    }

    protected OrderSecurityFeatureDescriptorBHL getCurrentSecurityFeatures() {
        return this.securityFeatureDescriptor;
    }

    private void initSecurityDependingValues() {
        setAllExchangesValidatorsEnabledDependingOnMandatory(true);
        fillOrderExchangeChoices(this.securityInfo);

        this.display.setLimitCurrencies(this.securityInfo.getCurrencyList());
        this.display.setLimitCurrenciesSelectedItem(null);

        resetLimitClausesSelectedItemToDefault();

        final OrderSecurityInfo security = this.securityInfo.getSecurity();
        final DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> display = this.display;
        display.setInstrumentName(security.getBezeichnung());
        display.setInstrumentType(security.getTyp());
        display.setIsin(security.getISIN());
        display.setWkn(security.getWKN());

        //process currency indicator: quantity or currency
        display.setAmountNominalCurrencyOrUnit(this.orderStrategy.getAmountNominalCurrencyOrUnit());

        //process info for depository/orderStock choice (Lagerstellen)
        if(OrderActionType.AT_SELL.equals(this.orderStrategy.getOrderActionType())) {
            final List<OrderStock> orderStockList = this.securityInfo.getStockBalance();
            this.display.setDepositories(orderStockList);
            if(orderStockList.size() == 1) {
                this.display.setDepositoriesSelectedItem(orderStockList.get(0));
            }
            else {
                this.display.setDepositoriesSelectedItem(null);
            }

            this.display.setDepositoriesEnabled(true);
        }
        else {
            this.display.setDepositories(null);
            this.display.setDepositoriesSelectedItem(null);
            this.display.setDepositoriesEnabled(false);
        }

        //process info for ArbitrageSnippet
        final String mmSecurityId = security.getMMSecurityID();
        final boolean arbitrageButtonVisibleAndEnabled = SessionData.isWithMarketData()
                && StringUtil.hasText(mmSecurityId) && !mmSecurityId.startsWith("&");
        setArbitrageButtonVisibleAndEnabled(arbitrageButtonVisibleAndEnabled);

        final List<AuthorizedRepresentative> authorizedRepresentatives = this.securityFeatureDescriptor.getAuthorizedRepresentatives();
        display.setAuthorizedRepresentatives(authorizedRepresentatives);
        display.setAuthorizedRepresentativesEnabled(!authorizedRepresentatives.isEmpty());

        display.setInstrumentDependingValuesEnabled(true);

        final boolean enabled = ENABLES_AUTHORIZED_REPRESENTATIVES_FLAG.equals(this.securityFeatureDescriptor.getAuthorizedRepresentativesFlag());
        setOrdererNameAndCustomerNumberEnabled(enabled);
        Firebug.debug("<OrderPresenterBHLKGS.initSecurityDependingValues> authorizedRepresentativesFlag===\"" + ENABLES_AUTHORIZED_REPRESENTATIVES_FLAG + "\"? " + enabled);
    }

    private void setArbitrageButtonVisibleAndEnabled(boolean arbitrageButtonVisibleAndEnabled) {
        this.display.setShowArbitrageButtonEnabled(arbitrageButtonVisibleAndEnabled);
        this.display.setShowArbitrageButtonVisible(arbitrageButtonVisibleAndEnabled);
    }

    protected void onLookupSecurityFailure(String isin, Throwable caught, LookupSecurityConfig config) {
        Firebug.error(LOG_PREFIX + ".onLookupSecurityFailure> isin=" + isin, caught);

        INSTANCE.showFailureMessage(caught, isin);

        resetDisplaySecuritySection();

        if(LookupSecurityConfig.PROCESS_PARAMETER_MAP != config) {
            resetSecurityDependingValuesToDefaults();
        }

        validateDisplay();
    }

    protected void reinitInstrumentName() {
        this.display.setInstrumentName(extractNameOfCurrentSecurityInfo());
    }

    /**
     * Starts validation of an order. Do always start with this method.
     * @param order filled order data type
     */
    protected void validateOrder(final OrderDataType order) {
        validateOrder(order, this.lookupSecurityValidationMessages);
    }

    /**
     * Validates an order with the given validation messages.
     * This method is called from within the validation process.
     * Possibly the user has overridden the answers of the messages.
     *
     * @param order filled order data type.
     * @param validationMessages validation messages from the last call to validate order.
     */
    protected void validateOrder(final OrderDataType order, List<ValidationMessage> validationMessages) {
        this.orderStrategy.validateOrder(
                this.orderSession,
                order,
                validationMessages
        );
    }

    protected void onValidateOrderSuccessful(OrderDataType order, List<ValidationMessage> validationMessages) {
        Firebug.debug(LOG_PREFIX + ".onValidateOrderSuccessful>");

        confirmOrder(order, validationMessages);
    }

    protected void onValidateOrderFailed(Throwable caught) {
        Firebug.debug(LOG_PREFIX + ".onValidateOrderFailed>");

        OrderMethods.INSTANCE.handleBackendValidationFailed(this.orderValidationMessagePresenter, caught, this);
    }

    public void onProceedAfterValidateOrderFailed(OrderDataTypeBHL receivedOrderDataType, List<ValidationMessage> editedValidationMessages) {
        final OrderLogBuilder log = new OrderLogBuilder(LOG_PREFIX + ".onProceedAfterValidateOrderFailed", //$NON-NLS$
                this.orderSession);

        DebugUtil.logToServer(log.addValidationMessages(editedValidationMessages).toString());

        validateOrder(receivedOrderDataType, editedValidationMessages);
    }

    public void onCancelAfterValidateOrderFailed(List<ValidationMessage> validationMessages) {
        final OrderLogBuilder log = new OrderLogBuilder(LOG_PREFIX + ".onCancelAfterValidateOrderFailed", //$NON-NLS$
                this.orderSession);

        getOrderViewContainerDisplay().setButtonsLocked(false);

        DebugUtil.logToServer(log.addValidationMessages(validationMessages).toString());
        Firebug.debug(LOG_PREFIX + ".onCancelAfterValidateOrderFailed>");
    }

    @Override
    public void onAnyExceptionAfterValidateOrderFailed(Throwable caught) {
        getOrderViewContainerDisplay().setButtonsLocked(false);
        Firebug.debug(LOG_PREFIX + ".onAnyExceptionAfterValidateOrderFailed>");
        OrderMethods.INSTANCE.showFailureMessage(caught);
    }

    protected void sendOrder(final OrderDataType order, List<ValidationMessage> validationMessages) {
        Firebug.debug(LOG_PREFIX + ".sendOrder>");

        this.orderStrategy.sendOrder(this.orderSession, order, validationMessages);
    }

    protected void onSendOrderSuccessful(OrderDataType order, String externalMessage) {
        Exception catched = null;
        List<OrderConfirmationDisplay.Section> ackOfReceiptModel = null;
        try {
            ackOfReceiptModel = createAckOfReceiptModel(order, externalMessage);
        }
        catch(Exception e) {
            catched = e;
        }
        final Exception catched2 = catched;

        if(ackOfReceiptModel == null) {
            final String message = I18n.I.orderEntryOrderSuccessfullyCreated(order.getOrderNumber());
            Dialog.info(message, new Command() {
                @Override
                public void execute() {
                    dispose();
                    if (catched2 != null) {
                        OrderMethods.INSTANCE.showFailureMessage(catched2);
                    }
                }
            });

            return;
        }

        this.returnParameterMap.setDepotId(this.orderSession.getSecurityAccount().getId());
        this.returnParameterMap.setOrderNumber(order.getOrderNumber());
        this.returnParameterMap.setOrderMessage(externalMessage);
        this.returnParameterMap.setProcessedQuantity(order.getQuantity());

        final OrderConfirmationPresenter ocp = new OrderConfirmationPresenter(this.windowTitle, new OrderConfirmationPresenter.AbstractCallback() {
            @Override
            public void onExecute() {
                dispose();
                final OrderEntryContext ctx = OrderEntryContext.Factory.create();
                ctx.getParameterMap().setDepotId(OrderPresenterBHLKGS.this.orderSession.getSecurityAccount().getId());
                OrderModule.showByDepotId(ctx);
            }

            @Override
            public void onCancel() {
                dispose();
            }
        });

        ocp.setColumns(2);

        ocp.setExecuteButtonVisible(this.orderStrategy.isCreateAnotherOrderForSameDepotButtonVisible());
        ocp.setExecuteButtonText(I18n.I.orderEntryBHLKGSCreateAnotherOrderForSameDepot());
        ocp.setCancelButtonText(I18n.I.ok());
        ocp.setCancelButtonVisible(true);
        ocp.setBackButtonVisible(false);
        ocp.setPrintDateVisible(true);

        ocp.show(ackOfReceiptModel);
    }

    protected void onSendOrderFailed(Throwable caught) {
        Firebug.debug(LOG_PREFIX + ".onSendOrderFailed> calling onValidateOrderFailed");
        onValidateOrderFailed(caught);
        this.returnParameterMap.setOrderMessage(caught.getMessage());
    }

    protected void initDisplay() {
        Firebug.debug(LOG_PREFIX + ".initDisplay>");

        initOrderActionTypes();
        initInvestor();
        initDepots();
        initAccounts();
        initInstrumentChoicePanel();
        initInstrumentSection();
    }

    private void initInstrumentSection() {
        final DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> d = this.display;
        d.setInstrumentName(null);
        d.setInstrumentType(null);
        d.setIsin(null);
        d.setWkn(null);
    }

    private void initInvestor() {
        final DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> display = this.display;
        final OrderSession.OrderSessionBHLKGS os = this.orderSession;

        display.setInvestorName(os.getOwner().getName());
        display.setInvestorNo(os.getOwner().getNumber());
    }

    private void initAccounts() {
        final DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> display = this.display;
        final OrderSession.OrderSessionBHLKGS os = this.orderSession;

        display.setAccounts(os.getAccountList());
        AccountData accountData = null;
        if (os.getAccountList() != null && os.getAccountList().size() > 0) {
            accountData = os.getAccountList().get(0);
            display.setAccountsSelectedItem(accountData);
        }
        setAccountData(accountData);
    }

    private void resetSecurityDependingValuesToDefaults() {
        final DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> display = this.display;

        setArbitrageButtonVisibleAndEnabled(false);

        display.setInstrumentDependingValuesEnabled(false);

        display.setExchangesDomestic(null);
        display.setExchangesForeign(null);
        display.setExchangesOther(null);
        setExchangesSelectedItem(null);

        display.setTradingIndicatorsSelectedItem(null);
        display.setExchangeAccordingToCustomer(false);

        display.setAmountNominal(null);

        display.setDepositories(null);
        display.setDepositoriesSelectedItem(null);

        display.setLimitCurrencies(null);
        display.setLimitCurrenciesSelectedItem(null);

        display.setValidUntil(DisplayBHLKGS.ValidUntil.DEFAULT);
        display.setValidUntilDate(null);

        display.setLimitOrStopLimit(null);
        resetLimitClausesSelectedItemToDefault();
        display.setLimitCurrenciesValidatorEnabled(false);
        display.setLimitTrailingAmountOrPercent(null);
        display.setLimitPeakSizeQuantity(null);
        display.setLimitFee(true);

        display.setOrderersSelectedItem(null);
        display.setOrdererIdentifier(null);
        display.setOrdererCustomerNumber(null);
    }

    private void resetDisplaySecuritySection() {
        this.securityInfo = null;
        this.securityFeatureDescriptor = null;

        initInstrumentSection();
    }

    @Override
    public HashMap<String, String> getCopyOfParameters() {
        return new HashMap<>();
    }

    @Override
    public void setParameters(HashMap<String, String> params) {
        Firebug.debug(LOG_PREFIX + ".setParameters>");

        final String isin = params.get("symbol"); //$NON-NLS$
        if (StringUtil.hasText(isin)) {
            Firebug.debug(LOG_PREFIX + ".setParameters> isin=" + isin);
            lookupSecurity(isin);
            return;
        }
        reinitInstrumentName();
    }

    public String getWindowTitle() {
        return this.windowTitle;
    }

    @Override
    public void onLoadDepotsSuccess(List<Depot> depots, Depot itemToSelect) {
        this.display.setDepots(depots);
        this.display.setDepotsSelectedItem(itemToSelect);
        this.display.setDepotsEnabled(true);
    }

    @Override
    public void onLoadDepotsFailed() {
        this.display.setDepotsEnabled(false);
    }

    private String formattedStringToDoubleString(String formattedString) {
        if(StringUtil.hasText(formattedString)) {
            return Double.toString(NumberFormat.getDecimalFormat().parse(formattedString));
        }
        return formattedString;
    }

    private DisplayBHLKGS.ValidUntil toValidUntil(OrderExpirationType type) {
        if(type == null) {
            return  DisplayBHLKGS.ValidUntil.DEFAULT;
        }

        switch (type) {
            case OET_DAY:
                return DisplayBHLKGS.ValidUntil.TODAY;
            case OET_DATE:
                return DisplayBHLKGS.ValidUntil.DATE;
            case OET_ULTIMO:
                return DisplayBHLKGS.ValidUntil.ULTIMO;
            case OET_NA:
            default:
                return DisplayBHLKGS.ValidUntil.DEFAULT;
        }
    }

    public ParameterMap getParameterMap() {
        if(this.orderEntryContext == null) {
            return null;
        }
        return this.orderEntryContext.getParameterMap();
    }

    @Override
    public ParameterMap getReturnParameterMap() {
        return this.returnParameterMap;
    }

    @Override
    public void onPresenterDisposed(PresenterDisposedEvent event) {
        Firebug.debug("<OrderPresenterBHLKGS.onPresenterDisposed>");
        if(this.loadingIndicatorHandlerRegistration != null) {
            this.loadingIndicatorHandlerRegistration.removeHandler();
            this.loadingIndicatorHandlerRegistration = null;
            Firebug.debug("<OrderPresenterBHLKGS.onPresenterDisposed>  removed loading indicator handler registration");
        }
    }

    public interface OrderStrategy {
        void sendOrder(OrderSession orderSession,
                       OrderDataType order,
                       List<ValidationMessage> validationMessages);

        void validateOrder(OrderSession orderSession,
                           OrderDataType order,
                           List<ValidationMessage> validationMessages);

        void setPresenter(OrderPresenterBHLKGS presenterBHLKGS);
        String getName();

        void init();
        void initOrderDataType(OrderDataTypeBHL order, OrderLogBuilder logBuilder);

        OrderTransaktionType getOrderTransactionType();
        OrderActionType getOrderActionType();

        OrderCurrency getDepotCurrencyData();
        boolean isQuotedPerUnit();

        String getAmountNominalCurrencyOrUnit();
        boolean isDisplayOrderNumberInConfirmationView();

        String getInstrumentName();
        boolean isOrderActionChangeable();
        boolean isInstrumentChangeable();
        boolean isDepotChangeable();
        boolean isCreateAnotherOrderForSameDepotButtonVisible();
        String getConfirmButtonText(OrderTransaktionType typ);
        String getSendOrderSuccessfulMessage(OrderDataTypeBHL order);

        void onBusinessSegmentChanged();

        String toLogStrategyRootNodeName(String s);
    }

    private abstract static class AbstractOrderStrategy implements OrderStrategy {
        private OrderPresenterBHLKGS presenter;

        @Override
        public final void setPresenter(OrderPresenterBHLKGS presenterBHLKGS) {
            if(this.presenter != null) {
                throw new IllegalStateException("Presenter can be set only once!"); //$NON-NLS$
            }
            this.presenter = presenterBHLKGS;
        }

        protected final OrderPresenterBHLKGS getPresenter() {
            return this.presenter;
        }

        @Override
        public String toLogStrategyRootNodeName(String s) {
            return "strategy:" + getName() + "." + s; //$NON-NLS$
        }
    }

    public static class NewOrderStrategy extends AbstractOrderStrategy {
        public static final String BUSINESS_SEGMENT_WITHOUT_CONSULTATION_TABEX_KEY = "2"; //$NON-NLS$

        @Override
        public void sendOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages) {
            Firebug.debug("<NewOrderStrategy.sendOrder>");

            final AsyncCallback<SendOrderDataResponse> callback = new AsyncCallback<SendOrderDataResponse>() {
                @Override
                public void onSuccess(SendOrderDataResponse result) {
                    getPresenter().onSendOrderSuccessful(result.getOrder(), result.getExternalMessage());
                }

                @Override
                public void onFailure(Throwable caught) {
                    getPresenter().onSendOrderFailed(caught);
                }
            };

            OrderMethods.INSTANCE.sendOrder(orderSession, order, validationMessages, callback);
        }

        @Override
        public void validateOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages) {
            Firebug.debug("<NewOrderStrategy.validateOrder>");

            final AsyncCallback<ValidateOrderDataResponse> callback = new AsyncCallback<ValidateOrderDataResponse>() {
                @Override
                public void onSuccess(ValidateOrderDataResponse result) {
                    getPresenter().onValidateOrderSuccessful(result.getOrder(), result.getValidationMsgList());
                }

                @Override
                public void onFailure(Throwable caught) {
                    getPresenter().onValidateOrderFailed(caught);
                }
            };

            OrderMethods.INSTANCE.validateOrder(orderSession, order, validationMessages, callback);
        }

        @Override
        public String getName() {
            return "NewOrder"; //$NON-NLS$
        }

        @Override
        public void init() {
            final OrderPresenterBHLKGS p = getPresenter();

            p.initDisplay();
            p.initDisplayToCreateOrder();
        }

        @Override
        public void initOrderDataType(OrderDataTypeBHL order, OrderLogBuilder log) {
            final OrderPresenterBHLKGS p = getPresenter();

            order.setActivityId(p.activityInstanceId);
            log.add("order.activityId", order.getActivityId());   // $NON-NLS$
            log.add(toLogStrategyRootNodeName("activityInstanceId"), p.activityInstanceId);   // $NON-NLS$

            order.setOrderSuggestionRef(p.activityListEntryId);
            log.add("order.orderSuggestionRef", order.getOrderSuggestionRef());  // $NON-NLS$
            log.add(toLogStrategyRootNodeName("activityListEntryId"), p.activityListEntryId);  // $NON-NLS$
        }

        @Override
        public OrderCurrency getDepotCurrencyData() {
            return getPresenter().securityFeatureDescriptor.getDepotCurrencyData();
        }

        @Override
        public boolean isQuotedPerUnit() {
            return getPresenter().securityInfo.getSecurity().isISQuotedPerUnit();
        }

        @Override
        public String getAmountNominalCurrencyOrUnit() {
            final OrderPresenterBHLKGS p = getPresenter();

            if(p.securityInfo.getSecurity().isISQuotedPerUnit()) {
                return I18n.I.orderEntryAmountNominalPerUnit();
            }

            return p.securityFeatureDescriptor.getDepotCurrencyData().getKuerzel();
        }

        @Override
        public boolean isDisplayOrderNumberInConfirmationView() {
            return false;
        }

        @Override
        public String getConfirmButtonText(OrderTransaktionType type) {
            return OeRenderers.ORDER_TRANSACTION_TYPE_RENDERER.render(type);
        }

        @Override
        public String getSendOrderSuccessfulMessage(OrderDataTypeBHL order) {
            return I18n.I.orderEntryOrderSuccessfullyCreated(order.getOrderNumber());
        }

        @Override
        public void onBusinessSegmentChanged() {
            final DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> d = getPresenter().display;
            final TextWithKey item = d.getBusinessSegmentsSelectedItem();

            final boolean enable = item == null || item.getKey() == null
                    || !BUSINESS_SEGMENT_WITHOUT_CONSULTATION_TABEX_KEY.equals(item.getKey().trim());

            Firebug.debug("<NewOrderStrategy.onBusinessSegmentChanged> enable minutesOfTheConsultation? " + enable);

            d.setMinutesOfTheConsultationTypesEnabled(enable);
            d.setMinutesOfTheConsultationNumberEnabled(enable);
            d.setMinutesOfTheConsultationDateEnabled(enable);

            if(!enable) {
                d.setMinutesOfTheConsultationTypesSelectedItem(null);
                d.setMinutesOfTheConsultationNumber(null);
                d.setMinutesOfTheConsultationDate(null);
            }
        }

        @Override
        public OrderTransaktionType getOrderTransactionType() {
            return OrderUtils.toOrderTransactionType(getOrderActionType());
        }

        @Override
        public OrderActionType getOrderActionType() {
            final ParameterMap parameterMap = getPresenter().getParameterMap();
            final OrderSession.OrderSessionBHLKGS orderSession = getPresenter().orderSession;

            if(parameterMap != null) {
                final OrderActionType orderActionType = parameterMap.getOrderActionType();
                if(OrderUtils.isOrderActionTypeSupported(orderSession, orderActionType)) {
                    return orderActionType;
                }
            }

            return getPresenter().display.getOrderActionTypesSelectedItem();
        }

        @Override
        public String getInstrumentName() {
            return null;
        }

        @Override
        public boolean isOrderActionChangeable() {
            final ParameterMap parameterMap = this.getPresenter().getParameterMap();
            return parameterMap == null || parameterMap.getOrderActionType() == null;
        }

        @Override
        public boolean isInstrumentChangeable() {
            final ParameterMap parameterMap = this.getPresenter().getParameterMap();
            return parameterMap == null || !StringUtil.hasText(parameterMap.getIsin());
        }

        @Override
        public boolean isDepotChangeable() {
            final ParameterMap parameterMap = this.getPresenter().getParameterMap();
            return parameterMap == null || !StringUtil.hasText(parameterMap.getDepotIdInitial());
        }

        @Override
        public boolean isCreateAnotherOrderForSameDepotButtonVisible() {
            final ParameterMap parameterMap = this.getPresenter().getParameterMap();
            return parameterMap == null || !StringUtil.hasText(parameterMap.getActivityInstanceId());
        }
    }

    public static class ChangeOrderStrategy extends AbstractOrderStrategy {
        private OrderDataTypeBHL orderDataTypeBHL;
        private OrderSecurityFeatureDescriptorBHL securityFeatures;
        private LookupSecurityInfo securityInfo;

        public ChangeOrderStrategy(OrderDataTypeBHL orderDataTypeBHL, OrderSecurityFeatureDescriptorBHL securityFeatures, LookupSecurityInfo securityInfo) {
            this.orderDataTypeBHL = orderDataTypeBHL;
            this.securityFeatures = securityFeatures;
            this.securityInfo = securityInfo;
        }

        @Override
        public void sendOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages) {
            Firebug.debug("<ChangeOrderStrategy.sendOrder>");

            final AsyncCallback<ChangeOrderDataResponse> callback = new AsyncCallback<ChangeOrderDataResponse>() {
                @Override
                public void onSuccess(ChangeOrderDataResponse result) {
                    Firebug.debug("<ChangeOrderStrategy.sendOrder> result.order==null" + (result.getOrder() == null));
                    getPresenter().onSendOrderSuccessful(result.getOrder(), result.getExternalMessage());
                }

                @Override
                public void onFailure(Throwable caught) {
                    getPresenter().onSendOrderFailed(caught);
                }
            };

            OrderMethods.INSTANCE.changeOrder(orderSession, order, validationMessages, callback);
        }

        @Override
        public void validateOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages) {
            Firebug.debug("<ChangeOrderStrategy.validateOrder>");

            final AsyncCallback<ValidateChangeOrderDataResponse> callback = new AsyncCallback<ValidateChangeOrderDataResponse>() {
                @Override
                public void onSuccess(ValidateChangeOrderDataResponse result) {
                    getPresenter().onValidateOrderSuccessful(result.getOrder(), result.getValidationMsgList());
                }

                @Override
                public void onFailure(Throwable caught) {
                    getPresenter().onValidateOrderFailed(caught);
                }
            };

            OrderMethods.INSTANCE.validateChangeOrder(orderSession, order, validationMessages, callback);
        }

        @Override
        public String getName() {
            return "ChangeOrder"; //$NON-NLS$
        }

        @Override
        public void init() {
            final OrderPresenterBHLKGS p = getPresenter();

            p.securityFeatureDescriptor = this.securityFeatures;
            p.securityInfo = this.securityInfo;

            Firebug.debug("<ChangeOrderStrategy.init> authorizedRepresentativesFlag=\"" + p.securityFeatureDescriptor.getAuthorizedRepresentativesFlag() + "\"");

            p.initDisplay();
            p.initSecurityDependingValues();
            p.initDisplayToChangeOrder(this.orderDataTypeBHL);
        }

        @Override
        public void initOrderDataType(OrderDataTypeBHL order, OrderLogBuilder log) {
            final String orderNumber = this.orderDataTypeBHL.getOrderNumber();
            order.setOrderNumber(orderNumber);
            log.add("order.orderNumber", orderNumber); //$NON-NLS$

            final String changeNumber = this.orderDataTypeBHL.getChangeNumber();
            order.setChangeNumber(changeNumber);
            log.add("order.changeNumber", changeNumber); //$NON-NLS$

            final String activityInstanceId = this.orderDataTypeBHL.getActivityId();
            order.setActivityId(activityInstanceId);
            log.add("order.activityId", activityInstanceId);  // $NON-NLS$

            final String activityListEntryId = this.orderDataTypeBHL.getOrderSuggestionRef();
            order.setOrderSuggestionRef(activityListEntryId);
            log.add("order.orderSuggestionRef", activityListEntryId);  // $NON-NLS$
        }

        @Override
        public OrderCurrency getDepotCurrencyData() {
            return this.orderDataTypeBHL.getDepotCurrencyData();
        }

        @Override
        public boolean isQuotedPerUnit() {
            return this.orderDataTypeBHL.isIsQuotedPerUnit();
        }

        @Override
        public String getAmountNominalCurrencyOrUnit() {
            if(this.orderDataTypeBHL.isIsQuotedPerUnit()) {
                return I18n.I.orderEntryAmountNominalPerUnit();
            }
            return this.orderDataTypeBHL.getDepotCurrencyData().getKuerzel();
        }

        @Override
        public boolean isDisplayOrderNumberInConfirmationView() {
            return true;
        }

        @Override
        public String getConfirmButtonText(OrderTransaktionType type) {
            switch(type) {
                case TT_BUY:
                    return I18n.I.orderEntryChangeBuyOrder();
                case TT_SELL:
                    return I18n.I.orderEntryChangeSellOrder();
                case TT_SUBSCRIBE:
                default:
                    return I18n.I.orderEntryChangeOrder();
            }
        }

        @Override
        public String getSendOrderSuccessfulMessage(OrderDataTypeBHL order) {
            return I18n.I.orderEntryOrderSuccessfullyChanged(order.getOrderNumber());
        }

        @Override
        public void onBusinessSegmentChanged() {
            /* do nothing; see AS-411 for further reference */
        }

        @Override
        public OrderTransaktionType getOrderTransactionType() {
            return this.orderDataTypeBHL.getTyp();
        }

        @Override
        public OrderActionType getOrderActionType() {
            return OrderUtils.toOrderActionType(getOrderTransactionType());
        }

        @Override
        public String getInstrumentName() {
            return this.orderDataTypeBHL.getSecurityData().getBezeichnung();
        }

        @Override
        public boolean isOrderActionChangeable() {
            return false;
        }

        @Override
        public boolean isInstrumentChangeable() {
            return false;
        }

        @Override
        public boolean isDepotChangeable() {
            return false;
        }

        @Override
        public boolean isCreateAnotherOrderForSameDepotButtonVisible() {
            return false;
        }
    }
}