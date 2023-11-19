/*
 * OrderConfirmationModelBuilderFuchsbriefe.java
 *
 * Created on 28.10.13 14:44
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log.LogBuilder;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.OrderDataType;

/**
 * @author Markus Dick
 */
public class OrderConfirmationModelBuilderFuchsbriefe extends AbstractOrderConfirmationModelBuilder {
    public OrderConfirmationModelBuilderFuchsbriefe() {
        super(true);
    }

    protected OrderConfirmationDisplay.Section addOrderSuccessfullyCreated() {
        final OrderConfirmationDisplay.Section section = new OrderConfirmationDisplay.Section(
                I18n.I.orderEntryFuchsbriefeOrderSuccessfullyCreated(), 2);
        getSections().add(0, section);
        return section;
    }

    public void addExchangeAndCurrencySection(OrderDataType order, LookupSecurityInfo security, LogBuilder log) {
        addExchangeAndCurrencySection("", order, security.getCurrencyList(), log);
    }

    public void addOrderSection(OrderDataType order) {
        addAmountNominalSection(I18n.I.orderEntryOrder(), order);
    }
}
