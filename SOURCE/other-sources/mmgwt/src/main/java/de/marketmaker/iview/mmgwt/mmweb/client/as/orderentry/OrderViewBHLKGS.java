/*
 * OrderViewBHLKGS.java
 *
 * Created on 10.07.13 12:44
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSelectButton;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.PriceWithCurrency;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.HasStringValueHost;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.MappedListBoxValidatorHost;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.TimeFormat;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.formatters.StringRendererFormatter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.limiters.Limiter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.limiters.TimeLimiter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.limiters.UnambiguousNumberLimiter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.mappers.DepotItemMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.mappers.TextWithKeyItemMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.DecimalFormatValidator;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.DoubleMaxValueValidator;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.TimeStringValidator;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ToolbarDateButtonValidatorHost;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationEventJoint;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidationHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.Validator;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidatorGroup;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.validators.ValidatorHost;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages.VisibilityMessageSink;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages.StyleMessageSink;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages.TooltipMessageSink;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.MappedListBox;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SimpleGlassablePanel;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.datepicker.ToolbarDateButton;
import de.marketmaker.iview.pmxml.AccountData;
import de.marketmaker.iview.pmxml.AuthorizedRepresentative;
import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.OrderActionType;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.pmxml.OrderExchangeInfo;
import de.marketmaker.iview.pmxml.OrderSecurityInfo;
import de.marketmaker.iview.pmxml.OrderStock;
import de.marketmaker.iview.pmxml.ProvisionType;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.TextWithKey;

import java.util.List;

/**
 * @author Markus Dick
 */
public class OrderViewBHLKGS extends Composite implements DisplayBHLKGS<DisplayBHLKGS.PresenterBHLKGS> {
    public static final String VIEW_STYLE = "as-oe"; //$NON-NLS$
    public static final String DATA_KEY = "data"; //$NON-NLS$
    private final HasStringValueHost amountNominalFieldMaxValueValidator;

    public interface MyUiBinder extends UiBinder<Widget, OrderViewBHLKGS> {}
    private final static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    protected PresenterBHLKGS presenter = null;

    protected final Menu selectInstrumentFromDepotMenu = new Menu();
    protected final Menu selectAuthorizedRepresentativeMenu = new Menu();

    @UiField(provided = true) protected final I18n i18n = I18n.I;

    @UiField protected SimpleGlassablePanel loadingIndicator;

    @UiField protected TabLayoutPanel tabLayoutPanel;
    @UiField(provided = true) protected final Image errorImageTab1 = IconImage.get("dialog-error-16").createImage();
    @UiField(provided = true) protected final Image errorImageTab2 = IconImage.get("dialog-error-16").createImage();

    @UiField protected MappedListBox<OrderActionType> orderActionTypeChoice;
    @UiField protected Label orderActionTypeField;

    @UiField protected Label investorNameField;
    @UiField protected Label investorNumberField;

    @UiField protected MappedListBox<Depot> depotChoice;
    @UiField protected Label depotNameField;
    @UiField protected Label depotNumberField;

    @UiField protected MappedListBox<AccountData> accountChoice;
    @UiField protected Label accountNumberField;
    @UiField protected Label accountBalanceField;

    @UiField protected HorizontalPanel selectInstrumentPanel;
    @UiField protected TextBox instrumentField;
    @UiField(provided=true) protected final ImageButton selectInstrumentButton =
            GuiUtil.createImageButton("mm-icon-finder", null, null, null); //$NON-NLS$

    protected final ImageSelectButton selectInstrumentFromDepotChoice =
            new ImageSelectButton(IconImage.get("pm-investor-depot") //$NON-NLS$
                    .createImage(), null, null, true)
                    .withMenu(this.selectInstrumentFromDepotMenu);

    @UiField(provided=true)
    protected final MenuItemSelectionDelegate<OrderSecurityInfo> selectInstrumentFromDepotChoiceDelegate
            = new MenuItemSelectionDelegate<OrderSecurityInfo>(selectInstrumentFromDepotChoice);

    @UiField protected HTMLPanel selectIpoInstrumentPanel;
    @UiField protected Label ipoInstrumentLabel;
    @UiField protected Button selectIpoInstrumentButton;

    @UiField protected HTMLPanel instrumentNamePanel;
    @UiField protected Label instrumentNameField;

    @UiField protected Button showArbitrageButton;

    @UiField protected Label instrumentTypeField;
    @UiField protected Label isinField;
    @UiField protected Label wknField;

    @UiField protected MappedListBox<OrderExchangeInfo> exchangeChoiceDomestic;
    @UiField protected MappedListBox<OrderExchangeInfo> exchangeChoiceForeign;
    @UiField protected MappedListBox<OrderExchangeInfo> exchangeChoiceOther;
    @UiField protected CheckBox exchangeAccordingToCustomerCheck;

    @UiField protected MappedListBox<TextWithKey> tradingIndicatorChoice;

    @UiField protected TextBox amountNominalField;
    @UiField protected Label amountNominalCurrencyOrUnit;
    @UiField protected MappedListBox<OrderStock> depositoriesChoice;

    @UiField protected TextBox limitOrStopLimitField;
    @UiField protected MappedListBox<CurrencyAnnotated> limitCurrencyChoice;
    @UiField protected MappedListBox<TextWithKey> limitClauseChoice;
    @UiField protected TextBox limitAfterStopLimitField;
    @UiField protected CheckBox limitFeeCheck;
    @UiField protected TextBox limitTrailingAmountField;
    @UiField protected TextBox limitPeakSizeQuantityField;

    @UiField protected RadioButton validityRadioNone;
    @UiField protected RadioButton validityRadioToday;
    @UiField protected RadioButton validityRadioUltimo;
    @UiField protected RadioButton validityRadioDate;
    @UiField protected ToolbarDateButton validityDateButton;

    @UiField protected MappedListBox<TextWithKey> ordererChoice;
    @UiField protected TextBox ordererIdentifierField;
    @UiField protected TextBox ordererCustomerNumberField;

    protected final ImageSelectButton selectOrdererChoice =
            new ImageSelectButton(IconImage.get("pm-investor") //$NON-NLS$
                    .createImage(), null, null, true)
                    .withMenu(this.selectAuthorizedRepresentativeMenu);

    @UiField(provided=true)
    protected final MenuItemSelectionDelegate<AuthorizedRepresentative> selectOrdererChoiceDelegate
            = new MenuItemSelectionDelegate<AuthorizedRepresentative>(this.selectOrdererChoice);

    @UiField protected MappedListBox<TextWithKey> minutesOfTheConsultationTypeChoice;
    @UiField protected TextBox minutesOfTheConsultationNumberField;
    private final TextBox minutesOfTheConsultationDateField = new TextBox();
    @UiField(provided = true) protected DateBox minutesOfTheConsultationDateBox = DateBox.factory()
            .withTextBox(this.minutesOfTheConsultationDateField)
            .withAllowNull()
            .build();

    @UiField protected MappedListBox<TextWithKey> settlementTypeChoice;
    @UiField protected MappedListBox<TextWithKey> businessSegmentChoice;
    @UiField protected MappedListBox<TextWithKey> placingOfOrderViaChoice;
    @UiField protected TextBox externalTypist;

    private final TextBox contractDateField = new TextBox();
    @UiField(provided = true) protected DateBox contractDateBox = DateBox.factory()
            .withTextBox(this.contractDateField)
            .withAllowNull()
            .build();
    @UiField protected TextBox contractTimeField;

    @UiField protected MappedListBox<ProvisionType> commissionChoice;
    @UiField protected TextBox differentCommissionField;

    @UiField protected MappedListBox<TextWithKey> cannedTextForBillingReceiptChoice1;
    @UiField protected MappedListBox<TextWithKey> cannedTextForBillingReceiptChoice2;
    @UiField protected MappedListBox<TextWithKey> cannedTextForBillingReceiptChoice3;
    @UiField protected MappedListBox<TextWithKey> cannedTextForOrderConfirmationChoice; //Auftragsbestätigung

