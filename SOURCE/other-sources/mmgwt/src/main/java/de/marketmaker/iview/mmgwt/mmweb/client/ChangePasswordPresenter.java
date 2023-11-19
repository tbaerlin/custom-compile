/*
 * ChangePasswordPresenter.java
 *
 * Created on 02.09.13 14:02
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.AsyncCallback;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.ChangePasswordDisplay.ValidationCause;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CompletionHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasShowHide;

import java.util.Date;

import static de.marketmaker.iview.mmgwt.mmweb.client.ChangePasswordDisplay.ValidationCause.*;

/**
 * @author Markus Dick
 */
public class ChangePasswordPresenter implements ChangePasswordDisplay.Presenter {
    private final ChangePasswordDisplay display;

    private final CompletionHandler handler;

    private final PasswordStrategy passwordStrategy;

    private boolean cancelEnabled = true;

    public ChangePasswordPresenter(ChangePasswordDisplay display, PasswordStrategy passwordStrategy,
            CompletionHandler completionHandler) {
        this.display = display;
        this.display.setPresenter(this);
        this.passwordStrategy = passwordStrategy;
        this.handler = completionHandler;
    }

    /**
     * Shows the display. Should only be used for display implementations that implement
     * {@link de.marketmaker.iview.mmgwt.mmweb.client.view.HasShowHide}.
     * E.g. display implementations that view their content in a window/dialog.
     *
     * If the initial password has to be changed when the user logs in for the first time,
     * enableCancel should be false.
     */
    public void show(boolean enableCancel) {
        this.cancelEnabled = enableCancel;

        if (!(this.display instanceof HasShowHide)) {
            throw new IllegalStateException("ChangePasswordPresenter: cannot call show, because display does not implement HasShowHide");  // $NON-NLS$
        }

        if (this.passwordStrategy == null) {
            Firebug.warn("<ChangePasswordPresenter.show> passwordStrategy not set!");
            return;
        }
        if (this.passwordStrategy.isShowViewAvailable()) {
            initPasswordStrategyDependingFields();
            resetButtons();
            ((HasShowHide) this.display).show();
        }
        else {
            // this case happens only if you are testing and trying to change the password again
            // but the strategy says that you are not allowed to show the change password view...
            // than it should go further on.
            tryOnComplete();
        }
    }

    public void initPasswordStrategyDependingFields() {
        final SafeHtml passwordRulesDescription = this.passwordStrategy.getPasswordRulesDescription();

        this.display.setPasswordRulesDescription(passwordRulesDescription);
        this.display.setPasswordRulesDescriptionVisible(passwordRulesDescription != null);

        final boolean blankAllowed = this.passwordStrategy.isBlankPasswordAllowed();
        this.display.setOldPasswordAllowBlank(blankAllowed);
        this.display.setNewPasswordsAllowBlank(blankAllowed);
    }

    /**
     * Resets the display. Should be used to clear/re-initialize the view of display implementations
     * that do not implement {@link de.marketmaker.iview.mmgwt.mmweb.client.view.HasShowHide} and which are intended to
     * be directly rendered in the content area.
     * E.g. called from {@link de.marketmaker.iview.mmgwt.mmweb.client.PageController#onPlaceChange(de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent)}.
     */
    public void reset() {
        initPasswordStrategyDependingFields();
        this.cancelEnabled = false;
        resetButtons();
        this.display.clearError();
        this.display.clearValidationError();
        this.display.reset();
    }

    private void resetButtons() {
        this.display.setCancelEnabled(this.cancelEnabled);
        this.display.setSubmitEnabled(false);
        this.display.setResetNewPasswordsEnabled(false);
    }

    private void showValidationError(ValidationCause cause) {
        this.display.setValidationError(cause, this.passwordStrategy.getValidationMessage(cause));
    }

    private void showErrorAndReset(ChangePasswordResponse response) {
        this.display.setError(this.passwordStrategy.getErrorMessage(response));
        this.passwordStrategy.amendDisplayedError(response, this.display);
    }

    private void tryOnComplete() {
        if (this.handler != null) {
            this.handler.onComplete(null);
        }
    }

    private void changePassword() {
        if (!validate(true)) return;

        UserServiceAsync.App.getInstance().changePassword(SessionData.INSTANCE.getUser().getUid(),
                this.display.getOldPassword(), this.display.getNewPassword1(), GuiDefsLoader.getModuleName(),
                new AsyncCallback<ChangePasswordResponse>() {
                    public void onFailure(Throwable throwable) {
                        display.setError(I18n.I.processError());
                    }

                    public void onSuccess(ChangePasswordResponse response) {
                        if (response == ChangePasswordResponse.OK) {
                            ChangePasswordPresenter.this.display.reset();
                            reset();
                            tryHide();
                            ChangePasswordPresenter.this.display.showSuccessMessage(I18n.I.passwordChanged());
                            SessionData.INSTANCE.getUser().setPasswordChangeDate(new Date());
                            SessionData.INSTANCE.getUser().setPasswordChangeRequired(false);
                            tryOnComplete();
                        }
                        else {
                            showErrorAndReset(response);
                        }
                    }
                });
    }

    private boolean validate(boolean showErrorEvenIfNewPasswordIsEmpty) {
        if (!this.passwordStrategy.isValid(this.display.getNewPassword1())) {
            showValidationError(REQUIREMENTS_NOT_MET);
            return false;
        }
        if (this.display.getOldPassword().equals(this.display.getNewPassword1())) {
            if (showErrorEvenIfNewPasswordIsEmpty || StringUtil.hasText(this.display.getNewPassword1())) {
                showValidationError(SAME_AS_OLD);
                return false;
            }
        }
        if (!this.passwordStrategy.equals(this.display.getNewPassword1(), this.display.getNewPassword2())) {
            if (showErrorEvenIfNewPasswordIsEmpty || StringUtil.hasText(this.display.getNewPassword2())) {
                showValidationError(REPETITION_DOES_NOT_MATCH);
                return false;
            }
        }
        this.display.clearValidationError();
        return true;
    }

    @Override
    public void onCancel() {
        this.display.reset();
        this.display.clearError();
        tryHide();
        tryOnComplete();
    }

    private void tryHide() {
        if (this.display instanceof HasShowHide) {
            ((HasShowHide) this.display).hide();
        }
    }

    @Override
    public void onOk() {
        changePassword();
    }

    @Override
    public void onValidate() {
        if (StringUtil.hasText(this.display.getNewPassword1()) || StringUtil.hasText(this.display.getNewPassword2())) {
            this.display.setResetNewPasswordsEnabled(true);
        }
        this.display.setSubmitEnabled(validate(false));
    }

    @Override
    public void onResetNewPasswords() {
        this.display.resetNewPasswords();
        this.display.setResetNewPasswordsEnabled(false);
        onValidate();
    }
}
