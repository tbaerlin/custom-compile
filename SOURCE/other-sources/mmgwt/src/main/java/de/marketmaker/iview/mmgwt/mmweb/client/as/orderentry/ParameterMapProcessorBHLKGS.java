/*
 * ParameterMapProcessorBHLKGS.java
 *
 * Created on 12.05.2014 08:32
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ParameterMap;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.AuthorizedRepresentative;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.HasCode;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderSecurityFeatureDescriptorBHL;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptorBHL;
import de.marketmaker.iview.pmxml.OrderStock;
import de.marketmaker.iview.pmxml.TextWithKey;

import java.util.List;

/**
 * @author Markus Dick
 */
public class ParameterMapProcessorBHLKGS extends ParameterMapProcessor<OrderPresenterBHLKGS,
        DisplayBHLKGS.PresenterBHLKGS, DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS>> {

    public static final String PARAMETER_MAP_KEY_PLACING_OF_ORDER_VIA = "CommunicationChannel";  // $NON-NLS$
    public static final String PARAMETER_MAP_KEY_BUSINESS_SEGMENT = "BusinessSegment";  // $NON-NLS$

    public ParameterMapProcessorBHLKGS(DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> display) {
        super(display);
    }

    @Override
    protected boolean doProcessOnSecurityLookupSuccessful(ParameterMap parameterMap, SafeHtmlBuilder messages) {
        boolean noMessages = super.doProcessOnSecurityLookupSuccessful(parameterMap, messages);
        noMessages &= processBusinessSegment(parameterMap, messages, noMessages);
        return processPlacingOfOrderVia(parameterMap, messages, noMessages);
    }

    private boolean processPlacingOfOrderVia(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final Object param = parameterMap.get(PARAMETER_MAP_KEY_PLACING_OF_ORDER_VIA);
        if(!(param instanceof MM)) {
            return noMessages;
        }

        final String placingOfOrderVia = param instanceof HasCode ? MmTalkHelper.asCode((MM) param) : MmTalkHelper.asString((MM) param);
        if(StringUtil.hasText(placingOfOrderVia)) {
           return doProcessPlacingOfOrderVia(messages, noMessages, placingOfOrderVia);
        }
        return noMessages;
    }

    private boolean doProcessPlacingOfOrderVia(SafeHtmlBuilder messages, boolean noMessages, String key) {
        final List<TextWithKey> orderPlacementVias = this.presenter.orderSession.getFeatures().getOrderPlacementVia();
        final TextWithKey textWithKey = OrderUtils.findTextWithKey(orderPlacementVias, key);
        if(textWithKey != null) {
            this.display.setPlacingOfOrderViasSelectedItem(textWithKey);
            this.display.setPlacingOfOrderViasEnabled(false);
            return noMessages;
        }
        else {
            appendMessage(messages, noMessages,
                    I18n.I.orderEntryParaMapBHLKGSCommunicationChannelNotFound(key));
            return false;
        }
    }

    @Override
    protected boolean processOrderActionType(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final OrderActionType orderActionType = parameterMap.getOrderActionType();
        if(orderActionType != null) {
            final List<OrderAction> orderActions = this.presenter.orderSession.getFeatures().getOrderActions();
            for(final OrderAction orderAction : orderActions) {
                if(orderActionType.equals(orderAction.getValue())) {
                    this.display.setOrderActionTypesSelectedItem(orderActionType);
                    this.presenter.onOrderActionTypeChanged(null);
                    return noMessages;
                }
            }

            appendMessage(messages, noMessages, I18n.I.orderEntryParaMapUnsuppOrderAction(
                    OeRenderers.ORDER_ACTION_TYPE_RENDERER.render(orderActionType)
            ));
            return false;
        }

        return noMessages;
    }

    @Override
    protected boolean processIsin(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final String isin = parameterMap.getIsin();
        if(StringUtil.hasText(isin)) {
            this.presenter.lookupSecurity(isin, OrderPresenterBHLKGS.LookupSecurityConfig.PROCESS_PARAMETER_MAP);
        }
        return noMessages;
    }

    @Override
    protected boolean processSettlementAccountId(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final String settlementAccountId = parameterMap.getSettlementAccount();
        if(StringUtil.hasText(settlementAccountId)) {
            final List<AccountData> accountList = this.presenter.orderSession.getAccountList();
            for(AccountData anAccountList : accountList) {
                if (settlementAccountId.equals(anAccountList.getId())) {
                    this.display.setAccountsSelectedItem(anAccountList);
                    return noMessages;
                }
            }

            appendMessage(messages, noMessages, I18n.I.orderEntryParaMapSettlementAccountNotFound(settlementAccountId));
            return false;
        }

        return noMessages;
    }

    @Override
    protected boolean processQuantity(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final String quantity = parameterMap.getQuantity();
        if(StringUtil.hasText(quantity)) {
            this.presenter.initAmountNominal(quantity);
            OrderActionType orderActionType = this.presenter.orderStrategy.getOrderActionType();
            if(OrderActionType.AT_BUY == orderActionType || OrderActionType.AT_SUBSCRIBE == orderActionType) {
                this.display.setAmountNominalEnabled(false);
            }
            else if(OrderActionType.AT_SELL == orderActionType) {
                final List<OrderStock> orderStockList = this.presenter.getCurrentSecurityInfo().getStockBalance();
                if(orderStockList.size() <= 1) {
                    this.display.setAmountNominalEnabled(false);
                }
                else {
                    try {
                        this.display.setAmountNominalEnabled(true);
                        this.display.setAmountNominalMaxValue(Double.parseDouble(quantity));
                    }
                    catch(Exception e) {
                        Firebug.error("<ParameterMapProcessorBHLKGS.processQuantity>", e);
                    }
                }
            }
        }

        return noMessages;
    }

    @Override
    protected boolean processLimitValue(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final String limit = parameterMap.getLimitValue();
        if(StringUtil.hasText(limit)) {
            this.presenter.initLimit(limit);
            this.display.setLimitOrStopLimitEnabled(false);
            this.presenter.doOnLimitChanged(limit);
            return noMessages;
        }
        this.display.setLimitOrStopLimitEnabled(true);
        return noMessages;
    }

    @Override
    protected boolean processLimitCurrency(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        if(StringUtil.hasText(parameterMap.getLimitValue())) {
            final String currencyIso3 = parameterMap.getLimitCurrencyIso3();
            if (StringUtil.hasText(currencyIso3)) {
                final LookupSecurityInfo currentSecurityInfo = this.presenter.getCurrentSecurityInfo();

                if (currentSecurityInfo != null) {
                    for (CurrencyAnnotated ca : currentSecurityInfo.getCurrencyList()) {
                        if (currencyIso3.equals(ca.getCurrency().getKuerzel())) {
                            this.display.setLimitCurrenciesSelectedItem(ca);
                            this.display.setLimitCurrenciesEnabled(false);
                            this.presenter.doOnLimitCurrenciesChanged();
                            return noMessages;
                        }
                    }

                    //reset a currency probably set by processLimitValue to <none>
                    this.display.setLimitCurrenciesSelectedItem(null);
                    this.presenter.doOnLimitCurrenciesChanged();
                    appendMessage(messages, noMessages, I18n.I.orderEntryParaMapLimitCurrencyNotFound(currencyIso3));
                    return false;
                }
            }
        }
        this.display.setLimitCurrenciesEnabled(true);
        return noMessages;
    }

    protected boolean processBusinessSegment(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final Object param = parameterMap.get(PARAMETER_MAP_KEY_BUSINESS_SEGMENT);
        if(!(param instanceof MM)) {
            return noMessages;
        }

        final String businessSegment = param instanceof HasCode ? MmTalkHelper.asCode((MM) param) : MmTalkHelper.asString((MM) param);
        if(StringUtil.hasText(businessSegment)) {
            doProcessBusinessSegment(messages, noMessages, businessSegment);
        }
        return noMessages;
    }

    private boolean doProcessBusinessSegment(SafeHtmlBuilder messages, boolean noMessages, String key) {
        final OrderSessionFeaturesDescriptorBHL f = this.presenter.orderSession.getFeatures();
        final TextWithKey textWithKey = OrderUtils.findTextWithKey(f.getBusinessSegments(), key);

        if(textWithKey != null) {
            this.display.setBusinessSegmentsSelectedItem(textWithKey);
            this.display.setBusinessSegmentsEnabled(false);
            this.presenter.onBusinessSegmentChanged(null);
            return noMessages;
        }

        this.display.setBusinessSegmentsEnabled(true);
        appendMessage(messages, noMessages,
                I18n.I.orderEntryParaMapBHLKGSBusinessSegmentNotFound(key));
        return false;
    }

    @Override
    protected boolean processPersonCustomerNumber(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final String personCustomerNumber = parameterMap.getPersonCustomerNumber();
        if(StringUtil.hasText(personCustomerNumber)) {
            final OrderSecurityFeatureDescriptorBHL f = this.presenter.getCurrentSecurityFeatures();
            if (f != null) {
                for (AuthorizedRepresentative ar : f.getAuthorizedRepresentatives()) {
                    if (personCustomerNumber.equals(ar.getNumber())) {
                        this.presenter.doOnSelectAuthorizedRepresentative(ar);
                        this.display.setAuthorizedRepresentativesEnabled(false);
                        this.display.setOrderersEnabled(false);
                        this.display.setOrdererIdentifierEnabled(false);
                        this.display.setOrdererCustomerNumberEnabled(false);
                        return noMessages;
                    }
                }

                appendMessage(messages, noMessages,
                        I18n.I.orderEntryParaMapBHLKGSAuthorizedRepresentativeNotFound(personCustomerNumber));
                return false;
            }
        }

        return noMessages;
    }

    @Override
    protected boolean processComment(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        return noMessages;
    }
}
