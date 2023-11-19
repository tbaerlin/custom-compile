/*
 * AltDeleteAlert.java
 *
 * Created on 06.01.2009 10:01:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.alert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.alert.DeleteAlertRequest;
import de.marketmaker.istar.merger.alert.DeleteAlertResponse;

/**
 * Deletes an alert that has been registered before.
 * <p>
 * AlertUsers can register one or more alerts, a user will be notified if one or more registered
 * alerts are triggered.
 * If such an alert is no longer of any interest, the Alert can be deleted and no notifications will be triggered.
 * The deletion can be performed regardless of the trigger status of the affected alert.
 * </p>
 * <p>
 * A response with a true status indicates a successful deletion. Otherwise an error with code
 * <code>alert.deletealert.failed</code> will be raised.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AltDeleteAlert extends AbstractAltBlock {

    public static class Command extends AlertCommand {

        private String alertId;

        /**
         * @return an existing alert id.
         */
        @NotNull
        public String getAlertId() {
            return alertId;
        }

        public void setAlertId(String alertId) {
            this.alertId = alertId;
        }
    }

    public AltDeleteAlert() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final DeleteAlertRequest alertRequest = new DeleteAlertRequest(getPreferredAppId(cmd), cmd.getVwdUserId());
        alertRequest.setAlertID(cmd.getAlertId());
        final DeleteAlertResponse deleteAlertResponse = this.alertProvider.deleteAlert(alertRequest);

        if (!deleteAlertResponse.isValid()) {
            errors.reject("alert.deletealert.failed", "internal error");
            return null;
        }

        return new ModelAndView(ALT_OK_TEMPLATE);
    }
}