    @UiField protected TextBox textForOrderReceiptField1; //Order-Beleg
    @UiField protected TextBox textForOrderReceiptField2;
    @UiField protected TextBox textForInternalUseField1;
    @UiField protected TextBox textForInternalUseField2;

    private final MappedListBoxValidatorHost<OrderActionType> transactionTypeChoiceValidator;
    private final MappedListBoxValidatorHost<AccountData> accountChoiceValidator;
    private final HasStringValueHost instrumentFieldValidator;
    private final DoubleMaxValueValidator amountNominalDoubleMaxValueValidator;
    private final HasStringValueHost amountNominalFieldValidator;
    private final HasStringValueHost limitOrStopLimitFieldValidatorNotEmpty;
    private final HasStringValueHost limitOrStopLimitFieldValidator;
    private final HasStringValueHost limitAfterStopLimitFieldValidator;
    private final HasStringValueHost limitPeakSizeQuantityFieldValidator;
    private final HasStringValueHost limitTrailingAmountFieldValidator;
    private final HasStringValueHost differentCommissionFieldValidator;
    private final HasStringValueHost contractDateFieldValidator;
    private final HasStringValueHost contractTimeFieldValidator;
    private final MappedListBoxValidatorHost<OrderExchangeInfo> exchangeChoiceDomesticValidator;
    private final MappedListBoxValidatorHost<OrderExchangeInfo> exchangeChoiceForeignValidator;
    private final MappedListBoxValidatorHost<OrderExchangeInfo> exchangeChoiceOtherValidator;
    private final MappedListBoxValidatorHost<OrderStock> depositoriesChoiceValidator;
    private final MappedListBoxValidatorHost<CurrencyAnnotated> limitCurrencyChoiceValidator;
    private final MappedListBoxValidatorHost<TextWithKey> ordererChoiceValidator;
    private final MappedListBoxValidatorHost<TextWithKey> settlementTypeChoiceValidator;
    private final MappedListBoxValidatorHost<TextWithKey> businessSegmentChoiceValidator;
    private final MappedListBoxValidatorHost<TextWithKey> placingOfOrderViaChoiceValidator;
    private final ToolbarDateButtonValidatorHost dateButtonValidator;

    private final ValidationEventJoint validationEventJoint = new ValidationEventJoint();
    private final ValidatorGroup validatorGroup = new ValidatorGroup();

