/*
 * InfoIcon.java
 *
 * Created on 16.12.2015 17:06
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.MessagePopup;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;

/**
 * Not intended to be used in legacy views!
 * TODO: replace SPS info icon with this version if possible.
 * @author mdick
 */
public class InfoIcon extends Composite {

    private static final String MM_INFO_ICON_ERROR = "mm-infoIcon-error";  // $NON-NLS$

    private static final String MM_INFO_ICON_TYPING_ERROR = "mm-infoIcon-typingError"; // $NON-NLS$

    private static final HasEnabled EVER_ENABLED = new HasEnabled() {
        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void setEnabled(boolean enabled) {
            // do nothing
        }
    };

    final private SimplePanel panel = new SimplePanel();

    private ValidationMessagePopup.MessageType messageType;

    private String message;

    private Widget widget;

    private HasEnabled hasEnabled;

    private boolean mandatory;

    private MessagePopup messagePopup;

    public InfoIcon() {
        Styles.trySetStyle(this.panel, Styles.get().infoIconPanel());
        initWidget(this.panel);
    }

    public void showError(ValidationMessagePopup.MessageType messageType, String message) {
        if (this.hasEnabled.isEnabled()) {
            this.messageType = messageType;
            this.message = message;
            update();
            if (WidgetUtil.hasFocus(this.widget)) {
                maybeShowErrorPopup(false);
            }
        }
        else {
            this.message = null;
        }
    }

    public void clearError() {
        this.message = null;
        hideErrorPopup(false);
        update();
    }

    protected void update() {
        final boolean enabled = this.hasEnabled != null && this.hasEnabled.isEnabled();

        if (this.message != null && enabled) {
            final Image iconError = IconImage.get(getIconClass()).createImage();
            iconError.addMouseOverHandler(event -> maybeShowErrorPopup(true));
            iconError.addMouseOutHandler(event -> ValidationMessagePopup.I.hide(true));
            setInfoIcon(iconError);
        }
        else if (this.mandatory && /*!this.omitMandatoryIcon &&*/ enabled) {
            final Image iconMandatory = IconImage.get("sps-field-mandatory").createImage(); // $NON-NLS$ // todo: necessary?
            iconMandatory.setStyleName("sps-iconMandatory");  // $NON-NLS$  //todo: necessary?
            Tooltip.addQtip(iconMandatory, I18n.I.mandatoryField());
            setInfoIcon(iconMandatory);
        }
        else {
            setInfoIcon(null);
        }
    }

    private String getIconClass() {
        if (this.messageType == null) {
            return MM_INFO_ICON_ERROR;
        }

        switch (this.messageType) {
            case VALIDATION_WHEN_TYPING_FAILED:
                return MM_INFO_ICON_TYPING_ERROR;
            case VALIDATION_FAILED:
            default:
                return MM_INFO_ICON_ERROR;
        }
    }

    private void setInfoIcon(Image image) {
        this.panel.setWidget(image);
        this.panel.setVisible(image != null);
    }

    protected void maybeShowErrorPopup(boolean fromHover) {
        ValidationMessagePopup.I.showErrorNearby(this.messageType, this.message, this.panel, fromHover);
    }

    protected void hideErrorPopup(boolean fromHover) {
        ValidationMessagePopup.I.hide(fromHover);
    }

    public void setEditWidget(Widget widget) {
        this.widget = widget;
        if (this.widget instanceof HasEnabled) {
            this.hasEnabled = (HasEnabled) widget;
        }
        else {
            this.hasEnabled = EVER_ENABLED;
        }
        if (this.widget instanceof HasMandatory) {
            setMandatory(((HasMandatory) this.widget).isMandatory(), false);
        }
        update();
    }

    public void setMandatory(boolean mandatory) {
        setMandatory(mandatory, true);
    }

    private void setMandatory(boolean mandatory, boolean doUpdate) {
        boolean oldValue = this.mandatory;
        this.mandatory = mandatory;
        if (oldValue != mandatory && doUpdate) {
            update();
        }
    }

    public MessagePopup getMessagePopup() {
        if (this.messagePopup == null) {
            this.messagePopup = new MessagePopup() {
                @Override
                public void show(Widget atWidget, String... messages) {
                    showError(ValidationMessagePopup.MessageType.VALIDATION_WHEN_TYPING_FAILED, messages[0]);
                }

                @Override
                public void hide() {
                    clearError();
                }
            };
        }
        return this.messagePopup;
    }
}
