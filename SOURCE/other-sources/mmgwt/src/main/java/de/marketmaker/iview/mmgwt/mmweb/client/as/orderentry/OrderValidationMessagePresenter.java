/*
 * OrderValidationMessagePresenter.java
 *
 * Created on 18.01.13 10:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain.OrderSession;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.log.LogBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.pmxml.OrderValidationServerityType;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;
import de.marketmaker.iview.pmxml.ValidationMessage;

import java.util.List;

/**
 * @author Markus Dick
 */
class OrderValidationMessagePresenter implements OrderValidationMessageDisplay.Presenter {
    public interface Callback {
        void onProceed(List<ValidationMessage> editedValidationMessages);
        void onCancel();
    }

    private final OrderValidationMessageDisplay display;

    private List<ValidationMessage> messages;

    private Callback callback;

    private final OrderSession orderSession;

    public OrderValidationMessagePresenter(OrderSession orderSession, OrderValidationMessageDisplay display) {
        this.orderSession = orderSession;
        this.display = display;
        this.display.setPresenter(this);
    }

    public void show(List<ValidationMessage> messages, Callback callback) {
        this.callback = callback;
        this.messages = messages;
        this.display.setMessages(messages);

        boolean anyError = false;
        for(ValidationMessage message : messages) {
            if(OrderValidationServerityType.VST_ERROR.equals(message.getServerity())) {
                anyError = true;
            }
        }
        this.display.setOkButtonVisible(!anyError);

        this.display.show();

        final LogBuilder log = new OrderLogBuilder("OrderValidationMessagePresenter.show", this.orderSession); //$NON-NLS$
        DebugUtil.logToServer(addToLog(log, this.messages).toString());
    }

    @Override
    public void onAnswer(int messageIndex, ThreeValueBoolean answer) {
        Firebug.debug("<OrderValidationMessagePresenter.onAnswer> index=" + messageIndex + " answer=" + answer);

        final LogBuilder log = new OrderLogBuilder("OrderValidationMessagePresenter.onAnswer", this.orderSession); //$NON-NLS$
        log.add("messageIndex", messageIndex) //$NON-NLS$
                .add("answer", answer); //$NON-NLS$

        final ValidationMessage message = this.messages.get(messageIndex);

        if(message == null) {
            log.add("no message for given index"); //$NON-NLS$
            OrderMethods.INSTANCE.showFailureMessage(SafeHtmlUtils.fromTrustedString("No message with index " + messageIndex + " found")); //$NON-NLS$
            DebugUtil.logToServer(log.toString());
            return;
        }

        DebugUtil.logToServer(addToLog(log, message).toString());

        message.setAnswer(answer);
    }

    @Override
    public void onOkButtonClicked() {
        Firebug.debug("<OrderValidationMessagePresenter.onOkButtonClicked>");
        DebugUtil.logToServer(new OrderLogBuilder("OrderValidationMessagePresenter.onOkButtonClicked", this.orderSession).toString());

        this.display.hide();

        if(this.callback != null) {
            this.callback.onProceed(this.messages);
        }
    }

    @Override
    public void onCancelButtonClicked() {
        Firebug.debug("<OrderValidationMessagePresenter.onCancelButtonClicked>");
        DebugUtil.logToServer(new OrderLogBuilder("OrderValidationMessagePresenter.onCancelButtonClicked", this.orderSession).toString());

        this.display.hide();

        if(this.callback != null) {
            this.callback.onCancel();
        }
    }

    private LogBuilder addToLog(LogBuilder log, ValidationMessage message) {
        log.beginObject("ValidationMessage") //$NON-NLS$
            .add("type", message.getTyp())   //$NON-NLS$
            .add("severity", message.getServerity())  //$NON-NLS$
            .add("msg", message.getMsg()) //$NON-NLS$
            .add("externalCode", message.getExternalCode()) //$NON-NLS$
            .add("answer", message.getAnswer()) //$NON-NLS$
            .endObject();

        return log;
    }

    private LogBuilder addToLog(LogBuilder log, List<ValidationMessage> messages) {
        if(messages == null) {
            return log;
        }

        log.beginList();
        for(ValidationMessage message : messages) {
            addToLog(log, message);
        }
        log.endList();

        return log;
    }
}
