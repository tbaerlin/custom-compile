/*
 * OrderDetailsPresenterBHLKGS.java
 *
 * Created on 12.11.13 17:08
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebServiceAsyncProxy;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log.LogBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PrintWindow;
import de.marketmaker.iview.pmxml.GetOrderDetailsResponse;
import de.marketmaker.iview.pmxml.LookupSecurityInfo;
import de.marketmaker.iview.pmxml.OrderDataTypeBHL;
import de.marketmaker.iview.pmxml.OrderSecurityFeatureDescriptorBHL;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptorBHL;

import java.util.Collections;

/**
 * @author Markus Dick
 */
public class OrderDetailsPresenterBHLKGS implements OrderDetailsDisplay.Presenter, AsyncCallback<GetOrderDetailsResponse> {
    private final OrderDetailsDisplay<OrderDetailsDisplay.Presenter> display;

    private OrderSession.OrderSessionBHLKGS orderSession;
    private OrderDataTypeBHL order;
    private OrderSecurityFeatureDescriptorBHL securityFeatures;
    private LookupSecurityInfo securityInfo;

    public OrderDetailsPresenterBHLKGS(OrderDetailsDisplay<OrderDetailsDisplay.Presenter> display) {
        this.display = display;
        this.display.setPresenter(this);
        this.display.setColumns(2);
        this.display.setExecuteButtonText(I18n.I.orderEntryCancelOrder());
        this.display.setCancelButtonVisible(true);
        this.display.setBackButtonVisible(false);
        this.display.setPrintDateVisible(true);
    }

    public void show(OrderSession.OrderSessionBHLKGS orderSession, String orderNumber, boolean cancelOrderAllowed) {
        this.orderSession = orderSession;

        this.display.setTitle(I18n.I.orderEntryBHLKGSOrderDetailsWindowTitle(orderNumber));
        this.display.setExecuteButtonVisible(cancelOrderAllowed);

        OrderMethods.INSTANCE.getOrderDetails(orderSession, orderNumber, this);
    }

    @Override
    public void onSuccess(GetOrderDetailsResponse result) {
        this.display.setReloadActive(false);

        final OrderDataTypeBHL order = this.order = (OrderDataTypeBHL)result.getOrder();
        final OrderSessionFeaturesDescriptorBHL sessionFeatures = this.orderSession.getFeatures();
        final OrderSecurityFeatureDescriptorBHL securityFeatures = this.securityFeatures = (OrderSecurityFeatureDescriptorBHL)result.getFeatures();
        this.securityInfo = result.getSecurityInfo();

        final LogBuilder logBuilder = new OrderLogBuilder("OrderDetailsPresenterBHLKGS.onSuccess", this.orderSession); //$NON-NLS$

        final OrderConfirmationModelBuilderBHLKGS builder = new OrderConfirmationModelBuilderBHLKGS(false);
        builder.addOrderNumberSectionForOrderDetails(order);
        builder.addTransactionSection(order);
        builder.addPortfolioSection(order, logBuilder);
        builder.addSecuritySection(order, logBuilder);
        builder.addSecuritySection2(order, sessionFeatures, securityFeatures, logBuilder);
        builder.addOrderSection(order, logBuilder);
        builder.addOrderTypesSection(order, sessionFeatures, logBuilder);
        builder.addOrdererSection(order, sessionFeatures, logBuilder);
        builder.addOthersSection(order, sessionFeatures, logBuilder);
        builder.addMinutesOfTheConsultationSection(order, sessionFeatures);
        builder.addCommissionSection(order);
        builder.addTextSections(order, sessionFeatures, logBuilder);
        builder.addCompletedTransactionsSection(order);
        builder.addPredecessorAndSuccessorTransactionsSection(order);
        //TODO: pending until BhL knows what they want...
//        builder.addReversalSection(order);
        builder.addStatusSections(order, sessionFeatures, true);

        this.display.setExecuteButtonVisible(order.isIsDeleteAllowed());
        this.display.setChangeOrderButtonVisible(order.isIsChangeAllowed());
        this.display.setPrintDate(new MmJsDate());

        this.display.setSections(builder.getSections());
        this.display.show();
    }

    @Override
    public void onFailure(Throwable caught) {
        this.display.setReloadActive(false);

        final OrderConfirmationDisplay.Section noData = new OrderConfirmationDisplay.Section(I18n.I.noDataAvailable());
        this.display.setSections(Collections.singletonList(noData));
        this.display.setExecuteButtonVisible(false);

        this.display.hide();
        OrderMethods.INSTANCE.showFailureMessage(caught);
    }

    @Override
    public void onExecuteClicked() {
        final CancelOrderPresenterBHLKGS cop = new CancelOrderPresenterBHLKGS(this.orderSession, new CancelOrderViewBHLKGS());
        cop.setCallback(new CancelOrderPresenterBHLKGS.Callback() {
            @Override
            public void onHideAfterOrderCanceled() {
                reload();
            }

            @Override
            public void onCancelClicked() {
                reload();
            }
        });
        cop.show(this.order);
    }

    protected void reload() {
        Firebug.debug("<OrderDetailsPresenterBHLKGS.reload>");
        this.display.setReloadActive(true);
        OrderMethods.INSTANCE.getOrderDetails(this.orderSession, this.order.getOrderNumber(), this);
    }

    @Override
    public void onCancelClicked() {
        Firebug.debug("<OrderDetailsPresenterBHLKGS.onCancelClicked>");
        if (MmwebServiceAsyncProxy.INSTANCE.hasPendingRequests()) {
            MmwebServiceAsyncProxy.INSTANCE.doCancelPending();
        }
        this.display.hide();
    }

    @Override
    public void onBackClicked() {
        Firebug.debug("<OrderDetailsPresenterBHLKGS.onBackClicked> do nothing");
    }

    @Override
    public void onPrintClicked() {
        PrintWindow.print(this.display.getPrintHtml(), null);
    }

    @Override
    public void onReloadClicked() {
        Firebug.debug("<OrderDetailsPresenterBHLKGS.onReloadClicked>");
        reload();
    }

    @Override
    public void onChangeOrderClicked() {
        Firebug.debug("<OrderDetailsPresenterBHLKGS.onChangeOrderClicked>");
        final OrderViewBHLKGS v = new OrderViewBHLKGS();
        final OrderPresenterBHLKGS.OrderStrategy strategy = new OrderPresenterBHLKGS.ChangeOrderStrategy(this.order, this.securityFeatures, this.securityInfo);
        final OrderPresenterBHLKGS p = new OrderPresenterBHLKGS(v, this.orderSession, strategy);
        p.addPresenterDisposedHandler(new PresenterDisposedHandler() {
            @Override
            public void onPresenterDisposed(PresenterDisposedEvent event) {
                reload();
            }
        });
        p.show();
    }
}
