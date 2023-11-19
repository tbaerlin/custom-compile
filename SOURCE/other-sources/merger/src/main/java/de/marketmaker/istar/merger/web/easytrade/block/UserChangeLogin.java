/*
 * UserChangeLogin.java
 *
 * Created on 22.09.2010 12:00:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import de.marketmaker.istar.merger.provider.UserProvider;
import de.marketmaker.istar.merger.user.LoginExistsException;
import de.marketmaker.istar.merger.user.NoSuchUserException;
import de.marketmaker.istar.merger.user.UpdateLoginCommand;

/**
 * @author oflege
 */
@Controller
public class UserChangeLogin  {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private UserProvider userProvider;

    public void setUserProvider(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    @RequestMapping("/*/change-login.html")
    protected void handle(HttpServletResponse response, UpdateLoginCommand cmd) throws Exception {
        if (cmd.getCompanyid() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "companyid undefined");
            return;
        }
        if (!StringUtils.hasText(cmd.getNewLogin())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "newLogin undefined");
            return;
        }
        if (!StringUtils.hasText(cmd.getOldLogin())) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "oldLogin undefined");
            return;
        }

        try {
            this.userProvider.updateLogin(cmd);

            response.getWriter().write("OK");
            response.getWriter().close();
            this.logger.info("<handle> login changed " + cmd);

        } catch (NoSuchUserException e) {
            this.logger.warn("<doHandle> no such user " + cmd);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, cmd.getOldLogin() + " is not known");
        } catch (LoginExistsException e) {
            this.logger.warn("<doHandle> new login exists " + cmd);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, cmd.getNewLogin() + " already exists");
        } catch (Exception e) {
            this.logger.error("<doHandle> failed for " + cmd, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
