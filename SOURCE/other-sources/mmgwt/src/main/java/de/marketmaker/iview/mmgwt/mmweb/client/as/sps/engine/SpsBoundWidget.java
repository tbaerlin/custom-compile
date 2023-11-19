/*
* SpsBoundWidget.java
*
* Created on 21.01.14
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.widgets.MessagePopup;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ValidationMessagePopup;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ErrorSeverity;

/**
 * @author umaurer
 */
public abstract class SpsBoundWidget<W extends Widget, P extends SpsProperty> extends
        SpsWidget<W> implements HasBindFeature, RequiresRelease {
    private static final String WEB_GUI_WIDGET_VALIDATION_ERROR_CODE = "WEB_GUI_WIDGET_VALIDATION"; // $NON-NLS$

    private BindFeature<P> bindFeature;

    private ErrorMM widgetValidationError;

    private boolean released = false;

    @Override
    protected void onWidgetConfigured() {
        super.onWidgetConfigured();
        initializeErrorInfoHandling();
    }

    public void release() {
        assert !released : "already released!";

        this.released = true;
        this.bindFeature.release();
    }

    protected SpsBoundWidget() {
        this.bindFeature = new BindFeature<>(this);
    }

    @Override
    public BindFeature<P> getBindFeature() {
        return this.bindFeature;
    }

    private void initializeErrorInfoHandling() {
        addFocusHandlerChecked(event -> {
            /* This listener is not only called when the user clicks into the text box. It is also called
             * if we want to display a popup for the first validation error message after the activity has been
             * loaded. Unfortunately, although focusFirst calls setFocus deferred, we have to give chrome some time
             * to process/finish rendering so that the popup box is definitely viewed nearby the position of the
             * the now visible widget. Without deferred, the popup will be positioned somewhere below the view
             * box of the content, where the widget that is scrolled per JavaScript into view, would had been
             * positioned naturally, which causes Chrome to render a second scrollbar for the window (although the
             * content has already a scrollbar). However, calling maybeShowErrorPopup deferred is not necessary
             * for IE9, IE10, IE11 and FF ESR 31, FF ESR 38. See AS-1263 for details.
             */
            Scheduler.get().scheduleDeferred(() -> maybeShowErrorPopup(false));
        });
        addBlurHandlerChecked(event -> hideErrorPopup());
        if (this.bindFeature.getSpsProperty() != null) {
            this.bindFeature.getSpsProperty().addChangeHandler(event -> {
                if (bindFeature.getSpsProperty().hasChanged()) {
                    hideErrorPopup();
                }
            });
        }
    }

    public boolean isReleased() {
        return released;
    }

    protected MessagePopup createMessagePopupImpl() {
        return new MessagePopup() {
            @Override
            public void show(Widget atWidget, String... messages) {
                visualizeWidgetValidationError(errorMM(ErrorSeverity.ESV_WARNING, messages));
            }

            @Override
            public void hide() {
                visualizeWidgetValidationError(null);
            }

            public ErrorMM errorMM(ErrorSeverity errorSeverity, String... messages) {
                final ErrorMM errorMM = new ErrorMM();
                errorMM.setErrorCode(WEB_GUI_WIDGET_VALIDATION_ERROR_CODE);
                errorMM.setErrorSeverity(errorSeverity);
                errorMM.setErrorString(concat(messages));
                return errorMM;
            }

            private String concat(String[] messages) {
                String msg = null;
                for (int i = 0; i < messages.length; i++) {
                    if (msg == null) {
                        msg = messages[i];
                    }
                    else {
                        if (i != 0) {
                            msg += " ";
                        }
                        msg += messages[i];
                    }
                }
                return msg;
            }
        };
    }

    public void visualizeWidgetValidationError(ErrorMM error) {
        //do not override an error symbol set by the activity framework
        this.widgetValidationError = error;

        if (this.widgetValidationError != null) {
            updateInfoIconPanel(this.widgetValidationError);
        }
        else {
            updateInfoIconPanel();
        }
        if (this.widgetValidationError != null) {
            showErrorPopupNearby(error, false);
        }
        else {
            hideErrorPopup();
        }
    }

    private void hideErrorPopup() {
        ValidationMessagePopup.I.hide(false);
    }

    protected void maybeShowErrorPopup(boolean fromHover) {
        if (this.bindFeature.getSpsProperty() == null) {
            return;
        }
        if (this.bindFeature.getSpsProperty().hasChanged() && !fromHover) {
            // only display error popup if data was not changed
            return;
        }
        if (getError() == null) {
            return;
        }
        showErrorPopupNearby(getError(), fromHover);
    }

    private void showErrorPopupNearby(ErrorMM error, boolean fromHover) {
        assert error != null;
        final ErrorSeverity errorSeverity = error.getErrorSeverity();
        ValidationMessagePopup.I.showErrorNearby(
                errorSeverity != null ? errorSeverity.value() : null,
                TextUtil.toSafeHtml(error.getErrorString()),
                getInfoIconPanel(), fromHover);
    }

    public ErrorMM getWidgetValidationError() {
        return widgetValidationError;
    }

    @Override
    protected void setWidget(final W widget, boolean force) {
        super.setWidget(widget, force);
        if (this instanceof HasEditWidget) {
            ((HasEditWidget) this).addKeyUpHandler(event -> {
                P spsProperty = bindFeature.getSpsProperty();
                if (spsProperty != null && spsProperty instanceof SpsLeafProperty) {
                    final String propertyValue = ((SpsLeafProperty) spsProperty).getStringValue();
                    final String widgetValue = ((HasEditWidget) SpsBoundWidget.this).getStringValue();
                    //do not hide widget validation errors which are sourced by the widget validation message popup impl
                    if (getError() != null
                            && getWidgetValidationError() == null
                            && !CompareUtil.equals(propertyValue, widgetValue)) {
                        hideErrorPopup();
                    }
                }
            });
        }
    }
}