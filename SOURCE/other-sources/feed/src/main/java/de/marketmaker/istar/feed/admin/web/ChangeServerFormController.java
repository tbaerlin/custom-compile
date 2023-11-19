/*
 * ChangeServerFormController.java
 *
 * Created on 24.04.2005 13:28:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

import de.marketmaker.istar.feed.admin.dao.ConnectionInfo;
import de.marketmaker.istar.feed.admin.dao.FeedMBeanServerConnector;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class ChangeServerFormController {

    private FeedMBeanServerConnector connector;

    public void setConnector(FeedMBeanServerConnector connector) {
        this.connector = connector;
    }

    @RequestMapping(value = "changeBackend.html", method = RequestMethod.POST)
    public ModelAndView onSubmit(HttpServletRequest request, ChangeServerForm csf) {
        final ConnectionInfo info = this.connector.getConnectionInfo(csf.getServerName());
        if (info != null) {
            WebUtils.setSessionAttribute(request, "serverName", csf.getServerName());
        }
        return new ModelAndView(new RedirectView("index.html?serverName=" + csf.getServerName(), true));
    }

    @RequestMapping(value = "changeBackend.html", method = RequestMethod.GET)
    public ModelAndView onShowForm() {
        return new ModelAndView("server");
    }

}
