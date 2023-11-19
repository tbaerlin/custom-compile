/*
 * AdminController.java
 *
 * Created on 27.11.2006 09:50:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.access.MBeanProxyFactoryBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import de.marketmaker.istar.common.jmx.IstarNamingStrategy;
import de.marketmaker.istar.feed.admin.dao.ConnectionInfo;
import de.marketmaker.istar.feed.admin.dao.FeedMBeanServerConnector;
import de.marketmaker.istar.feed.tick.AbstractTickRecord;
import de.marketmaker.istar.feed.tick.TickServerMBean;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AdminController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private FeedMBeanServerConnector connector;

    public void setConnector(FeedMBeanServerConnector connector) {
        this.connector = connector;
    }

    protected ModelAndView prepareCommand(HttpServletRequest request, AbstractFeedAdminCommand cmd) {
        final String serverName = FeedAdminWebUtils.getServerName(request, this.connector);
        if (serverName == null) {
            new ModelAndView("error.vm", "error", "No backend service available, choose another one");
        }
        cmd.setServerName(serverName);

        final ConnectionInfo info = this.connector.getConnectionInfo(serverName);
        if (info == null) {
            return new ModelAndView(new RedirectView("index.html", true));
        }
        cmd.setInfo(info);

        if (!"monitoring".equals(cmd.getName())) {
            final ObjectName on = FeedAdminWebUtils.getNameForBean(info, cmd.getName());
            if (on == null || !isRequiredType(IstarNamingStrategy.getType(on))) {
                return new ModelAndView(new RedirectView("index.html", true));
            }
            cmd.setObjectName(on);
        }

        final ModelAndView result = new ModelAndView("main");
        result.addAllObjects(FeedAdminWebUtils.getDefaultModel(connector, serverName));
        result.addObject("command", cmd);
        return result;
    }

    protected Object getService(Class clazz, AbstractFeedAdminCommand cmd) throws Exception {
        final MBeanProxyFactoryBean mp = new MBeanProxyFactoryBean();
        mp.setServer(cmd.getConnection());
        mp.setProxyInterface(clazz);
        mp.setObjectName(cmd.getObjectName().getCanonicalName());
        mp.afterPropertiesSet();
        return mp.getObject();
    }

    protected AbstractTickRecord.TickItem getTicks(AbstractTicksCommand command,
            TickServerMBean bean) {
        return bean.getTickItem(command.getKey(), command.getDay());
    }

    protected abstract boolean isRequiredType(String type);
}
