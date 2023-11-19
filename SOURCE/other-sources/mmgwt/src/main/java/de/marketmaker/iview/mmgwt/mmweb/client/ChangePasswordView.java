/*
 * ChangePasswordView.java
 *
 * Created on 29.04.2008 18:03:37
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasShowHide;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ChangePasswordView implements ChangePasswordDisplay, HasShowHide {
    private Presenter presenter;

    private final TextField<String> oldField;
    private final TextField<String> newField1;
    private final TextField<String> newField2;

    private final HTML errorText = new HTML("&nbsp;"); //$NON-NLS$
    private final HTML passwordRulesDescription = new HTML();

    private final Button cancelButton;
    private final Window w;

    public ChangePasswordView() {
        this.w = new Window();
        this.w.setHeading(I18n.I.changePassword());
        this.w.setAutoHeight(true);
        this.w.setWidth(380);
        this.w.setAutoWidth(false);
        this.w.setModal(true);
        this.w.setResizable(false);
        this.w.setDraggable(false);
        this.w.setClosable(false);

        this.w.setLayout(new FlowLayout());

        final FocusPanel focusPanel = new FocusPanel();
        final FormPanel formPanel = new FormPanel();
        focusPanel.add(formPanel);
        focusPanel.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if(ChangePasswordView.this.cancelButton.isEnabled()
                        && event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE) {
                    ChangePasswordView.this.presenter.onCancel();
                }
                if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    ChangePasswordView.this.presenter.onOk();
                }
            }
        });
        formPanel.setHeaderVisible(false);
        formPanel.setPadding(10);
        formPanel.setLabelWidth(110);

        this.oldField = createPasswordField(I18n.I.oldPassword());
        this.newField1 = createPasswordField(I18n.I.newPassword());
        this.newField2 = createPasswordField(I18n.I.repeatNewPassword());

        final Button okButton = new Button(I18n.I.ok(), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                ChangePasswordView.this.presenter.onOk();
            }
        });

        this.cancelButton = new Button(I18n.I.cancel(), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                ChangePasswordView.this.presenter.onCancel();
            }
        });

        formPanel.add(this.oldField);
        formPanel.add(this.newField1);
        formPanel.add(this.newField2);
        formPanel.add(this.errorText);

        this.w.addButton(okButton);
        this.w.addButton(this.cancelButton);
        this.w.setButtonAlign(Style.HorizontalAlignment.RIGHT);

        final FlowData flowData = new FlowData(3, 3, 3, 3);

        this.w.add(focusPanel, flowData);
        this.w.add(this.passwordRulesDescription, flowData);

        this.w.addListener(Events.Activate, new Listener<WindowEvent>() {
            public void handleEvent(WindowEvent event) {
                ChangePasswordView.this.oldField.focus();
            }
        });

    }

    private TextField<String> createPasswordField(String label) {
        final TextField<String> result = new TextField<>();
        result.setFieldLabel(label);
        result.setWidth(100);
        result.setPassword(true);
        return result;
    }

    @Override
    public String getOldPassword() {
        return this.oldField.getValue();
    }

    @Override
    public String getNewPassword1() {
        return this.newField1.getValue();
    }

    @Override
    public String getNewPassword2() {
        return this.newField2.getValue();
    }

    @Override
    public void reset() {
        this.oldField.setValue("");
        this.newField1.setValue("");
        this.newField2.setValue("");
        this.oldField.focus();
    }

    @Override
    public void resetNewPasswords() {
        //do nothing
    }

    @Override
    public void selectOldAndKeepAll() {
        //do nothing
    }

    @Override
    public void setError(String errorText) {
        this.errorText.setText(errorText);
    }

    @Override
    public void clearError() {
        this.errorText.setText(null);
    }

    @Override
    public void setValidationError(ValidationCause cause, String message) {
        this.errorText.setText(message);
    }

    @Override
    public void clearValidationError() {
        this.errorText.setText(null);
    }

    @Override
    public void showSuccessMessage(String message) {
        AbstractMainController.INSTANCE.showMessage(message);
    }

    @Override
    public void setCancelEnabled(boolean enabled) {
        this.cancelButton.setEnabled(enabled);
    }

    @Override
    public void setSubmitEnabled(boolean enabled) {
        //do nothing
    }

    @Override
    public void setResetNewPasswordsEnabled(boolean visible) {
        //do nothing
    }

    @Override
    public void setPasswordRulesDescription(SafeHtml passwordRulesDescription) {
        if(passwordRulesDescription == null) {
            this.passwordRulesDescription.setHTML((String)null);
            return;
        }
        this.passwordRulesDescription.setHTML(passwordRulesDescription);
    }

    @Override
    public void setPasswordRulesDescriptionVisible(boolean visible) {
        this.passwordRulesDescription.setVisible(visible);
    }

    @Override
    public void setOldPasswordAllowBlank(boolean allowBlank) {
        this.oldField.setAllowBlank(allowBlank);
    }

    @Override
    public void setNewPasswordsAllowBlank(boolean allowBlank) {
        this.newField1.setAllowBlank(allowBlank);
        this.newField2.setAllowBlank(allowBlank);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void show() {
        this.w.show();
        this.w.center();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
                ChangePasswordView.this.oldField.focus();
            }
        });
    }

    @Override
    public void hide() {
        this.w.hide();
    }
}
