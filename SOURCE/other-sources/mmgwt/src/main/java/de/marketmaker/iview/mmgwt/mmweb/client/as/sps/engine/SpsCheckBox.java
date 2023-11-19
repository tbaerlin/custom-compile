package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.itools.gwtutil.client.util.HasFocusableElement;
import de.marketmaker.itools.gwtutil.client.widgets.input.CheckBox;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.pmxml.DefaultMM;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMBool;
import de.marketmaker.iview.pmxml.SimpleMM;
import de.marketmaker.iview.pmxml.ThreeValueBoolean;

/**
 * Author: umaurer
 * Created: 28.03.14
 */
public class SpsCheckBox extends SpsBoundWidget<SpsCheckBox.DecoratedCheckBox, SpsLeafProperty> implements HasBindFeature, HasCaption {
    private boolean threeValueBoolean;

    @Override
    public SpsCheckBox withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    public SpsCheckBox withThreeValueBoolean(boolean threeValueBoolean) {
        this.threeValueBoolean = threeValueBoolean;
        return this;
    }

    private CheckBox.Mode getCheckBoxMode() {
        if (isThreeValueBoolean()) {
            return CheckBox.Mode.TRUE_FALSE_NULL;
        }
        else {
            return CheckBox.Mode.TRUE_FALSE;
        }
    }

    @Override
    protected DecoratedCheckBox createWidget() {
        final DecoratedCheckBox checkBox = new DecoratedCheckBox(null);
        checkBox.setMode(getCheckBoxMode());
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                updateProperty(event.getValue());
            }
        });
        final HTML captionWidget = getCaptionWidget();
        if (captionWidget != null) {
            checkBox.configureLabel(captionWidget);
        }
        return checkBox;
    }

    private void updateProperty(Boolean value) {
        getBindFeature().getSpsProperty().setValue(toDataItemSimple(value), true, true);
    }

    @Override
    public void onPropertyChange() {
        final Boolean checked = toBoolean(getBindFeature().getSpsProperty().getDataItem());
        getWidget().setChecked(checked, false);
    }

    private Boolean toBoolean(MM dataItem) {
        if (!(dataItem instanceof MMBool)) {
            if (!isThreeValueBoolean()) {
                return false;
            }
            return null;
        }
        return MmTalkHelper.asBoolean(dataItem);
    }

    private SimpleMM toDataItemSimple(Boolean value) {
        if (value == null) {
            if (!isThreeValueBoolean()) {
                final MMBool diBoolean = new MMBool();
                diBoolean.setValue(ThreeValueBoolean.TV_FALSE);
                return diBoolean;
            }
            return new DefaultMM();
        }
        else {
            final MMBool bool = new MMBool();
            bool.setValue(value
                    ? ThreeValueBoolean.TV_TRUE
                    : ThreeValueBoolean.TV_FALSE);
            return bool;
        }
    }

    public boolean isThreeValueBoolean() {
        return this.threeValueBoolean;
    }

    /**
     * Decorates the Checkbox with a FlowPanel, so that it is possible to set the width of the checkbox.
     * Without the FlowPanel one would set the width of the checkbox via MMTalk, in fact it is a simple image, which
     * in turn unveils the other images of the sprite.
     */
    public static class DecoratedCheckBox extends Composite implements HasFocusableElement, HasValueChangeHandlers<Boolean>, Focusable, HasFocusHandlers, HasBlurHandlers, HasEnabled {
        private final CheckBox decorated;
        private final FlowPanel panel;

        public DecoratedCheckBox(Boolean checked) {
            this.decorated = new CheckBox(checked);
            this.panel = new FlowPanel();
            this.panel.add(this.decorated);

            initWidget(this.panel);
        }

        @Override
        public int getTabIndex() {
            return decorated.getTabIndex();
        }

        @Override
        public void setAccessKey(char key) {
            decorated.setAccessKey(key);
        }

        @Override
        public void setFocus(boolean focused) {
            decorated.setFocus(focused);
        }

        @Override
        public void setTabIndex(int tabIndex) {
            decorated.setTabIndex(tabIndex);
        }

        @Override
        public HandlerRegistration addFocusHandler(FocusHandler handler) {
            return this.decorated.addDomHandler(handler, FocusEvent.getType());
        }

        @Override
        public HandlerRegistration addBlurHandler(BlurHandler handler) {
            return this.decorated.addDomHandler(handler, BlurEvent.getType());
        }

        @Override
        public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler) {
            return this.decorated.addValueChangeHandler(handler);
        }

        public void setMode(CheckBox.Mode mode) {
            this.decorated.setMode(mode);
        }

        public void setChecked(Boolean checked) {
            this.decorated.setChecked(checked);
        }

        public void setChecked(Boolean checked, boolean fireEvent) {
            this.decorated.setChecked(checked, fireEvent);
        }

        @Override
        public boolean isEnabled() {
            return this.decorated.isEnabled();
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.decorated.setEnabled(enabled);
        }

        public boolean isChecked() {
            return this.decorated.isChecked();
        }

        public Boolean getChecked() {
            return this.decorated.getChecked();
        }

        public Label configureLabel(Label label) {
            return this.decorated.configureLabel(label);
        }

        @Override
        public Element getFocusableElement() {
            return this.decorated.getElement();
        }
    }
}