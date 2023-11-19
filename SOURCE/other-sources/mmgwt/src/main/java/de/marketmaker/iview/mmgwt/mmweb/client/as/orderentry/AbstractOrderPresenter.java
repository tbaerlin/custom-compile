/*
 * AbstractOrderPresenter.java
 *
 * Created on 30.10.12 10:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.history.OrderEntryHistorySupport;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ParameterMap;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SearchMethods;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.SelectPmSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.ConfigurationPresenter;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView.SymbolParameterType;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.TabbedSnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SymbolUtil;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.LookupSecurityDataResponse;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderSecurityFeatureDescriptor;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptor;
import de.marketmaker.iview.pmxml.OrderSingleQuote;
import de.marketmaker.iview.pmxml.OrderTransaktionType;
import de.marketmaker.iview.pmxml.SendOrderDataResponse;
import de.marketmaker.iview.pmxml.SessionState;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ValidateOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidationMessage;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderConfirmationDisplay.Section;
import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderMethods.INSTANCE;
import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderMethods.PmxmlValidateOrderResponseException;
import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderValidationMessagePresenter.Callback;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
abstract class AbstractOrderPresenter<P extends AbstractOrderPresenter, F extends OrderSessionFeaturesDescriptor, S extends OrderSession<F>>
        extends AbstractOrderViewContainerPresenter
        implements DisplayAbstract.PresenterAbstract, ConfigurableSnippet,
        LoadDepotsAndInvestorsMethod.InvestorsCallback, LoadDepotsAndInvestorsMethod.DepotsCallback,
        HasReturnParameterMap, IsActivityAware {

    public enum LookupSecurityConfig { DEFAULT, PROCESS_HISTORY_ITEM, PROCESS_PARAMETER_MAP }

    public static final String HISTORY_CURRENCY_ID = "currency.id"; //$NON-NLS$
    public static final String HISTORY_EXCHANGE_ID = "exchange.id"; //$NON-NLS$
    public static final String HISTORY_SECURITY_ISIN = "security.isin"; //$NON-NLS$
    public static final String HISTORY_TRANSACTION_NAME = "transaction.name"; //$NON-NLS$

    /**
     * After doing any form validation, this method should call {@link #validateOrder(OrderDataType, List)}.
     */
    protected abstract void validateOrder();

    /**
     * A null model indicates that the order confirmation dialog should not be shown.
     */
    protected abstract List<Section> createVerificationModel(OrderDataType order);

    /**
     * A null model indicates that the order confirmation dialog should not be shown.
     */
    protected abstract List<Section> createAckOfReceiptModel(OrderDataType order);

    protected abstract void updateExpectedMarketValue();

    private final String CLASS_NAME = this.getClass().getName();

    private final String SIMPLE_CLASS_NAME = CLASS_NAME.substring(CLASS_NAME.lastIndexOf('.') + 1);
    private final String CLASS_PREFIX = SIMPLE_CLASS_NAME + ".super"; //$NON-NLS$
    private final String LOG_PREFIX = "<" + CLASS_PREFIX; //$NON-NLS$
    private final String windowTitle;

    private final DisplayAbstract display;

    private OrderEntryContext orderEntryContext;
    private ParameterMap returnParameterMap;

    private final S orderSession;

    private OrderAction currentOrderAction = null;

    private List<ValidationMessage> lookupSecurityValidationMessages = null;

    private LookupSecurityInfo securityInfo = null;

    private int currentQuoteIndex = -1;

    private final ConfigurationPresenter snippetConfigurationView;

    private final OrderValidationMessagePresenter orderValidationMessagePresenter;

    private OrderSecurityFeatureDescriptor securityFeatureDescriptor = null;

    private final OrderEntryHistorySupport historySupport = new OrderEntryHistorySupport();

    private final boolean withDepotAndInvestorChoiceSupport;

    protected final ParameterMapProcessor<P, ? extends DisplayAbstract.PresenterAbstract, ? extends DisplayAbstract<? extends DisplayAbstract.PresenterAbstract>> parameterMapProcessor;

    private String activityInstanceId;
    private String activityListEntryId;

    protected AbstractOrderPresenter(DisplayAbstract<? extends DisplayAbstract.PresenterAbstract> display, S orderSession, String windowTitle, ParameterMapProcessor<P, ? extends DisplayAbstract.PresenterAbstract, ? extends DisplayAbstract<? extends DisplayAbstract.PresenterAbstract>> parameterMapProcessor) {
        this(display, orderSession, windowTitle, true, parameterMapProcessor);
    }

    @SuppressWarnings("unchecked")
    protected AbstractOrderPresenter(DisplayAbstract<? extends DisplayAbstract.PresenterAbstract> display,
                                     S orderSession, String windowTitle, boolean withDepotAndInvestorChoiceSupport,
                                     ParameterMapProcessor<P, ? extends DisplayAbstract.PresenterAbstract,
            ? extends DisplayAbstract<? extends DisplayAbstract.PresenterAbstract>>  parameterMapProcessor) {
        super(new OrderViewContainerView(windowTitle));

        this.parameterMapProcessor = parameterMapProcessor;
        this.parameterMapProcessor.setPresenter((P)this);

        final OrderViewContainerDisplay containerDisplay = this.getOrderViewContainerDisplay();
        containerDisplay.setPresenter(this);

        this.display = display;
        this.display.setPresenter(this);

        this.windowTitle = windowTitle;
        this.orderSession = orderSession;

        this.returnParameterMap = new ParameterMap(this.orderSession.getSecurityAccount().getId());

        this.withDepotAndInvestorChoiceSupport = withDepotAndInvestorChoiceSupport;

        if(OrderUtils.isWithDmXmlSymbolSearch()) {
            this.snippetConfigurationView = new TabbedSnippetConfigurationView(this, SymbolParameterType.ISIN);
        }
        else {
            this.snippetConfigurationView = new SnippetConfigurationView(this, SymbolParameterType.ISIN);
        }

        this.orderValidationMessagePresenter = new OrderValidationMessagePresenter(this.orderSession, new OrderValidationMessageView());

        init();
    }

    private void init() {
        initHistorySupport();

        final Set<ShellMMType> tradableSecurityTypes = this.orderSession.getTradableSecurityTypes();

        if(OrderUtils.isWithDmXmlSymbolSearch()) {
            final SelectPmSymbolForm dmxmlSymbolForm = SelectPmSymbolForm.createDmWithOrderEntryAvail(
                    this.snippetConfigurationView.getParams(),
                    tradableSecurityTypes);
            dmxmlSymbolForm.setHeaderVisible(false);
            dmxmlSymbolForm.setFooter(false);
            this.snippetConfigurationView.addConfigurationWidget(dmxmlSymbolForm, IconImage.get("mm-icon-16"), I18n.I.orderEntryInstrumentSearchMmTab()); //$NON-NLS$
        }

        final SelectPmSymbolForm pmxmlSymbolForm = SelectPmSymbolForm.createPmWithOrderEntryAvail(
                this.snippetConfigurationView.getParams(),
                tradableSecurityTypes);
        pmxmlSymbolForm.setHeaderVisible(false);
        pmxmlSymbolForm.setFooter(false);
        this.snippetConfigurationView.addConfigurationWidget(pmxmlSymbolForm, IconImage.get("pm-icon-16"), I18n.I.orderEntryInstrumentSearchPmTab()); //$NON-NLS$

        this.snippetConfigurationView.addActionPerformedHandler(new ActionPerformedHandler() {
            @Override
            public void onAction(ActionPerformedEvent event) {
                if (TabbedSnippetConfigurationView.Actions.CANCEL.name().equals(event.getKey())) {
                    onSymbolSearchDialogCanceled();
                }
            }
        });
    }

    @Override
    public String getActivityInstanceId() {
        return activityInstanceId;
    }

    @Override
    public void setActivityInstanceId(String activityInstanceId) {
        this.activityInstanceId = activityInstanceId;
    }

    @Override
    public String getActivityListEntryId() {
        return activityListEntryId;
    }

    @Override
    public void setActivityListEntryId(String activityListEntryId) {
        this.activityListEntryId = activityListEntryId;
    }

    protected void onSymbolSearchDialogCanceled() {
        reinitInstrumentName();
    }

    @Override
    public void onSymbolSearchTextBoxValueChanged(ValueChangeEvent<String> event) {
        final String rawValue = event.getValue();
        if(!StringUtil.hasText(rawValue)) {
            return;
        }
        final String value = rawValue.trim().toUpperCase();

        if(SymbolUtil.isIsin(value)) {
            lookupSecurity(value);
            return;
        }
        if(SymbolUtil.isWkn(value)) {
            SearchMethods.INSTANCE.instrumentSearchWkn(value, this.orderSession.getTradableSecurityTypes(), new AsyncCallback<ShellMMInfo>() {
                @Override
                public void onFailure(Throwable caught) {
                    Firebug.error("WKN search failed. Showing symbol search dialog", caught);
                    AbstractOrderPresenter.this.snippetConfigurationView.show(value);
                }

                @Override
                public void onSuccess(ShellMMInfo result) {
                    Firebug.debug("ISIN for WKN " + value + " found " + result.getISIN());
                    lookupSecurity(result.getISIN());
                }
            });
            return;
        }

        AbstractOrderPresenter.this.snippetConfigurationView.show(value);
    }

    private void initHistorySupport() {
        final OrderEntryHistorySupport hs = this.historySupport;

        hs.load();

        getOrderViewContainerDisplay().withToolbar().addOrderHistoryTool(hs.getItems());

        hs.addProcessStep(new OrderEntryHistorySupport.ProcessStep() {
            @Override
            public void process(OrderEntryHistorySupport.Item historyItem) {
                processHistoryItemBeforeSecurityLookup(historyItem);
            }
        });

        hs.addProcessStep(new OrderEntryHistorySupport.ProcessStep() {
            @Override
            public void process(OrderEntryHistorySupport.Item historyItem) {
                processHistoryItemAfterSecurityLookup(historyItem);
            }
        });
    }

    protected DisplayAbstract getDisplay() {
        return this.display;
    }

    protected abstract void validateDisplay();

    @Override
    public void onDepotsSelectedItemChanged(ChangeEvent event) {
        final Depot depot = getDisplay().getDepotsSelectedItem();
        if(depot == null) {
            Firebug.warn(LOG_PREFIX + ".onDepotsSelectedItemChanged> selected depot is null. nothing to do!");
            return;
        }

        final String depotId = depot.getId();
        final OrderActionType orderActionType = this.currentOrderAction.getValue();
        final String isin = extractIsinOfCurrentSecurityInfo();

        Firebug.debug(LOG_PREFIX + ".onDepotsSelectedItemChanged> depotId=" + depotId + " orderActionType=" + orderActionType.name() + " isin=" + isin);

        if(StringUtil.hasText(depotId)) {
            cancelOrder();
            final ParameterMap parameterMap = this.orderEntryContext.getParameterMap();
            parameterMap.setDepotId(depotId);
            parameterMap.setOrderActionType(orderActionType);
            parameterMap.setIsin(isin);
            OrderModule.showByDepotId(this.orderEntryContext);
        }
    }

    @Override
    public void onInvestorChoiceChanged(ChangeEvent event) {
        final String investorId = getDisplay().getSelectedInvestorId();
        final OrderActionType orderActionType = this.currentOrderAction.getValue();
        final String isin = extractIsinOfCurrentSecurityInfo();

        Firebug.debug(LOG_PREFIX + ".onInvestorChoiceChanged> investorId=" + investorId);

        if(StringUtil.hasText(investorId)) {
            cancelOrder();

            final OrderEntryContext context = OrderEntryContext.Factory.create();
            final ParameterMap parameterMap = context.getParameterMap();
            parameterMap.setInvestorId(investorId);
            parameterMap.setOrderActionType(orderActionType);
            parameterMap.setIsin(isin);

            OrderModule.showByInvestorId(context);
        }
    }

    private String extractIsinOfCurrentSecurityInfo() {
        if(this.securityInfo != null && this.securityInfo.getSecurity() != null) {
            return this.securityInfo.getSecurity().getISIN();
        }
        return null;
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
        validateOrder();
    }

    @Override
    public void onOrderActionChanged(ChangeEvent event) {
        Firebug.debug(LOG_PREFIX + ".onOrderActionChanged>");

        final DisplayAbstract display = getDisplay();
        final OrderAction action = this.currentOrderAction = display.getSelectedOrderAction();

        display.setSelectedOrderAction(action);

        /* looking up the security is here necessary, because it may already produce validation messages,
         * which we have to send along with our first validateOrder request. So we have to throw away any validation
         * messages from previous validateOrder/sendOrder requests, because validation messages of order type
         * ttSell must not be sent along with an order of type ttBuy.
         */
        if(this.securityInfo != null) {
            final String isin = this.securityInfo.getSecurity().getISIN();
            Firebug.debug(LOG_PREFIX + ".onOrderActionChanged> looking up security: isin=" + isin);
            lookupSecurity(isin);
        }
    }

    @Override
    public void onAccountChanged(ChangeEvent event) {
        final DisplayAbstract display = getDisplay();
        final AccountData accountData = this.orderSession.getAccountList().get(display.getAccountsSelectedItem());

        display.setAccountNo(accountData.getNumber());
        display.setAccountBalance(accountData.getBalance(), accountData.getCurrency().getKuerzel());
    }

    @Override
    public void onSymbolSearchButtonClicked(ClickEvent event) {
        this.snippetConfigurationView.getParams().remove(SelectSymbolForm.PRESET_SEARCH_STRING);
        this.snippetConfigurationView.show();
    }

    @Override
    public void onSelectSymbolFromDepotSelected(SelectionEvent<OrderSecurityInfo> securityDataSelectionEvent) {
        final OrderSecurityInfo item = securityDataSelectionEvent.getSelectedItem();
        final String isin = item.getISIN();
        lookupSecurity(isin);
    }

    @Override
    public void onExchangeChangedHandler(ChangeEvent event) {
        doExchangeOrExchangeCurrencyChanged();
    }

    @Override
    public void onExchangeCurrencyChangedHandler(ChangeEvent event) {
        doExchangeOrExchangeCurrencyChanged();
    }

    private void doExchangeOrExchangeCurrencyChanged() {
        final DisplayAbstract display = getDisplay();

        final OrderExchangeInfo exchange = this.securityInfo.getExchangeList().get(display.getSelectedExchangeChoice());
        final CurrencyAnnotated currency = this.securityInfo.getCurrencyList().get(display.getSelectedExchangeCurrency());

        final OrderSingleQuote quote = findQuoteForExchangeAndExchangeCurrency(exchange, currency);

        displayExchangeCurrency(currency);
        displayQuote(quote);
    }

    protected OrderSingleQuote findQuoteForExchangeAndExchangeCurrency(OrderExchangeInfo exchange, CurrencyAnnotated currencyAnnotated) {
        if(exchange == null || exchange.getID() == null || currencyAnnotated == null ||
                currencyAnnotated.getCurrency() == null || currencyAnnotated.getCurrency().getId() == null) {
            Firebug.debug(LOG_PREFIX + ".findQuoteForExchangeAndExchangeCurrency> no quote found: exchange, currencyAnnotated, currencyAnnotated.currency or their IDs are null!");
            return null;
        }

        final String exchangeId = exchange.getID();
        final String currencyId = currencyAnnotated.getCurrency().getId();

        Firebug.debug(LOG_PREFIX + ".findQuoteForExchangeAndExchangeCurrency> exchangeId=" + exchangeId + ", currencyId=" + currencyId);

        final List<OrderSingleQuote> quotes = this.securityInfo.getQuoteList();
        for (int i = 0; i < quotes.size(); i++) {
            final OrderSingleQuote quote = quotes.get(i);
            Firebug.debug("Quote " + i + " exchangeId='"+ quote.getExchangeID() + "' currencyId='"+ quote.getCurrency().getId() + "' value=" + quote.getValue());
            if (exchangeId.equals(quote.getExchangeID()) && currencyId.equals(quote.getCurrency().getId())) {
                this.currentQuoteIndex = i;
                return quote;
            }
        }
        this.currentQuoteIndex = -1;

        //TODO: improve i18n for null value
        final String escapedCurrency = SafeHtmlUtils.htmlEscape(currencyAnnotated.getCurrency().getKuerzel());
        final String escapedExchange = SafeHtmlUtils.htmlEscape(exchange.getName());
        final String message = I18n.I.orderEntryNoPriceForExchangeAndExchangeCurrency(escapedExchange, escapedCurrency);
        INSTANCE.showFailureMessage(SafeHtmlUtils.fromTrustedString(message));
        return null;
    }

    protected void displayExchangeCurrency(CurrencyAnnotated currency) {
        final DisplayAbstract display = getDisplay();
        if(currency == null) {
            display.setCurrencyLabels(null);
            return;
        }
        display.setCurrencyLabels(currency.getCurrency().getKuerzel());
    }

    protected void displayQuote(OrderSingleQuote quote) {
        final DisplayAbstract display = getDisplay();

        if (quote == null) {
            display.setPrice(null);
            display.setPriceDate(null);
            return;
        }

        display.setPrice(formatPmPriceAndCurrency(quote.getValue(), quote.getCurrency()));
        display.setPriceDate(PmRenderers.DATE_STRING.render(quote.getDate()));
    }

    protected void cancelOrder() {
        Firebug.debug(LOG_PREFIX + ".cancelOrder>");
        dispose();
    }

    @Override
    public void show(OrderEntryContext orderEntryContext) {
        this.orderEntryContext = orderEntryContext;

        final PresenterDisposedHandler presenterDisposedHandler = this.orderEntryContext.getPresenterDisposedHandler();
        if(presenterDisposedHandler != null) {
            addPresenterDisposedHandler(presenterDisposedHandler);
        }
        show();
    }

    public void show() {
        Firebug.debug(LOG_PREFIX + ".show>");

        final S session = getCurrentOrderSession();
        final SessionState sessionState = session.getSessionState();

        if(SessionState.SS_INITIALISED.equals(sessionState)) {
            final IllegalStateException ise = new IllegalStateException("Order session must be in state SS_AUTHENTICATED before show can be called!"); //$NON-NLS$
            Firebug.error(LOG_PREFIX + ".show>", ise);
            throw ise;
        }

        showDialog();
    }

    protected void showDialog() {
        final OrderViewContainerDisplay containerDisplay = getOrderViewContainerDisplay();
        final SafeHtmlBuilder messages = new SafeHtmlBuilder();

        containerDisplay.setContent(getDisplay().getOrderView());
        initDisplay();

        final boolean privacyModeActive = PrivacyMode.isActive();
        if(StringUtil.hasText(getParameterMap().getActivityInstanceId()) || privacyModeActive) {
            loadDepotsOnly(privacyModeActive);
        }
        else {
            loadDepotsAndInvestors(false);
        }

        containerDisplay.show();

        final boolean noMessages = processParameterMap(messages, ParameterMapProcessor.State.ON_INIT);

        validateDisplay();

        if(!noMessages) {
            Dialog.warning(messages.toSafeHtml());
        }
    }

    protected void loadDepotsAndInvestors(boolean privacyModeActive) {
        if(this.withDepotAndInvestorChoiceSupport) {
            new LoadDepotsAndInvestorsMethod(this.orderSession, LoadDepotsAndInvestorsMethod.Type.STANDALONE, privacyModeActive, this, this).invoke();
        }
    }

    protected void loadDepotsOnly(boolean privacyModeActive) {
        if(this.withDepotAndInvestorChoiceSupport) {
            new LoadDepotsAndInvestorsMethod(this.orderSession, LoadDepotsAndInvestorsMethod.Type.ACTIVITY, privacyModeActive, this).invoke();
        }
    }

    protected boolean processParameterMap(SafeHtmlBuilder messages, ParameterMapProcessor.State state) {
        return this.parameterMapProcessor.process(getParameterMap(), messages, state);
    }

    protected void confirmOrder(final OrderDataType order, final List<ValidationMessage> validationMessages) {
        Firebug.debug(LOG_PREFIX + ".confirmOrder>");
        List<Section> model = createVerificationModel(order);

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
            public void onBack() {
                onConfirmOrderCanceled();
            }
        };

        final OrderConfirmationPresenter ocp = new OrderConfirmationPresenter(this.windowTitle, callback);
        ocp.setColumns(2);
        ocp.setExecuteButtonText(OeRenderers.ORDER_TRANSACTION_TYPE_RENDERER.render(order.getTyp()));
        ocp.setCancelButtonVisible(false);
        ocp.setBackButtonVisible(true);
        ocp.setPrintDateVisible(true);
        ocp.show(model);
    }

    protected void onConfirmOrderCanceled() {
        DebugUtil.logToServer(new OrderLogBuilder(CLASS_PREFIX + ".onConfirmOrderCanceled", //$NON-NLS$
                this.getCurrentOrderSession()).toString());
    }

    protected void onConfirmOrderProceed(OrderDataType order, List<ValidationMessage> validationMessages) {
        DebugUtil.logToServer(new OrderLogBuilder(CLASS_PREFIX + ".onConfirmOrderProceed", //$NON-NLS$
                this.getCurrentOrderSession()).toString());

        sendOrder(order, validationMessages);
    }

    protected void lookupSecurity(final String isin) {
        lookupSecurity(isin, LookupSecurityConfig.DEFAULT);
    }

    protected void lookupSecurity(final String isin, final LookupSecurityConfig config) {
        final OrderTransaktionType transactionType = OrderUtils.toOrderTransactionType(this.getCurrentOrderAction());

        Firebug.debug(LOG_PREFIX + ".lookupSecurity> isin=" + isin + " orderTransactionType=" + transactionType);

        INSTANCE.lookupSecurity(this.orderSession, transactionType, isin, new AsyncCallback<LookupSecurityDataResponse>() {
            @Override
            public void onSuccess(LookupSecurityDataResponse result) {
                onLookupSecuritySuccess(result, config);
            }

            @Override
            public void onFailure(Throwable caught) {
                onLookupSecurityFailure(caught, isin, config);
            }
        });
    }

    protected void onLookupSecurityFailure(Throwable caught, String isin, LookupSecurityConfig config) {
        doOnLookupSecurityFailure(isin, config, caught);

        if (LookupSecurityConfig.PROCESS_HISTORY_ITEM == config) {
            this.historySupport.cancelPendingHistoryItem();
        }
    }

    protected void onLookupSecuritySuccess(LookupSecurityDataResponse result, LookupSecurityConfig config) {
        doOnLookupSecuritySuccess(result, config);

        if (LookupSecurityConfig.PROCESS_HISTORY_ITEM == config) {
            this.historySupport.processPendingHistoryItem();
        }
    }

    protected void doOnLookupSecuritySuccess(LookupSecurityDataResponse result, LookupSecurityConfig config) {
        Firebug.debug(LOG_PREFIX + ".doOnLookupSecuritySuccess>");
        this.securityInfo = result.getSecurityInfo();
        this.securityFeatureDescriptor = result.getFeatures();
        this.currentQuoteIndex = -1;

        final OrderSecurityInfo security = this.securityInfo.getSecurity();
        final DisplayAbstract display = getDisplay();

        display.setInstrumentName(security.getBezeichnung());
        display.setInstrumentType(security.getTyp());
        display.setIsin(security.getISIN());
        display.setWkn(security.getWKN());

        final List<OrderExchangeInfo> exchangeInfoList = this.securityInfo.getExchangeList();
        final OrderExchangeInfo exchangeInfo;
        if(!exchangeInfoList.isEmpty()) {
            final int defaultExchangeIndex = getDefaultExchangeIndex(exchangeInfoList);
            exchangeInfo = this.securityInfo.getExchangeList().get(defaultExchangeIndex);
            display.setExchangeChoices(exchangeInfoList);
            display.setExchangeChoiceEnabled(true);
            display.setSelectedExchangeChoice(defaultExchangeIndex);
        }
        else {
            exchangeInfo = null;
            display.setExchangeCurrencyChoices(Collections.<CurrencyAnnotated>emptyList());
            display.setExchangeChoiceEnabled(false);
            display.setSelectedExchangeChoice(-1);
        }

        final List<CurrencyAnnotated> currencyList = this.securityInfo.getCurrencyList();
        final CurrencyAnnotated currency;
        if(!currencyList.isEmpty()) {
            final int defaultCurrencyIndex = getDefaultCurrencyIndex(currencyList);
            currency = this.securityInfo.getCurrencyList().get(defaultCurrencyIndex);

            display.setExchangeCurrencyChoices(this.securityInfo.getCurrencyList());
            display.setExchangeCurrencyChoiceEnabled(true);
            display.setSelectedExchangeCurrency(defaultCurrencyIndex);
            displayExchangeCurrency(currency);
        }
        else {
            currency = null;

            display.setExchangeCurrencyChoices(Collections.<CurrencyAnnotated>emptyList());
            display.setExchangeCurrencyChoiceEnabled(false);
            display.setSelectedExchangeCurrency(-1);
            displayExchangeCurrency(null);
        }

        displayQuote(findQuoteForExchangeAndExchangeCurrency(exchangeInfo, currency));

        if(LookupSecurityConfig.PROCESS_PARAMETER_MAP == config) {
            final SafeHtmlBuilder sb = new SafeHtmlBuilder();
            if(!this.parameterMapProcessor.process(this.orderEntryContext.getParameterMap(), sb,
                    ParameterMapProcessor.State.ON_SECURITY_LOOKUP_SUCCESSFUL)) {
                Dialog.warning(sb.toSafeHtml());
            }
        }

        validateDisplay();

        this.lookupSecurityValidationMessages = result.getValidationMsgList();
        if (!this.lookupSecurityValidationMessages.isEmpty()) {
            this.orderValidationMessagePresenter.show(this.lookupSecurityValidationMessages, null);
        }
    }

    protected int getDefaultCurrencyIndex(List<CurrencyAnnotated> currencyList) {
        for(int i = 0; i < currencyList.size(); i++) {
            final CurrencyAnnotated currency = currencyList.get(i);
            if(currency.isIsDefault()) {
                Firebug.debug(LOG_PREFIX + ".getDefaultCurrencyIndex> default currency found: index=" + i + " , value=" + currency.getCurrency().getKuerzel());
                return i;
            }
        }
        Firebug.debug(LOG_PREFIX + ".getDefaultCurrencyIndex> no default currency found. Using the first entry.");
        return 0;
    }

    protected int getDefaultExchangeIndex(@SuppressWarnings("UnusedParameters") List<OrderExchangeInfo> exchangeInfoList) {
        return 0;
    }

    protected void doOnLookupSecurityFailure(String isin, LookupSecurityConfig config, Throwable caught) {
        Firebug.error(LOG_PREFIX + ".doOnLookupSecurityFailure> isin=" + isin, caught);

        INSTANCE.showFailureMessage(caught, isin);

        resetDisplaySecuritySection();
    }

    protected void reinitInstrumentName() {
        this.display.setInstrumentName(extractNameOfCurrentSecurityInfo());
    }

    protected String formatPmPriceAndCurrency(String value, OrderCurrency currency) {
        return Renderer.PRICE23.render(value) + " " + currency.getKuerzel();
    }

    /**
     * Starts validation of an order. Do always start with this method.
     * @param order filled order data type
     */
    protected void validateOrder(final OrderDataType order) {
        validateOrder(order, getLookupSecurityValidationMessages());
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
        INSTANCE.validateOrder(
                this.getCurrentOrderSession(),
                order,
                validationMessages,
                new AsyncCallback<ValidateOrderDataResponse>() {
                    @Override
                    public void onSuccess(ValidateOrderDataResponse result) {
                        onValidateOrderSuccessful(result);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        onValidateOrderFailed(caught);
                    }
                });
    }

    protected void onValidateOrderSuccessful(ValidateOrderDataResponse result) {
        Firebug.debug(LOG_PREFIX + ".onValidateOrderSuccessful>");

        confirmOrder(result.getOrder(), result.getValidationMsgList());
    }

    protected void onValidateOrderFailed(Throwable caught) {
        Firebug.debug(LOG_PREFIX + ".onValidateOrderFailed>");

        if (caught instanceof PmxmlValidateOrderResponseException) {
            final PmxmlValidateOrderResponseException pmvore = (PmxmlValidateOrderResponseException) caught;

            final List<ValidationMessage>validationMessages = pmvore.getValidationMessages();

            if (validationMessages != null && !validationMessages.isEmpty()) {
                final Callback callback = new Callback() {
                    @Override
                    public void onProceed(List<ValidationMessage> editedValidationMessages) {
                        onProceedAfterValidateOrderFailed(pmvore.getReceivedDataType(), editedValidationMessages);
                    }

                    @Override
                    public void onCancel() {
                        onCancelAfterValidateOrderFailed(validationMessages);
                    }
                };

                this.orderValidationMessagePresenter.show(validationMessages, callback);
            }
            else {
                INSTANCE.showFailureMessage(caught);
            }
        }
        else {
            INSTANCE.showFailureMessage(caught);
        }
    }

    protected void onProceedAfterValidateOrderFailed(OrderDataType receivedOrderDataType, List<ValidationMessage> editedValidationMessages) {
        final OrderLogBuilder log = new OrderLogBuilder(CLASS_PREFIX + ".onProceedAfterValidateOrderFailed", //$NON-NLS$
                getCurrentOrderSession());

        DebugUtil.logToServer(log.addValidationMessages(editedValidationMessages).toString());

        validateOrder(receivedOrderDataType, editedValidationMessages);
    }

    protected void onCancelAfterValidateOrderFailed(List<ValidationMessage> validationMessages) {
        final OrderLogBuilder log = new OrderLogBuilder(CLASS_PREFIX + ".onCancelAfterValidateOrderFailed", //$NON-NLS$
                getCurrentOrderSession());

        DebugUtil.logToServer(log.addValidationMessages(validationMessages).toString());
        Firebug.debug(LOG_PREFIX + ".onCancelAfterValidateOrderFailed>");
    }

    protected void sendOrder(final OrderDataType order, List<ValidationMessage> validationMessages) {
        Firebug.debug(LOG_PREFIX + ".sendOrder>");
        INSTANCE.sendOrder(
                this.getCurrentOrderSession(),
                order,
                validationMessages,
                new AsyncCallback<SendOrderDataResponse>() {
                    @Override
                    public void onSuccess(SendOrderDataResponse result) {
                        onSendOrderSuccessful(result);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        onSendOrderFailed(caught);
                    }
                });
    }

    protected void onSendOrderSuccessful(SendOrderDataResponse result) {
        List<Section> ackOfReceiptModel = createAckOfReceiptModel(result.getOrder());

        this.historySupport.addItem(createOrderEntryHistoryItem());

        this.returnParameterMap.setDepotId(this.orderSession.getSecurityAccount().getId());
        this.returnParameterMap.setOrderNumber(result.getOrder().getOrderNumber());
        this.returnParameterMap.setProcessedQuantity(result.getOrder().getQuantity());

        if(ackOfReceiptModel == null) {
            final String message = I18n.I.orderEntryOrderSuccessfullyCreated(result.getOrder().getOrderNumber());
            Dialog.info(message, new Command() {
                @Override
                public void execute() {
                    dispose();
                }
            });

            return;
        }

        final OrderConfirmationPresenter ocp = new OrderConfirmationPresenter(this.windowTitle, new OrderConfirmationPresenter.AbstractCallback() {
            @Override
            public void onExecute() {
                dispose();
            }
        });

        ocp.setColumns(2);
        ocp.setExecuteButtonText(I18n.I.ok());
        ocp.setExecuteButtonVisible(true);
        ocp.setCancelButtonVisible(false);
        ocp.setBackButtonVisible(false);
        ocp.setPrintDateVisible(true);
        ocp.show(ackOfReceiptModel);
    }

    protected void onSendOrderFailed(Throwable caught) {
        Firebug.debug(LOG_PREFIX + ".onSendOrderFailed> calling onValidateOrderFailed");
        onValidateOrderFailed(caught);
    }

    protected void initDisplay() {
        Firebug.debug(LOG_PREFIX + ".initDisplay>");

        final DisplayAbstract display = getDisplay();
        final S os = this.orderSession;

        //Init order transaction section
        display.setOrderActions(os.getFeatures().getOrderActions());
        this.currentOrderAction = os.getFeatures().getOrderActions().get(0);
        display.setSelectedOrderAction(this.currentOrderAction);

        //Init security account section
        display.setDepotBankName(os.getSecurityAccount().getBank().getBankname());

        initDefaultInvestor(display, os);
        display.setInvestorNo(os.getOwner().getNumber());

        initDefaultDepot(display, os);
        display.setDepotNo(os.getSecurityAccount().getNumber());

        display.setSymbolsOfDepot(os.getSecurityList());
        display.setSelectSymbolFromDepotEnabled(!(os.getSecurityList() == null || os.getSecurityList().isEmpty()));
        display.setAccounts(os.getAccountList());
        if (os.getAccountList() != null && os.getAccountList().size() > 0) {
            final AccountData accountData = os.getAccountList().get(0);
            display.setAccountsSelectedItem(0);
            display.setAccountNo(accountData.getNumber());
            display.setAccountBalance(accountData.getBalance(), accountData.getCurrency().getKuerzel());
        }

        //Init security section
        resetDisplaySecuritySection();
    }

    private void resetDisplaySecuritySection() {
        final DisplayAbstract display = getDisplay();

        this.securityInfo = null;
        this.currentQuoteIndex = -1;
        this.securityFeatureDescriptor = null;

        display.setInstrumentName(null);
        display.setInstrumentType(null);
        display.setIsin(null);
        display.setWkn(null);
        display.setPrice(null);
        display.setPriceDate(null);
        display.setCurrencyLabels(null);
        display.setExchangeChoices(Collections.<OrderExchangeInfo>emptyList());
        display.setExchangeCurrencyChoices(Collections.<CurrencyAnnotated>emptyList());

        //TODO: CHECK IF THESE TWO ARE CORRECT HERE!!!!
        //TODO: IF NOT, MOVE TO doOnLookupSecurityFailure!
        display.setExchangeChoiceEnabled(false);
        display.setExchangeCurrencyChoiceEnabled(false);
    }

    private void initDefaultInvestor(DisplayAbstract display, OrderSession os) {
        final ShellMMInfo owner = new ShellMMInfo();
        owner.setBezeichnung(os.getOwner().getName());
        owner.setId(Integer.toString(-1));
        ArrayList<ShellMMInfo> investors = new ArrayList<>(1);
        investors.add(owner);
        display.setInvestors(investors, null);
        display.setInvestorsEnabled(false);
    }

    private void initDefaultDepot(DisplayAbstract display, final OrderSession os) {
        final Depot depot = new Depot() {
            @Override
            public String getName() {
                return os.getSecurityAccount().getName();
            }

            @Override
            public String getId() {
                return Integer.toString(-1);
            }

            @Override
            public ShellMMType getShellMMType() {
                return ShellMMType.ST_DEPOT;
            }
        };
        ArrayList<Depot> depots = new ArrayList<>(1);
        depots.add(depot);
        display.setDepots(depots);
        display.setDepotsEnabled(false);
    }

    public ParameterMap getParameterMap() {
        if(this.orderEntryContext == null) {
            return null;
        }
        return this.orderEntryContext.getParameterMap();
    }

    @Override
    public ParameterMap getReturnParameterMap() {
        return returnParameterMap;
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

    protected OrderAction getCurrentOrderAction() {
        return currentOrderAction;
    }

    protected S getCurrentOrderSession() {
        return this.orderSession;
    }

    protected LookupSecurityInfo getCurrentSecurityInfo() {
        return this.securityInfo;
    }

    @SuppressWarnings("unused")
    protected OrderSecurityFeatureDescriptor getCurrentSecurityFeatureDescriptor() {
        return securityFeatureDescriptor;
    }

    protected int getCurrentQuoteIndex() {
        return this.currentQuoteIndex;
    }

    protected void setCurrentQuoteIndex(int index) {
        this.currentQuoteIndex = index;
    }

    protected List<ValidationMessage> getLookupSecurityValidationMessages() {
        return this.lookupSecurityValidationMessages;
    }

    @SuppressWarnings("unused")
    protected void setLookupSecurityValidationMessages(List<ValidationMessage> lookupSecurityValidationMessages) {
        this.lookupSecurityValidationMessages = lookupSecurityValidationMessages;
    }

    public String getWindowTitle() {
        return this.windowTitle;
    }

    @Override
    public void onOrderEntryHistoryItemSelected(OrderEntryHistorySupport.Item item) {
        final OrderEntryHistorySupport hs = this.historySupport;

        if(hs.hasPendingHistoryItem()) {
            hs.cancelPendingHistoryItem();
        }
        hs.processPendingHistoryItem(item);
    }

    private void processHistoryItemBeforeSecurityLookup(OrderEntryHistorySupport.Item item) {
        final String transaction = item.get(HISTORY_TRANSACTION_NAME);
        if(StringUtil.hasText(transaction)) {
            try {
                final OrderAction oa = new OrderAction();
                oa.setValue(OrderActionType.valueOf(transaction));
                this.getDisplay().setSelectedOrderAction(oa);
                this.securityInfo = null; //prevents onOrderActionChanged from looking up the security
                this.onOrderActionChanged(null);
            }
            catch(IllegalArgumentException e) {
                Firebug.error("OrderActionType of history item not valid", e);
            }
        }

        final String isin = item.get(HISTORY_SECURITY_ISIN);
        if(StringUtil.hasText(isin)) {
            lookupSecurity(isin, LookupSecurityConfig.PROCESS_HISTORY_ITEM);
        }
    }

    private void processHistoryItemAfterSecurityLookup(OrderEntryHistorySupport.Item item) {
        final String exchangeId = item.get(HISTORY_EXCHANGE_ID);
        if(StringUtil.hasText(exchangeId)) {
            int i = 0;
            for(OrderExchangeInfo oei : getCurrentSecurityInfo().getExchangeList()) {
                if(exchangeId.equals(oei.getID())) {
                    getDisplay().setSelectedExchangeChoice(i);
                }
                i++;
            }
        }

        final String currencyId = item.get(HISTORY_CURRENCY_ID);
        if(StringUtil.hasText(currencyId)) {
            int i = 0;
            for(CurrencyAnnotated ca : getCurrentSecurityInfo().getCurrencyList()) {
                if(currencyId.equals(ca.getCurrency().getId())) {
                    getDisplay().setSelectedExchangeCurrency(i);
                    onExchangeCurrencyChangedHandler(null);
                }
                i++;
            }
        }

        this.historySupport.processPendingHistoryItem();
    }

    OrderEntryHistorySupport getHistorySupport() {
        return historySupport;
    }

    protected OrderEntryHistorySupport.Item createOrderEntryHistoryItem() {
        final OrderEntryHistorySupport.Item item = new OrderEntryHistorySupport.Item();
        final StringBuilder label = new StringBuilder();

        final OrderActionType orderActionType = getCurrentOrderAction().getValue();
        item.put(HISTORY_TRANSACTION_NAME, orderActionType.name());
        label.append(OeRenderers.ORDER_ACTION_NOUN_RENDERER.render(getCurrentOrderAction()));

        final LookupSecurityInfo currentSecurityInfo = getCurrentSecurityInfo();
        if(currentSecurityInfo != null) {
            final String isin = currentSecurityInfo.getSecurity().getISIN();
            if(StringUtil.hasText(isin)) {
                item.put(HISTORY_SECURITY_ISIN, isin);

                final String type = PmRenderers.SHELL_MM_TYPE.render(currentSecurityInfo.getSecurity().getTyp());
                final String name = currentSecurityInfo.getSecurity().getBezeichnung();

                label.append(": ").append(type).append(" "); //$NON-NLS$
                label.append(name).append(" (").append(isin).append(")"); //$NON-NLS$
            }

            try {
                final int exchangeIdx = getDisplay().getSelectedExchangeChoice();
                final OrderExchangeInfo exchangeInfo = currentSecurityInfo.getExchangeList().get(exchangeIdx);
                item.put(HISTORY_EXCHANGE_ID, exchangeInfo.getID());

                label.append(", ").append(exchangeInfo.getName()); //$NON-NLS$
            }
            catch(Exception e) {
                Firebug.error("Cannot get exchange ID", e);
            }

            try {
                final int currencyIdx = getDisplay().getSelectedExchangeCurrency();
                final OrderCurrency currency = currentSecurityInfo.getCurrencyList().get(currencyIdx).getCurrency();
                item.put(HISTORY_CURRENCY_ID, currency.getId());

                label.append(", ").append(currency.getKuerzel()); //$NON-NLS$
            }
            catch(Exception e) {
                Firebug.error("Cannot get currency ID", e);
            }
        }

        item.setLabel(label.toString());

        return item;
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

    @Override
    public void onLoadInvestorsSuccess(List<ShellMMInfo> investors, Map<String, ZoneDesc> zoneDescs, int index) {
        this.display.setInvestors(investors, zoneDescs);
        this.display.setInvestorsSelectedIndex(index);
        this.display.setInvestorsEnabled(true);
    }

    @Override
    public void onLoadInvestorsFailed() {
        this.display.setInvestorsEnabled(false);
    }
}