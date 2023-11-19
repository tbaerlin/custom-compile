/*
 * AbstractParameterMapProcessor.java
 *
 * Created on 09.05.2014 16:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ParameterMap;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.OrderAction;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptor;

import java.util.List;

/**
 * @author Markus Dick
 */
public abstract class AbstractParameterMapProcessor<
        F extends OrderSessionFeaturesDescriptor,
        S extends OrderSession<F>,
        P extends AbstractOrderPresenter<P, F, S>,
        IP extends DisplayAbstract.PresenterAbstract,
        D extends DisplayAbstract<IP>>
        extends ParameterMapProcessor<P, IP, D> {

    protected AbstractParameterMapProcessor(D display) {
        super(display);
    }

    @Override
    protected boolean doProcessOnSecurityLookupSuccessful(ParameterMap parameterMap, SafeHtmlBuilder messages) {
        return processUpdateExpectedMarketValue(super.doProcessOnSecurityLookupSuccessful(parameterMap, messages));
    }

    @Override
    protected boolean processOrderActionType(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final OrderActionType orderActionType = parameterMap.getOrderActionType();
        if(orderActionType != null) {
            final List<OrderAction> orderActions = this.presenter.getCurrentOrderSession().getFeatures().getOrderActions();
            for(final OrderAction orderAction : orderActions) {
                if(orderActionType.equals(orderAction.getValue())) {
                    this.display.setSelectedOrderAction(orderAction);
                    this.presenter.onOrderActionChanged(null);
                    return noMessages;
                }
            }

            appendMessage(messages, noMessages, I18n.I.orderEntryParaMapUnsuppOrderAction(
                    OeRenderers.ORDER_ACTION_TYPE_RENDERER.render(orderActionType)));
            return false;
        }

        return noMessages;
    }

    @Override
    protected boolean processIsin(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final String isin = parameterMap.getIsin();
        if(StringUtil.hasText(isin)) {
            this.presenter.lookupSecurity(isin, AbstractOrderPresenter.LookupSecurityConfig.PROCESS_PARAMETER_MAP);
        }
        return noMessages;
    }

    @Override
    protected boolean processSettlementAccountId(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final String settlementAccountId = parameterMap.getSettlementAccount();
        if(StringUtil.hasText(settlementAccountId)) {
            final List<AccountData> accountList = this.presenter.getCurrentOrderSession().getAccountList();
            for(int i = 0; i < accountList.size(); i++) {
                if(settlementAccountId.equals(accountList.get(i).getId())) {
                    this.display.setAccountsSelectedItem(i);
                    return noMessages;
                }
            }

            appendMessage(messages, noMessages, I18n.I.orderEntryParaMapSettlementAccountNotFound(settlementAccountId));
            return false;
        }

        return noMessages;
    }

    protected boolean processUpdateExpectedMarketValue(boolean noMessages) {
        this.presenter.updateExpectedMarketValue();
        return noMessages;
    }

    @Override
    protected boolean processPersonCustomerNumber(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        //do not add a message, not relevant for HA or Fuchsbriefe.
        return noMessages;
    }

    @Override
    protected boolean processComment(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        //do not add a message, not relevant for HA or Fuchsbriefe.
        return noMessages;
    }
}
