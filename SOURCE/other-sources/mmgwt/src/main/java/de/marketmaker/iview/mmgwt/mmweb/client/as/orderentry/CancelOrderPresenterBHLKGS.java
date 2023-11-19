/*
 * CancelOrderPresenterBHLKGS.java
 *
 * Created on 11.11.13 14:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;


import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.pmxml.CancelOrderResponse;
import de.marketmaker.iview.pmxml.OrderDataTypeBHL;
import de.marketmaker.iview.pmxml.OrderSessionFeaturesDescriptorBHL;
import de.marketmaker.iview.pmxml.TextType;
import de.marketmaker.iview.pmxml.TextWithKey;
import de.marketmaker.iview.pmxml.TextWithTyp;
import de.marketmaker.iview.pmxml.ValidationMessage;

import java.util.Collections;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderMethods.INSTANCE;

/**
 * @author Markus Dick
 */
public class CancelOrderPresenterBHLKGS implements CancelOrderDisplayBHLKGS.Presenter, OrderMethods.OrderValidationCallback<OrderDataTypeBHL> {
    private final CancelOrderDisplayBHLKGS display;
    private final OrderSession<OrderSessionFeaturesDescriptorBHL> orderSession;
    private OrderDataTypeBHL orderDataTypeBHL;

    private Callback callback;

    private OrderValidationMessagePresenter orderValidationMessagePresenter;

