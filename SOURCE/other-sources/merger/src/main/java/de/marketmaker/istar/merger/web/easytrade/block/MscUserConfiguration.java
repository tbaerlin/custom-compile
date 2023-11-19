/*
 * WlWatchlistAnzeigen.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.merger.user.User;
import de.marketmaker.istar.merger.user.UserContext;
import de.marketmaker.istar.merger.user.Company;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

/**
 * Returns information about the number of workspaces a user may create. A workspace is a
 * collection of snippets that can be selected, configured and arranged by the user.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscUserConfiguration extends UserHandler {
    private static final String VIEW = "mscuserconfiguration";

    public MscUserConfiguration() {
        super(UserCommandImpl.class);
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final UserCommand cmd = (UserCommand) o;

        final UserContext userContext = getUserContext(cmd);

        final User user = userContext.getUser();
        final Company c = userContext.getCompany();

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        final Map<String, Object> model = new HashMap<>();
        model.put("watchlists", user.getWatchlists());
        model.put("portfolios", user.getPortfolios());
        model.put("company", c);
        model.put("maxNumMyspaces", profile.isAllowed(Selector.MY_WORKSPACE_ADVANCED) ? "10" : "1");
        return new ModelAndView(VIEW, model);
    }
}