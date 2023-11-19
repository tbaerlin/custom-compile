/*
 * AltGetUser.java
 *
 * Created on 06.01.2009 10:01:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.alert;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.marketmaker.istar.merger.context.RequestContextHolder;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.alert.RetrieveAlertUserRequest;
import de.marketmaker.istar.merger.alert.RetrieveAlertUserResponse;

/**
 * Retrieves meta information related with the given user for the given application.
 * <p>
 * Meta information about a user using alerting service contains:
 * <ul>
 * <li>configured addresses for alert notifications</li>
 * <li>alerting status of the given user</li>
 * <li>language used by notification</li>
 * </ul>
 * If successful these information are listed, otherwise an error with code
 * <code>alert.getuser.failed</code> is returned.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@SuppressWarnings("SpellCheckingInspection")
public class AltGetUser extends AbstractAltBlock {

    public AltGetUser() {
        super(AlertCommand.class);
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final AlertCommand cmd = (AlertCommand) o;

        final Locale locale = RequestContextHolder.getRequestContext().getLocale();

        final RetrieveAlertUserResponse ur
                = this.alertProvider.retrieveAlertUser(new RetrieveAlertUserRequest(cmd.getVwdUserId(), locale));

        if (!ur.isValid()) {
            errors.reject("alert.getuser.failed", "internal error");
            return null;
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("user", ur.getUser());
        model.put("vwdUserId", cmd.getVwdUserId());
        return new ModelAndView("altgetuser", model);
    }
}