    public CancelOrderPresenterBHLKGS(OrderSession<OrderSessionFeaturesDescriptorBHL> orderSession, CancelOrderDisplayBHLKGS display) {
        this.display = display;
        this.display.setPresenter(this);
        this.orderSession = orderSession;

        this.orderValidationMessagePresenter = new OrderValidationMessagePresenter(this.orderSession, new OrderValidationMessageView());
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void show(OrderDataTypeBHL order) {
        this.orderDataTypeBHL = order;
        this.display.setOrderNumber(order.getOrderNumber());

        final OrderSessionFeaturesDescriptorBHL features = this.orderSession.getFeatures();

        final List<TextWithKey> cancellationReasons = features.getCancellationReason();
        this.display.setCancellationReasons(cancellationReasons);
        this.display.setCancellationReasonsSelectedItem(OrderUtils.findDefaultTextWithKey(cancellationReasons, true));

        this.display.setPrintCancellationConfirmation(false);

        final List<TextWithKey> cannedText = features.getTextLibrariesOrderConfirmation();
        this.display.setCannedTextForOrderConfirmations(cannedText);
        this.display.setCannedTextForOrderConfirmationsSelectedItem(OrderUtils.findTextWithKey(cannedText, order.getOrderConfirmation()));

        for(TextWithTyp twt : order.getFreeText()) {
            final String text = twt.getText();
            switch(twt.getTyp()) {
                case TT_INTERNAL_TEXT_1:
                    this.display.setInternalText1(text);
                    break;
                case TT_INTERNAL_TEXT_2:
                    this.display.setInternalText2(text);
                    break;
            }
        }

        this.display.show();
    }

    private OrderDataTypeBHL amendOrderDataType(OrderDataTypeBHL order) {
        final OrderLogBuilder log = new OrderLogBuilder("CancelOrderPresenter.amendOrderDataType", this.orderSession); //$NON-NLS$

        log.add("order.orderNumber", order.getOrderNumber()); //$NON-NLS$

        order.setCancellationConfirmationPrint(display.isPrintCancellationConfirmation());
        log.add("display.isPrintCancellationConfirmation", display.isPrintCancellationConfirmation()); //$NON-NLS$
        log.add("order.cancellationConfirmationPrint", order.isCancellationConfirmationPrint()); //$NON-NLS$

        final TextWithKey cancellationReason = this.display.getCancellationReasonsSelectedItem();
        if(cancellationReason != null) {
            order.setCancellationReason(cancellationReason.getKey());
        }
        log.addTextWithKey("display.cancellationReasonsSelectedItem", cancellationReason); //$NON-NLS$
        log.add("order.cancellationReason", order.getCancellationReason()); //$NON-NLS$

        //canned text
        final TextWithKey cannedText =  this.display.getCannedTextForOrderConfirmationsSelectedItem();
        if(cannedText != null) {
            order.setOrderConfirmation(cannedText.getKey());
        }
        log.addTextWithKey("display.cannedTextForOrderConfirmationsSelectedItem", cannedText); //$NON-NLS$
        log.add("order.orderConfirmation", order.getOrderConfirmation()); //$NON-NLS$

        //free texts
        final List<TextWithTyp> freeTexts = order.getFreeText();

        final String internalText1 =  this.display.getInternalText1();
        updateOrAddNewFreeText(order, freeTexts, internalText1, TextType.TT_INTERNAL_TEXT_1);

        final String internalText2 =  this.display.getInternalText2();
        updateOrAddNewFreeText(order, freeTexts, internalText2, TextType.TT_INTERNAL_TEXT_2);

        log.add("display.getInternalText1", internalText1); //$NON-NLS$
        log.add("display.getInternalText2", internalText2); //$NON-NLS$
        log.addTextWithTypeList("order.freeText", order.getFreeText()); //$NON-NLS$

        final String logStr = log.toString();
        DebugUtil.logToServer(logStr);
        Firebug.debug(logStr);

        return order;
    }

    private void updateOrAddNewFreeText(OrderDataTypeBHL order, List<TextWithTyp> freeTexts, String freeTextStr, TextType textType) {
        final TextWithTyp textWithType = OrderUtils.findTextWithType(freeTexts, textType);
        if(textWithType != null) {
            textWithType.setText(freeTextStr);
        }
        else {
            order.getFreeText().add(OrderUtils.newTextWithType(textType, freeTextStr));
        }
    }

    @Override
    public void onProceedClicked() {
        try {
            lockDisplayAndShowLoadingIndicator();
            cancelOrder(amendOrderDataType(this.orderDataTypeBHL), Collections.<ValidationMessage>emptyList());
        }
        catch(Exception e) {
            unlockDisplayAndHideLoadingIndicator();
            OrderMethods.INSTANCE.showFailureMessage(e);
        }
    }

    protected void cancelOrder(final OrderDataTypeBHL order, List<ValidationMessage> validationMessages) {
        OrderMethods.INSTANCE.cancelOrder(this.orderSession, order, validationMessages, new AsyncCallback<CancelOrderResponse>() {
            @Override
            public void onSuccess(CancelOrderResponse result) {
                onCancelOrderSuccessful(order.getOrderNumber());
            }

            @Override
            public void onFailure(Throwable caught) {
                onCancelOrderFailed(caught);
            }
        });
    }

    protected void onCancelOrderSuccessful(String orderNumber) {
        unlockDisplayAndHideLoadingIndicator();
        this.display.hide();
        Dialog.info(I18n.I.orderEntryCancelOrderSuccessful(orderNumber));

        this.orderDataTypeBHL = null;

        if(this.callback != null) {
            this.callback.onHideAfterOrderCanceled();
        }
    }

    protected void onCancelOrderFailed(Throwable caught) {
        unlockDisplayAndHideLoadingIndicator();
        INSTANCE.handleBackendValidationFailed(this.orderValidationMessagePresenter, caught, this);
    }

    @Override
    public void onProceedAfterValidateOrderFailed(OrderDataTypeBHL receivedDataTypeBHL, List<ValidationMessage> editedValidationMessages) {
        final OrderLogBuilder log = new OrderLogBuilder("CancelOrderPresenterBHLKGS.onProceedAfterValidateOrderFailed", //$NON-NLS$
                this.orderSession);
        DebugUtil.logToServer(log.addValidationMessages(editedValidationMessages).toString());

        lockDisplayAndShowLoadingIndicator();
        cancelOrder(receivedDataTypeBHL, editedValidationMessages);
    }

    @Override
    public void onCancelAfterValidateOrderFailed(List<ValidationMessage> validationMessages) {
        final OrderLogBuilder log = new OrderLogBuilder("CancelOrderPresenterBHLKGS.onCancelAfterValidateOrderFailed", //$NON-NLS$
                this.orderSession);

        DebugUtil.logToServer(log.addValidationMessages(validationMessages).toString());
        Firebug.debug("<CancelOrderPresenterBHLKGS.onCancelAfterValidateOrderFailed>");
    }

    @Override
    public void onAnyExceptionAfterValidateOrderFailed(Throwable caught) {
        OrderMethods.INSTANCE.showFailureMessage(caught);
        Firebug.debug("<CancelOrderPresenterBHLKGS.onAnyExceptionAfterValidateOrderFailed>");
    }

    @Override
    public void onCancelClicked() {
        this.display.hide();
        if(callback != null) {
            this.callback.onCancelClicked();
        }
    }

    private void lockDisplayAndShowLoadingIndicator() {
        this.display.setProceedButtonEnabled(false);
        this.display.setCancelButtonEnabled(false);
        this.display.setLoadingIndicatorVisible(true);
    }

    private void unlockDisplayAndHideLoadingIndicator() {
        this.display.setProceedButtonEnabled(true);
        this.display.setCancelButtonEnabled(true);
        this.display.setLoadingIndicatorVisible(false);
    }

    public interface Callback {
        public void onHideAfterOrderCanceled();
        public void onCancelClicked();
    }
}
