/*
 * OrderViewHA.java
 *
 * Created on 05.11.12 08:48
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.HasStringValueHost;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.formatters.StringRendererFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.limiters.UnambiguousNumberLimiter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages.StyleMessageSink;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages.TooltipMessageSink;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.DecimalFormatValidator;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ToolbarDateButtonValidatorHost;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEventJoint;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.Validator;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidatorGroup;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.datepicker.ToolbarDateButton;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
class OrderViewHA extends AbstractOrderView<DisplayHA.PresenterHA> implements DisplayHA<DisplayHA.PresenterHA> {
    private static final String VALID_UNTIL_GROUP = "VALID_UNTIL_GROUP"; //$NON-NLS$
    private static final String ADVICE_STYLE = "ha-advice"; //$NON-NLS$
    private static final String PSEUDO_DISABLED_STYLE = "ha-pdisabled"; //$NON-NLS$

    private final TextBox amountField = new TextBox();
    private final CheckBox limitChoice = new CheckBox();
    private final TextBox limitField = new TextBox();
    private final CheckBox stopBuyChoice = new CheckBox();

    private final CheckBox stopLossChoice = new CheckBox();
    private final Panel stopBuyPanel = new HorizontalPanel();

    private final Panel stopLossPanel = new HorizontalPanel();

    private final TextBox expectedMarketValueField = new TextBox();
    private final RadioButton goodForTheDayChoice = new RadioButton(VALID_UNTIL_GROUP);
    private final RadioButton dateChoice = new RadioButton(VALID_UNTIL_GROUP);
    private final RadioButton ultimoChoice = new RadioButton(VALID_UNTIL_GROUP);
    private final HashMap<ValidUntilChoice, RadioButton> validUntilGroup = new HashMap<ValidUntilChoice, RadioButton>();
    private final ToolbarDateButton dateButton = new ToolbarDateButton(null);
    private final Label limitChoiceCurrencyLabel = new Label();
    private final Label expectedMarketValueCurrencyLabel = new Label();
    private final HasStringValueHost amountFieldValidator;
    private final HasStringValueHost limitFieldValidator;
    private final ToolbarDateButtonValidatorHost dateButtonValidator =
            new ToolbarDateButtonValidatorHost(this.dateButton, Validator.DATE_NOT_BEFORE_TODAY);

    private final ValidationEventJoint validationEventJoint = new ValidationEventJoint();
    private final ValidatorGroup validatorGroup = new ValidatorGroup();

    private boolean mouseOverLimitCheck = false;

    public OrderViewHA() {
        final UnambiguousNumberLimiter unambiguousNumberLimiter = new UnambiguousNumberLimiter();
        final StringRendererFormatter numberStringFormatter = new StringRendererFormatter(OeRenderers.UNAMBIGUOUS_NUMBER_RENDERER);

        unambiguousNumberLimiter.attach(this.amountField);
        this.amountFieldValidator = new HasStringValueHost(this.amountField, numberStringFormatter, Validator.NOT_EMPTY,
                new DecimalFormatValidator().with(Validator.DOUBLE_NOT_ZERO));

        unambiguousNumberLimiter.attach(this.limitField);
        this.limitFieldValidator = new HasStringValueHost(this.limitField, numberStringFormatter, Validator.NOT_EMPTY,
                Validator.DECIMAL_FORMAT);
        initView();
        initHandlers();
    }

    private void initView() {
        int cols = 0;
        final int lcol1 = cols++;
        final int fcol1 = cols++;
        final int rcol1 = cols++; /* column for remarks etc. */
        final int lcol2 = cols++;
        final int fcol2 = cols++;
        final int lcol3 = cols++;
        final int fcol3 = cols++;
        final int colCount = cols;

        int lcrow = 0;
        int rcrow = lcrow;

        final StyleMessageSink styleMessageSink = new StyleMessageSink();

        final FlexTable table = new FlexTable();
        table.addDomHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                updateMouseOverLimitCheck(event);
            }
        }, MouseMoveEvent.getType());

        table.setStyleName(FLEX_TABLE_STYLE);
        final FlexTable.FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
        addWidgetToCustomerPanel(table);

        //Order
        table.setWidget(lcrow, lcol1, new Label(I18n.I.orderEntryOrder()));
        cellFormatter.setStyleName(lcrow, lcol1, SECTION_HEADLINE_CELL_STYLE);
        cellFormatter.setColSpan(lcrow, lcol1, colCount);

        //left column
        //amount
        addLabel(I18n.I.orderEntryAmountNominal(), table, cellFormatter, ++lcrow, lcol1);
        table.setWidget(lcrow, fcol1, this.amountField);
        TooltipMessageSink amountMessages = new TooltipMessageSink(this.amountField);
        this.amountFieldValidator.attach(styleMessageSink).attach(amountMessages);
        table.setWidget(lcrow, rcol1, new Label(I18n.I.orderEntryAmountPerCurrency()));
        cellFormatter.setStyleName(lcrow, rcol1, REMARK_COLUMNS_STYLE);
        this.amountField.setValue("0", false); //$NON-NLS$
        this.amountField.setValue("", true);

        //limit
        final Panel limitPanel = new HorizontalPanel();
        limitPanel.add(this.limitChoice);
        limitPanel.add(new Label(I18n.I.orderEntryLimit()));
        table.setWidget(++lcrow, lcol1, limitPanel);
        cellFormatter.setStyleName(lcrow, lcol1, LABEL_CELL_STYLE);

        table.setWidget(lcrow, rcol1, this.limitChoiceCurrencyLabel);
        cellFormatter.setStyleName(lcrow, rcol1, REMARK_COLUMNS_STYLE);

        TooltipMessageSink limitMessages = new TooltipMessageSink(this.limitField);
        this.limitFieldValidator.attach(styleMessageSink).attach(limitMessages);
        table.setWidget(lcrow, fcol1, this.limitField);

        //stop buy
        this.stopBuyPanel.add(this.stopBuyChoice);
        this.stopBuyPanel.add(new Label(I18n.I.orderEntryStopBuyMarket()));

        //stop loss
        this.stopLossPanel.add(this.stopLossChoice);
        this.stopLossPanel.add(new Label(I18n.I.orderEntryStopLossMarket()));

        final Panel stopPanel = new FlowPanel();
        stopPanel.add(this.stopBuyPanel);
        stopPanel.add(this.stopLossPanel);

        table.setWidget(++lcrow, lcol1, stopPanel);
        cellFormatter.setStyleName(lcrow, lcol1, LABEL_CELL_STYLE);

        //expected market value
        addLabel(I18n.I.orderEntryExpectedMarketValue(), table, cellFormatter, ++lcrow, lcol1);
        this.expectedMarketValueField.setReadOnly(true);
        table.setWidget(lcrow, fcol1, this.expectedMarketValueField);
        table.setWidget(lcrow, rcol1, this.expectedMarketValueCurrencyLabel);
        cellFormatter.setStyleName(lcrow, rcol1, REMARK_COLUMNS_STYLE);

        //no liability assumed
        table.setWidget(++lcrow, fcol1, new Label(I18n.I.orderEntryExpectedMarketValueNoLiabilityAssumed()));
        cellFormatter.setColSpan(lcrow, fcol1, 2);

        //rightColumn
        //valid until
        addLabel(I18n.I.orderEntryValidity(), table, cellFormatter, ++rcrow, lcol2);
        cellFormatter.addStyleName(rcrow, lcol2, RIGHT_COLUMNS_CELL_STYLE);

        this.goodForTheDayChoice.setText(I18n.I.orderEntryGoodForTheDay());
        this.dateChoice.setText(I18n.I.date());
        this.ultimoChoice.setText(I18n.I.orderEntryUltimo());

        this.dateButton.setEnabled(true);
        this.dateButton.getElement().getStyle().setWidth(100, Style.Unit.PX);
        this.dateButton.setMinDate(new MmJsDate().atMidnight());
        TooltipMessageSink dateMessages = new TooltipMessageSink(this.dateButton);
        this.dateButtonValidator.attach(styleMessageSink).attach(dateMessages);

        this.validUntilGroup.put(ValidUntilChoice.GOOD_FOR_THE_DAY, this.goodForTheDayChoice);
        this.validUntilGroup.put(ValidUntilChoice.DATE, this.dateChoice);
        this.validUntilGroup.put(ValidUntilChoice.ULTIMO, this.ultimoChoice);

        table.setWidget(++rcrow, lcol2, this.goodForTheDayChoice);
        cellFormatter.setStyleName(rcrow, lcol2, LABEL_CELL_STYLE);
        cellFormatter.addStyleName(rcrow, lcol2, RIGHT_COLUMNS_CELL_STYLE);

        table.setWidget(++rcrow, lcol2, this.dateChoice);
        cellFormatter.setStyleName(rcrow, lcol2, LABEL_CELL_STYLE);
        cellFormatter.addStyleName(rcrow, lcol2, RIGHT_COLUMNS_CELL_STYLE);
        table.setWidget(rcrow, fcol2, this.dateButton);

        table.setWidget(++rcrow, lcol2, this.ultimoChoice);
        cellFormatter.setStyleName(rcrow, lcol2, LABEL_CELL_STYLE);
        cellFormatter.addStyleName(rcrow, lcol2, RIGHT_COLUMNS_CELL_STYLE);

        //advice
        final Label advice = new HTML(I18n.I.orderEntryHAAdvice());
        advice.setStyleName(ADVICE_STYLE);
        addWidgetToCustomerPanel(advice);

        //Form validation
        this.validatorGroup.attach(this.amountFieldValidator)
                .attach(this.limitFieldValidator)
                .attach(this.dateButtonValidator);

        this.validationEventJoint.attach(this.amountFieldValidator)
                .attach(this.limitFieldValidator)
                .attach(this.dateButtonValidator);
    }

    private void initHandlers() {
        this.limitField.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                getPresenter().onLimitFocus(event);
            }
        });

        this.limitField.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                getPresenter().onLimitBlur(event);
            }
        });

        this.limitChoice.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                getPresenter().onLimitCheckValueChange(event);
            }
        });

        final ValueChangeHandler<Boolean> validUntilRadioGroupValueChangeHandler = new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                getPresenter().onValidUntilRadioGroupValueChanged(booleanValueChangeEvent);
            }
        };
        for(RadioButton radioButton : this.validUntilGroup.values()) {
            radioButton.addValueChangeHandler(validUntilRadioGroupValueChangeHandler);
        }

        this.amountField.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                getPresenter().onAmountKeyUp(event);
            }
        });

        this.amountField.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                getPresenter().onAmountValueChange(event);
            }
        });

        this.dateButton.addHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                getPresenter().onValidDateClick(event);
            }
        }, ClickEvent.getType());

        this.validationEventJoint.addValidationHandler(new ValidationHandler() {
            @Override
            public void onValidation(ValidationEvent event) {
                getPresenter().onValidationEventJoint(event);
            }
        });
    }

    private void updateMouseOverLimitCheck(MouseMoveEvent event) {
        int relativeX = event.getRelativeX(this.limitChoice.getElement());
        int relativeY = event.getRelativeY(this.limitChoice.getElement());

        int width = this.limitChoice.getOffsetWidth();
        int height = this.limitChoice.getOffsetHeight();

        this.mouseOverLimitCheck = !(relativeX < 0 || relativeY < 0 || relativeX > width || relativeY > height);

        /*DebugUtil.logToFirebugConsole("relativeX=" + relativeX + " relativeY=" + relativeY);
        DebugUtil.logToFirebugConsole("width=" + width + " height=" + height);
        DebugUtil.logToFirebugConsole("mouseOverLimitCheck <- " + this.mouseOverLimitCheck);*/
    }

    @Override
    public String getAmount() {
        return this.amountField.getValue();
    }

    @Override
    public void setAmount(String value) {
        this.amountField.setValue(value);
    }

    @Override
    public HasStringValueHost getAmountFieldValidator() {
        return this.amountFieldValidator;
    }

    @Override
    public boolean isLimitChecked() {
        return this.limitChoice.getValue();
    }

    @Override
    public boolean isMouseOverLimitCheck() {
        return this.mouseOverLimitCheck;
    }

    @Override
    public void setLimitChecked(boolean checked) {
        this.limitChoice.setValue(checked);
    }

    @Override
    public void setLimitPseudoEnabled(boolean enabled) {
        if(enabled) {
            this.limitField.removeStyleName(PSEUDO_DISABLED_STYLE);
        }
        else {
            this.limitField.addStyleName(PSEUDO_DISABLED_STYLE);
        }
        BrowserSpecific.INSTANCE.setReadOnly(this.limitField, !enabled);
        this.limitFieldValidator.setEnabled(enabled);
        this.limitFieldValidator.validate();
    }

    @Override
    public String getLimit() {
        return this.limitField.getValue();
    }

    @Override
    public void setLimit(String limit) {
        this.limitField.setValue(limit);
    }

    @Override
    public HasStringValueHost getLimitFieldValidator() {
        return this.limitFieldValidator;
    }

    @Override
    public boolean isStopBuyMarketChecked() {
        return this.stopBuyChoice.getValue();
    }

    @Override
    public void setStopBuyMarketChecked(boolean checked) {
        this.stopBuyChoice.setValue(checked);
    }

    @Override
    public boolean isStopLossMarketChecked() {
        return this.stopLossChoice.getValue();
    }

    @Override
    public void setStopLossMarketChecked(boolean checked) {
        this.stopLossChoice.setValue(checked);
    }

    @Override
    public void setShowStopChoice(ShowStopChoice showStopChoice) {
        if(ShowStopChoice.STOP_BUY.equals(showStopChoice)) {
            this.stopBuyPanel.setVisible(true);
            this.stopLossPanel.setVisible(false);
        }
        else if(ShowStopChoice.STOP_LOSS.equals(showStopChoice)) {
            this.stopBuyPanel.setVisible(false);
            this.stopLossPanel.setVisible(true);
        }
    }

    @Override
    public void setExpectedMarketValue(String approximatedPrice) {
        this.expectedMarketValueField.setValue(approximatedPrice);
    }

    @Override
    public ValidatorGroup getValidatorGroup() {
        return this.validatorGroup;
    }

    @Override
    public ValidUntilChoice getValidUntilChoice() {
        for(Map.Entry<ValidUntilChoice, RadioButton> entry : this.validUntilGroup.entrySet()) {
            if(entry.getValue().getValue()) {
                return entry.getKey();
            }
        }

        return null;
    }

    @Override
    public void setValidUntilChoice(ValidUntilChoice choice) {
        for(Map.Entry<ValidUntilChoice, RadioButton> e : this.validUntilGroup.entrySet()) {
            e.getValue().setValue(e.getKey() == choice);
        }
    }

    @Override
    public void setValidUntilChoiceDate(MmJsDate mmJsDate) {
       this.dateButton.setValue(mmJsDate);
    }

    @Override
    public MmJsDate getValidUntilChoiceDate() {
        return dateButton.getValue();
    }

    @Override
    public String setValidUntilChoiceEnabled(ValidUntilChoice... enable) {
        for(Map.Entry<ValidUntilChoice, RadioButton> entry : this.validUntilGroup.entrySet()) {
            Boolean enabled = null;
            for(ValidUntilChoice choice : enable) {
                if(entry.getKey() == choice) {
                    enabled = Boolean.TRUE;
                }
            }
            if(enabled == null) {
                enabled = Boolean.FALSE;
            }
            entry.getValue().setEnabled(enabled);
        }

        return null;
    }

    @Override
    public void setSelectedOrderAction(de.marketmaker.iview.pmxml.OrderAction action) {
        super.setSelectedOrderAction(action);
    }

    @Override
    public void setCurrencyLabels(String currentCurrency) {
        super.setCurrencyLabels(currentCurrency);
        this.limitChoiceCurrencyLabel.setText(currentCurrency);
        this.expectedMarketValueCurrencyLabel.setText(currentCurrency);
    }
}