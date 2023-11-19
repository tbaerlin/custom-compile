/*
 * OrderEntryGoToDelegate.java
 *
 * Created on 19.03.13 15:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderEntryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderModule;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.PresenterDisposedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.OrderActionType;

/**
 * This class may be used in ordinary code, even if the order module has not been loaded.
 * If its goTo method is invoked, it loads the OrderEntry module.
 *
 * @author Markus Dick
 */
public class OrderEntryGoToDelegate implements NavItemSpec.GoToDelegate {
    private final Type type;
    private final String id;
    private final OrderActionType orderActionType;
    private final String isin;
    private final ParameterMap parameterMap;
    private final PresenterDisposedHandler presenterDisposedHandler;

    public static enum Type { BY_DEPOT_ID, BY_INVESTOR_ID, BY_ISIN, BY_PARAMETER_MAP }

    public OrderEntryGoToDelegate(String isin, OrderActionType orderActionType) {
        this(Type.BY_ISIN, null, orderActionType, isin);
    }

    public OrderEntryGoToDelegate(String isin) {
        this(isin, null);
    }

    public OrderEntryGoToDelegate(Type type, String id) {
        this(type, id, null, null);
    }

    public OrderEntryGoToDelegate(Type type, String id, OrderActionType orderActionType) {
        this(type, id, orderActionType, null);
    }

    public OrderEntryGoToDelegate(Type type, String id, OrderActionType orderActionType, String isin) {
        this.type = type;
        this.id = id;
        this.orderActionType = orderActionType;
        this.isin = isin;
        this.parameterMap = null;
        this.presenterDisposedHandler = null;
    }

    public OrderEntryGoToDelegate(Type type, ParameterMap parameterMap, PresenterDisposedHandler presenterDisposedHandler) {
        this.type = type;
        this.parameterMap = parameterMap;
        this.presenterDisposedHandler = presenterDisposedHandler;
        this.id = null;
        this.isin = null;
        this.orderActionType = null;
    }

    private static OrderEntryContext createOrderEntryContext(Type type, String id, OrderActionType orderActionType, String isin) {
        final OrderEntryContext context = OrderEntryContext.Factory.create();

        final ParameterMap parameterMap = context.getParameterMap();
        parameterMap.setOrderActionType(orderActionType);
        parameterMap.setIsin(isin);

        switch(type) {
            case BY_DEPOT_ID:
                parameterMap.setDepotId(id);
                break;

            case BY_INVESTOR_ID:
                parameterMap.setInvestorId(id);
                break;
        }

        return context;
    }

    private static OrderEntryContext createOrderEntryContext(ParameterMap parameterMap, PresenterDisposedHandler presenterDisposedHandler) {
        return OrderEntryContext.Factory.create(parameterMap, presenterDisposedHandler);
    }

    @Override
    public void goTo(String somethingCompletelyUnnecessaryHere) {
        Firebug.debug("<OrderEntryGoToDelegate.goTo>");

        final OrderEntryContext orderEntryContext;
        if(this.type == Type.BY_PARAMETER_MAP) {
            orderEntryContext = createOrderEntryContext(this.parameterMap, this.presenterDisposedHandler);
        }
        else {
            orderEntryContext = createOrderEntryContext(this.type, this.id, this.orderActionType, this.isin);
        }

        final Type type = this.type;

        GWT.runAsync(new RunAsyncCallback() {
            @Override
            public void onFailure(Throwable reason) {
                Firebug.warn("<OrderEntryGoToDelegate.goTo> GWT.runAsync failed!", reason);
            }

            @Override
            public void onSuccess() {
                OrderModule.createInstance(new AsyncCallback<ResponseType>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        Firebug.warn("<OrderEntryGoToDelegate.goTo> Initializing order module failed!", caught);
                        AbstractMainController.INSTANCE.showError(I18n.I.orderEntryError(caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(ResponseType result) {
                        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                            @Override
                            public void execute() {
                                doExecute(type, orderEntryContext);
                            }
                        });
                    }
                });
            }
        });
    }

    private static void doExecute(Type type, OrderEntryContext orderEntryContext) {
        final ParameterMap parameterMap = orderEntryContext.getParameterMap();
        Firebug.debug("<OrderEntryGoToDelegate.goTo> Order module initialized. Executing " + type + ": " + parameterMap.toString());

        switch(type) {
            case BY_PARAMETER_MAP:
                if(StringUtil.hasText(parameterMap.getDepotId())) {
                    OrderModule.showByDepotId(orderEntryContext);
                }
                else if(StringUtil.hasText(parameterMap.getInvestorId())) {
                    OrderModule.showByInvestorId(orderEntryContext);
                }
                break;

            case BY_DEPOT_ID:
                OrderModule.showByDepotId(orderEntryContext);
                break;

            case BY_INVESTOR_ID:
                OrderModule.showByInvestorId(orderEntryContext);
                break;

            case BY_ISIN:
                OrderModule.showByIsin(orderEntryContext);
                break;
        }
    }
}
