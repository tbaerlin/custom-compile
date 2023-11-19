/*
 * RemoteServiceHandler.java
 *
 * Created on 15.01.15 15:36
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.springframework.web.HttpRequestHandler;

/**
 * Forwards requests to a RemoteServiceServlet
 * @author oflege
 */
public class RemoteServiceHandler implements HttpRequestHandler {
    private RemoteServiceServlet service = null;

    public void setService(RemoteServiceServlet service) {
        this.service = service;
    }

    public void handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        ServletRequestHolder.setHttpServletRequest(request);
        try {
            this.service.doPost(request, response);
        }
        finally {
            ServletRequestHolder.setHttpServletRequest(null);
        }
    }
}
