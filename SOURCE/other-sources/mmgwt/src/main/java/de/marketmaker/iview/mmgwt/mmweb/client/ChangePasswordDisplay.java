/*
 * ChangePasswordDisplay.java
 *
 * Created on 02.09.13 14:02
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.Date;

/**
 * @author Markus Dick
 */
public interface ChangePasswordDisplay {
    /**
     * This method is called by the presenter with its reference.
     */
    void setPresenter(Presenter presenter);

    void setCancelEnabled(boolean enabled);

    void setSubmitEnabled(boolean enabled);

    void setResetNewPasswordsEnabled(boolean enabled);

    void setPasswordRulesDescription(SafeHtml passwordRulesDescription);

    void setPasswordRulesDescriptionVisible(boolean visible);

    String getOldPassword();

    String getNewPassword1();

    String getNewPassword2();

    void setOldPasswordAllowBlank(boolean allowBlank);

    void setNewPasswordsAllowBlank(boolean allowBlank);

    void setError(String message);

    void clearError();

    void setValidationError(ValidationCause cause, String message);

    void clearValidationError();

    void showSuccessMessage(String message);

    void reset();

    void resetNewPasswords();

    void selectOldAndKeepAll();

    interface Presenter {
        void onCancel();

        void onOk();

        void onValidate();

        void onResetNewPasswords();

        interface PasswordStrategy {
            String getValidationMessage(ValidationCause cause);

            String getErrorMessage(ChangePasswordResponse changePasswordResponse);

            void amendDisplayedError(ChangePasswordResponse response,
                    ChangePasswordDisplay display);

            boolean isShowViewAvailable();

            boolean isBlankPasswordAllowed();

            boolean isValid(String password);

            SafeHtml getPasswordRulesDescription();

            boolean equals(String newPassword1, String newPassword2);
        }
    }

    enum ValidationCause {REQUIREMENTS_NOT_MET, REPETITION_DOES_NOT_MATCH, SAME_AS_OLD}

    final class MmfWebPasswordStrategy implements Presenter.PasswordStrategy {
        @Override
        public String getValidationMessage(ValidationCause cause) {
            switch (cause) {
                case REPETITION_DOES_NOT_MATCH:
                    return I18n.I.changePasswordValidationRepetitionDoesNotMatchMmfweb();
                case SAME_AS_OLD:
                    return I18n.I.changePasswordValidationSameAsOldMmfweb();
                case REQUIREMENTS_NOT_MET:
                default:
                    return I18n.I.changePasswordValidationRequirementsNotMetMmfweb();
            }
        }

        @Override
        public String getErrorMessage(ChangePasswordResponse changePasswordResponse) {
            switch (changePasswordResponse) {
                case REPEATED_RECENT_PASSWORD:
                    return I18n.I.changePasswordResponseRepeatedRecentPasswordMmfweb();
                case WRONG_OLD_PASSWORD:
                    return I18n.I.changePasswordResponseWrongOldPasswordMmfweb();
                case INITIAL_PASSWORD:
                    return I18n.I.changePasswordResponseInitialPasswordMmfweb();
                case PASSWORD_IS_TOO_SHORT:
                    return I18n.I.changePasswordResponsePasswordIsTooShortMmfweb();
                case UNKNOWN_USER_OR_PASSWORD:
                    return I18n.I.changePasswordResponseUnknownUserOrPasswordMmfweb();
                default:
                    return I18n.I.internalError();
            }
        }

        @Override
        public boolean isShowViewAvailable() {
            final Date changeDate = SessionData.INSTANCE.getUser().getPasswordChangeDate();
            if (changeDate != null) {
                if (changeDate.after(DateTimeUtil.nowMinus("1d"))) { // $NON-NLS$
                    Dialog.error(I18n.I.hint(), I18n.I.passwordChangeIntervalMin1Day());
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean isValid(String password) {
            if (password == null) return false;

            if (password.length() < 7) {
                return false;
            }
            boolean withDigit = false;
            boolean withUpperChar = false;
            for (int i = 0; i < password.length(); i++) {
                final char c = password.charAt(i);
                withDigit |= (c >= '0' && c <= '9');
                withUpperChar |= ((c >= 'A' && c <= 'Z') || c == 'Ä' || c == 'Ö' || c == 'Ü');
            }

            return withDigit && withUpperChar;
        }

        @Override
        public SafeHtml getPasswordRulesDescription() {
            return SafeHtmlUtils.fromSafeConstant(I18n.I.passwordRulesDescription());
        }

        @Override
        public boolean isBlankPasswordAllowed() {
            return false;
        }

        @Override
        public boolean equals(String newPassword1, String newPassword2) {
            return newPassword1.equals(newPassword2);
        }

        @Override
        public void amendDisplayedError(ChangePasswordResponse response,
                ChangePasswordDisplay display) {
            display.reset();
        }
    }

    final class PmWebPasswordStrategy implements Presenter.PasswordStrategy {
        @Override
        public String getValidationMessage(ValidationCause cause) {
            switch (cause) {
                case REPETITION_DOES_NOT_MATCH:
                    return I18n.I.changePasswordValidationRepetitionDoesNotMatchPm();
                case SAME_AS_OLD:
                    return I18n.I.changePasswordValidationSameAsOldPm();
                case REQUIREMENTS_NOT_MET:
                default:
                    //Should never occur
                    throw new IllegalArgumentException("ValidationCause " + cause.name() + " not supported by PmWebPasswordStrategy");  // $NON-NLS$
            }
        }

        @Override
        public String getErrorMessage(ChangePasswordResponse changePasswordResponse) {
            switch (changePasswordResponse) {
                case REPEATED_RECENT_PASSWORD:
                    return I18n.I.changePasswordResponseRepeatedRecentPasswordPm();
                case WRONG_OLD_PASSWORD:
                    return I18n.I.changePasswordResponseWrongOldPasswordPm();
                case INITIAL_PASSWORD:
                    return I18n.I.changePasswordResponseInitialPasswordPm();
                case PASSWORD_IS_TOO_SHORT:
                    return I18n.I.changePasswordResponsePasswordIsTooShortPm();
                case UNKNOWN_USER_OR_PASSWORD:
                    return I18n.I.changePasswordResponseUnknownUserOrPasswordPm();
                default:
                    return I18n.I.internalError();
            }
        }

        @Override
        public boolean isShowViewAvailable() {
            return true;
        }

        @Override
        public boolean isValid(String password) {
            return true;
        }

        @Override
        public SafeHtml getPasswordRulesDescription() {
            return null;
        }

        @Override
        public boolean isBlankPasswordAllowed() {
            return true;
        }

        @Override
        public boolean equals(String newPassword1, String newPassword2) {
            return !StringUtil.hasText(newPassword1) && !StringUtil.hasText(newPassword2) || //allow
                    newPassword1.equals(newPassword2);
        }

        @Override
        public void amendDisplayedError(ChangePasswordResponse response,
                ChangePasswordDisplay display) {
            if (response == ChangePasswordResponse.UNKNOWN_USER_OR_PASSWORD
                    || response == ChangePasswordResponse.WRONG_OLD_PASSWORD) {
                display.selectOldAndKeepAll();
            }
            else {
                display.reset();
            }
        }
    }
}