    public OrderViewBHLKGS() {
        initWidget(uiBinder.createAndBindUi(this));

        //add validators
        final OeRenderers.UnambiguousNumberRenderer unambiguousNumberRenderer = new OeRenderers.UnambiguousNumberRenderer(0,5);
        final DecimalFormatValidator decimalFormatNotZero = new DecimalFormatValidator().with(Validator.DOUBLE_NOT_ZERO);

        final Limiter<TextBoxBase> unambiguousNumberLimiter = new UnambiguousNumberLimiter();
        final Limiter<TextBoxBase> timeLimiter = new TimeLimiter(TimeFormat.HHMM);
        final StringRendererFormatter numberStringFormatter = new StringRendererFormatter(unambiguousNumberRenderer);

        this.transactionTypeChoiceValidator = new MappedListBoxValidatorHost<OrderActionType>(this.orderActionTypeChoice, Validator.NOT_NULL);
        this.accountChoiceValidator = new MappedListBoxValidatorHost<AccountData>(this.accountChoice, Validator.NOT_NULL);

        this.instrumentFieldValidator = new HasStringValueHost(this.instrumentField, null, Validator.NOT_EMPTY);

        unambiguousNumberLimiter.attach(this.amountNominalField);
        this.amountNominalFieldValidator = new HasStringValueHost(this.amountNominalField, numberStringFormatter,
                Validator.NOT_EMPTY, decimalFormatNotZero);

        this.amountNominalDoubleMaxValueValidator = new DoubleMaxValueValidator();
        this.amountNominalFieldMaxValueValidator = new HasStringValueHost(this.amountNominalField, numberStringFormatter,
                Validator.NOT_EMPTY, new DecimalFormatValidator().with(this.amountNominalDoubleMaxValueValidator));

        unambiguousNumberLimiter.attach(this.limitOrStopLimitField);
        this.limitOrStopLimitFieldValidatorNotEmpty = new HasStringValueHost(this.limitOrStopLimitField, null, Validator.NOT_EMPTY);
        this.limitOrStopLimitFieldValidator = new HasStringValueHost(this.limitOrStopLimitField, numberStringFormatter,
                decimalFormatNotZero);

        unambiguousNumberLimiter.attach(this.limitAfterStopLimitField);
        this.limitAfterStopLimitFieldValidator = new HasStringValueHost(this.limitAfterStopLimitField,
                numberStringFormatter, decimalFormatNotZero);

        unambiguousNumberLimiter.attach(this.limitTrailingAmountField);
        this.limitTrailingAmountFieldValidator = new HasStringValueHost(this.limitTrailingAmountField,
                numberStringFormatter, decimalFormatNotZero);

        unambiguousNumberLimiter.attach(this.limitPeakSizeQuantityField);
        this.limitPeakSizeQuantityFieldValidator = new HasStringValueHost(this.limitPeakSizeQuantityField,
                numberStringFormatter, decimalFormatNotZero);

        unambiguousNumberLimiter.attach(this.differentCommissionField);
        this.differentCommissionFieldValidator = new HasStringValueHost(this.differentCommissionField,
                numberStringFormatter, Validator.NOT_EMPTY, decimalFormatNotZero);

        this.contractDateFieldValidator = new HasStringValueHost(this.contractDateField, null, Validator.NOT_EMPTY);
        final TimeFormat timeFormat = TimeFormat.HHMM;
        this.contractTimeFieldValidator = new HasStringValueHost(this.contractTimeField, null, Validator.NOT_EMPTY,
                new TimeStringValidator(timeFormat));
        timeLimiter.attach(this.contractTimeField);

        this.dateButtonValidator = new ToolbarDateButtonValidatorHost(this.validityDateButton, Validator.DATE_NOT_BEFORE_TODAY);

        this.exchangeChoiceDomesticValidator = new MappedListBoxValidatorHost<OrderExchangeInfo>(this.exchangeChoiceDomestic, Validator.NOT_NULL);
        this.exchangeChoiceForeignValidator = new MappedListBoxValidatorHost<OrderExchangeInfo>(this.exchangeChoiceForeign, Validator.NOT_NULL);
        this.exchangeChoiceOtherValidator = new MappedListBoxValidatorHost<OrderExchangeInfo>(this.exchangeChoiceOther, Validator.NOT_NULL);
        this.depositoriesChoiceValidator = new MappedListBoxValidatorHost<OrderStock>(this.depositoriesChoice, Validator.NOT_NULL);
        this.limitCurrencyChoiceValidator = new MappedListBoxValidatorHost<CurrencyAnnotated>(this.limitCurrencyChoice, Validator.NOT_NULL);
        this.ordererChoiceValidator = new MappedListBoxValidatorHost<TextWithKey>(this.ordererChoice, Validator.NOT_NULL);

        this.settlementTypeChoiceValidator = new MappedListBoxValidatorHost<TextWithKey>(this.settlementTypeChoice, Validator.NOT_NULL);
        this.businessSegmentChoiceValidator = new MappedListBoxValidatorHost<TextWithKey>(this.businessSegmentChoice, Validator.TEXT_WITH_KEY_KEY_NOT_NULL_OR_EMPTY_VALIDATOR);
        this.placingOfOrderViaChoiceValidator = new MappedListBoxValidatorHost<TextWithKey>(this.placingOfOrderViaChoice, Validator.TEXT_WITH_KEY_KEY_NOT_NULL_OR_EMPTY_VALIDATOR);

        //add message sinks
        final VisibilityMessageSink tab1MessageSink = new VisibilityMessageSink(this.errorImageTab1);
        final VisibilityMessageSink tab2MessageSink = new VisibilityMessageSink(this.errorImageTab2);
        final StyleMessageSink styleMessageSink = new StyleMessageSink();

        attachMessageSinks(this.orderActionTypeChoice,
                this.transactionTypeChoiceValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.accountChoice,
                this.accountChoiceValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.instrumentField,
                this.instrumentFieldValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.exchangeChoiceDomestic,
                this.exchangeChoiceDomesticValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.exchangeChoiceForeign,
                this.exchangeChoiceForeignValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.exchangeChoiceOther,
                this.exchangeChoiceOtherValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.amountNominalField,
                this.amountNominalFieldValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.amountNominalField,
                this.amountNominalFieldMaxValueValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.depositoriesChoice,
                this.depositoriesChoiceValidator, tab1MessageSink, styleMessageSink);

        final TooltipMessageSink limitOrStopLimitMessages = new TooltipMessageSink(this.limitOrStopLimitField);
        this.limitOrStopLimitFieldValidatorNotEmpty.attach(tab1MessageSink).attach(styleMessageSink).attach(limitOrStopLimitMessages);
        this.limitOrStopLimitFieldValidator.attach(tab1MessageSink).attach(styleMessageSink).attach(limitOrStopLimitMessages);

        attachMessageSinks(this.limitCurrencyChoice,
                this.limitCurrencyChoiceValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.limitAfterStopLimitField,
                this.limitAfterStopLimitFieldValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.limitTrailingAmountField,
                this.limitTrailingAmountFieldValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.limitPeakSizeQuantityField,
                this.limitPeakSizeQuantityFieldValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.validityDateButton,
                this.dateButtonValidator, tab1MessageSink, styleMessageSink);

        attachMessageSinks(this.ordererChoice,
                this.ordererChoiceValidator, tab2MessageSink, styleMessageSink);

        attachMessageSinks(this.differentCommissionField,
                this.differentCommissionFieldValidator, tab2MessageSink, styleMessageSink);

        attachMessageSinks(this.settlementTypeChoice,
                this.settlementTypeChoiceValidator, tab2MessageSink, styleMessageSink);

        attachMessageSinks(this.businessSegmentChoice,
                this.businessSegmentChoiceValidator, tab2MessageSink, styleMessageSink);

        attachMessageSinks(this.placingOfOrderViaChoice,
                this.placingOfOrderViaChoiceValidator, tab2MessageSink, styleMessageSink);

        attachMessageSinks(this.contractDateField,
                this.contractDateFieldValidator, tab2MessageSink, styleMessageSink);

        attachMessageSinks(this.contractTimeField,
                this.contractTimeFieldValidator, tab2MessageSink, styleMessageSink);

        //Form validation
        this.validatorGroup.attach(this.transactionTypeChoiceValidator)
                .attach(this.accountChoiceValidator)
                .attach(this.instrumentFieldValidator)
                .attach(this.exchangeChoiceDomesticValidator)
                .attach(this.exchangeChoiceForeignValidator)
                .attach(this.exchangeChoiceOtherValidator)
                .attach(this.amountNominalFieldValidator)
                .attach(this.amountNominalFieldMaxValueValidator)
                .attach(this.depositoriesChoiceValidator)
                .attach(this.limitOrStopLimitFieldValidatorNotEmpty)
                .attach(this.limitOrStopLimitFieldValidator)
                .attach(this.limitAfterStopLimitFieldValidator)
                .attach(this.limitTrailingAmountFieldValidator)
                .attach(this.limitPeakSizeQuantityFieldValidator)
                .attach(this.limitCurrencyChoiceValidator)
                .attach(this.dateButtonValidator)
                .attach(this.ordererChoiceValidator)
                .attach(this.differentCommissionFieldValidator)
                .attach(this.settlementTypeChoiceValidator)
                .attach(this.businessSegmentChoiceValidator)
                .attach(this.placingOfOrderViaChoiceValidator)
                .attach(this.contractDateFieldValidator)
                .attach(this.contractTimeFieldValidator);

        this.validationEventJoint.attach(this.transactionTypeChoiceValidator)
                .attach(this.accountChoiceValidator)
                .attach(this.instrumentFieldValidator)
                .attach(this.exchangeChoiceDomesticValidator)
                .attach(this.exchangeChoiceForeignValidator)
                .attach(this.exchangeChoiceOtherValidator)
                .attach(this.amountNominalFieldValidator)
                .attach(this.amountNominalFieldMaxValueValidator)
                .attach(this.depositoriesChoiceValidator)
                .attach(this.limitOrStopLimitFieldValidatorNotEmpty)
                .attach(this.limitOrStopLimitFieldValidator)
                .attach(this.limitAfterStopLimitFieldValidator)
                .attach(this.limitTrailingAmountFieldValidator)
                .attach(this.limitPeakSizeQuantityFieldValidator)
                .attach(this.limitCurrencyChoiceValidator)
                .attach(this.dateButtonValidator)
                .attach(this.ordererChoiceValidator)
                .attach(this.differentCommissionFieldValidator)
                .attach(this.settlementTypeChoiceValidator)
                .attach(this.businessSegmentChoiceValidator)
                .attach(this.placingOfOrderViaChoiceValidator)
                .attach(this.contractDateFieldValidator)
                .attach(this.contractTimeFieldValidator);

        initContent();
    }

    private void attachMessageSinks(Widget target, ValidatorHost host, VisibilityMessageSink tabMessagesSink, StyleMessageSink styleMessageSink) {
        host.attach(tabMessagesSink).attach(styleMessageSink).attach(new TooltipMessageSink(target));
    }

