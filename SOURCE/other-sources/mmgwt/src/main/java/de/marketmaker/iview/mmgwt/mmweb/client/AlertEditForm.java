/*
 * AlertEditForm.java
 *
 * Created on 08.01.2009 10:03:48
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.DialogButton;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.input.CheckBox;
import de.marketmaker.itools.gwtutil.client.widgets.input.LimitedTextArea;
import de.marketmaker.itools.gwtutil.client.widgets.input.RadioGroup;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.Alert;
import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.dmxml.MSCQuotes;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.MarketSelectionButton;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;
import de.marketmaker.iview.mmgwt.mmweb.client.util.AlertUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FormValidationHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NumberUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.function.SingleConsumable;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.Caption;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.FloatingRadio;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ReadonlyField;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.TextBox;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.WidgetUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Collections;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AlertEditForm implements AsyncCallback<ResponseType> {

    public static final AlertEditForm INSTANCE = new AlertEditForm();

    private final CheckBox email = new CheckBox(false);

    private final CheckBox saveAsNew = new CheckBox(false);

    private final TextArea infoText = Styles.tryAddStyles(new LimitedTextArea(200), Styles.get().textBox());

    private final TextBox limitName = new TextBox();

    private final TextBox referencePrice = new TextBox();

    private final TextBox lowerLimit = new TextBox();

    private final TextBox upperLimitPercent = new TextBox();

    private final TextBox upperLimit = new TextBox();

    private final TextBox lowerLimitPercent = new TextBox();

    private final Label errorText = new Label("");

    private final Label instrumentNameField = new ReadonlyField("");

    private final DialogIfc dialog = Dialog.getImpl().createDialog();

    private final MarketSelectionButton marketsButton = new MarketSelectionButton(
            this::updateReferencePrice, 40,
            SessionData.isAsDesign() ? Button.RendererType.SPAN : Button.RendererType.TABLE)
            .withFormStyling();

    private IFieldSelector currentFieldSelector;

    private final SimplePanel priceFieldSelectContainer = new SimplePanel();

    private final IFieldSelector defaultFieldSelector = new RadioFieldSelector(
            AlertUtil.ADF_Bezahlt, AlertUtil.ADF_Geld, AlertUtil.ADF_Brief);

    private final IFieldSelector fondsFieldSelector = new RadioFieldSelector(
            AlertUtil.ADF_Ruecknahme);

    private final IFieldSelector lmeFieldSelector = new ComboFieldSelector(
            AlertUtil.ADF_Bezahlt, AlertUtil.ADF_Geld, AlertUtil.ADF_Brief, AlertUtil.ADF_Official_Bid,
            AlertUtil.ADF_Official_Ask, AlertUtil.ADF_Unofficial_Bid, AlertUtil.ADF_Unofficial_Ask,
            AlertUtil.ADF_Interpo_Closing, AlertUtil.ADF_Prov_Evaluation);

    private final DmxmlContext.Block<MSCPriceData> block = new DmxmlContext().addBlock("MSC_PriceData"); // $NON-NLS-0$

    private final DmxmlContext.Block<MSCQuotes> quotesBlock = new DmxmlContext().addBlock("MSC_Quotes"); // $NON-NLS-0$

    private final DialogButton btnSave;


    private Alert alert;

    private final SingleConsumable<Integer> pendingSourceFieldId = new SingleConsumable<>();

    private AlertEditForm() {
        this.quotesBlock.setParameter("disablePaging", true);  // $NON-NLS$
        this.quotesBlock.setParameter("sortBy", "marketName");  // $NON-NLS$

        final Grid g = new Grid(12, 2);
        if (!SessionData.isAsDesign()) {
            g.setStyleName("mm-alertForm-table");
        }
        else {
            Styles.tryAddStyles(g, Styles.get().generalFormStyle());
        }

        g.setCellSpacing(SessionData.isAsDesign() ? 0 : 7);
        g.setCellPadding(0);

        g.setWidget(0, 0, new Caption(I18n.I.instrument()));
        g.setWidget(0, 1, this.instrumentNameField);

        g.setWidget(1, 0, new Caption(I18n.I.market1()));
        g.setWidget(1, 1, this.marketsButton);

        g.setWidget(2, 0, new Caption(I18n.I.limitName()));
        g.setWidget(2, 1, this.limitName);
        this.limitName.setMaxLength(40);

        g.setWidget(3, 0, new Caption(I18n.I.referencePrice()));
        g.setWidget(3, 1, horizontalPanel(this.referencePrice, createRefreshButton()));

        g.setWidget(4, 0, new Caption(I18n.I.priceField()));
        g.setWidget(4, 1, this.priceFieldSelectContainer);

        g.setWidget(5, 0, new Caption(I18n.I.upperLimit()));
        g.setWidget(5, 1, this.upperLimit);

        g.setWidget(6, 0, new Caption(I18n.I.upperLimit() + " %")); // $NON-NLS$
        g.setWidget(6, 1, this.upperLimitPercent);

        g.setWidget(7, 0, new Caption(I18n.I.lowerLimit()));
        g.setWidget(7, 1, this.lowerLimit);

        g.setWidget(8, 0, new Caption(I18n.I.lowerLimit() + " %")); // $NON-NLS$
        g.setWidget(8, 1, this.lowerLimitPercent);

        g.getRowFormatter().setStyleName(9, "mm-top"); // $NON-NLS-0$
        g.setWidget(9, 0, new Caption(I18n.I.infoText()));
        g.setWidget(9, 1, this.infoText);
        this.infoText.setHeight("60px"); // $NON-NLS-0$

        g.setWidget(10, 0, new Caption(I18n.I.notification()));
        g.setWidget(10, 1, flowPanel(this.email, this.email.createSpan(SafeHtmlUtils.fromString(I18n.I.byEmail()))));

        g.getRowFormatter().setStyleName(11, "mm-top"); // $NON-NLS-0$
        g.setWidget(11, 0, new Caption(I18n.I.saveAs()));
        g.setWidget(11, 1, flowPanel(this.saveAsNew,
                this.saveAsNew.createSpan(StringUtility.toHtmlLines(I18n.I.newLimitNotReplace()))));

        this.btnSave = this.dialog.addDefaultButton(I18n.I.save(), this::onSave);
        this.dialog.addButton(I18n.I.cancel(), this::onCancel);
        if (SessionData.isAsDesign()) {
            this.dialog.withStyle("ice-dialog-no-max-width");  // $NON-NLS$
        }

        final FlowPanel view = new FlowPanel();
        Styles.tryAddStyles(view, Styles.get().generalViewStyle());
        view.add(g);
        view.add(this.errorText);

        this.dialog.withWidget(view);

        this.lowerLimitPercent.addBlurHandler(this::onPercentBlur);
        this.upperLimitPercent.addBlurHandler(this::onPercentBlur);

        if (!SessionData.isAsDesign()) {
            WidgetUtil.applyFormStyling(this.limitName, this.referencePrice, this.lowerLimit, this.lowerLimitPercent, this.upperLimit, this.upperLimitPercent, this.infoText);
        }
        applyValidation(new FormValidationHandler() {
            @Override
            protected void validateForm() {
                AlertEditForm.this.validateForm();
            }
        }, this.limitName, this.referencePrice, this.lowerLimit, this.lowerLimitPercent, this.upperLimit, this.upperLimitPercent, this.infoText);

        setCurrentFieldSelector(this.defaultFieldSelector);
    }

    private void onPercentBlur(BlurEvent blurEvent) {
        AlertEditForm.this.onLeavingPercentField((Widget) blurEvent.getSource());
    }

    private void setCurrentFieldSelector(IFieldSelector currentFieldSelector) {
        this.currentFieldSelector = currentFieldSelector;
        this.priceFieldSelectContainer.setWidget(this.currentFieldSelector);
    }

    private void onLeavingPercentField(Widget field) {
        final TextBox tf = getPercentField(field);
        checkPercentValue(tf);
        updateRelatedFieldState(tf);
    }

    private void updateRelatedFieldState(TextBox percentField) {
        final TextBox related = getRelatedField(percentField);
        final String pct = percentField.getText().trim();
        if (pct.length() == 0) {
            related.setEnabled(true);
            related.removeStyleName(Styles.get().textBoxDisabled());
        }
        else {
            related.setText(getPercentOfReferenceValue(getPrefix(percentField) + pct));
            related.setEnabled(false);
            related.addStyleName(Styles.get().textBoxDisabled());
        }
    }

    private void updateReferencePrice(String qid, int fieldId) {
        this.pendingSourceFieldId.push(fieldId);
        updateReferencePrice(qid);
    }

    private void updateReferencePrice(String qid) {
        this.block.setParameter("symbol", qid); // $NON-NLS$
        this.block.issueRequest(this);
    }

    private void updateReferencePrice() {
        this.block.issueRequest(this);
    }

    private IsWidget horizontalPanel(IsWidget... widgets) {
        if (widgets.length == 1) {
            return widgets[0];
        }
        final Grid table = new Grid(1, widgets.length);
        table.setCellPadding(0);
        table.setCellSpacing(0);
        table.setBorderWidth(0);
        for (int i = 0; i < widgets.length; i++) {
            table.setWidget(0, i, widgets[i]);
        }
        return table;
    }

    private IsWidget flowPanel(IsWidget... widgets) {
        final FlowPanel flowPanel = new FlowPanel();
        for (IsWidget widget : widgets) {
            flowPanel.add(widget.asWidget());
        }
        return flowPanel;
    }

    private void applyValidation(FormValidationHandler handler, TextBoxBase... textBoxes) {
        for (TextBoxBase textBox : textBoxes) {
            textBox.addFocusHandler(handler);
            textBox.addChangeHandler(handler);
            textBox.addKeyUpHandler(handler);
        }
    }

    private void checkPercentValue(TextBox tf) {
        final String pct = tf.getText().trim();
        if (pct.length() == 0) {
            return;
        }
        if (tf == this.lowerLimitPercent) {
            if (!isValidNumber(pct, 0, 100)) {
                tf.setText(""); // $NON-NLS-0$
                showError(I18n.I.numberBetween0And100Expected());
            }
        }
        if (tf == this.upperLimitPercent) {
            if (!isValidNumber(pct, 0, 1000)) {
                tf.setText(""); // $NON-NLS-0$
                showError(I18n.I.numberBetween0And100Expected());
            }
        }
    }

    private boolean isValidNumber(String t, double min, double max) {
        try {
            final double v = NumberUtil.toDouble(t);
            return v >= min && v <= max;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidNumber(String t) {
        try {
            NumberUtil.toDouble(t);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private TextBox getPercentField(Widget f) {
        return f == this.lowerLimitPercent ? this.lowerLimitPercent : this.upperLimitPercent;
    }

    private TextBox getRelatedField(Widget f) {
        return f == this.lowerLimitPercent ? this.lowerLimit : this.upperLimit;
    }

    private String getPrefix(Widget f) {
        return f == this.lowerLimitPercent ? "-" : ""; // $NON-NLS$
    }

    private String getPercentOfReferenceValue(String pct) {
        try {
            final double ref = NumberUtil.toDouble(this.referencePrice.getText());
            final double factor = 1d + (NumberUtil.toDouble(pct) / 100d);
            return StringBasedNumberFormat.ROUND_0_5.format(ref * factor);
        } catch (Exception e) {
            return "";
        }
    }

    private void onSave() {
        if (this.saveAsNew.isChecked()) {
            this.alert.setId(null);
        }
        this.alert.setFieldId(currentFieldSelector.getSelectedFieldId());
        this.alert.setName(getTextOrNull(this.limitName));
        this.alert.setInfoText(getTextOrNull(this.infoText));
        this.alert.setEmail(this.email.isChecked());
        this.alert.setSms(false);
        this.alert.setReferenceValue(getNumOrNull(this.referencePrice));
        this.alert.setLowerBoundary(getNumOrNull(this.lowerLimit));
        this.alert.setLowerBoundaryPercent(getNumOrNull(this.lowerLimitPercent));
        this.alert.setUpperBoundary(getNumOrNull(this.upperLimit));
        this.alert.setUpperBoundaryPercent(getNumOrNull(this.upperLimitPercent));

        AlertController.INSTANCE.update(this.alert);

        if (this.email.isChecked() && !StringUtil.hasText(AlertController.INSTANCE.getEmailAddress())) {
            Dialog.error(I18n.I.hint(), I18n.I.pleaseSpecifyEmailAddress());
        }
    }

    private void onCancel() {
        AlertController.INSTANCE.update(null);
    }

    public void show(Alert source, final QuoteWithInstrument qwi) {
        this.dialog.withTitle(source.getId() == null ? I18n.I.enterNewLimit() : I18n.I.editLimit());
        this.instrumentNameField.setText(qwi.getName());
        this.marketsButton.updateQuotesMenu(Collections.singletonList(qwi.getQuoteData()), qwi.getQuoteData());
        this.marketsButton.setEnabled(false);
        this.quotesBlock.setParameter("symbol", qwi.getIid(true)); // $NON-NLS-0$
        this.quotesBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                onQuotesBlockResult(qwi.getQuoteData());
            }
        });

        this.limitName.setText(source.getName());
        this.referencePrice.setText(getPriceValue(source.getReferenceValue()));
        this.lowerLimit.setText(getPriceValue(source.getLowerBoundary()));
        this.lowerLimitPercent.setText(getPriceValue(source.getLowerBoundaryPercent()));
        this.upperLimit.setText(getPriceValue(source.getUpperBoundary()));
        this.upperLimitPercent.setText(getPriceValue(source.getUpperBoundaryPercent()));
        this.infoText.setText(source.getInfoText());

        this.saveAsNew.setCheckedValue(source.getId() == null);
        this.saveAsNew.setEnabled(source.getId() != null);

        this.email.setCheckedValue(source.isEmail());

        updateRelatedFieldState(lowerLimitPercent);
        updateRelatedFieldState(upperLimitPercent);

        showError("");

        this.email.setEnabled(StringUtil.hasText(AlertController.INSTANCE.getEmailAddress()));

        updateReferencePrice(qwi.getId(), source.getFieldId());

        this.alert = new Alert();
        this.alert.setId(source.getId());
        this.alert.setQuotedata(source.getQuotedata());

        Scheduler.get().scheduleDeferred(() -> this.limitName.setFocus(true));

        this.dialog.show();
        validateForm();
    }

    @NonNLS
    private void trySetCurrentFieldSelector(int fieldId) {
        try {
            this.currentFieldSelector.setSelectedFieldId(fieldId);
        } catch (Exception e) {
            final String msg = "<AlertEditForm.trySetCurrentFieldSelector> fieldId:"
                    + fieldId + " currently valid field IDs:"
                    + (this.currentFieldSelector != null
                    ? this.currentFieldSelector.getFieldIds() : "currentFieldSelector is null");
            Firebug.warn(msg, e);
            DebugUtil.logToServer(msg);
        }
    }

    private void onQuotesBlockResult(QuoteData selected) {
        if (!this.quotesBlock.isResponseOk()) {
            return;
        }
        this.marketsButton.updateQuotesMenu(this.quotesBlock.getResult().getQuotedata(), selected);
        this.marketsButton.setEnabled(this.quotesBlock.getResult().getQuotedata().size() > 1);
    }

    private String getTextOrNull(TextBoxBase tf) {
        final String result = tf.getText().trim();
        return result.length() > 0 ? result : null;
    }

    private String getNumOrNull(TextBox tf) {
        final String result = getTextOrNull(tf);
        // need a string that can be converted into a BigDecimal:
        return (result == null) ? null : NumberUtil.toDoubleString(result);
    }

    private String getPriceValue(final String price) {
        if (price == null) {
            return "";
        }
        return Renderer.PRICE.render(price);
    }

    private void validateForm() {
        showError("");
        boolean valid = true;

        final String invalidStyle = Styles.get().textBoxInvalid();

        final String ul = this.upperLimit.getText();
        final String ulp = this.upperLimitPercent.getText();
        final String ll = this.lowerLimit.getText();
        final String llp = this.lowerLimitPercent.getText();
        if ("".equals(ul) && "".equals(ulp) && "".equals(ll) && "".equals(llp)) {
            Styles.tryAddStyles(this.upperLimit, invalidStyle);
            Styles.tryAddStyles(this.upperLimitPercent, invalidStyle);
            Styles.tryAddStyles(this.lowerLimit, invalidStyle);
            Styles.tryAddStyles(this.lowerLimitPercent, invalidStyle);
            valid = false;
            showError(I18n.I.specifyAtLeastOneLimit());
        }
        else {
            Styles.tryRemoveStyles(this.upperLimit, invalidStyle);
            Styles.tryRemoveStyles(this.upperLimitPercent, invalidStyle);
            Styles.tryRemoveStyles(this.lowerLimit, invalidStyle);
            Styles.tryRemoveStyles(this.lowerLimitPercent, invalidStyle);

            if (!"".equals(ul) && !isValidNumber(ul)) {
                Styles.tryAddStyles(this.upperLimit, invalidStyle);
                showError(I18n.I.wrongNumberFormat());
                valid = false;
            }
            if (!"".equals(ulp) && !isValidNumber(ulp, 0, 1000)) {
                Styles.tryAddStyles(this.upperLimitPercent, invalidStyle);
                showError(I18n.I.wrongNumberFormat());
                valid = false;
            }
            if (!"".equals(ll) && !isValidNumber(ll)) {
                Styles.tryAddStyles(this.lowerLimit, invalidStyle);
                showError(I18n.I.wrongNumberFormat());
                valid = false;
            }
            if (!"".equals(llp) && !isValidNumber(llp, 0, 100)) {
                Styles.tryAddStyles(this.lowerLimitPercent, invalidStyle);
                showError(I18n.I.wrongNumberFormat());
                valid = false;
            }
        }

        if ("".equals(this.referencePrice.getText())) {
            Styles.tryAddStyles(this.referencePrice, invalidStyle);
            valid = false;
            showError(I18n.I.referencePriceRequired());
        }
        else {
            Styles.tryRemoveStyles(this.referencePrice, invalidStyle);
        }

        if ("".equals(this.limitName.getText())) {
            Styles.tryAddStyles(this.limitName, invalidStyle);
            valid = false;
            showError(I18n.I.limitNameRequired());
        }
        else {
            Styles.tryRemoveStyles(this.limitName, invalidStyle);
        }

        if (!this.email.isEnabled() && !StringUtil.hasText(this.errorText.getText())) {
            showError(I18n.I.notificationRequiresEmailAddress());
        }

        this.btnSave.setEnabled(valid);
    }

    private void showError(String s) {
        this.errorText.setText(s);
    }

    public void onFailure(Throwable throwable) {
        AbstractMainController.INSTANCE.showError(I18n.I.cannotLoadPriceData());
        this.btnSave.setEnabled(false);
    }

    public void onSuccess(ResponseType responseType) {
        if (this.block.isResponseOk()) {
            final MSCPriceData result = this.block.getResult();
            this.alert.setQuotedata(result.getQuotedata());

            updateCurrentFieldSelector(result);

            final int selectedFieldId = this.currentFieldSelector.getSelectedFieldId();
            this.referencePrice.setText(AlertUtil.pickFieldValue(selectedFieldId, result));
            validateForm();
        }
        else {
            onFailure(null);
        }
    }

    private void updateCurrentFieldSelector(MSCPriceData priceData) {
        if ("FND".equals(priceData.getInstrumentdata().getType())  // $NON-NLS-0$
                && AlertUtil.FUND_MARKETS.contains(priceData.getQuotedata().getMarketVwd())) {
            setCurrentFieldSelector(this.fondsFieldSelector);
        }
        else if ("lme".equals(priceData.getPricedatatype())) { // $NON-NLS-0$
            setCurrentFieldSelector(this.lmeFieldSelector);
        }
        else {
            setCurrentFieldSelector(this.defaultFieldSelector);
        }
        this.pendingSourceFieldId.pull().ifPresent(this::trySetCurrentFieldSelector);
    }

    private Button createRefreshButton() {
        return Button.icon(SessionData.isAsDesign() ? "as-reload-16" : "mm-reload-icon-small") // $NON-NLS$
                .tooltip(I18n.I.updateReferencePrice())
                .clickHandler(event -> AlertEditForm.this.updateReferencePrice())
                .build();
    }

    interface IFieldSelector extends IsWidget {
        int getSelectedFieldId();

        void setSelectedFieldId(int fieldId);

        int[] getFieldIds();
    }

    private static class RadioFieldSelector implements IFieldSelector {

        private final Panel view = new FlowPanel();

        private final RadioGroup<Integer> radioGroup = new RadioGroup<>();

        private final int[] fieldIds;

        RadioFieldSelector(int... fieldIds) {
            this.fieldIds = fieldIds;

            for (int fieldId : fieldIds) {
                this.view.add(new FloatingRadio<>(this.radioGroup.add(fieldId, false),
                        SafeHtmlUtils.fromString(AlertUtil.getLimitFieldName(fieldId))));
            }
        }

        @Override
        public Widget asWidget() {
            return view;
        }

        @Override
        public int getSelectedFieldId() {
            final Integer value = this.radioGroup.getValue();
            if (value == null) {
                this.radioGroup.setValue(this.fieldIds[0], false);
                return this.radioGroup.getValue();
            }
            return value;
        }

        @Override
        public void setSelectedFieldId(int fieldId) {
            this.radioGroup.setValue(fieldId);
        }

        @Override
        public int[] getFieldIds() {
            return this.fieldIds;
        }
    }

    private static class ComboFieldSelector extends SelectButton implements IFieldSelector {
        private static final String FIELD_ID = "fieldId";  // $NON-NLS$

        final Menu menu = new Menu();

        private final int[] fieldIds;

        ComboFieldSelector(int... fieldIds) {
            this.fieldIds = fieldIds;
            for (int fieldId : fieldIds) {
                this.menu.add(new MenuItem(AlertUtil.getLimitFieldName(fieldId)).withData(FIELD_ID, fieldId));
            }
            withMenu(this.menu);
        }

        @Override
        public int getSelectedFieldId() {
            if (getSelectedItem() == null) {
                setSelectedData(FIELD_ID, this.fieldIds[0], false);
            }
            return (Integer) getSelectedItem().getData(FIELD_ID);
        }

        @Override
        public void setSelectedFieldId(int fieldId) {
            setSelectedData(FIELD_ID, fieldId);
        }

        @Override
        public int[] getFieldIds() {
            return fieldIds;
        }
    }
}
