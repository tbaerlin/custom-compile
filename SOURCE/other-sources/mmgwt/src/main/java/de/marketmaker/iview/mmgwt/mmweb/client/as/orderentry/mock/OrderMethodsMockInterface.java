/*
 * OrderMethodsMockInterface.java
 *
 * Created on 13.11.13 17:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock;

import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderMethods;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.pmxml.BrokerageModuleID;

/**
 * @author Markus Dick
 */
public interface OrderMethodsMockInterface {
    boolean isModuleSupported(BrokerageModuleID id);
    boolean isSessionHandled(OrderSession orderSession);
    boolean isDepotIdHandled(String depotId);
    OrderMethods asOrderMethods();
}
