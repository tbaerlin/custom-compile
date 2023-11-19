/*
 * DecimalBox.java
 *
 * Created on 25.06.2014 12:44
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.HasMouseWheelHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Panel;

import de.marketmaker.itools.gwtutil.client.event.EventUtil;
import de.marketmaker.itools.gwtutil.client.event.KeyCombination;
import de.marketmaker.itools.gwtutil.client.util.HasFocusableElement;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.MessagePopup;
import de.marketmaker.itools.gwtutil.client.widgets.input.ValidatingBigDecimalBox;
import de.marketmaker.itools.gwtutil.client.widgets.input.ValidationFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * Note: the widget itself is never focused, instead its internal input element holds the focus. Hence, in order to
 * check if the widget is focused, {@link WidgetUtil#hasFocus(Element)} checks for {@link HasFocusableElement} and calls {@link #getFocusableElement()}.
 * @author umaurer
 * @author Markus Dick
 */
public class DecimalBox extends Composite implements HasEnabled, HasValue<BigDecimal>,
        HasKeyUpHandlers,
        HasKeyDownHandlers, Focusable, HasFocusHandlers, HasBlurHandlers, HasMouseWheelHandlers,
        HasChangeHandlers,
        HasFocusableElement, HasMandatory {
    private enum Spin {UP, DOWN}

    private static final BigDecimal BD_HUNDRED = new BigDecimal("100"); // $NON-NLS$

    private static final BigDecimal BD_PERCENT = BigDecimal.ONE.divide(BD_HUNDRED, RoundingMode.HALF_UP);

    private final ValidationFeature.ValidatorDelegate delegatingValidator;

    private final ValidatingBigDecimalBox internalBox;

    private final Renderer<BigDecimal> bigDecimalRenderer;

    private final FormatDescriptionProviderDelegate formatDescriptionProviderDelegate;

    private final InlineLabel percentSign;

    private BigDecimal min;

    private BigDecimal max;

    private boolean mandatory = false;

    private boolean percent = false;

    private int scale = -1;

    private BigDecimal spin = BigDecimal.ONE;

    private boolean spinOnKeyDownAndMouseWheel = true;

    public DecimalBox() {
        this(false);
    }

    public DecimalBox(boolean renderTrailingZeros) {
        this(renderTrailingZeros, new MessagePopup.DefaultMessagePopup());
    }

    public DecimalBox(boolean renderTrailingZeros, MessagePopup messagePopup) {
        this(new ValidationFeature.ValidatorDelegate(), new FormatDescriptionProviderDelegate(), renderTrailingZeros, messagePopup);
        setStylePrimaryName("mm-decimalBox"); // $NON-NLS$
    }

    private DecimalBox(ValidationFeature.ValidatorDelegate validatorDelegate,
            FormatDescriptionProviderDelegate formatDescriptionProviderDelegate,
            boolean renderTrailingZeros, MessagePopup messagePopup) {
        final Panel layout = new HorizontalPanel();
        initWidget(layout);

        this.internalBox = new InternalValidatingBigDecimalBox(validatorDelegate, formatDescriptionProviderDelegate, renderTrailingZeros, messagePopup);
        layout.add(this.internalBox);

        this.percentSign = new InlineLabel("%"); // $NON-NLS$
        this.percentSign.setStylePrimaryName("mm-percentSign"); // $NON-NLS$
        layout.add(this.percentSign);
        this.percentSign.setVisible(this.percent);

        this.bigDecimalRenderer = getBigDecimalRenderer(renderTrailingZeros);

        this.formatDescriptionProviderDelegate = formatDescriptionProviderDelegate;
        this.formatDescriptionProviderDelegate.setDelegate(new FormatDescriptionProviderImpl());

        this.delegatingValidator = validatorDelegate;
        this.delegatingValidator.setDelegate(new ValidatorImpl());

        this.internalBox.addKeyDownHandler(new KeyDownHandlerImpl());
        this.internalBox.addKeyPressHandler(new KeyPressHandlerImpl());
        this.internalBox.addMouseWheelHandler(new MouseWheelHandlerImpl());

        this.internalBox.addChangeHandler(event -> {
            // formats the input value again (especially necessary if the values are equal,
            // but the rendered presentation is not).
            final BigDecimal parsed = getValue();
            setValue(parsed, false);
        });
    }

    private static Renderer<BigDecimal> getBigDecimalRenderer(boolean renderTrailingZeros) {
        return renderTrailingZeros ? ValidatingBigDecimalBox.BigDecimalRenderer.instanceTrailingZeros() : ValidatingBigDecimalBox.BigDecimalRenderer.instance();
    }

    @Override
    public BigDecimal getValue() {
        return this.internalBox.getValue();
    }

    @Override
    public void setValue(BigDecimal value) {
        this.setValue(value, false);
    }

    @Override
    public void setValue(BigDecimal value, boolean fireEvents) {
        this.internalBox.setValue(value, fireEvents);
    }

    public boolean isValid(List<String> messages) {
        return this.delegatingValidator.isValid(this.internalBox.getText(), messages);
    }

    private void spin(Spin direction) {
        if (!this.spinOnKeyDownAndMouseWheel) {
            return;
        }

        assert direction != null : "parameter spin direction must not be null"; // $NON-NLS$

        if (!isEnabled()) {
            return;
        }
        final BigDecimal value = getValue();

        final BigDecimal bd;
        switch (direction) {
            case UP:
                bd = value == null ? this.spin : value.add(this.spin);
                break;
            case DOWN:
            default:
                bd = value == null ? this.spin.negate() : value.subtract(this.spin);
        }

        final String formatted = formatString(bd);
        if (this.delegatingValidator.isValid(formatted, new ArrayList<>())) {
            setValue(bd, true);
        }
    }

    private String formatString(BigDecimal bd) {
        return this.bigDecimalRenderer.render(format(bd));
    }

    private BigDecimal format(BigDecimal bd) {
        bd = applyScale(bd);
        if (this.percent) {
            bd = bd.movePointRight(2);
        }
        return bd;
    }

    private BigDecimal parse(String value) {
        try {
            return adjust(ValidatingBigDecimalBox.BigDecimalParser.instance().parse(value));
        } catch (ParseException e) {
            return null;
        }
    }

    private BigDecimal adjust(BigDecimal bd) {
        if (bd == null) {
            return null;
        }
        if (this.percent) {
            bd = bd.movePointLeft(2);
        }
        bd = applyScale(bd);
        return bd;
    }

    public BigDecimal applyScale(BigDecimal bd) {
        if (this.scale != -1) {
            bd = bd.setScale(this.scale, RoundingMode.HALF_UP);
        }
        return bd;
    }

    public boolean isPercent() {
        return percent;
    }

    public DecimalBox withPercent(boolean percent) {
        this.percent = percent;
        this.percentSign.setVisible(percent);

        if (percent) {
            this.spin = BD_PERCENT;
            this.addStyleName("mm-percent");
        }

        return this;
    }

    public BigDecimal getMin() {
        return min;
    }

    public void setMin(BigDecimal min) {
        this.min = min;
    }

    public DecimalBox withMin(String min) {
        this.min = min == null ? null : new BigDecimal(min);
        return this;
    }

    public DecimalBox withMin(int min) {
        this.min = new BigDecimal(min);
        return this;
    }

    @SuppressWarnings("unused")
    public DecimalBox withMin(double min) {
        this.min = new BigDecimal(min);
        return this;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }

    public DecimalBox withMax(String max) {
        this.max = max == null ? null : new BigDecimal(max);
        return this;
    }

    public DecimalBox withMax(int max) {
        this.max = new BigDecimal(max);
        return this;
    }

    @SuppressWarnings("unused")
    public DecimalBox withMax(double max) {
        this.max = new BigDecimal(max);
        return this;
    }

    public DecimalBox withoutSpinOnKeyDownAndMouseWheel() {
        this.spinOnKeyDownAndMouseWheel = false;
        return this;
    }

    @SuppressWarnings("unused")
    public BigDecimal getSpin() {
        return spin;
    }

    @SuppressWarnings("unused")
    public void setSpin(BigDecimal spin) {
        this.spin = spin;
    }

    public DecimalBox withNumberSpin(String numberSpin) {
        if (StringUtil.hasText(numberSpin) && !"0".equals(numberSpin)) { // $NON-NLS$
            this.spin = new BigDecimal(numberSpin);
        }
        else {
            this.spin = this.percent ? BD_PERCENT : BigDecimal.ONE;
        }
        return this;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public DecimalBox withVUnit(String vUnit) {
        this.scale = MmTalkHelper.getBigDecimalScale(vUnit);
        return this;
    }

    public DecimalBox withMandatory(boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public DecimalBox withMandatory() {
        this.mandatory = true;
        return this;
    }

    public DecimalBox withValueChangeHandler(ValueChangeHandler<BigDecimal> handler) {
        addValueChangeHandler(handler);
        return this;
    }

    public DecimalBox withAdditionalStyleName(String styleName) {
        addStyleName(styleName);
        return this;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<BigDecimal> handler) {
        return this.internalBox.addValueChangeHandler(handler);
    }

    @Override
    public boolean isEnabled() {
        return this.internalBox.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.internalBox.setEnabled(enabled);
    }

    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return this.internalBox.addKeyUpHandler(handler);
    }

    @Override
    public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
        return this.internalBox.addKeyDownHandler(handler);
    }

    @Override
    public int getTabIndex() {
        return this.internalBox.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        this.internalBox.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        this.internalBox.setFocus(focused);
    }

    public void selectAll() {
        this.internalBox.selectAll();
    }

    @Override
    public void setTabIndex(int index) {
        this.internalBox.setTabIndex(index);
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.internalBox.addBlurHandler(handler);
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.internalBox.addFocusHandler(handler);
    }

    @Override
    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
        return this.internalBox.addMouseWheelHandler(handler);
    }

    @Override
    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return this.internalBox.addChangeHandler(handler);
    }

    @Override
    public Element getFocusableElement() {
        return this.internalBox.getElement();
    }

    @Override
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    @Override
    public boolean isMandatory() {
        return this.mandatory;
    }

    /**
     * This small override is simply necessary to allow moving the point right and left in the case of percent values.
     */
    private class InternalValidatingBigDecimalBox extends ValidatingBigDecimalBox {
        public InternalValidatingBigDecimalBox(
                ValidationFeature.ValidatorDelegate validatorDelegate,
                FormatDescriptionProviderDelegate formatDescriptionProviderDelegate,
                boolean renderTrailingZeros, MessagePopup messagePopup) {
            super(validatorDelegate, formatDescriptionProviderDelegate, BigDecimalParser.instance(), DecimalBox.getBigDecimalRenderer(renderTrailingZeros), messagePopup);
        }

        @Override
        public BigDecimal getValue() {
            return adjust(super.getValue());
        }

        @Override
        public void setValue(BigDecimal value) {
            this.setValue(value, false);
        }

        @Override
        public void setValue(BigDecimal value, boolean fireEvents) {
            if (value == null) {
                super.setValue(null, fireEvents);
                return;
            }
            super.setValue(format(value), fireEvents);
        }
    }

    private static class FormatDescriptionProviderDelegate implements
            ValidationFeature.FormatDescriptionProvider {
        private ValidationFeature.FormatDescriptionProvider delegate;

        @Override
        public String getFormatDescription() {
            if (this.delegate == null) {
                return "";
            }
            return this.delegate.getFormatDescription();
        }

        public void setDelegate(
                ValidationFeature.FormatDescriptionProvider formatDescriptionProvider) {
            this.delegate = formatDescriptionProvider;
        }
    }

    private class ValidatorImpl implements ValidationFeature.Validator {
        @Override
        public boolean isValid(String value, List<String> messages) {
            final BigDecimal bd = parse(value);
            if (bd == null) {
                return !mandatory;
            }
            if (min != null) {
                if (max != null) {
                    if (min.equals(max)) {
                        return true;
                    }
                    if (bd.compareTo(max) > 0) {
                        messages.add(formatDescriptionProviderDelegate.getFormatDescription());
                        return false;
                    }
                }
                if (bd.compareTo(min) < 0) {
                    messages.add(formatDescriptionProviderDelegate.getFormatDescription());
                    return false;
                }
            }
            else if (max != null) {
                if (bd.compareTo(max) > 0) {
                    messages.add(formatDescriptionProviderDelegate.getFormatDescription());
                    return false;
                }
            }
            return true;
        }
    }

    private class KeyDownHandlerImpl implements KeyDownHandler {
        @Override
        public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_UP) {
                spin(Spin.UP);
                event.preventDefault();
            }
            else if (event.getNativeKeyCode() == KeyCodes.KEY_DOWN) {
                spin(Spin.DOWN);
                event.preventDefault();
            }
        }
    }

    private class MouseWheelHandlerImpl implements MouseWheelHandler {
        @Override
        public void onMouseWheel(MouseWheelEvent event) {
            if (!WidgetUtil.hasFocus(internalBox.getElement())) {
                return;
            }
            if (event.isNorth()) {
                spin(Spin.UP);
                event.preventDefault();
            }
            else if (event.isSouth()) {
                spin(Spin.DOWN);
                event.preventDefault();
            }
        }
    }

    private class KeyPressHandlerImpl implements KeyPressHandler {
        @Override
        public void onKeyPress(KeyPressEvent event) {
            if (event.getNativeEvent().getCharCode() > 0
                    && event.getNativeEvent().getKeyCode() == 0
                    && !Character.isDigit(event.getCharCode())
                    && !EventUtil.charCodeIn(event, '.', ',', '-')
                    && !EventUtil.keyCombinationIn(event,
                    KeyCombination.CTRL_A,
                    KeyCombination.CTRL_C,
                    KeyCombination.CTRL_V,
                    KeyCombination.CTRL_X
            )) {
                DecimalBox.this.internalBox.cancelKey();
            }
        }
    }

    private class FormatDescriptionProviderImpl implements
            ValidationFeature.FormatDescriptionProvider {
        @Override
        public String getFormatDescription() {
            if (DecimalBox.this.min != null && DecimalBox.this.max != null && !DecimalBox.this.min.equals(DecimalBox.this.max)) {
                return I18n.I.decimalFormatDescriptionMinMax(formatString(DecimalBox.this.min), formatString(DecimalBox.this.max));
            }
            else if (DecimalBox.this.min != null && !DecimalBox.this.min.equals(DecimalBox.this.max)) {
                return I18n.I.decimalFormatDescriptionMin(formatString(DecimalBox.this.min));
            }
            else if (DecimalBox.this.max != null && !DecimalBox.this.max.equals(DecimalBox.this.min)) {
                return I18n.I.decimalFormatDescriptionMax(formatString(DecimalBox.this.max));
            }
            else {
                return I18n.I.decimalFormatDescription();
            }
        }
    }
}
