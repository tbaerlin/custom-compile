/*
 * ResetPassword.java
 *
 * Created on 06.08.2008 10:20:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.UserRequest;
import de.marketmaker.iview.mmgwt.mmweb.client.UserResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class WebXLAuthenticate {
    private static final String KEY_WEB_XL = Integer.toString(Selector.WEB_XL.getId());

    private UserServiceIfc userService;

    @Required
    public void setUserService(UserServiceIfc userService) {
        this.userService = userService;
    }

    @RequestMapping("/web.xl/authenticate.xml")
    protected ModelAndView authenticate(@RequestParam Map<String, String> params) throws Exception {
        final String locale = params.get("locale");
        final String xun = params.containsKey("Xun") ? params.get("Xun") : params.get("xun");
        final String xpw = params.containsKey("Xpw") ? params.get("Xpw") : params.get("xpw");
        final UserRequest userRequest = new UserRequest(false, xun, xpw, "web.xl", null, locale);
        final UserResponse response = this.userService.getUser(userRequest);
        final User user = response.getUser();

        final HashMap<String, Object> model = new HashMap<>();
        model.put("xun", xun);
        model.put("user", user);
        if (user != null) {
            model.put("ddeAllowed", user.getAppProfile().isFunctionAllowed(KEY_WEB_XL));
        }
        return new ModelAndView("webxlauthenticate", model);
    }
}