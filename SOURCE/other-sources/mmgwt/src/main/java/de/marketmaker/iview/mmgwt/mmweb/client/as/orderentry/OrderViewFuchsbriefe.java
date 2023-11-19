/*
 * OrderViewFuchsbriefe.java
 *
 * Created on 28.10.13 09:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.HasStringValueHost;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.formatters.StringRendererFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.limiters.UnambiguousNumberLimiter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.DecimalFormatValidator;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.Validator;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages.StyleMessageSink;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages.TooltipMessageSink;

/**
 * @author Markus Dick
 */
class OrderViewFuchsbriefe extends AbstractOrderView<DisplayFuchsbriefe.PresenterFuchsbriefe>
        implements DisplayFuchsbriefe<DisplayFuchsbriefe.PresenterFuchsbriefe> {

    private final TextBox quantityField;
    private final HasStringValueHost quantityValidatorHost;

    private final TextBox expectedMarketValueField;
    private final Label expectedMarketValueCurrencyLabel;

    public OrderViewFuchsbriefe() {
        int cols = 0;
        final int lcol1 = cols++;
        final int fcol1 = cols++;
        final int rcol1 = cols;

        int lcrow = 0;

        final StyleMessageSink styleMessageSink = new StyleMessageSink();
        final UnambiguousNumberLimiter unambiguousNumberLimiter = new UnambiguousNumberLimiter();
        final StringRendererFormatter numberStringFormatter = new StringRendererFormatter(OeRenderers.UNAMBIGUOUS_NUMBER_RENDERER);

        final FlexTable table = new FlexTable();
        table.setStyleName(FLEX_TABLE_STYLE);
        addWidgetToCustomerPanel(table);

        final FlexTable.FlexCellFormatter cellFormatter = table.getFlexCellFormatter();

        table.setWidget(0, 0, new Label(I18n.I.orderEntryOrder()));
        cellFormatter.setStyleName(0, 0, SECTION_HEADLINE_CELL_STYLE);
        cellFormatter.setColSpan(0, 0, 1);

        //quantity
        addLabel(I18n.I.orderEntryAmountNominal(), table, cellFormatter, ++lcrow, lcol1);
        this.quantityField = new TextBox();
        table.setWidget(lcrow, fcol1, this.quantityField);
        TooltipMessageSink amountMessages = new TooltipMessageSink(this.quantityField);

        table.setWidget(lcrow, rcol1, new Label(I18n.I.orderEntryAmountPerCurrency()));
        cellFormatter.setStyleName(lcrow, rcol1, REMARK_COLUMNS_STYLE);
        this.quantityField.setValue("", true);

        unambiguousNumberLimiter.attach(this.quantityField);
        this.quantityValidatorHost = new HasStringValueHost(this.quantityField, numberStringFormatter, Validator.NOT_EMPTY,
                new DecimalFormatValidator().with(Validator.DOUBLE_NOT_ZERO));
        this.quantityValidatorHost.attach(styleMessageSink).attach(amountMessages);

        //expected market value
        addLabel(I18n.I.orderEntryExpectedMarketValue(), table, cellFormatter, ++lcrow, lcol1);
        this.expectedMarketValueField = new TextBox();
        this.expectedMarketValueField.setReadOnly(true);
        table.setWidget(lcrow, fcol1, this.expectedMarketValueField);
        this.expectedMarketValueCurrencyLabel = new Label();
        table.setWidget(lcrow, rcol1, this.expectedMarketValueCurrencyLabel);
        cellFormatter.setStyleName(lcrow, rcol1, REMARK_COLUMNS_STYLE);

        //no liability assumed
        table.setWidget(++lcrow, fcol1, new Label(I18n.I.orderEntryExpectedMarketValueNoLiabilityAssumed()));
        cellFormatter.setColSpan(lcrow, fcol1, 2);

        //add event handlers
        this.quantityField.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getPresenter().onQuantityFieldKeyUp(event);
            }
        });

        this.quantityField.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getPresenter().onQuantityValueChanged(event);
            }
        });

        this.quantityValidatorHost.addValidationHandler(new ValidationHandler() {
            @Override
            public void onValidation(ValidationEvent validationEvent) {
                getPresenter().onValidation(validationEvent);
            }
        });
    }

    @Override
    public void setExpectedMarketValue(String value) {
        this.expectedMarketValueField.setText(value);
    }

    @Override
    public void setCurrencyLabels(String currentCurrency) {
        this.expectedMarketValueCurrencyLabel.setText(currentCurrency);
    }

    @Override
    public void setQuantity(String value) {
        this.quantityField.setText(value);
    }

    @Override
    public void setQuantityRaw(String value) {
        this.quantityField.setText(value);
        this.quantityValidatorHost.formatAndValidate();
    }

    @Override
    public String getQuantity() {
        return this.quantityField.getText();
    }

    @Override
    public boolean isValid() {
        this.quantityValidatorHost.validate();
        return this.quantityValidatorHost.isValid();
    }
}
