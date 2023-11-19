package de.marketmaker.itools.gwtcomet.comet.server.impl;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import de.marketmaker.itools.gwtcomet.comet.server.CometServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * Created on 31.01.13 16:53
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * <p/>
 * SOURCE: https://groups.google.com/forum/?fromgroups=#!topic/gwt-comet/NkfGh6-nREM
 *
 * @author Michael Lösch
 */

public class IEXDRCometServletResponse extends RawDataCometServletResponse {

    // so much padding...
    private static final int PADDING_REQUIRED = 2048;

    public IEXDRCometServletResponse(HttpServletRequest request, HttpServletResponse response, SerializationPolicy serializationPolicy, CometServlet servlet,
                                     AsyncServlet async, int heartbeat) {
        super(request, response, serializationPolicy, servlet, async, heartbeat);
    }

    @Override
    protected void setupHeaders(HttpServletResponse response) {
        super.setupHeaders(response);
        response.setContentType("application/comet");
        response.setCharacterEncoding("UTF-8");

        String origin = getRequest().getHeader("Origin");
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }
    }

    @Override
    protected OutputStream getOutputStream(OutputStream outputStream) {
        return setupCountOutputStream(outputStream);
    }

    @Override
    protected int getPaddingRequired() {
        return PADDING_REQUIRED;
    }
}