/*
 * AltGetAlerts.java
 *
 * Created on 06.01.2009 10:01:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.alert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.merger.alert.Alert;
import de.marketmaker.istar.merger.alert.RetrieveAlertStatus;
import de.marketmaker.istar.merger.alert.RetrieveAlertsRequest;
import de.marketmaker.istar.merger.alert.RetrieveAlertsResponse;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.MultiListSorter;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * Lists details of all non-deleted alerts for the given user and application.
 * <p>
 * Non-deleted alerts are those alerts that are not deleted through the service
 * <code>ALT_DeleteAlert</code>. The quotes associated with the alerts are also retrieved.
 * </p>
 * <p>
 * After successful retrieval alerts and their related quotes are listed. Otherwise an error with
 * code <code>alert.getalerts.failed</code> is raised.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AltGetAlerts extends AbstractAltBlock {

    private EasytradeInstrumentProvider instrumentProvider;

    public static class Command extends AlertCommand {

        private String alertId;

        private String vwdSymbol;

        private RetrieveAlertStatus retrieveStatus = RetrieveAlertStatus.ANY_UNDELETED;


        /**
         * @return an existing alert id, might be null if all alerts should be returned by the request
         */
        public String getAlertId() {
            return alertId;
        }

        public void setAlertId(String alertId) {
            this.alertId = alertId;
        }

        /**
         * @return vwdSymbol, might be null if all symbols should be returned by the request
         */
        public String getVwdSymbol() {
            return vwdSymbol;
        }

        public void setVwdSymbol(String vwdSymbol) {
            this.vwdSymbol = vwdSymbol;
        }

        /**
         *
         * @return the alert status, might be null if the status doesn't matter for the request
         * values should be one of: {ANY, ACTIVE, FIRED, EXPIRED, DELETED, ANY_UNDELETED}
         */
        public RetrieveAlertStatus getRetrieveStatus() {
            return retrieveStatus;
        }

        public void setRetrieveStatus(RetrieveAlertStatus retrieveStatus) {
            this.retrieveStatus = retrieveStatus;
        }

    }

    public AltGetAlerts() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        RetrieveAlertsRequest alertRequest = new RetrieveAlertsRequest(getPreferredAppId(cmd), cmd.getVwdUserId());
        // optional filters
        alertRequest.setAlertId(cmd.getAlertId());
        alertRequest.setVwdSymbol(cmd.getVwdSymbol());
        alertRequest.setRetrieveStatus(cmd.getRetrieveStatus());

        final RetrieveAlertsResponse ar = this.alertProvider.retrieveAlerts(alertRequest);

        if (!ar.isValid()) {
            errors.reject("alert.getalerts.failed", "internal error");
            return null;
        }

        final Map<String, Object> model = new HashMap<>();
        final List<Alert> alerts = ar.getAlerts();
        final List<Quote> quotes = getQuotes(alerts);
        new MultiListSorter(QuoteComparator
                .byName(RequestContextHolder.getRequestContext().getQuoteNameStrategy()), false)
                .sort(quotes, alerts);
        model.put("alerts", alerts);
        model.put("quotes", quotes);
        return new ModelAndView("altgetalerts", model);
    }

    private List<Quote> getQuotes(List<Alert> alerts) {
        if (alerts.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> vwdcodes = new ArrayList<>(alerts.size());
        for (final Alert alert : alerts) {
            vwdcodes.add(alert.getVwdCode());
        }
        return this.instrumentProvider.identifyQuotes(vwdcodes, SymbolStrategyEnum.VWDCODE, null, null);
    }
}