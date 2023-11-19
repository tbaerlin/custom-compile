/*
 * OrderPresenterFactory.java
 *
 * Created on 30.10.12 13:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.pmxml.BrokerageModuleID;

import java.io.Serializable;

/**
 * @author Michael Lösch
 */
class OrderPresenterFactory {

    interface OrderPresenterCreator extends Serializable{
        Display.Presenter createPresenter(OrderSession orderSession);
        OrderBookDisplay.Presenter createOrderBookPresenter(OrderSession orderSession);
    }

    enum OrderEntry {
        HA(new OrderPresenterCreator() {
            @Override
            public Display.Presenter createPresenter(OrderSession orderSession) {
                return new OrderPresenterHA(new OrderViewHA(), (OrderSession.OrderSessionHA)orderSession);
            }

            @Override
            public OrderBookDisplay.Presenter createOrderBookPresenter(OrderSession orderSession) {
                return null;
            }
        }, BrokerageModuleID.BM_HA),

        BANKHAUS_LAMPE_KGS (new OrderPresenterCreator() {
            @Override
            public Display.Presenter createPresenter(OrderSession orderSession) {
                return new OrderPresenterBHLKGS(new OrderViewBHLKGS(),
                        (OrderSession.OrderSessionBHLKGS)orderSession,
                        new OrderPresenterBHLKGS.NewOrderStrategy());
            }

            @Override
            public OrderBookDisplay.Presenter createOrderBookPresenter(OrderSession orderSession) {
                return new OrderBookPresenterBHLKGS(new OrderBookViewBHLKGS(),
                        (OrderSession.OrderSessionBHLKGS)orderSession);
            }
        }, BrokerageModuleID.BM_BHLKGS),

        FUCHSBRIEFE(new OrderPresenterCreator() {
            @Override
            public AbstractOrderPresenter createPresenter(OrderSession orderSession) {
                return new OrderPresenterFuchsbriefe(new OrderViewFuchsbriefe(),
                        (OrderSession.OrderSessionFuchsbriefe)orderSession);
            }

            @Override
            public OrderBookDisplay.Presenter createOrderBookPresenter(OrderSession orderSession) {
                return null;
            }
        }, BrokerageModuleID.BM_FUCHSBRIEFE);

        private final OrderPresenterCreator creator;
        private final BrokerageModuleID module;

        OrderEntry(OrderPresenterCreator creator, BrokerageModuleID moduleId) {
            this.creator = creator;
            this.module = moduleId;
        }

        public static OrderEntry fromModule(BrokerageModuleID module) {
            final OrderEntry[] entries = OrderEntry.values();
            for (OrderEntry entry : entries) {
                if (module.equals(entry.module)) {
                    return entry;
                }
            }
            throw new IllegalStateException("Unknown brokerage module: " + module.value()); //$NON-NLS$
        }
    }

    public static Display.Presenter createBrokingPresenter(OrderSession orderSession) {
        final OrderEntry entry = OrderEntry.fromModule(orderSession.getBrokerageModuleID());
        return entry.creator.createPresenter(orderSession);
    }

    public static OrderBookDisplay.Presenter createOrderBookPresenter(OrderSession orderSession) {
        final OrderEntry entry = OrderEntry.fromModule(orderSession.getBrokerageModuleID());
        return entry.creator.createOrderBookPresenter(orderSession);
    }
}
