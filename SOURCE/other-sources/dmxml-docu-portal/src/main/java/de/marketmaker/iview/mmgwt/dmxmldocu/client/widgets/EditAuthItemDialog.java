/*
 * EditAuthItemDialog.java
 *
 * Created on 26.07.12 08:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Markus Dick
 */
public class EditAuthItemDialog extends DialogBox {
    protected AuthItem authItem;
    protected Callback callback;
    protected boolean isVirginItemName = true;

    private TextBox itemNameTextBox;
    private TextBox authenticationTextBox;
    private TextBox authenticationTypeTextBox;
    private TextBox localeTextBox;

    public EditAuthItemDialog(Callback callback) {
        super();
        this.callback = callback;
        initDialog();
    }

    protected void initDialog() {
        final KeyUpHandler fillItemNameHandler = new FillItemNameKeyUpHandler();
        final EnterKeyDownHandler enterKeyDownHandler = new EnterKeyDownHandler();

        Label authenticationLabel = new Label();
        authenticationLabel.setText("authentication");  //$NON-NLS$
        authenticationTextBox = new TextBox();
        authenticationTextBox.addKeyUpHandler(fillItemNameHandler);
        authenticationTextBox.addKeyDownHandler(enterKeyDownHandler);


        Label authenticationTypeLabel = new Label();
        authenticationTypeLabel.setText("authenticationType"); //$NON-NLS$
        authenticationTypeTextBox = new TextBox();
        authenticationTypeTextBox.addKeyUpHandler(fillItemNameHandler);
        authenticationTypeTextBox.addKeyDownHandler(enterKeyDownHandler);

        Label itemNameLabel = new Label();
        itemNameLabel.setText("Item Name");  //$NON-NLS$
        itemNameTextBox = new TextBox();
        itemNameTextBox.addKeyDownHandler(enterKeyDownHandler);
        itemNameTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> stringValueChangeEvent) {
                final String newValue = stringValueChangeEvent.getValue();
                isVirginItemName = newValue == null || newValue.isEmpty();
            }
        });

        Label localeLabel = new Label();
        localeLabel.setText("locale");  //$NON-NLS$
        localeTextBox = new TextBox();

        FlexTable layout = new FlexTable();
        int row = 0;

        layout.setWidget(row, 0, authenticationLabel);
        layout.setWidget(row++, 1, authenticationTextBox);
        layout.setWidget(row, 0, authenticationTypeLabel);
        layout.setWidget(row++, 1, authenticationTypeTextBox);
        layout.setWidget(row, 0, localeLabel);
        layout.setWidget(row++, 1, localeTextBox);
        layout.setWidget(row, 0, itemNameLabel);
        layout.setWidget(row++, 1, itemNameTextBox);

        FlowPanel buttonPanel = new FlowPanel();

        Button okButton = new Button();
        okButton.setText("Ok"); //$NON-NLS$
        okButton.addClickHandler(new OkButtonClickHandler());
        buttonPanel.add(okButton);

        Button cancelButton = new Button();
        cancelButton.setText("Cancel"); //$NON-NLS$
        cancelButton.addClickHandler(new CancelButtonClickHandler());
        buttonPanel.add(cancelButton);

        layout.setWidget(row, 0, buttonPanel);
        layout.getFlexCellFormatter().setColSpan(row, 0, 2);
        layout.getFlexCellFormatter().setAlignment(row, 0, HasHorizontalAlignment.ALIGN_RIGHT,
                HasVerticalAlignment.ALIGN_MIDDLE);

        setWidget(layout);
    }

    public void reset() {
        authItem = null;
        isVirginItemName = true;
        itemNameTextBox.setValue(null);
        authenticationTextBox.setValue(null);
        authenticationTypeTextBox.setValue(null);
        localeTextBox.setValue(null);
    }

    public AuthItem getAuthItem() {
        return authItem;
    }

    public void setAuthItem(AuthItem authItem) {
        if(authItem == null) throw new IllegalArgumentException("AuthItem must not be null!"); //$NON-NLS$

        this.authItem = authItem;
        authenticationTextBox.setValue(authItem.getAuthentication());
        authenticationTypeTextBox.setValue(authItem.getAuthenticationType());
        localeTextBox.setValue(authItem.getLocale());
        itemNameTextBox.setValue(authItem.getItemName());
        isVirginItemName = guessVirginState(authItem);
    }

    private boolean guessVirginState(AuthItem authItem) {
        final String itemName = authItem.getItemName();
        String authentication = authItem.getAuthentication();
        String authenticationType = authItem.getAuthenticationType();

        //This should be true for a new authItem.
        if(itemName == null && authentication == null && authenticationType == null) {
            return true;
        }

        //This checks already filled items.
        if(authentication == null) authentication = "";
        if(authenticationType == null) authenticationType = "";

        final String genericItemName = toItemName(authentication, authenticationType);

        return genericItemName.equals(itemName);
    }

    private void okButtonAction() {
        //Validate
        String itemName = itemNameTextBox.getValue();
        if(itemName == null || itemName.isEmpty()) {
            itemNameTextBox.setValue("n/a");  //$NON-NLS$
        }

        //Set data
        authItem.setItemName(itemNameTextBox.getText());
        authItem.setAuthentication(authenticationTextBox.getText());
        authItem.setAuthenticationType(authenticationTypeTextBox.getText());
        authItem.setLocale(localeTextBox.getText());

        hide();
        callback.onEditOk();
        reset();
    }

    public String toItemName(String authentication, String authenticationType) {
        if(!(authentication.isEmpty() || authenticationType.isEmpty())) {
            authentication += " ";
        }
        authentication += authenticationType;
        return authentication;
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    private class FillItemNameKeyUpHandler implements KeyUpHandler {
        @Override
        public void onKeyUp(KeyUpEvent event) {
            if(!isVirginItemName) return;
            final String result = toItemName(authenticationTextBox.getValue(), authenticationTypeTextBox.getValue());
            itemNameTextBox.setValue(result);
        }
    }

    private class OkButtonClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            okButtonAction();

        }
    }

    private class CancelButtonClickHandler implements ClickHandler {
        @Override
        public void onClick(ClickEvent event) {
            authItem = null;
            hide();
            reset();
        }
    }

    private class EnterKeyDownHandler implements KeyDownHandler {
        /**
         * Called when KeyDownEvent is fired.
         *
         * @param event the {@link com.google.gwt.event.dom.client.KeyDownEvent} that was fired
         */
        @Override
        public void onKeyDown(KeyDownEvent event) {
            if(event.getNativeKeyCode() != KeyCodes.KEY_ENTER) return;
            okButtonAction();
        }
    }

    public static interface Callback {
        public void onEditOk();
    }
}
