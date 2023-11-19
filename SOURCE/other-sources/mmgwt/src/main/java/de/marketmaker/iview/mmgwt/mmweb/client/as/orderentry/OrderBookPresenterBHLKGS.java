/*
 * OrderBookPresenterBHLKGS.java
 *
 * Created on 05.09.13 16:02
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.PageControllerAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.AccountRef;
import de.marketmaker.iview.pmxml.GetOrderbookRequestBHL;
import de.marketmaker.iview.pmxml.GetOrderbookResponse;
import de.marketmaker.iview.pmxml.OrderbookDataTypeBHL;
import de.marketmaker.iview.pmxml.TextWithKey;

import java.util.List;

/**
 * @author Markus Dick
 */
class OrderBookPresenterBHLKGS extends PageControllerAdapter implements OrderBookDisplay.Presenter<OrderbookDataTypeBHL>, AsyncCallback<GetOrderbookResponse> {
    private final OrderBookDisplay<OrderbookDataTypeBHL> display;
    private final OrderSession.OrderSessionBHLKGS orderSession;

    private final OrderBookQueryDisplay queryDisplay;
    private final OrderBookQueryDisplay.Presenter queryPresenter;

    private final MultiplexingWidget mux;

    private GetOrderbookRequestBHL orderbookRequestBHL;

    public OrderBookPresenterBHLKGS(OrderBookDisplay<OrderbookDataTypeBHL> display, OrderSession.OrderSessionBHLKGS orderSession) {
        this.display = display;
        this.display.setPresenter(this);
        this.orderSession = orderSession;

        //TODO: extract to own class, when we exactly know how the interface should look.
        this.queryPresenter = new OrderBookQueryDisplay.Presenter() {
            @Override
            public void onOkClicked() {
                queryDisplayOkClicked();
            }

            @Override
            public void onCancelClicked() {
                OrderBookPresenterBHLKGS.this.mux.setVisibleWidget(OrderBookPresenterBHLKGS.this.display.asWidget());
            }
        };

        this.queryDisplay = new OrderBookQueryViewBHLKGS();
        this.queryDisplay.setPresenter(this.queryPresenter);

        final List<TextWithKey> states = this.orderSession.getKWSOrderBlaetternStateIDs();
        this.queryDisplay.setOrderExecutionStates(states);
        for(TextWithKey state : states) {
            if(state.isDefault()) {
                this.queryDisplay.setSelectedOrderExecutionState(state);
            }
        }

        this.mux = new MultiplexingWidget();
        this.mux.addWidget(this.display.asWidget());
        this.mux.addWidget(this.queryDisplay.asWidget());
        this.mux.setVisibleWidget(this.queryDisplay.asWidget());

        this.orderbookRequestBHL = new GetOrderbookRequestBHL();
        this.orderbookRequestBHL.setDepotId(this.orderSession.getSecurityAccount().getId());
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        Firebug.debug("<OrderBookPresenterBHLKGS.onPlaceChange>");
        setContentHeader();
    }

    private void setContentHeader() {
        final AccountRef depot =  this.orderSession.getSecurityAccount();

        final StringBuilder sb = new StringBuilder()
                .append(I18n.I.orderEntryBHLKGSOrderBookWindowTitle()).append(": ") //$NON-NLS$
                .append(I18n.I.depot()).append(" ").append(depot.getName());

        if(StringUtil.hasText(depot.getNumber())) {
            sb.append(" (").append(depot.getNumber()).append(")"); //$NON-NLS$
        }

        AbstractMainController.INSTANCE.getView().setContentHeader(sb.toString());
    }

