/*
 * ViewStatisticsController.java
 *
 * Created on 25.04.2005 08:54:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.web;

import java.util.Set;
import java.util.TreeMap;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Controller
public class ViewStatisticsController extends AdminController {

    static final ObjectName MONITOR_OBJECT;

    static {
        try {
            MONITOR_OBJECT = new ObjectName("servo.monitors:type=monitoring,name=monitoring");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean isRequiredType(String type) {
        return false;
    }

    @RequestMapping(value = "/monitoring.html")
    protected ModelAndView doHandle(HttpServletRequest request, ViewStatisticsCommand cmd) throws Exception {
        final ModelAndView mav = prepareCommand(request, cmd);
        if (cmd.getInfo() == null) {
            return mav;
        }
        if (!cmd.getInfo().isWithMonitoring()) {
            return new ModelAndView(new RedirectView("index.html", true));
        }

        MBeanServerConnection conn = cmd.getInfo().getConnection();
        Set<ObjectInstance> monitors = conn.queryMBeans(new ObjectName("servo.monitors:*"), null);
        TreeMap<String, String> result = new TreeMap<>();
        for (ObjectInstance monitor : monitors) {
            String name = monitor.getObjectName().getKeyProperty("name");
            Object value = conn.getAttribute(monitor.getObjectName(), "value");
            result.put(name, String.valueOf(value));
        }
        mav.addObject("result", result);
        return mav;
    }
}
