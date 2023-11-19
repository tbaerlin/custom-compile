/*
 * MessageBox.java
 *
 * Created on 26.07.12 08:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * @author Markus Dick
 */
public final class MessageBox {
    public static enum BoxType { CONFIRM, MESSAGE /*, WARN, ERROR */ }
    public static enum Choice { OK, YES, NO, CANCEL }
    public static final String MESSAGE_BOX_STYLE = "messageBox"; //$NON-NLS$

    private MessageBox() {
        /* This must not be created */
    }

    public static void messagePopup(String message, UIObject target) {
        PopupPanel popupPanel = new PopupPanel();
        popupPanel.addStyleName(MESSAGE_BOX_STYLE);
        popupPanel.addStyleName(MESSAGE_BOX_STYLE + "-" + BoxType.MESSAGE.name());
        popupPanel.setWidget(new MessagePanel(BoxType.MESSAGE, message, null, popupPanel));
        popupPanel.setPreviewingAllNativeEvents(true);
        popupPanel.setAutoHideEnabled(true);
        popupPanel.showRelativeTo(target);
    }

    public static void confirmPopup(String message, UIObject target, final Callback callback) {
        //TODO: Refactor pop-up creation: extract method.
        //TODO: Refactor: extract as specialized class.
        PopupPanel popupPanel = new PopupPanel() {
            @Override
            protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
                super.onPreviewNativeEvent(event);

                switch (event.getTypeInt()) {
                    case Event.ONKEYDOWN:
                        final int keyCode = event.getNativeEvent().getKeyCode();

                        if (keyCode == KeyCodes.KEY_ENTER || keyCode == KeyCodes.KEY_ESCAPE) {
                            hide();
                            if(callback != null) {
                                callback.onSelect(Choice.NO);
                            }
                        }
                        break;
                }
            }
        };
        popupPanel.addStyleName(MESSAGE_BOX_STYLE);
        popupPanel.addStyleName(MESSAGE_BOX_STYLE + "-" + BoxType.CONFIRM.name());
        popupPanel.setWidget(new MessagePanel(BoxType.CONFIRM, message, callback, popupPanel));
        popupPanel.setPreviewingAllNativeEvents(true);
        popupPanel.setAutoHideEnabled(true);
        popupPanel.showRelativeTo(target);
    }

    private static class ButtonClickHandler implements ClickHandler {
        private final Choice choice;
        private final PopupPanel parent;
        private final Callback callback;

        public ButtonClickHandler(Choice choice, Callback callback, PopupPanel parent) {
            super();
            this.choice = choice;
            this.parent = parent;
            this.callback = callback;
        }

        /**
         * Called when a native click event is fired.
         *
         * @param event the {@link com.google.gwt.event.dom.client.ClickEvent} that was fired
         */
        @Override
        public void onClick(ClickEvent event) {
            parent.hide();
            if(callback != null) {
                callback.onSelect(choice);
            }
        }
    }

    private static class MessagePanel extends Composite {
        public static final String MESSAGE_STYLE_PREFIX = "message-"; //$NON-NLS$
        private final Callback callback;

        public MessagePanel(BoxType type, String message, Callback callback, PopupPanel parent) {
            this.callback = callback;

            VerticalPanel panel = new VerticalPanel();
            this.initWidget(panel);

            Label messageLabel = new Label();
            messageLabel.setText(message);
            messageLabel.setStyleName(MESSAGE_STYLE_PREFIX + type.name());
            panel.add(messageLabel);

            FlowPanel buttonPanel = new FlowPanel();
            panel.add(buttonPanel);

            switch(type) {
                case CONFIRM:
                    Button yesButton = new Button();
                    yesButton.setText("Yes"); //$NON-NLS$
                    yesButton.addClickHandler(new ButtonClickHandler(Choice.YES, callback, parent));
                    buttonPanel.add(yesButton);

                    Button noButton = new Button();
                    noButton.setText("No"); //$NON-NLS$
                    noButton.addClickHandler(new ButtonClickHandler(Choice.NO, callback, parent));
                    buttonPanel.add(noButton);

                    break;

                case MESSAGE:
                default:
                    Button okButton = new Button();
                    okButton.setText("OK"); //$NON-NLS$
                    okButton.addClickHandler(new ButtonClickHandler(Choice.OK, callback, parent));
                    buttonPanel.add(okButton);
            }
        }
    }

    public static interface Callback {
        public void onSelect(Choice value);
    }
}
