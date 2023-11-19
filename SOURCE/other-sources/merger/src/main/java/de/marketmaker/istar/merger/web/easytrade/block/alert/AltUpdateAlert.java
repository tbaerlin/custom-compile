/*
 * AltUpdateAlert.java
 *
 * Created on 06.01.2009 10:01:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.alert;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.alert.Alert;
import de.marketmaker.istar.merger.alert.UpdateAlertRequest;
import de.marketmaker.istar.merger.alert.UpdateAlertResponse;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * Adds an alert with the given fields or updates an existing alert with altered fields.
 * <p>
 * An alert is a notification mechanism for a defined condition, which is expressed via a boundary
 * value on a specific field of a given instrument. If the observed value on that field of that
 * instrument breaks the given boundary, the user would be notified by means of email, SMS. A boundary
 * can be defined either as absolute value or in percentage.
 * </p>
 * <p>
 * If the alert id is null, this service creates a new alert with the given fields on the given instrument.
 * If the alert id is not null:
 * <ul>
 * <li>if the flag <code>deleteExisting</code> is set to false, the existing alert with that id
 * is updated with the given fields.</li>
 * <li>if the flag <code>deleteExisting</code> is set to true, the existing alert with that id
 * is deleted and a new one with the altered fields is created.</li>
 * </ul>
 * </p>
 * <p>
 * A successful creation or update is communicated back by a response with true status additionally the unique id
 * of the alert that has been created or altered will be provided.
 * Failure to create or alter an alert will result in an response with status <code>alert.updatealert.failed</code>.
 * Note that trying to create an alert or update an alert on a non-existing instrument will also cause this
 * error. <b><em>An already triggered alert can not be updated.</em></b>
 * </p>
 * <p>
 * Alerts created this way are user and application specific and can be queried using user and
 * application id through the service ALT_GetAlerts.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@SuppressWarnings("SpellCheckingInspection")
public class AltUpdateAlert extends AbstractAltBlock {

    public static class Command extends AlertCommand implements SymbolCommand {

        private String alertId;

        private boolean email;

        private String infoText;

        private BigDecimal lowerBoundary;

        private BigDecimal lowerBoundaryPercent;

        private String name;

        private BigDecimal referenceValue;

        private boolean sms;

        private BigDecimal upperBoundary;

        private BigDecimal upperBoundaryPercent;

        private DateTime validUntil;

        private int fieldId;

        private boolean deleteExisting;

        private String symbol;

        private SymbolStrategyEnum symbolStrategy;

        /**
         * A valid alert id for modifying an existing alert,
         * or null to create a new alert.
         *
         * @return an alert id
         */
        public String getAlertId() {
            return alertId;
        }

        public void setAlertId(String alertId) {
            this.alertId = alertId;
        }

        /**
         * @return A flag indicating if triggering of this alert is notified by email.
         *         The current user must have a valid email address set up with ALT_UpdateUser
         */
        public boolean isEmail() {
            return email;
        }

        public void setEmail(boolean email) {
            this.email = email;
        }

        /**
         * A VWD field id. The value of this field is monitored by this alert. Allowed fields are:
         * <table border="1">
         * <tr><th>Instrument Type</th><th>Fields</th></tr>
         * <tr><td>Fund</td><td>redemption (414)</td></tr>
         * <tr><td>Others</td><td>ask (28), bid (30), trade (80)</td></tr>
         * </table>
         *
         * @return A VWD field id.
         */
        public int getFieldId() {
            return fieldId;
        }

        public void setFieldId(int fieldId) {
            this.fieldId = fieldId;
        }

        /**
         * @return A text message used in notifications of this alert.
         */
        public String getInfoText() {
            return infoText;
        }

        public void setInfoText(String infoText) {
            this.infoText = infoText;
        }

        /**
         * @return The lower boundary of this alert as an absolute value.
         */
        public BigDecimal getLowerBoundary() {
            return lowerBoundary;
        }

        public void setLowerBoundary(BigDecimal lowerBoundary) {
            this.lowerBoundary = lowerBoundary;
        }

        /**
         * @return The lower boundary of this alert in percent.
         */
        public BigDecimal getLowerBoundaryPercent() {
            return lowerBoundaryPercent;
        }

        public void setLowerBoundaryPercent(BigDecimal lowerBoundaryPercent) {
            this.lowerBoundaryPercent = lowerBoundaryPercent;
        }

        /**
         * @return A descriptive name for this alert.
         */
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        /**
         * If the boundaries are given in percent, the limit value's calculation is based on this reference value,
         * for absolute values the reference value still needs to be non null, und usually reflects the current value
         * if the field at the time of limit creation
         *
         * @return The reference value for relative limits calculation or the current instrument value at the
         * time of limit creation
         */
        @NotNull
        public BigDecimal getReferenceValue() {
            return referenceValue;
        }

        public void setReferenceValue(BigDecimal referenceValue) {
            this.referenceValue = referenceValue;
        }

        /**
         * @return A flag indicating if the limit will trigger an SMS notification (not supported yet)
         */
        public boolean isSms() {
            return sms;
        }

        public void setSms(boolean sms) {
            this.sms = sms;
        }

        /**
         * @return The upper boundary of this alert as an absolute value.
         */
        public BigDecimal getUpperBoundary() {
            return upperBoundary;
        }

        public void setUpperBoundary(BigDecimal upperBoundary) {
            this.upperBoundary = upperBoundary;
        }

        /**
         * @return The upper boundary of this alert in percent.
         */
        public BigDecimal getUpperBoundaryPercent() {
            return upperBoundaryPercent;
        }

        public void setUpperBoundaryPercent(BigDecimal upperBoundaryPercent) {
            this.upperBoundaryPercent = upperBoundaryPercent;
        }

        /**
         * @return The expiration date of this alert, expired alerts aren't triggered.
         */
        public DateTime getValidUntil() {
            return validUntil;
        }

        public void setValidUntil(DateTime validUntil) {
            this.validUntil = validUntil;
        }

        /**
         * @return A flag when set to true, the alert with the given id is deleted and a new one
         *         with the given fields is created.
         */
        public boolean isDeleteExisting() {
            return deleteExisting;
        }

        public void setDeleteExisting(boolean deleteExisting) {
            this.deleteExisting = deleteExisting;
        }

        @Override
        @NotNull
        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public SymbolStrategyEnum getSymbolStrategy() {
            return symbolStrategy;
        }

        public void setSymbolStrategy(SymbolStrategyEnum symbolStrategy) {
            this.symbolStrategy = symbolStrategy;
        }

        /**
         * @return No market strategy required, always null.
         */
        @Override
        public String getMarketStrategy() {
            return null;
        }

        /**
         * @return No market required, always null.
         */
        @Override
        public String getMarket() {
            return null;
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    public AltUpdateAlert() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        if (quote == null) {
            errors.reject("alert.updatealert.failed", "internal error");
            return null;
        }

        final UpdateAlertRequest updateRequest = new UpdateAlertRequest(getPreferredAppId(cmd), cmd.getVwdUserId());

        final int fieldId = getFieldId(quote, cmd, errors);
        if (errors.hasErrors()) {
            return null;
        }

        final Alert alert = new Alert();
        alert.setId(cmd.getAlertId());
        alert.setName(cmd.getName());
        alert.setReferenceValue(cmd.getReferenceValue());
        alert.setInfoText(cmd.getInfoText());
        alert.setLowerBoundaryPercent(cmd.getLowerBoundaryPercent());
        if (alert.getLowerBoundaryPercent() == null) {
            alert.setLowerBoundary(cmd.getLowerBoundary());
        }
        alert.setUpperBoundaryPercent(cmd.getUpperBoundaryPercent());
        if (alert.getUpperBoundaryPercent() == null) {
            alert.setUpperBoundary(cmd.getUpperBoundary());
        }
        alert.setValidUntil(cmd.getValidUntil());
        alert.setVwdCode(quote.getSymbolVwdcode());
        alert.setFieldId(fieldId);
        alert.setEmail(cmd.isEmail());
        alert.setSms(cmd.isSms());
        updateRequest.setAlert(alert);
        updateRequest.setDeleteExisting(cmd.isDeleteExisting());

        final UpdateAlertResponse updateResponse = this.alertProvider.updateAlert(updateRequest);

        if (!updateResponse.isValid()) {
            errors.reject("alert.updatealert.failed", "internal error");
            return null;
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("alertId", updateResponse.getAlertID());
        return new ModelAndView(ALT_OK_TEMPLATE, model);
    }

    private int getFieldId(Quote quote, Command cmd, BindException errors) {
        if (InstrumentUtil.isVwdFund(quote)) {
            if (validateFieldId(cmd, VwdFieldDescription.ADF_Ruecknahme.id(), errors)) {
                return VwdFieldDescription.ADF_Ruecknahme.id();
            }
        }
        if (InstrumentUtil.isLMEMarket(quote.getSymbolVwdfeedMarket())) {
            if ((cmd.getFieldId() == VwdFieldDescription.ADF_Interpo_Closing.id())
                    || (cmd.getFieldId() == VwdFieldDescription.ADF_Prov_Evaluation.id())
                    || (cmd.getFieldId() == VwdFieldDescription.ADF_Official_Ask.id())
                    || (cmd.getFieldId() == VwdFieldDescription.ADF_Official_Bid.id())
                    || (cmd.getFieldId() == VwdFieldDescription.ADF_Unofficial_Ask.id())
                    || (cmd.getFieldId() == VwdFieldDescription.ADF_Unofficial_Bid.id())
                    || (cmd.getFieldId() == VwdFieldDescription.ADF_Benchmark.id())) {
                return cmd.getFieldId();
            }
        }
        if (cmd.getFieldId() == VwdFieldDescription.ADF_Geld.id()) {
            return VwdFieldDescription.ADF_Geld.id();
        }
        if (cmd.getFieldId() == VwdFieldDescription.ADF_Brief.id()) {
            return VwdFieldDescription.ADF_Brief.id();
        }
        if (validateFieldId(cmd, VwdFieldDescription.ADF_Bezahlt.id(), errors)) {
            return VwdFieldDescription.ADF_Bezahlt.id();
        }
        return -1;
    }

    private boolean validateFieldId(Command cmd, int allowedFieldId, BindException errors) {
        if (cmd.getFieldId() > 0 && cmd.getFieldId() != allowedFieldId) {
            errors.rejectValue("fieldId", "fieldId.invalid", "unsupported fieldId " + cmd.getFieldId());
            return false;
        }
        return true;
    }
}
