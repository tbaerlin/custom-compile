/*
 * TextFilterPanel.java
 *
 * Created on 24.03.2015 08:02
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import de.marketmaker.itools.gwtutil.client.widgets.input.CheckBox;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DTCell;

/**
 * @author mdick
 */
public class TextFilterPanel extends AbstractFilterPanel<TextExpression, DTCell> {
    @Override
    protected HasValue<TextExpression> createEditorWidget() {
        final TextExpressionBox box = new TextExpressionBox();
        box.addValueChangeHandler(new ValueChangeHandler<TextExpression>() {
            @Override
            public void onValueChange(ValueChangeEvent<TextExpression> event) {
                final TextExpression value = event.getValue();
                if(isValueAddable(value)) {
                    if (!isEditorValueEnabled()) {
                        setEditorValueEnabled(true);
                    }
                    fireChangeEvent();
                }
            }
        });

        return box;
    }

    @Override
    protected IsWidget createValueWidget(TextExpression value) {
        return new Label(value.isExactMatch() ? I18n.I.contains(value.getText()) : value.getText());
    }

    @Override
    protected DTTableRenderer.ColumnFilter createFilter(TextExpression value) {
        if(value == null || !StringUtil.hasText(value.getText())) {
            return null;
        }

        if(value.isExactMatch()) {
            return new DTTableRenderer.EqualsColumnFilter(getMetadata().getColumnIndex(), value.getText());
        }
        return new DTTableRenderer.SubstringColumnFilter(getMetadata().getColumnIndex(), value.getText());
    }

    @Override
    protected boolean isValueAddable(TextExpression value) {
        return value != null && StringUtil.hasText(value.getText());
    }

    private static class TextExpressionBox extends Composite implements HasValue<TextExpression> {
        private final TextBox textBox;
        private final CheckBox exactMatchCheckBox;

        public TextExpressionBox() {
            final VerticalPanel panel = new VerticalPanel();

            this.textBox = new TextBox();

            this.textBox.addValueChangeHandler(new ValueChangeHandler<String>() {
                @Override
                public void onValueChange(ValueChangeEvent<String> event) {
                    ValueChangeEvent.fire(TextExpressionBox.this, getValue());
                }
            });
            panel.add(this.textBox);

            final HorizontalPanel horizontalMatchPanel = new HorizontalPanel();
            panel.add(horizontalMatchPanel);

            this.exactMatchCheckBox = new CheckBox(false);
            this.exactMatchCheckBox.setMode(CheckBox.Mode.TRUE_FALSE);
            this.exactMatchCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> valueChangeEvent) {
                    ValueChangeEvent.fire(TextExpressionBox.this, getValue());
                }
            });
            horizontalMatchPanel.add(this.exactMatchCheckBox);
            horizontalMatchPanel.add(this.exactMatchCheckBox.createLabel(I18n.I.exactMatch()));

            initWidget(panel);
        }

        @Override
        public TextExpression getValue() {
            final String value = this.textBox.getValue();
            final boolean exactMatch = this.exactMatchCheckBox.isChecked();

            return new TextExpression(value, exactMatch);
        }

        @Override
        public void setValue(TextExpression value) {
            setValue(value, false);
        }

        @Override
        public void setValue(TextExpression value, boolean fireChangeEvent) {
            final TextExpression oldValue = getValue();

            if(value == null) {
                this.textBox.setValue(null);
                this.exactMatchCheckBox.setChecked(false);
            }
            else {
                this.textBox.setValue(value.getText());
                this.exactMatchCheckBox.setChecked(value.isExactMatch());
            }

            final TextExpression newValue = getValue();

            ValueChangeEvent.fireIfNotEqual(this, oldValue, newValue);
        }

        @Override
        public HandlerRegistration addValueChangeHandler(ValueChangeHandler<TextExpression> valueChangeHandler) {
            return addHandler(valueChangeHandler, ValueChangeEvent.getType());
        }
    }
}
