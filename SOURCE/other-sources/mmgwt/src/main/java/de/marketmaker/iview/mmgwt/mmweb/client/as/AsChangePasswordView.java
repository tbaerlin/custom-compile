/*
 * AsChangePasswordView.java
 *
 * Created on 21.01.2015 11:22:42
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.ChangePasswordDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SimpleStandaloneEngine;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsColumnSection;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsPasswordEdit;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsSection;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.TiType;

/**
 * @author Markus Dick
 */
public abstract class AsChangePasswordView implements ChangePasswordDisplay, IsWidget {
    private Presenter presenter;

    private final FlowPanel widget;

    private final SpsLeafProperty oldPasswordProperty;

    private final SpsLeafProperty newPassword1Property;

    private final SpsLeafProperty newPassword2Property;

    protected final SpsColumnSection formSection;

    private final SpsPasswordEdit newPassword1;

    private final SpsPasswordEdit newPassword2;

    private final SpsPasswordEdit oldPassword;

    private final SpsButton resetNewPasswordsButton;

    private final SpsHtml passwordRulesDescription;

    protected boolean changeHandlersEnabled = false;

    private final SpsColumnSection passwordRulesSection;

    public AsChangePasswordView(boolean isForDialogIfc) {
        this.oldPassword = createPasswordField(I18n.I.oldPassword());
        this.newPassword1 = createPasswordField(I18n.I.newPassword());
        this.newPassword2 = createPasswordField(I18n.I.repeatNewPassword());
        this.oldPasswordProperty = oldPassword.getBindFeature().getSpsProperty();
        this.newPassword1Property = this.newPassword1.getBindFeature().getSpsProperty();
        this.newPassword2Property = this.newPassword2.getBindFeature().getSpsProperty();

        this.resetNewPasswordsButton = new SpsButton().withClickHandler(clickEvent -> this.presenter.onResetNewPasswords());
        this.resetNewPasswordsButton.setCaption(I18n.I.reset());
        SimpleStandaloneEngine.configureSpsWidget(this.resetNewPasswordsButton);
        this.resetNewPasswordsButton.getWidget().getElement().getStyle().setMarginLeft(10, Style.Unit.PX);

        final SpsSection trailingSection = SimpleStandaloneEngine.createTrailingSection();
        trailingSection.getChildrenFeature().addChild(this.newPassword2);
        trailingSection.getChildrenFeature().addChild(this.resetNewPasswordsButton);
        SimpleStandaloneEngine.configureSpsWidget(trailingSection);

        this.formSection = SimpleStandaloneEngine.createColumnSection(0, null);
        if(isForDialogIfc) {
            this.formSection.setStyle(null);
        }
        this.formSection.withErrorPanelPosition(SpsSection.ErrorPanelPosition.BOTTOM);
        this.formSection.getChildrenFeature().addChild(this.oldPassword);
        this.formSection.getChildrenFeature().addChild(this.newPassword1);
        this.formSection.getChildrenFeature().addChild(trailingSection);
        SimpleStandaloneEngine.configureSpsWidget(this.formSection);

        this.passwordRulesDescription = new SpsHtml();
        SimpleStandaloneEngine.configureSpsWidget(this.passwordRulesDescription);

        this.passwordRulesSection = SimpleStandaloneEngine.createColumnSection(0, null);
        if(isForDialogIfc) {
            this.passwordRulesSection.setStyle(null);
        }
        this.passwordRulesSection.getChildrenFeature().addChild(this.passwordRulesDescription);
        SimpleStandaloneEngine.configureSpsWidget(this.passwordRulesSection);

        final SpsColumnSection container = SimpleStandaloneEngine.createColumnSection(0, null);
        container.setStyle("sps-form-1");  // $NON-NLS$
        container.getChildrenFeature().addChild(formSection);
        container.getChildrenFeature().addChild(this.passwordRulesSection);
        SimpleStandaloneEngine.configureSpsWidget(container);

        final ChangeHandler validationHandler = event -> {
            if (this.changeHandlersEnabled) {
                this.presenter.onValidate();
            }
        };
        this.oldPasswordProperty.addChangeHandler(validationHandler);
        this.newPassword1Property.addChangeHandler(validationHandler);
        this.newPassword2Property.addChangeHandler(validationHandler);

        this.oldPasswordProperty.addChangeHandler(event -> {
            if (this.changeHandlersEnabled) {
                clearError();
            }
        });

        this.widget = new FlowPanel();
        this.widget.add(container.getWidget());
    }

    protected Presenter getPresenter() {
        return this.presenter;
    }

    @Override
    public Widget asWidget() {
        return this.widget;
    }

    private SpsPasswordEdit createPasswordField(String label) {
        final ParsedTypeInfo pti = new ParsedTypeInfo();
        pti.setTypeId(TiType.TI_STRING);
        pti.setDemanded(true);

        final SpsPasswordEdit spsPassword = new SpsPasswordEdit().withCaption(label).withPropertyUpdateOnKeyUp();
        spsPassword.setBaseStyle("sps-edit");  // $NON-NLS$

        final SpsBoundWidget<PasswordTextBox, SpsLeafProperty> spsWidget = SimpleStandaloneEngine.configureSpsWidget(spsPassword, pti);
        spsWidget.getBindFeature().getSpsProperty().setNullValue();

        return spsPassword;
    }

