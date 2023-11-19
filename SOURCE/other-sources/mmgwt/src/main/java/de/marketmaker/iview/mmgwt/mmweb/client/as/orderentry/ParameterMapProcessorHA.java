/*
 * ParameterMapProcessorHA.java
 *
 * Created on 09.05.2014 18:25
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec.ParameterMap;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptor;

/**
 * @author Markus Dick
 */
public class ParameterMapProcessorHA extends AbstractParameterMapProcessor
        <OrderSessionFeaturesDescriptor,
                OrderSession.OrderSessionHA,
                OrderPresenterHA,
                DisplayHA.PresenterHA,
                DisplayHA<DisplayHA.PresenterHA>> {

    public ParameterMapProcessorHA(DisplayHA<DisplayHA.PresenterHA> display) {
        super(display);
    }

    @Override
    protected boolean processQuantity(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final String quantity = parameterMap.getQuantity();
        if(StringUtil.hasText(quantity)) {
            this.display.setAmount(quantity);
            this.display.getAmountFieldValidator().formatAndValidate();
            this.presenter.updateExpectedMarketValue();
        }

        return noMessages;
    }

    @Override
    protected boolean processLimitValue(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final String limit = parameterMap.getLimitValue();
        if(StringUtil.hasText(limit)) {
            this.presenter.checkLimitChoice(true);
            this.display.setLimit(limit);
            this.display.getLimitFieldValidator().formatAndValidate();
        }

        return noMessages;
    }
}
