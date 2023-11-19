/*
 * AbstractServiceProxy.java
 *
 * Created on 29.04.2008 16:45:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.ApplicationContext;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AbstractServiceProxy extends RemoteServiceServlet {
    private static ClassPathXmlApplicationContext context;

    public AbstractServiceProxy() {
    }

    protected static ApplicationContext getContext() {
        if (context == null) {
            context = new ClassPathXmlApplicationContext("de/marketmaker/iview/mmgwt/mmweb/server/proxyContext.xml");
        }
        return context;
    }

    protected static void destroyContext() {
        if (context != null) {
            context.destroy();
            context = null;
        }
    }

    public void destroy() {
        destroyContext();
        super.destroy();
    }
}