    private void initContent() {
        this.validationEventJoint.addValidationHandler(new ValidationHandler() {
            @Override
            public void onValidation(ValidationEvent event) {
                OrderViewBHLKGS.this.presenter.onJointValidationEvent(event);
            }
        });

        this.orderActionTypeChoice.setItemMapper(new MappedListBox.ItemMapper<OrderActionType>() {
            @Override
            public String getLabel(OrderActionType item) {
                return OeRenderers.ORDER_ACTION_TYPE_RENDERER.render(item);
            }

            @Override
            public String getValue(OrderActionType item) {
                if (item == null) {
                    return "null"; //$NON-NLS$
                }
                return item.name();
            }
        });

        this.accountChoice.setItemMapper(new MappedListBox.ItemMapper<AccountData>() {
            @Override
            public String getLabel(AccountData item) {
                return OeRenderers.ACCOUNT_LIST_ITEM_RENDERER.render(item);
            }

            @Override
            public String getValue(AccountData item) {
                if (item == null) {
                    return "null"; //$NON-NLS$
                }
                return item.getId();
            }
        });

        this.selectInstrumentFromDepotChoice.setClickOpensMenu(true);
        this.selectInstrumentFromDepotChoice.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> menuItemSelectionEvent) {
                final MenuItem item = menuItemSelectionEvent.getSelectedItem();
                final OrderSecurityInfo data = (OrderSecurityInfo)item.getData(DATA_KEY);
                SelectionEvent.fire(OrderViewBHLKGS.this.selectInstrumentFromDepotChoiceDelegate, data);
                OrderViewBHLKGS.this.selectInstrumentFromDepotMenu.setSelectedItem(null);
            }
        });

        this.depotChoice.setItemMapper(new DepotItemMapper());

        this.limitCurrencyChoice.setItemMapper(new MappedListBox.ItemMapper<CurrencyAnnotated>() {
            @Override
            public String getLabel(CurrencyAnnotated item) {
                if(item != null) return item.getCurrency().getKuerzel();
                return "null"; //$NON-NLS$
            }

            @Override
            public String getValue(CurrencyAnnotated item) {
                if(item != null) return item.getCurrency().getId();
                return "null"; //$NON-NLS$
            }
        });

        final MappedListBox.ItemMapper<OrderExchangeInfo> orderExchangeInfoMapper = new MappedListBox.ItemMapper<OrderExchangeInfo>() {
            @Override
            public String getLabel(OrderExchangeInfo item) {
                if(item == null) return "null"; //$NON-NLS$
                if(item.isUseExtern()) {
                    return item.getExternName();
                }
                return item.getName();
            }

            @Override
            public String getValue(OrderExchangeInfo item) {
                if(item == null) return "null"; //$NON-NLS$
                //by definition, we do not use the extern ref, even if isUseExtern evaluates to true.
                return item.getID();
            }
        };

        final MappedListBox.ItemMapper<OrderStock> orderStockMapper = new MappedListBox.ItemMapper<OrderStock>() {
            @Override
            public String getLabel(OrderStock item) {
                if(item == null) {
                    return "null"; //$NON-NLS$
                }
                return OeRenderers.BHLKGS_DEPOSITORY_LIST_ITEM_RENDERER.render(item);
            }

            @Override
            public String getValue(OrderStock item) {
                if(item == null) {
                    return "null"; //$NON-NLS$
                }
                return item.getStockID();
            }
        };

        final MappedListBox.ItemMapper<ProvisionType> provisionTypeMapper = new MappedListBox.ItemMapper<ProvisionType>() {
            @Override
            public String getLabel(ProvisionType item) {
                if(item == null) {
                    return "null"; //$NON-NLS$
                }
                return OeRenderers.BHLKGS_PROVISION_TYPE_RENDERER.render(item);
            }

            @Override
            public String getValue(ProvisionType item) {
                if(item == null) {
                    return "null"; //$NON-NLS$
                }

                return item.name();
            }
        };

        this.exchangeChoiceDomestic.setItemMapper(orderExchangeInfoMapper);
        this.exchangeChoiceForeign.setItemMapper(orderExchangeInfoMapper);
        this.exchangeChoiceOther.setItemMapper(orderExchangeInfoMapper);

        final MappedListBox.ItemMapper<TextWithKey> textWithKeyItemMapper = new TextWithKeyItemMapper();

        this.tradingIndicatorChoice.setItemMapper(textWithKeyItemMapper);

        this.depositoriesChoice.setItemMapper(orderStockMapper);

        this.limitClauseChoice.setItemMapper(textWithKeyItemMapper);
        this.validityDateButton.setMinDate(new MmJsDate().atMidnight());

        this.ordererChoice.setItemMapper(textWithKeyItemMapper);

        this.selectOrdererChoice.setClickOpensMenu(true);
        this.selectOrdererChoice.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> menuItemSelectionEvent) {
                final MenuItem item = menuItemSelectionEvent.getSelectedItem();
                final AuthorizedRepresentative data = (AuthorizedRepresentative)item.getData(DATA_KEY);
                SelectionEvent.fire(OrderViewBHLKGS.this.selectOrdererChoiceDelegate, data);
                OrderViewBHLKGS.this.selectAuthorizedRepresentativeMenu.setSelectedItem(null);
            }
        });

        this.minutesOfTheConsultationTypeChoice.setItemMapper(textWithKeyItemMapper);
        this.placingOfOrderViaChoice.setItemMapper(textWithKeyItemMapper);
        this.settlementTypeChoice.setItemMapper(textWithKeyItemMapper);
        this.businessSegmentChoice.setItemMapper(textWithKeyItemMapper);

        this.commissionChoice.setItemMapper(provisionTypeMapper);

        this.cannedTextForBillingReceiptChoice1.setItemMapper(textWithKeyItemMapper);
        this.cannedTextForBillingReceiptChoice2.setItemMapper(textWithKeyItemMapper);
        this.cannedTextForBillingReceiptChoice3.setItemMapper(textWithKeyItemMapper);
        this.cannedTextForOrderConfirmationChoice.setItemMapper(textWithKeyItemMapper);
    }

    @UiHandler("orderActionTypeChoice")
    void onOrderActionChanged(ChangeEvent changeEvent) {
        this.presenter.onOrderActionTypeChanged(changeEvent);
    }

    @UiHandler("depotChoice")
    void onDepotChoiceValueChanged(ChangeEvent event) {
        this.presenter.onDepotChanged(event);
    }

    @UiHandler("accountChoice")
    void onAccountChanged(ChangeEvent changeEvent) {
        this.presenter.onAccountChanged(changeEvent);
    }

    @UiHandler("instrumentField")
    public void onInstrumentFieldKeyPress(KeyPressEvent event) {
        if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
            OrderViewBHLKGS.this.instrumentField.setFocus(false);
        }
    }

    @UiHandler("instrumentField")
    void onSymbolSearchTextBoxValueChanged(ValueChangeEvent<String> changeEvent) {
        this.presenter.onSymbolSearchTextBoxValueChanged(changeEvent);
    }

    @UiHandler("selectInstrumentButton")
    void onSymbolSearchButtonClicked(ClickEvent changeEvent) {
        this.presenter.onSymbolSearchButtonClicked(changeEvent);
    }

    @UiHandler("selectInstrumentFromDepotChoiceDelegate")
    void onSelectSymbolFromDepotSelected(SelectionEvent<OrderSecurityInfo> securityDataSelectionEvent) {
        this.presenter.onSelectSymbolFromDepotSelected(securityDataSelectionEvent);
    }

    @UiHandler("selectIpoInstrumentButton")
    void onIpoSymbolSearchButtonClicked(ClickEvent clickEvent) {
        this.presenter.onIpoSearchButtonClicked(clickEvent);
    }

    @UiHandler("showArbitrageButton")
    void onShowArbitrageButtonClicked(ClickEvent changeEvent) {
        this.presenter.onShowArbitrageButtonClicked(changeEvent);
    }

    @UiHandler("exchangeChoiceDomestic")
    void onExchangeDomesticChanged(ChangeEvent changeEvent) {
        this.presenter.onExchangesDomesticChanged(changeEvent);
    }

    @UiHandler("exchangeChoiceForeign")
    void onExchangeForeignChanged(ChangeEvent changeEvent) {
        this.presenter.onExchangesForeignChanged(changeEvent);
    }

    @UiHandler("exchangeChoiceOther")
    void onExchangeOtherChanged(ChangeEvent changeEvent) {
        this.presenter.onExchangesOtherChanged(changeEvent);
    }

    @UiHandler("validityRadioNone")
    void onValidityRadioNoneClicked(ClickEvent event) {
        this.setValidUntilDate(null);
        this.setValidUntil(ValidUntil.DEFAULT);
    }

    @UiHandler("validityRadioToday")
    void onValidityRadioTodayClicked(ClickEvent event) {
        this.setValidUntilDate(null);
        this.setValidUntil(ValidUntil.TODAY);
    }

    @UiHandler("validityRadioUltimo")
    void onValidityRadioUltimoClicked(ClickEvent event) {
        this.setValidUntilDate(null);
        this.setValidUntil(ValidUntil.ULTIMO);
    }

    @UiHandler("validityRadioDate")
    void onValidityRadioDateClicked(ClickEvent event) {
        this.setValidUntil(ValidUntil.DATE);
        this.setValidUntilDate(new MmJsDate());
    }

    @UiHandler("validityDateButton")
    void onValidityDateButtonClicked(ClickEvent event) {
        this.validityRadioDate.setEnabled(true);
        this.setValidUntil(ValidUntil.DATE);
        // set to current date, necessary in cases someone leaves the date box without selecting a date, cf. R-77846
        if(this.getValidUntilDate() == null) {
            this.setValidUntilDate(new MmJsDate());
        }
    }

    @UiHandler("limitOrStopLimitField")
    void onLimitFieldValueChanged(ValueChangeEvent<String> event) {
        this.presenter.onLimitChanged(event);
    }

    @UiHandler("limitCurrencyChoice")
    void onLimitCurrencyChoiceChanged(ChangeEvent event) {
        this.presenter.onLimitCurrenciesChanged(event);
    }

    @UiHandler("limitClauseChoice")
    void onLimitClauseChoiceChanged(ChangeEvent event) {
        this.presenter.onLimitClausesChanged(event);
    }

    @UiHandler("selectOrdererChoiceDelegate")
    void onSelectAuthorizedRepresentativeSelected(SelectionEvent<AuthorizedRepresentative> selectionEvent) {
        this.presenter.onSelectAuthorizedRepresentativeSelected(selectionEvent);
    }

    @UiHandler("ordererChoice")
    void onOrdererChoiceChanged(ChangeEvent event) {
        this.presenter.onOrdererChanged(event);
    }

    @UiHandler("businessSegmentChoice")
    void onBusinessSegmentChoiceChanged(ChangeEvent event) {
        this.presenter.onBusinessSegmentChanged(event);
    }

    @UiHandler("commissionChoice")
    void onCommissionChoiceChanged(ChangeEvent event) {
        this.presenter.onCommissionChanged(event);
    }

    @Override
    public void setOrderActionTypesSelectedItem(OrderActionType action) {
        this.orderActionTypeChoice.setSelectedItem(action);
    }

    @Override
    public OrderActionType getOrderActionTypesSelectedItem() {
        return this.orderActionTypeChoice.getSelectedItem();
    }

    @Override
    public void setOrderActionTypesEnabled(boolean enabled) {
        this.orderActionTypeChoice.setEnabled(enabled);
    }

    @Override
    public void setOrderActionTypesVisible(boolean visible) {
        this.orderActionTypeChoice.setVisible(visible);
    }

    @Override
    public void setOrderActionTypeName(OrderActionType type) {
        this.orderActionTypeField.setText(OeRenderers.ORDER_ACTION_TYPE_RENDERER.render(type));
    }

    @Override
    public void setOrderActionTypeNameVisible(boolean visible) {
        this.orderActionTypeField.setVisible(true);
    }

    @Override
    public void setLoadingIndicatorVisible(boolean visible) {
        if(visible) {
            this.loadingIndicator.showGlass();
        }
        else {
            this.loadingIndicator.hideGlass();
        }
    }

    @Override
    public void setOrderActionTypes(List<OrderActionType> actions) {
        this.orderActionTypeChoice.setItems(actions);
    }

    @Override
    public void setPresenter(PresenterBHLKGS presenter) {
        this.presenter = presenter;
    }

    @Override
    public PresenterBHLKGS getPresenter() {
        return this.presenter;
    }

    @Override
    public Widget getOrderView() {
        return this;
    }

    @Override
    public void setInstrumentName(String name) {
        this.instrumentField.setText(name);
        this.instrumentNameField.setText(name);
        this.ipoInstrumentLabel.setText(name);
        this.ipoInstrumentLabel.setTitle(name);
    }

    @Override
    public void setInstrumentType(ShellMMType type) {
        this.instrumentTypeField.setText(PmRenderers.SHELL_MM_TYPE.render(type));
    }

    @Override
    public void setIsin(String isin) {
        this.isinField.setText(isin);
    }

    @Override
    public void setWkn(String wkn) {
        this.wknField.setText(wkn);
    }

    @Override
    public void setSelectInstrumentPanelVisible(boolean visible) {
        this.selectInstrumentPanel.setVisible(visible);
    }

    @Override
    public void setInstrumentNamePanelVisible(boolean visible) {
        this.instrumentNamePanel.setVisible(visible);
    }

    @Override
    public void setDepotNo(String depotNo) {
        this.depotNumberField.setText(depotNo);
    }

    @Override
    public void setAccountNo(String accountNo) {
        this.accountNumberField.setText(Renderer.STRING_DOUBLE_DASH.render(accountNo));
    }

    @Override
    public void setAccountBalance(String accountBalance, OrderCurrency currency) {
        final String VWD_SUFFIX = " (vwd)"; //$NON-NLS$
        final String accountBalanceText;

        if(StringUtil.hasText(accountBalance) && currency != null && StringUtil.hasText(currency.getKuerzel())) {
            final PriceWithCurrency pwc = new PriceWithCurrency(accountBalance, currency.getKuerzel());
            accountBalanceText = Renderer.PRICE23_WITH_CURRENCY.render(pwc) + VWD_SUFFIX;
        }
        else {
            accountBalanceText = Renderer.STRING_DOUBLE_DASH.render(null) + VWD_SUFFIX;
        }

        this.accountBalanceField.setText(accountBalanceText);
    }

    @Override
    public AccountData getAccountsSelecedItem() {
        return this.accountChoice.getSelectedItem();
    }

    @Override
    public void setAccountsSelectedItem(AccountData accountData) {
        this.accountChoice.setSelectedItem(accountData);
    }

    @Override
    public void setAccounts(List<AccountData> accounts) {
        this.accountChoice.setItems(accounts);
    }

    @Override
    public void setInvestorNo(String investorNo) {
        this.investorNumberField.setText(Renderer.STRING_DOUBLE_DASH.render(investorNo));
    }

    @Override
    public void setDepotName(String depotName) {
        this.depotNameField.setText(Renderer.STRING_DOUBLE_DASH.render(depotName));
    }

    @Override
    public void setDepotNameVisible(boolean visible) {
        this.depotNameField.setVisible(visible);
    }

    @Override
    public void setDepotsVisible(boolean visible) {
        this.depotChoice.setVisible(visible);
    }

    @Override
    public void setSymbolsOfDepot(List<OrderSecurityInfo> securityDataList) {
        this.selectInstrumentFromDepotMenu.removeAll();

        if(securityDataList == null) return;

        for(OrderSecurityInfo item : securityDataList) {
            final StringBuilder sb = new StringBuilder();
            sb.append(item.getBezeichnung()).append(" (").append(item.getISIN()).append(")"); //$NON-NLS$
            if(StringUtil.hasText(item.getStock())) {
                sb.append(", ").append(OeRenderers.PRICE_NOT_EMPTY_OR_ZERO_RENDERER.render(item.getStock())) //$NON-NLS$
                        .append(" ").append(I18n.I.orderEntryAmountNominal());
            }

            MenuItem menuItem = new MenuItem(sb.toString());
            menuItem.setEnabled(true);
            menuItem.addStyleName(VIEW_STYLE);
            menuItem.setData(DATA_KEY, item);
            this.selectInstrumentFromDepotMenu.add(menuItem);
        }
    }

    @Override
    public void reset() {
        this.orderActionTypeChoice.clear();

        this.investorNumberField.setText(null);
        this.depotNumberField.setText(null);

        this.accountChoice.clear();
        this.accountNumberField.setText(null);
        this.accountBalanceField.setText(null);

        this.instrumentField.setText(null);
        this.instrumentTypeField.setText(null);
        this.selectInstrumentFromDepotMenu.removeAll();
        this.isinField.setText(null);
        this.wknField.setText(null);
    }

    @Override
    public void setExchangesDomestic(List<OrderExchangeInfo> exchanges) {
        this.exchangeChoiceDomestic.setItems(exchanges);
    }

    @Override
    public void setExchangesForeign(List<OrderExchangeInfo> exchanges) {
        this.exchangeChoiceForeign.setItems(exchanges);
    }

    @Override
    public void setExchangesOther(List<OrderExchangeInfo> exchanges) {
        this.exchangeChoiceOther.setItems(exchanges);
    }

    @Override
    public void setExchangesDomesticSelectedItem(OrderExchangeInfo selectedItem) {
        this.exchangeChoiceDomestic.setSelectedItem(selectedItem);
    }

    @Override
    public void setExchangesForeignSelectedItem(OrderExchangeInfo selectedItem) {
        this.exchangeChoiceForeign.setSelectedItem(selectedItem);
    }

    @Override
    public void setExchangesOtherSelectedItem(OrderExchangeInfo selectedItem) {
        this.exchangeChoiceOther.setSelectedItem(selectedItem);
    }

    @Override
    public void setExchangesDomesticEnabled(boolean enabled) {
        this.exchangeChoiceDomestic.setEnabled(enabled);
        if(enabled) {
            this.exchangeChoiceDomesticValidator.validate();
        }
        else {
            this.exchangeChoiceDomesticValidator.clear();
        }
    }

    @Override
    public void setExchangesDomesticValidatorEnabled(boolean enabled) {
        this.exchangeChoiceDomesticValidator.setEnabled(enabled);
        if(enabled) {
            this.exchangeChoiceDomesticValidator.validate();
        }
        else {
            this.exchangeChoiceDomesticValidator.clear();
        }
    }

    @Override
    public void setExchangesForeignEnabled(boolean enabled) {
        this.exchangeChoiceForeign.setEnabled(enabled);
        if(enabled) {
            this.exchangeChoiceForeignValidator.validate();
        }
        else {
            this.exchangeChoiceForeignValidator.clear();
        }
    }

    @Override
    public void setExchangesForeignValidatorEnabled(boolean enabled) {
        this.exchangeChoiceForeignValidator.setEnabled(enabled);
        if(enabled) {
            this.exchangeChoiceForeignValidator.validate();
        }
        else {
            this.exchangeChoiceForeignValidator.clear();
        }
    }

    @Override
    public void setExchangesOtherEnabled(boolean enabled) {
        this.exchangeChoiceOther.setEnabled(enabled);
        if(enabled) {
            this.exchangeChoiceOtherValidator.validate();
        }
        else {
            this.exchangeChoiceOtherValidator.clear();
        }
    }

    @Override
    public void setExchangesOtherValidatorEnabled(boolean enabled) {
        this.exchangeChoiceOtherValidator.setEnabled(enabled);
        if(enabled) {
            this.exchangeChoiceOtherValidator.validate();
        }
        else {
            this.exchangeChoiceOtherValidator.clear();
        }
    }

    @Override
    public OrderExchangeInfo getExchangesDomesticSelectedItem() {
        return this.exchangeChoiceDomestic.getSelectedItem();
    }

    @Override
    public OrderExchangeInfo getExchangesForeignSelectedItem() {
        return this.exchangeChoiceForeign.getSelectedItem();
    }

    @Override
    public OrderExchangeInfo getExchangesOtherSelectedItem() {
        return this.exchangeChoiceOther.getSelectedItem();
    }

    @Override
    public void setExchangeAccordingToCustomer(boolean checked) {
        this.exchangeAccordingToCustomerCheck.setValue(checked);
    }

    @Override
    public boolean isExchangeAccordingToCustomer() {
        return this.exchangeAccordingToCustomerCheck.getValue();
    }

    @Override
    public void setAmountNominal(String value) {
        this.amountNominalField.setValue(value);
    }

    @Override
    public String getAmountNominal() {
        return this.amountNominalField.getValue();
    }

    @Override
    public void setAmountNominalEnabled(boolean enabled) {
        this.amountNominalField.setEnabled(enabled);
    }

    @Override
    public void setAmountNominalCurrencyOrUnit(String currencyOrUnit) {
        this.amountNominalCurrencyOrUnit.setText(currencyOrUnit);
    }

    @Override
    public void setAmountNominalMaxValue(double maxValue) {
        this.amountNominalDoubleMaxValueValidator.setMaxValue(maxValue);
        this.amountNominalFieldMaxValueValidator.setEnabled(true);
        this.amountNominalFieldMaxValueValidator.validate();
    }

    @Override
    public void resetAmountNominalMaxValue() {
        this.amountNominalFieldMaxValueValidator.setEnabled(false);
    }

    @Override
    public void setDepositories(List<OrderStock> depositories) {
        this.depositoriesChoice.setItems(depositories);
    }

    @Override
    public void setDepositoriesSelectedItem(OrderStock selectedItem) {
        this.depositoriesChoice.setSelectedItem(selectedItem);
    }

    @Override
    public OrderStock getDepositoriesSelectedItem() {
        return this.depositoriesChoice.getSelectedItem();
    }

    @Override
    public void setDepositoriesEnabled(boolean enabled) {
        this.depositoriesChoice.setEnabled(enabled);
    }

    @Override
    public void setTradingIndicators(List<TextWithKey> tradingIndicators) {
        this.tradingIndicatorChoice.setItems(tradingIndicators);
    }

    @Override
    public void setTradingIndicatorsSelectedItem(TextWithKey selectedItem) {
        this.tradingIndicatorChoice.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getTradingIndicatorsSelectedItem() {
        return this.tradingIndicatorChoice.getSelectedItem();
    }

    @Override
    public void setTradingIndicatorsEnabled(boolean enabled) {
        this.tradingIndicatorChoice.setEnabled(enabled);
    }

    @Override
    public void setLimitOrStopLimit(String limit) {
        this.limitOrStopLimitField.setValue(limit);
    }

    @Override
    public String getLimitOrStopLimit() {
        return this.limitOrStopLimitField.getValue();
    }

    @Override
    public void setLimitOrStopLimitEnabled(boolean enabled) {
        this.limitOrStopLimitField.setEnabled(enabled);
    }

    @Override
    public void setLimitOrStopLimitNotEmptyValidatorEnabled(boolean enabled) {
        final boolean wasEnabled = this.limitOrStopLimitFieldValidatorNotEmpty.isEnabled();

        this.limitOrStopLimitFieldValidatorNotEmpty.setEnabled(enabled);

        if (!wasEnabled && enabled) {
            this.limitOrStopLimitFieldValidatorNotEmpty.validate();
        }

        this.validationEventJoint.checkAllValid();
    }

    @Override
    public void setLimitTrailingAmountOrPercent(String value) {
        this.limitTrailingAmountField.setValue(value);
    }

    @Override
    public String getLimitTrailingAmountOrPercent() {
        return this.limitTrailingAmountField.getValue();
    }

    @Override
    public void setLimitTrailingAmountOrPercentEnabled(boolean enabled) {
        this.limitTrailingAmountField.setEnabled(enabled);
    }

    @Override
    public void setLimitPeakSizeQuantity(String value) {
        this.limitPeakSizeQuantityField.setValue(value);
    }

    @Override
    public String getLimitPeakSizeQuantity() {
        return this.limitPeakSizeQuantityField.getValue();
    }

    @Override
    public void setLimitPeakSizeQuantityEnabled(boolean enabled) {
        this.limitPeakSizeQuantityField.setEnabled(enabled);
    }

    @Override
    public void setSettlementTypes(List<TextWithKey> types) {
        this.settlementTypeChoice.setItems(types);
    }

    @Override
    public void setSettlementTypesSelectedItem(TextWithKey selectedItem) {
        this.settlementTypeChoice.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getSettlementTypesSelectedItem() {
        return this.settlementTypeChoice.getSelectedItem();
    }

    @Override
    public void setLimitCurrencies(List<CurrencyAnnotated> currencyAnnotateds) {
        this.limitCurrencyChoice.setItems(currencyAnnotateds);
    }

    @Override
    public void setLimitCurrenciesSelectedItem(CurrencyAnnotated selectedItem) {
        this.limitCurrencyChoice.setSelectedItem(selectedItem);
    }

    @Override
    public CurrencyAnnotated getLimitCurrenciesSelectedItem() {
        return this.limitCurrencyChoice.getSelectedItem();
    }

    @Override
    public void setLimitCurrenciesEnabled(boolean enabled) {
        this.limitCurrencyChoice.setEnabled(enabled);
    }

    @Override
    public void setLimitCurrenciesValidatorEnabled(boolean enabled) {
        this.limitCurrencyChoiceValidator.setEnabled(enabled);
        if(enabled) {
            this.limitCurrencyChoiceValidator.validate();
        }
    }

    @Override
    public void setLimitClauses(List<TextWithKey> limitClauses) {
        this.limitClauseChoice.setItems(limitClauses);
    }

    @Override
    public void setLimitClausesSelectedItem(TextWithKey selectedItem) {
        this.limitClauseChoice.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getLimitClausesSelectedItem() {
        return this.limitClauseChoice.getSelectedItem();
    }

    @Override
    public void setLimitClausesEnabled(boolean enable) {
        this.limitClauseChoice.setEnabled(enable);
    }

    @Override
    public void setLimitAfterStopLimit(String stopLimit) {
        this.limitAfterStopLimitField.setValue(stopLimit);
    }

    @Override
    public String getLimitAfterStopLimit() {
        return this.limitAfterStopLimitField.getValue();
    }

    @Override
    public void setLimitAfterStopLimitEnabled(boolean enabled) {
        final boolean wasEnabled = this.limitAfterStopLimitField.isEnabled();

        this.limitAfterStopLimitField.setEnabled(enabled);

        if(!wasEnabled && enabled) {
            this.limitAfterStopLimitFieldValidator.validate();
        }
    }

    @Override
    public void setLimitFee(boolean limitFee) {
        this.limitFeeCheck.setValue(limitFee);
    }

    @Override
    public boolean isLimitFee() {
        final Boolean value = this.limitFeeCheck.getValue();
        return value != null && value;
    }

    @Override
    public ValidUntil getValidUntil() {
        if(this.validityRadioToday.getValue()) {
            return ValidUntil.TODAY;
        }
        else if(this.validityRadioDate.getValue()) {
            return ValidUntil.DATE;
        }
        else if(this.validityRadioUltimo.getValue()) {
            return ValidUntil.ULTIMO;
        }
        return ValidUntil.DEFAULT;
    }

    @Override
    public void setValidUntil(ValidUntil validUntil) {
        Boolean none = Boolean.FALSE;
        Boolean today = Boolean.FALSE;
        Boolean ultimo = Boolean.FALSE;
        Boolean date = Boolean.FALSE;

        switch(validUntil) {
            case TODAY:
                today = Boolean.TRUE;
                break;
            case DATE:
                date = Boolean.TRUE;
                break;
            case ULTIMO:
                ultimo = Boolean.TRUE;
                break;
            case DEFAULT:
            default:
                none = Boolean.TRUE;
                break;
        }

        this.validityRadioNone.setValue(none);
        this.validityRadioToday.setValue(today);
        this.validityRadioUltimo.setValue(ultimo);
        this.validityRadioDate.setValue(date);
    }

    @Override
    public void setValidUntilDate(MmJsDate mmJsDate) {
        this.validityDateButton.setDate(mmJsDate);
    }

    @Override
    public MmJsDate getValidUntilDate() {
        return this.validityDateButton.getValue();
    }

    @Override
    public void setAuthorizedRepresentatives(List<AuthorizedRepresentative> representatives) {
        Firebug.debug("<OrderViewBHLKGS.setAuthorizedRepresentatives>");
        this.selectAuthorizedRepresentativeMenu.removeAll();

        if(representatives == null) return;
        Firebug.debug("<OrderViewBHLKGS.setAuthorizedRepresentatives> size=" + representatives.size());

        for(AuthorizedRepresentative item : representatives) {
            Firebug.debug("<OrderViewBHLKGS.setAuthorizedRepresentatives> adding=" + item.getName());

            MenuItem menuItem = new MenuItem(item.getName() + " (" + item.getAuthorization() +")"); //$NON-NLS$
            menuItem.setEnabled(true);
            menuItem.addStyleName(VIEW_STYLE);
            menuItem.setData(DATA_KEY, item);
            this.selectAuthorizedRepresentativeMenu.add(menuItem);
        }
    }

    @Override
    public void setAuthorizedRepresentativesEnabled(boolean enabled) {
        this.selectOrdererChoice.setEnabled(enabled);
    }

    @Override
    public void setOrderers(List<TextWithKey> orderers) {
        this.ordererChoice.setItems(orderers);
    }

    @Override
    public void setOrderersSelectedItem(TextWithKey selectedItem) {
        this.ordererChoice.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getOrderersSelectedItem() {
        return this.ordererChoice.getSelectedItem();
    }

    @Override
    public void setOrderersEnabled(boolean enabled) {
        this.ordererChoice.setEnabled(enabled);
    }

    @Override
    public void setOrdererIdentifier(String ordererIdentifier) {
        this.ordererIdentifierField.setText(ordererIdentifier);
    }

    @Override
    public String getOrdererIdentifier() {
        return this.ordererIdentifierField.getText();
    }

    @Override
    public void setOrdererIdentifierEnabled(boolean enabled) {
        this.ordererIdentifierField.setEnabled(enabled);
    }

    @Override
    public void setOrdererCustomerNumber(String ordererCustomerNo) {
        this.ordererCustomerNumberField.setText(ordererCustomerNo);
    }

    @Override
    public String getOrdererCustomerNumber() {
        return this.ordererCustomerNumberField.getText();
    }

    @Override
    public void setOrdererCustomerNumberEnabled(boolean enabled) {
        this.ordererCustomerNumberField.setEnabled(enabled);
    }

    @Override
    public void setPlacingOfOrderVias(List<TextWithKey> vias) {
        this.placingOfOrderViaChoice.setItems(vias);
    }

    @Override
    public void setPlacingOfOrderViasSelectedItem(TextWithKey item) {
        this.placingOfOrderViaChoice.setSelectedItem(item);
    }

    @Override
    public TextWithKey getPlacingOfOrderViasSelectedItem() {
        return this.placingOfOrderViaChoice.getSelectedItem();
    }

    @Override
    public void setPlacingOfOrderViasEnabled(boolean enabled) {
        this.placingOfOrderViaChoice.setEnabled(enabled);
    }

    @Override
    public void setCommission(List<ProvisionType> commission) {
        this.commissionChoice.setItems(commission);
    }

    @Override
    public void setCommissionSelectedItem(ProvisionType selectedItem) {
         this.commissionChoice.setSelectedItem(selectedItem);
    }

    @Override
    public ProvisionType getCommissionSelectedItem() {
        return this.commissionChoice.getSelectedItem();
    }

    @Override
    public void setDifferentCommission(String differentCommission) {
        this.differentCommissionField.setValue(differentCommission);
    }

    @Override
    public String getDifferentCommission() {
        return this.differentCommissionField.getValue();
    }

    @Override
    public void setDifferentCommissionEnabled(boolean enabled) {
        this.differentCommissionField.setEnabled(enabled);
    }

    @Override
    public void setMinutesOfTheConsultationTypes(List<TextWithKey> types) {
        this.minutesOfTheConsultationTypeChoice.setItems(types);
    }

    @Override
    public void setMinutesOfTheConsultationTypesSelectedItem(TextWithKey selectedItem) {
        this.minutesOfTheConsultationTypeChoice.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getMinutesOfTheConsultationTypesSelectedItem() {
        return this.minutesOfTheConsultationTypeChoice.getSelectedItem();
    }

    @Override
    public void setMinutesOfTheConsultationTypesEnabled(boolean enabled) {
        this.minutesOfTheConsultationTypeChoice.setEnabled(enabled);
    }

    @Override
    public void setMinutesOfTheConsultationNumber(String number) {
        this.minutesOfTheConsultationNumberField.setText(number);
    }

    @Override
    public String getMinutesOfTheConsultationNumber() {
        return this.minutesOfTheConsultationNumberField.getText();
    }

    @Override
    public void setMinutesOfTheConsultationNumberEnabled(boolean enabled) {
        this.minutesOfTheConsultationNumberField.setEnabled(enabled);
    }

    @Override
    public void setMinutesOfTheConsultationDate(MmJsDate date) {
        this.minutesOfTheConsultationDateBox.setDate(date);
    }

    @Override
    public MmJsDate getMinutesOfTheConsultationDate() {
        return this.minutesOfTheConsultationDateBox.getDate();
    }

    @Override
    public void setMinutesOfTheConsultationDateEnabled(boolean enabled) {
        this.minutesOfTheConsultationDateField.setEnabled(enabled);
    }

    @Override
    public void setBusinessSegments(List<TextWithKey> businessSegments) {
        this.businessSegmentChoice.setItems(businessSegments);
    }

    @Override
    public void setBusinessSegmentsSelectedItem(TextWithKey item) {
        this.businessSegmentChoice.setSelectedItem(item);
    }

    @Override
    public TextWithKey getBusinessSegmentsSelectedItem() {
        return this.businessSegmentChoice.getSelectedItem();
    }

    @Override
    public void setBusinessSegmentsEnabled(boolean enabled) {
        this.businessSegmentChoice.setEnabled(enabled);
    }

    @Override
    public void setExternalTypist(String typist) {
        this.externalTypist.setValue(typist);
    }

    @Override
    public String getExternalTypist() {
        return this.externalTypist.getValue();
    }

    @Override
    public void setContractDateTime(MmJsDate date) {
        this.contractDateBox.setDate(date);
        if(date == null) {
            this.contractTimeField.setValue(null);
        }
        else {
            this.contractTimeField.setValue(Formatter.formatTimeHhmm(date.getJavaDate()));
        }
    }

    @Override
    public MmJsDate getContractDateTime() {
        Firebug.debug("<OrderViewBHLKGS.getContractDateTime>");
        final MmJsDate date = this.contractDateBox.getDate().atMidnight();
        final String timeStr = this.contractTimeField.getValue();

        if(StringUtil.hasText(timeStr)) {
            final MmJsDate time = new MmJsDate(Formatter.FORMAT_TIME_HHMM.parse(timeStr));
            date.setHours(time.getHours());
            date.setMinutes(time.getMinutes());
            date.setSeconds(0);
        }

        Firebug.debug("<OrderViewBHLKGS.getContractDateTime> return=" + date);

        return date;
    }

    @Override
    public void setCannedTextForBillingReceipts1(List<TextWithKey> cannedTexts) {
        this.cannedTextForBillingReceiptChoice1.setItems(cannedTexts);
    }

    @Override
    public void setCannedTextForBillingReceipts1SelectedItem(TextWithKey selectedItem) {
        this.cannedTextForBillingReceiptChoice1.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getCannedTextForBillingReceipts1SelectedItem() {
        return this.cannedTextForBillingReceiptChoice1.getSelectedItem();
    }

    @Override
    public void setCannedTextForBillingReceipts2(List<TextWithKey> cannedTexts) {
        this.cannedTextForBillingReceiptChoice2.setItems(cannedTexts);
    }

    @Override
    public void setCannedTextForBillingReceipts2SelectedItem(TextWithKey selectedItem) {
        this.cannedTextForBillingReceiptChoice2.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getCannedTextForBillingReceipts2SelectedItem() {
        return this.cannedTextForBillingReceiptChoice2.getSelectedItem();
    }

    @Override
    public void setCannedTextForBillingReceipts3(List<TextWithKey> cannedTexts) {
        this.cannedTextForBillingReceiptChoice3.setItems(cannedTexts);
    }

    @Override
    public void setCannedTextForBillingReceipts3SelectedItem(TextWithKey selectedItem) {
        this.cannedTextForBillingReceiptChoice3.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getCannedTextForBillingReceipts3SelectedItem() {
        return this.cannedTextForBillingReceiptChoice3.getSelectedItem();
    }

    @Override
    public void setCannedTextForOrderConfirmations(List<TextWithKey> cannedTexts) {
        this.cannedTextForOrderConfirmationChoice.setItems(cannedTexts);
    }

    @Override
    public void setCannedTextForOrderConfirmationsSelectedItem(TextWithKey selectedItem) {
        this.cannedTextForOrderConfirmationChoice.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getCannedTextForOrderConfirmationsSelectedItem() {
        return this.cannedTextForOrderConfirmationChoice.getSelectedItem();
    }

    @Override
    public void setTextForOrderReceipt1(String text) {
        this.textForOrderReceiptField1.setValue(text);
    }

    @Override
    public String getTextForOrderReceipt1() {
        return this.textForOrderReceiptField1.getValue();
    }

    @Override
    public void setTextForOrderReceipt2(String text) {
        this.textForOrderReceiptField2.setValue(text);
    }

    @Override
    public String getTextForOrderReceipt2() {
        return this.textForOrderReceiptField2.getValue();
    }

    @Override
    public void setTextForInternalUse1(String text) {
        this.textForInternalUseField1.setValue(text);
    }

    @Override
    public String getTextForInternalUse1() {
        return this.textForInternalUseField1.getValue();
    }

    @Override
    public void setTextForInternalUse2(String text) {
        this.textForInternalUseField2.setValue(text);
    }

    @Override
    public String getTextForInternalUse2() {
        return this.textForInternalUseField2.getValue();
    }

    @Override
    public ValidatorGroup getValidatorGroup() {
        return this.validatorGroup;
    }

    @Override
    public void setSelectSymbolFromDepotEnabled(boolean enabled) {
        this.selectInstrumentFromDepotChoice.setEnabled(enabled);
    }

    @Override
    public void setSelectIpoInstrumentPanelVisible(boolean visible) {
        this.selectIpoInstrumentPanel.setVisible(visible);
    }

    @Override
    public void setSelectIpoInstrumentPanelEnabled(boolean enabled) {
        this.selectIpoInstrumentButton.setEnabled(enabled);
    }

    @Override
    public void setShowArbitrageButtonEnabled(boolean enabled) {
        this.showArbitrageButton.setEnabled(enabled);
    }

    @Override
    public void setShowArbitrageButtonVisible(boolean visible) {
        this.showArbitrageButton.setVisible(visible);
    }

    @Override
    public void setInvestorName(String investorName) {
        this.investorNameField.setText(investorName);
    }

    @Override
    public void setDepotsEnabled(boolean enabled) {
        this.depotChoice.setEnabled(enabled);
    }

    @Override
    public void setDepots(List<Depot> depots) {
        this.depotChoice.setItems(depots);
    }

    @Override
    public void setDepotsSelectedItem(Depot selectedItem) {
        this.depotChoice.setSelectedItem(selectedItem);
    }

    @Override
    public Depot getDepotsSelectedItem() {
        return this.depotChoice.getSelectedItem();
    }

    @Override
    public void setInstrumentDependingValuesEnabled(boolean enabled) {
        this.amountNominalField.setEnabled(enabled);
        this.exchangeChoiceDomestic.setEnabled(enabled);
        this.exchangeChoiceForeign.setEnabled(enabled);
        this.exchangeChoiceOther.setEnabled(enabled);

        this.exchangeAccordingToCustomerCheck.setEnabled(enabled);

        this.tradingIndicatorChoice.setEnabled(enabled);

        this.limitOrStopLimitField.setEnabled(enabled);
        this.limitCurrencyChoice.setEnabled(enabled);
        this.limitClauseChoice.setEnabled(enabled);

        this.validityRadioToday.setEnabled(enabled);
        this.validityRadioUltimo.setEnabled(enabled);
        this.validityRadioDate.setEnabled(enabled);
        this.validityDateButton.setEnabled(enabled);

        this.limitFeeCheck.setEnabled(enabled);

        //widgets that are only disabled but never enabled
        if(!enabled) {
            this.depositoriesChoice.setEnabled(false);
            this.limitAfterStopLimitField.setEnabled(false);
            //is enabled depending on the item list size
            this.selectOrdererChoice.setEnabled(false);
        }
    }
}
