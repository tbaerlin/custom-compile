/*
 * ParameterMapProcessor.java
 *
 * Created on 09.05.2014 16:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ParameterMap;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Markus Dick
 */
public class ParameterMapProcessor<P, IP extends Display.Presenter, D extends Display<IP>> {
    public enum State {ON_INIT, ON_SECURITY_LOOKUP_SUCCESSFUL}

    protected final D display;
    protected P presenter;

    public ParameterMapProcessor(D display) {
        this.display = display;
    }

    public void setPresenter(P presenter) {
        this.presenter = presenter;
    }

    public boolean process(ParameterMap parameterMap, SafeHtmlBuilder messages, State state) {
        if(parameterMap == null) {
            Firebug.debug("<ParameterMapProcessor> parameterMap is null. nothing to process.");
            return true;
        }
        switch (state) {
            case ON_SECURITY_LOOKUP_SUCCESSFUL:
                return doProcessOnSecurityLookupSuccessful(parameterMap, messages);
            case ON_INIT:
            default:
                return doProcessBeforeSecurityLookup(parameterMap, messages);
        }
    }

    protected boolean doProcessBeforeSecurityLookup(ParameterMap parameterMap, SafeHtmlBuilder messages) {
        boolean noMessages;

        noMessages = processActivityInstanceId(parameterMap, messages, true);
        noMessages &= processActivityListEntryId(parameterMap, messages, noMessages);
        noMessages &= processOrderActionType(parameterMap, messages, noMessages);
        noMessages &= processSettlementAccountId(parameterMap, messages, noMessages);
        noMessages &= processIsin(parameterMap, messages, noMessages);
        noMessages &= processComment(parameterMap, messages, noMessages);

        return noMessages;
    }

    protected boolean processActivityInstanceId(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        if(this.presenter instanceof IsActivityAware) {
            final String id = parameterMap.getActivityInstanceId();
            if(StringUtil.hasText(id) && !"0".equals(id)) {  // $NON-NLS$
                ((IsActivityAware) this.presenter).setActivityInstanceId(id);
            }
            return noMessages;
        }

        return defaultMessage(messages, noMessages, "ActivityInstanceId", parameterMap.getActivityInstanceId()); // $NON-NLS$
    }

    private boolean processActivityListEntryId(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        if(this.presenter instanceof IsActivityAware) {
            final String id = parameterMap.getActivityListEntryId();
            if(StringUtil.hasText(id)) {
                ((IsActivityAware) this.presenter).setActivityListEntryId(id);
            }
            return noMessages;
        }

        return defaultMessage(messages, noMessages, "ActivityListEntryId", parameterMap.getActivityListEntryId());  // $NON-NLS$
    }

    protected boolean doProcessOnSecurityLookupSuccessful(ParameterMap parameterMap, SafeHtmlBuilder messages) {
        boolean noMessages = processLimitValue(parameterMap, messages, true);
        noMessages &= processLimitCurrency(parameterMap, messages, noMessages);
        noMessages &= processQuantity(parameterMap, messages, noMessages);
        return processPersonCustomerNumber(parameterMap, messages, noMessages); //is left here due to BhL
    }

    protected boolean processOrderActionType(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        return defaultMessage(messages, noMessages, I18n.I.orderEntryTransaction(),
                OeRenderers.ORDER_ACTION_TYPE_RENDERER.render(parameterMap.getOrderActionType()));
    }

    protected boolean processSettlementAccountId(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        return defaultMessage(messages, noMessages, I18n.I.orderEntryAccount(), parameterMap.getSettlementAccount());
    }

    protected boolean processIsin(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        return defaultMessage(messages, noMessages, "ISIN", parameterMap.getIsin());  // $NON-NLS$
    }

    protected boolean processQuantity(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        return defaultMessage(messages, noMessages, I18n.I.orderEntryAmountNominal(), parameterMap.getQuantity());
    }

    protected boolean processLimitCurrency(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        return defaultMessage(messages, noMessages, I18n.I.orderEntryLimitCurrency(),
                parameterMap.getLimitCurrencyIso3());
    }

    protected boolean processLimitValue(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        return defaultMessage(messages, noMessages, I18n.I.orderEntryLimit(), parameterMap.getLimitValue());
    }

    protected boolean processPersonCustomerNumber(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        return defaultMessage(messages, noMessages, I18n.I.orderEntryOrderer(), parameterMap.getPersonCustomerNumber());
    }

    protected boolean processComment(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        return defaultMessage(messages, noMessages, I18n.I.comment(), parameterMap.getComment());
    }

    private boolean defaultMessage(SafeHtmlBuilder messages, boolean noMessages, String parameterName, String value) {
        if(StringUtil.hasText(value)) {
            return defaultMessage(messages, noMessages, parameterName, (Object)value);
        }
        return noMessages;
    }

    private boolean defaultMessage(SafeHtmlBuilder messages, boolean noMessages, String parameterName, Object value) {
        if(value != null ) {
            appendMessage(messages, noMessages, I18n.I.orderEntryParaMapDefaultMessage(parameterName));
            return false;
        }
        return noMessages;
    }

    protected static void appendMessage(SafeHtmlBuilder messages, boolean noMessages, String message) {
        if(!noMessages) {
            messages.appendHtmlConstant("<br/>");  // $NON-NLS$
        }
        messages.appendEscaped(message);
    }
}
