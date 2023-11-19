/*
 * HttpRequestHandlerServlet.java
 *
 * Created on 02.09.2014 09:21
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author mdick
 */
public class HttpRequestHandlerServlet extends org.springframework.web.context.support.HttpRequestHandlerServlet {
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ServletRequestHolder.setHttpServletRequest(request);
        try {
            super.service(request, response);
        }
        finally {
            ServletRequestHolder.setHttpServletRequest(null);
        }
    }
}
