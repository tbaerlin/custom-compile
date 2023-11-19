/*
 * MmwebServiceImpl.java
 *
 * Created on 27.02.2008 09:53:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.springframework.context.ApplicationContext;

import de.marketmaker.iview.mmgwt.mmweb.client.MmwebResponse;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebService;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebRequest;

/**
 * To be used in hosted mode as a connector to a service that provides an MmwebService.
 * Connects to a local activemq instance on the default port to get access 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MmwebServiceProxy extends AbstractServiceProxy implements MmwebService {

    private MmwebService delegate;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        final ApplicationContext ac = getContext();
        this.delegate = (MmwebService) ac.getBean("mmwebService", MmwebService.class);
    }

    public MmwebResponse getData(MmwebRequest request) {
        return this.delegate.getData(request);
    }
}