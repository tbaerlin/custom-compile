/*
 * OrderMethodsMock.java
 *
 * Created on 10.07.13 14:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderMethods;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSessionContainer;
import de.marketmaker.iview.pmxml.BrokerageModuleID;
import de.marketmaker.iview.pmxml.CancelOrderResponse;
import de.marketmaker.iview.pmxml.ChangeOrderDataResponse;
import de.marketmaker.iview.pmxml.GetIPOListResponse;
import de.marketmaker.iview.pmxml.GetOrderDetailsResponse;
import de.marketmaker.iview.pmxml.GetOrderbookRequest;
import de.marketmaker.iview.pmxml.GetOrderbookResponse;
import de.marketmaker.iview.pmxml.LookupSecurityDataResponse;
import de.marketmaker.iview.pmxml.OrderDataType;
import de.marketmaker.iview.pmxml.OrderTransaktionType;
import de.marketmaker.iview.pmxml.SendOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidateChangeOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidateOrderDataResponse;
import de.marketmaker.iview.pmxml.ValidationMessage;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Arrays;
import java.util.List;

/**
 * Enable order entry mocks by setting the property orderEntry in your DevMmweb to mock.
 * Be aware that depot objects with specific object IDs must exist in your pm database.
 * <code>&lt;set-property name=&quot;orderEntry&quot; value=&quot;mock&quot;/&lt;</code>
 * </code>&lt;set-property name=&quot;orderEntry&quot; value=&quot;prod&quot;/&lt;</code>
 *
 * This class decides to which mock a call should be routed.
 * If no mock impl. feels responsible for handling a given depot ID,
 * the call is routed to the prod impl.
 *
 * @author Markus Dick
 */
@NonNLS
public class OrderMethodsMockMux extends OrderMethods {
    final List<OrderMethodsMockInterface> mocks = Arrays.asList(
            (OrderMethodsMockInterface)new OrderMethodsMockBHLKGS(),
            (OrderMethodsMockInterface)new OrderMethodsMockFuchsbriefe()
    );

    public OrderMethodsMockMux() {
        Firebug.warn("!!!! Infront Advisory Solution Order Entry IS MOCKED -- DO NOT FORGET TO REMOVE IT FOR PRODUCTION ENVIRONMENTS !!!!");
    }

    @Override
    public void allocateOrderSession(OrderSessionContainer containerHandle, String depotId, AsyncCallback<OrderSession> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isDepotIdHandled(depotId)) {
                mock.asOrderMethods().allocateOrderSession(containerHandle, depotId, callback);
                return;
            }
        }

        super.allocateOrderSession(containerHandle, depotId, callback);
    }

    @Override
    public void closeOrderSession(OrderSession orderSession, AsyncCallback<OrderSession> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().closeOrderSession(orderSession, callback);
                return;
            }
        }

        super.closeOrderSession(orderSession, callback);
    }

    @Override
    public void lookupSecurity(OrderSession orderSession, OrderTransaktionType type, String isin, AsyncCallback<LookupSecurityDataResponse> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().lookupSecurity(orderSession, type, isin, callback);
                return;
            }
        }

        super.lookupSecurity(orderSession, type, isin, callback);
    }

    @Override
    public void loginBroker(OrderSession orderSession, String user, String password, AsyncCallback<Void> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().loginBroker(orderSession, user, password, callback);
                return;
            }
        }

        super.loginBroker(orderSession, user, password, callback);
    }

    @Override
    public void validateOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages, AsyncCallback<ValidateOrderDataResponse> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().validateOrder(orderSession, order, validationMessages, callback);
                return;
            }
        }

        super.validateOrder(orderSession, order, validationMessages, callback);
    }

    @Override
    public void validateChangeOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages, AsyncCallback<ValidateChangeOrderDataResponse> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().validateChangeOrder(orderSession, order, validationMessages, callback);
                return;
            }
        }

        super.validateChangeOrder(orderSession, order, validationMessages, callback);
    }

    @Override
    public void sendOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages, AsyncCallback<SendOrderDataResponse> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().sendOrder(orderSession, order, validationMessages, callback);
                return;
            }
        }
        super.sendOrder(orderSession, order, validationMessages, callback);
    }

    @Override
    public void changeOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages, AsyncCallback<ChangeOrderDataResponse> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().changeOrder(orderSession, order, validationMessages, callback);
                return;
            }
        }
        super.changeOrder(orderSession, order, validationMessages, callback);
    }

    @Override
    public void cancelOrder(OrderSession orderSession, OrderDataType order, List<ValidationMessage> validationMessages, AsyncCallback<CancelOrderResponse> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().cancelOrder(orderSession, order, validationMessages, callback);
                return;
            }
        }
        super.cancelOrder(orderSession, order, validationMessages, callback);
    }

    @Override
    public void getOrderDetails(OrderSession orderSession, String orderNumber, AsyncCallback<GetOrderDetailsResponse> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().getOrderDetails(orderSession, orderNumber, callback);
                return;
            }
        }
        super.getOrderDetails(orderSession, orderNumber, callback);
    }

    @Override
    public void queryOrderBook(OrderSession orderSession, GetOrderbookRequest request, AsyncCallback<GetOrderbookResponse> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().queryOrderBook(orderSession, request, callback);
                return;
            }
        }
        super.queryOrderBook(orderSession, request, callback);
    }

    @Override
    public void getIPOList(OrderSession orderSession, AsyncCallback<GetIPOListResponse> callback) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isSessionHandled(orderSession)) {
                mock.asOrderMethods().getIPOList(orderSession, callback);
                return;
            }
        }
        super.getIPOList(orderSession, callback);
    }

    @Override
    protected boolean isBrokerageModuleSupported(BrokerageModuleID id) {
        for(OrderMethodsMockInterface mock : this.mocks) {
            if(mock.isModuleSupported(id)) {
                return true;
            }
        }
        return super.isBrokerageModuleSupported(id);
    }
}
