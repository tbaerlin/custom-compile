/*
 * ParameterMapProcessorFuchsbriefe.java
 *
 * Created on 09.05.2014 17:08
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
public class ParameterMapProcessorFuchsbriefe extends AbstractParameterMapProcessor
        <OrderSessionFeaturesDescriptor,
        OrderSession.OrderSessionFuchsbriefe,
        OrderPresenterFuchsbriefe,
        DisplayFuchsbriefe.PresenterFuchsbriefe,
        DisplayFuchsbriefe<DisplayFuchsbriefe.PresenterFuchsbriefe>> {

    public ParameterMapProcessorFuchsbriefe(DisplayFuchsbriefe<DisplayFuchsbriefe.PresenterFuchsbriefe> view) {
        super(view);
    }

    @Override
    protected boolean processQuantity(ParameterMap parameterMap, SafeHtmlBuilder messages, boolean noMessages) {
        final String quantity = parameterMap.getQuantity();
        if(StringUtil.hasText(quantity)) {
            this.display.setQuantityRaw(quantity);
            this.presenter.updateExpectedMarketValue();
        }
        return noMessages;
    }
}
