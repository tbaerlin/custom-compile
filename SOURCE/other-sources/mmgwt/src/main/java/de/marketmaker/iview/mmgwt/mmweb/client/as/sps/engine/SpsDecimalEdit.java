/*
* SpsDecimalEdit.java
*
* Created on 22.01.14
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.math.BigDecimal;
import java.util.ArrayList;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

import de.marketmaker.itools.gwtutil.client.widgets.MessagePopup;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.DecimalBox;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NumberUtil;

/**
 * @author umaurer
 */
public class SpsDecimalEdit extends SpsBoundWidget<DecimalBox, SpsLeafProperty> implements HasCaption, HasEditWidget,
        RequiresPropertyUpdateBeforeSave {
    private boolean propertyUpdateOnKeyUp = false;

    private boolean demanded = false;
    private boolean percent;
    private String min;
    private String max;
    private String vUnit;
    private String spin;
    private boolean renderTrailingZeros;

    //special feature for hand crafted SPS views
    //for PM provided SPS views not necessary
    public SpsDecimalEdit withPropertyUpdateOnKeyUp() {
        this.propertyUpdateOnKeyUp = true;
        return this;
    }

    public SpsDecimalEdit withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    public SpsDecimalEdit withPercent(boolean percent) {
        this.percent = percent;
        return this;
    }

    public SpsDecimalEdit withSpin(String spin) {
        this.spin = spin;
        return this;
    }

    public SpsDecimalEdit withVUnit(String vUnit) {
        this.vUnit = vUnit;
        return this;
    }

    public SpsDecimalEdit withMin(String min) {
        this.min = min;
        return this;
    }

    public SpsDecimalEdit withMax(String max) {
        this.max = max;
        return this;
    }

    public SpsDecimalEdit withDemanded(boolean demanded) {
        this.demanded = demanded;
        return this;
    }

    public SpsDecimalEdit withRenderTrailingZeros(boolean renderTrailingZeros) {
        this.renderTrailingZeros = renderTrailingZeros;
        return this;
    }

    @Override
    public void setReadonly(boolean readonly) {
        super.setReadonly(readonly);
        if (getWidget() != null) {
            getWidget().setEnabled(readonly);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(getWidget() != null) {
            getWidget().setEnabled(enabled);
        }
    }

    @Override
    public void onPropertyChange() {
        if(this.propertyUpdateOnKeyUp) {
            // do not update if the big decimal value is the same as the current value,
            // e.g. if the property value is 10 and the typed value is 10.0 we do not update
            // the value box, because 10.0 is shortened to 10 and so it is not possible to
            // enter e.g. 10.01
            final String propertyString = getBindFeature().getSpsProperty().getStringValue();

            final BigDecimal propertyValue = (propertyString == null ? null : new BigDecimal(propertyString));
            final BigDecimal widgetValue = getWidget().getValue();

            //BigDecimal's equals takes the scale into account. Hence, we use our own method.
            if(widgetValue == null || !compareEqual(propertyValue, widgetValue)) {
                setValue(getBindFeature().getSpsProperty().getStringValue(), false);
            }
        }
        else {
            setValue(getBindFeature().getSpsProperty().getStringValue(), false);
        }
    }

    private void setValue(String value, boolean fireEvent) {
        getWidget().setValue(value == null ? null : new BigDecimal(value), fireEvent);
    }

    @Override
    public String getStringValue() {
        return NumberUtil.toPlainStringValue(getWidget().getValue());
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return getWidget().addKeyUpHandler(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        getWidget().fireEvent(event);
    }

    private void updateProperty(String value) {
        getBindFeature().getSpsProperty().setValue(value);
    }

    @Override
    public void updatePropertyBeforeSave() {
        if(getWidget() != null) {
            final String value = getBindFeature().getSpsProperty().getStringValue();

            final BigDecimal original = value == null ? null : new BigDecimal(value);
            final BigDecimal current = getWidget().getValue();

            //BigDecimal's equals takes the scale into account. Hence, we use our own method.
            if(!compareEqual(original, current)) {
                updateProperty(getStringValue());
            }
        }
    }

    @Override
    protected DecimalBox createWidget() {
        final MessagePopup messagePopupImpl = createMessagePopupImpl();
        final DecimalBox valueBox = new DecimalBox(this.renderTrailingZeros, messagePopupImpl)
                .withPercent(this.percent)
                .withMandatory(this.demanded)
                .withMin(this.min)
                .withMax(this.max)
                .withVUnit(this.vUnit)
                .withNumberSpin(this.spin);

        valueBox.setEnabled(!isReadonly());

        final String exPrimaryStyleName = valueBox.getStyleName(); //preserve old style names as additional style to ease styling in SPS views
        valueBox.setStyleName(getBaseStyle());
        valueBox.addStyleName(exPrimaryStyleName);

        valueBox.addValueChangeHandler(event -> {
            updateProperty(NumberUtil.toPlainStringValue(event.getValue()));
            // Necessary because ValidationFeature#validateDeferred onChange calls showErrorMessages
            // but ValidationFeature.blurHandler calls hideErrorMessages not deferred and thus, if the
            // 1234 is formatted to 1.234, the popup is not hidden, as it usually is. The problem is
            // located in ValidationFeature#validate which operates on text and thus 1234 is not equal
            // to 1.234: if (this.lastValidatedValue != null && this.lastValidatedValue.equals(value))
            // Without this line, the popup will be shown as long as the formatted text represents an
            // invalid value.
            // It especially applies in cases if a min max validator is applied.
            Scheduler.get().scheduleDeferred(messagePopupImpl::hide);
        });
        if(this.propertyUpdateOnKeyUp) {
            valueBox.addKeyUpHandler(keyUpEvent -> updatePropertyOnKeyUp());
        }
        return valueBox;
    }

    private void updatePropertyOnKeyUp() {
        final DecimalBox me = getWidget();
        if(me.isValid(new ArrayList<>())) {
            updateProperty(NumberUtil.toPlainStringValue(me.getValue()));
        }
    }

    private static <T extends Comparable<T>> boolean compareEqual(T c1, T c2) {
        return c1 == null ? c2 == null : c1.compareTo(c2) == 0;
    }

}
