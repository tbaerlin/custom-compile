/*
 * DefaultController.java
 *
 * Created on 25.04.2005 08:17:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import de.marketmaker.istar.common.jmx.IstarNamingStrategy;
import de.marketmaker.istar.feed.admin.dao.ConnectionInfo;
import de.marketmaker.istar.feed.admin.dao.FeedMBeanServerConnector;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class StartController {

    private FeedMBeanServerConnector connector;

    public void setConnector(FeedMBeanServerConnector connector) {
        this.connector = connector;
    }

    @RequestMapping("/index.html")
    public ModelAndView handleRequest(HttpServletRequest httpServletRequest) {
        final String serverName = FeedAdminWebUtils.getServerName(httpServletRequest, this.connector);
        if (serverName == null) {
            return new ModelAndView("error", "error", "No backend service available, serverName is null");
        }

        final ConnectionInfo ci = this.connector.getConnectionInfo(serverName);
        if (ci == null) {
            return new ModelAndView("error", "error", "No backend service available for '" + serverName
                    + "' at " + this.connector.getUrlForName(serverName));
        }

        // find default bean and redirect accordingly
        final ObjectName on = FeedAdminWebUtils.getDefaultObject(ci);
        if (on == null) {
            if (ci.isWithMonitoring()) {
                return new ModelAndView(new RedirectView("monitoring.html?name=monitoring", true));
            }
            httpServletRequest.getSession().invalidate();
            return new ModelAndView(new RedirectView("index.html", true));
        }

        final String type = IstarNamingStrategy.getType(on);
        final String name = IstarNamingStrategy.getName(on);
        return new ModelAndView(new RedirectView(type + ".html?name=" + name, true));
    }
}
