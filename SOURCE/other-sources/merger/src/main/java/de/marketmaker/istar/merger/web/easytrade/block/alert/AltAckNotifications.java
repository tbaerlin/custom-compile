/*
 * AltAckNotifications.java
 *
 * Created on 06.01.2009 10:01:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.alert;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.alert.AckAlertNotificationRequest;
import de.marketmaker.istar.merger.alert.AckAlertNotificationResponse;

/**
 * Acknowledges the alert center that the notifications of triggered alerts have been given attention.
 * <p>
 * When an alert is triggered, notifications of some configured types are raised in order to notify
 * the interested users. Through acknowledgements the notifications set can be updated, which in turn
 * can be checked by some front end tools, in order to set their notification status properly.
 * </p>
 * <p>
 * Successful acknowledgements are indicated by a feedback with a true status. Failures are instead
 * communicated back via error code <code>alert.deletealert.failed</code>.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AltAckNotifications extends AbstractAltBlock {

    public static class Command extends AlertCommand {

        private String[] notificationId;

        /**
         * @return Some notification ids of some triggered alerts.
         */
        @NotNull
        public String[] getNotificationId() {
            return notificationId;
        }

        public void setNotificationId(String[] notificationId) {
            this.notificationId = notificationId;
        }
    }

    public AltAckNotifications() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;

        final AckAlertNotificationRequest ackAlertNotificationRequest = new AckAlertNotificationRequest(
                getPreferredAppId(cmd), cmd.getVwdUserId());
        ackAlertNotificationRequest.setIds(Arrays.asList(cmd.getNotificationId()));
        final AckAlertNotificationResponse alertNotificationResponse =
                this.alertProvider.ackAlerts(ackAlertNotificationRequest);

        if (!alertNotificationResponse.isValid()) {
            errors.reject("alert.deletealert.failed", "internal error");
            return null;
        }

        return new ModelAndView(ALT_OK_TEMPLATE);
    }
}