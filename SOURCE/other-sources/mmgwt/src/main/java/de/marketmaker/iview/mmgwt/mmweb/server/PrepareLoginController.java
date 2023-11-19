/*
 * LoginController.java
 *
 * Created on 28.03.13 10:25
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Prepares a session with two attributes, {@link UserServiceImpl#SESSION_KEY_PRE_LOGIN}
 * and {@link UserServiceImpl#SESSION_KEY_PRE_LOGIN}.
 * If {@link UserServiceIfc#getUser(de.marketmaker.iview.mmgwt.mmweb.client.UserRequest)} is
 * invoked and these attributes exist in the session, they will replace the login/password
 * values in the request object.
 * @author oflege
 */
@Controller
public class PrepareLoginController {

    public static class Command {
        private String login;

        private String password;

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    @RequestMapping("/*/login.html")
    protected void handle(HttpServletRequest request, HttpServletResponse response, Command cmd) throws Exception {
        if (cmd.getLogin() == null || cmd.getPassword() == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final HttpSession session = request.getSession(true);
        session.setAttribute(UserServiceImpl.SESSION_KEY_PRE_LOGIN, cmd.getLogin());
        session.setAttribute(UserServiceImpl.SESSION_KEY_PRE_PASSWORD, cmd.getPassword());

        response.sendRedirect(getRedirectURI(request.getRequestURI()));
    }

    private String getRedirectURI(String requestURI) {
        final int last = requestURI.lastIndexOf("/");
        final int before = requestURI.lastIndexOf("/", last - 1);
        return requestURI.substring(before, last) + "/index.html#/login=auto/password=****";
    }
}