    private void queryDisplayOkClicked() {
        final GetOrderbookRequestBHL request = new GetOrderbookRequestBHL();
        request.setHandle(this.orderSession.getHandle());
        request.setDepotId(this.orderSession.getSecurityAccount().getId());
        request.setOnlineOrderbook(true);

        final TextWithKey state = this.queryDisplay.getSelectedOrderExecutionState();
        if(state != null) {
            request.setOrderStateID(state.getKey());
        }

        final String wkn = this.queryDisplay.getWkn();
        if(StringUtil.hasText(wkn)) {
            request.setWKN(wkn.toUpperCase());
        }

        final String orderNo = this.queryDisplay.getOrderNumber();
        if(StringUtil.hasText(orderNo)) {
            request.setOrderNumber(orderNo);
        }

        this.orderbookRequestBHL = request;

        queryOrderBook();
    }

    @Override
    public void refresh() {
        Firebug.debug("<OrderBookPresenterBHLKGS.refresh>");
        if(this.mux.getVisibleWidget() == this.display.asWidget()) {
            Firebug.debug("<OrderBookPresenterBHLKGS.refresh> table is visible, calling queryOrderBook");
            queryOrderBook();
        }
    }

    private void queryOrderBook() {
        OrderMethods.INSTANCE.queryOrderBook(this.orderSession, this.orderbookRequestBHL, this);
    }

    @Override
    public void onSuccess(GetOrderbookResponse result) {
        Firebug.debug("<OrderBookPresenterBHLKGS.onSuccess>");
        updateTable(result);
        this.mux.setVisibleWidget(this.display.asWidget());
    }

    @Override
    public void onFailure(Throwable caught) {
        Firebug.warn("<OrderBookPresenterBHLKGS.onFailure>", caught);
        OrderMethods.INSTANCE.showFailureMessage(caught);
        this.mux.setVisibleWidget(this.queryDisplay.asWidget());
    }

    private void updateTable(GetOrderbookResponse response) {
        Firebug.debug("<OrderBookPresenterBHLKGS.updateTable>");
        this.display.setEntries(OrderUtils.cast(OrderbookDataTypeBHL.class, response.getOrders()));
    }

    @Override
    public PageController asPageController() {
        return this;
    }

    @Override
    public Widget asWidget() {
        return this.mux;
    }

    @Override
    public void onShowQueryCriteriaClicked(ClickEvent event) {
        this.mux.setVisibleWidget(this.queryDisplay.asWidget());
    }

    @Override
    public void onCancelOrderClicked(ClickEvent event, OrderbookDataTypeBHL data) {
        onShowOrderClicked(event, data);
    }

    @Override
    public void onShowOrderClicked(ClickEvent event, OrderbookDataTypeBHL data) {
        final OrderDetailsPresenterBHLKGS odp = new OrderDetailsPresenterBHLKGS(new OrderDetailsView());
        odp.show(this.orderSession, data.getOrderNumber(), data.isIsDeleteAllowed());
    }

    @Override
    public void onChangeOrderClicked(ClickEvent event, OrderbookDataTypeBHL data) {
        Firebug.debug("Change order is not implemented. Order number: " + data.getOrderNumber()); //$NON-NLS$
    }

    @Override
    public boolean isShowOrderSupported() {
        return true;
    }

    @Override
    public boolean isChangeOrderSupported() {
        return false;
    }

    @Override
    public boolean isCancelOrderSupported() {
        return true;
    }

    @Override
    public boolean isPrintable() {
        return true;
    }

    @Override
    public String getPrintHtml() {
        if(this.mux.getVisibleWidget() == this.display.asWidget()) {
            return this.display.getPrintHtml();
        }
        return this.mux.getVisibleWidget().getElement().getInnerHTML();
    }

    public class MultiplexingWidget extends Composite {
        private DeckLayoutPanel layout;

        public MultiplexingWidget() {
            this.layout = new DeckLayoutPanel();
            initWidget(this.layout);
        }

        public void addWidget(Widget w) {
            this.layout.add(w);
        }

        public void setVisibleWidget(Widget w) {
            this.layout.showWidget(w);
        }

        public Widget getVisibleWidget() {
            return this.layout.getVisibleWidget();
        }
    }
}
