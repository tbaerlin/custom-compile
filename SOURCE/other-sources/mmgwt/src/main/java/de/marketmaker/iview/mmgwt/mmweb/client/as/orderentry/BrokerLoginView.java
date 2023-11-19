/*
 * BrokerLoginView.java
 *
 * Created on 18.12.12 10:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;

/**
 * @author Markus Dick
 */
class BrokerLoginView implements BrokerLoginDisplay {
    interface BrokerLoginViewBinder extends UiBinder<HTMLPanel, BrokerLoginView> {}
    private static BrokerLoginViewBinder uiBinder = GWT.create(BrokerLoginViewBinder.class);

    private Presenter presenter;

    final DialogIfc dialog;

    @UiField(provided = true)
    protected final I18n i18n = I18n.I;

    @UiField
    protected TextBox userField;
    @UiField
    protected PasswordTextBox passwordField;
    @UiField
    protected HTML messageLabel;

    public BrokerLoginView() {
        final HTMLPanel panel = uiBinder.createAndBindUi(this);
        panel.setWidth("300px");  // $NON-NLS$
        panel.setHeight("220px"); // $NON-NLS$
        this.dialog = Dialog.getImpl().createDialog().withTitle(I18n.I.login()).withWidget(panel);

        this.dialog.withDefaultButton(I18n.I.ok(), new Command() {
            @Override
            public void execute() {
                BrokerLoginView.this.dialog.keepOpen();
                BrokerLoginView.this.presenter.onPassword();
            }
        });

        final Command cancelCommand = new Command() {
            @Override
            public void execute() {
                BrokerLoginView.this.dialog.keepOpen();
                BrokerLoginView.this.presenter.onCancel();
            }
        };
        this.dialog.withButton(I18n.I.cancel(), cancelCommand);
        this.dialog.withEscapeCommand(cancelCommand);

        this.dialog.withFocusWidget(this.passwordField);
    }

    @UiHandler("userField")
    public void onUserFieldKeyPress(KeyPressEvent event) {
        if(KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
            BrokerLoginView.this.presenter.onPassword();
        }
    }

    @UiHandler("passwordField")
    public void onPasswordFieldKeyPress(KeyPressEvent event) {
        if(KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
            BrokerLoginView.this.presenter.onPassword();
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setHeading(String title) {
        this.dialog.withTitle(title);
    }

    @Override
    public String getPassword() {
        return this.passwordField.getText();
    }

    @Override
    public void setUser(final String brokingUsername) {
        this.userField.setValue(brokingUsername);
    }

    @Override
    public String getUser() {
        return this.userField.getValue();
    }

    @Override
    public void setMessage(SafeHtml message) {
        this.messageLabel.setHTML(message);
    }

    @Override
    public void setMessageVisible(boolean visible) {
        this.messageLabel.setVisible(visible);
    }

    @Override
    public void show() {
        this.dialog.show();
    }

    @Override
    public void hide() {
        this.dialog.closePopup();
    }
}