    @Override
    public String getOldPassword() {
        return this.oldPasswordProperty.getStringValue();
    }

    @Override
    public String getNewPassword1() {
        return this.newPassword1Property.getStringValue();
    }

    @Override
    public String getNewPassword2() {
        return this.newPassword2Property.getStringValue();
    }

    @Override
    public void setResetNewPasswordsEnabled(boolean enabled) {
        this.resetNewPasswordsButton.setEnabled(enabled);
    }

    @Override
    public void reset() {
        this.changeHandlersEnabled = false;
        this.oldPasswordProperty.setNullValue();
        this.newPassword1Property.setNullValue();
        this.newPassword2Property.setNullValue();
        this.newPassword1.visualizeError(null, true);
        this.newPassword2.visualizeError(null, true);
        this.changeHandlersEnabled = true;
        Scheduler.get().scheduleDeferred(this.formSection::focusFirst);
    }

    @Override
    public void resetNewPasswords() {
        this.changeHandlersEnabled = false;
        this.newPassword1Property.setNullValue();
        this.newPassword2Property.setNullValue();
        this.newPassword1.visualizeError(null, true);
        this.newPassword2.visualizeError(null, true);
        this.changeHandlersEnabled = true;

        Scheduler.get().scheduleDeferred(this.newPassword1::focusFirst);
    }

    @Override
    public void selectOldAndKeepAll() {
        this.oldPassword.focusFirst();
        this.oldPassword.selectAll();
    }

    @Override
    public void setError(String message) {
        this.formSection.visualizeError(SimpleStandaloneEngine.toErrorMM(message), true);
    }

    @Override
    public void clearError() {
        this.formSection.visualizeError(null, true);
    }

    @Override
    public void setValidationError(ValidationCause cause, String message) {
        this.newPassword1.visualizeError(null, true);
        this.newPassword2.visualizeError(null, true);

        switch (cause) {
            case REQUIREMENTS_NOT_MET:
            case SAME_AS_OLD:
                this.newPassword1.visualizeError(SimpleStandaloneEngine.toErrorMM(message), true);
                break;
            case REPETITION_DOES_NOT_MATCH:
                this.newPassword2.visualizeError(SimpleStandaloneEngine.toErrorMM(message), true);
                break;
        }
    }

    @Override
    public void clearValidationError() {
        this.newPassword1.visualizeError(null, true);
        this.newPassword2.visualizeError(null, true);
    }

    @Override
    public void setPasswordRulesDescription(SafeHtml passwordRulesDescription) {
        if (passwordRulesDescription == null) {
            this.passwordRulesDescription.setHTML((String) null);
            return;
        }
        this.passwordRulesDescription.setHTML(passwordRulesDescription);
    }

    @Override
    public void setPasswordRulesDescriptionVisible(boolean visible) {
        this.passwordRulesSection.setVisible(visible);
    }

    @Override
    public void setOldPasswordAllowBlank(boolean allowBlank) {
        if (!allowBlank) {
            Firebug.warn("setOldPasswordAllowBlank: not implemented allowBlank?false");
        }
    }

    @Override
    public void setNewPasswordsAllowBlank(boolean allowBlank) {
        if (!allowBlank) {
            Firebug.warn("setNewPasswordsAllowBlank: not implemented allowBlank?false");
        }
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public static class SpsButton extends SpsWidget<Button> {
        private ClickHandler clickHandler;

        @Override
        protected Button createWidget() {
            return Button.build();
        }

        @Override
        protected void onWidgetConfigured() {
            super.onWidgetConfigured();

            final Button w = getWidget();
            w.setText(getCaption().asString());
            if (this.clickHandler != null) {
                w.addClickHandler(this.clickHandler);
            }
        }

        SpsButton withClickHandler(ClickHandler clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }

        @Override
        protected HTML createCaptionWidget() {
            return isForceCaptionWidget() ? new HTML() : null;
        }

        @Override
        public void setEnabled(boolean enabled) {
            getWidget().setEnabled(enabled);
            super.setEnabled(enabled);
        }
    }

    public static class SpsHtml extends SpsWidget<HTML> {
        public SafeHtml html;

        @Override
        protected HTML createWidget() {
            return new HTML();
        }

        @Override
        protected void onWidgetConfigured() {
            super.onWidgetConfigured();
            if (this.html != null) {
                getWidget().setHTML(this.html);
            }
        }

        @Override
        protected HTML createCaptionWidget() {
            return isForceCaptionWidget() ? new HTML() : null;
        }

        public void setHTML(String html) {
            setHTML(html != null
                    ? SafeHtmlUtils.fromTrustedString(html)
                    : SafeHtmlUtils.EMPTY_SAFE_HTML);
        }

        public void setHTML(SafeHtml html) {
            this.html = html != null ? html : SafeHtmlUtils.EMPTY_SAFE_HTML;
            if (getWidget() != null) {
                getWidget().setHTML(this.html);
            }
        }
    }
}
