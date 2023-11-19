/*
 * AltUpdateUser.java
 *
 * Created on 06.01.2009 10:01:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.alert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.alert.AlertUser;
import de.marketmaker.istar.merger.alert.UpdateAlertUserRequest;
import de.marketmaker.istar.merger.alert.UpdateAlertUserResponse;

import java.util.List;
import java.util.Locale;

/**
 * Updates notification addresses for an user.
 * <p>
 * A user can be notified by email and SMS. For each user three email addressed and one SMS address
 * can be configured to receive notifications.
 * </p>
 * <p>
 * Successful update is indicated by a feedback with true status. Otherwise an error with code
 * <code>alert.updateuser.failed</code> will be raised.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AltUpdateUser extends AbstractAltBlock {

    public static class Command extends AlertCommand {

        private String emailAddress1;

        private String emailAddress2;

        private String emailAddress3;

        private String smsAddress;

        /**
         * @return The first configured email address.
         */
        public String getEmailAddress1() {
            return emailAddress1;
        }

        public void setEmailAddress1(String emailAddress1) {
            this.emailAddress1 = emailAddress1;
        }

        /**
         * @return The second configured email address.
         */
        public String getEmailAddress2() {
            return emailAddress2;
        }

        public void setEmailAddress2(String emailAddress2) {
            this.emailAddress2 = emailAddress2;
        }

        /**
         * @return The third configured email address.
         */
        public String getEmailAddress3() {
            return emailAddress3;
        }

        public void setEmailAddress3(String emailAddress3) {
            this.emailAddress3 = emailAddress3;
        }

        /**
         * @return The configured SMS address.
         */
        public String getSmsAddress() {
            return smsAddress;
        }

        public void setSmsAddress(String smsAddress) {
            this.smsAddress = smsAddress;
        }
    }

    public AltUpdateUser() {
        super(Command.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final Command cmd = (Command) o;
        final Locale locale = RequestContextHolder.getRequestContext().getLocale();

        final AlertUser user = new AlertUser();
        user.setEmailAddress1(cmd.getEmailAddress1());
        user.setEmailAddress2(cmd.getEmailAddress2());
        user.setEmailAddress3(cmd.getEmailAddress3());
        user.setSms(cmd.getSmsAddress());
        user.setLocale(locale);

        final UpdateAlertUserResponse ur
                = this.alertProvider.updateAlertUser(new UpdateAlertUserRequest(cmd.getVwdUserId(), user));

        if (!ur.isValid()) {
            errors.reject("alert.updateuser.failed", "internal error");
            return null;
        }

        return new ModelAndView(ALT_OK_TEMPLATE);
    }
}