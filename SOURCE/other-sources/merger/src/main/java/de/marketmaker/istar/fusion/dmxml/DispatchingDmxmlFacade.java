/*
 * DispatchingDmxmlFacade.java
 *
 * Created on 14.02.14 10:45
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.fusion.dmxml;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import de.marketmaker.istar.merger.util.JaxbHandler;
import de.marketmaker.istar.merger.web.HttpException;
import de.marketmaker.istar.merger.web.RequestWrapper;
import de.marketmaker.istar.merger.web.StringWriterResponse;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;

import static de.marketmaker.istar.merger.web.ProfileResolver.AUTHENTICATION_KEY;
import static de.marketmaker.istar.merger.web.ProfileResolver.AUTHENTICATION_TYPE_KEY;

/**
 * A <tt>DmxmlFacade</tt> that uses servlet container internal request forwarding to dispatch
 * dmxml requests to the dmxml servlet.
 *
 * @author oflege
 */
public class DispatchingDmxmlFacade implements DmxmlFacade, ServletContextAware {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final JaxbHandler handler = new JaxbHandler(getClass().getPackage().getName());

    private ServletContext servletContext;

    private String defaultPath = "/iview/retrieve.xml";

    public void setDefaultPath(String defaultPath) {
        this.defaultPath = defaultPath;
    }

    @Override
    public boolean evaluate(DmxmlRequest request) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean evaluate(HttpServletRequest servletRequest, DmxmlRequest request) {
        String path = getPath(request);
        RequestDispatcher rd = servletContext.getRequestDispatcher(path);
        if (rd == null) {
            this.logger.error("<evaluate> no dispatcher found for path '" + path +"'");
            return false;
        }

        final StringWriterResponse response = new StringWriterResponse();
        HttpServletRequest myServletRequest
                = createWrappedRequest(servletRequest, path, request.getMoleculeRequest());

        try {
            rd.forward(myServletRequest, response);
            if (response.isError()) {
                request.setResult(new HttpException(response.getErrorCode(), response.getErrorMessage()));
                return false;
            }
            request.setResult(unmarshal(response.toString()));
            return true;
        } catch (ServletException | IOException e) {
            this.logger.error("<evaluate> failed for " + request, e);
            return false;
        }
    }

    private HttpServletRequest createWrappedRequest(HttpServletRequest servletRequest,
            String path, final MoleculeRequest mr) {
        return RequestWrapper.create(servletRequest, getParameterMap(mr), path, getAttributeMap(mr));
    }

    private HashMap<String, Object> getAttributeMap(MoleculeRequest mr) {
        final HashMap<String, Object> result = new HashMap<>();
        result.put(MoleculeRequest.REQUEST_ATTRIBUTE_NAME, mr);
        return result;
    }

    private HashMap<String, String[]> getParameterMap(MoleculeRequest mr) {
        final HashMap<String, String[]> result = new HashMap<>();
        if (mr.getAuthentication() != null) {
            result.put(AUTHENTICATION_KEY, new String[]{mr.getAuthentication()});
        }
        if (mr.getAuthenticationType() != null) {
            result.put(AUTHENTICATION_TYPE_KEY, new String[]{mr.getAuthenticationType()});
        }
        return result;
    }

    private String getPath(DmxmlRequest request) {
        String path = request.getPath();
        return (path != null) ? path : defaultPath;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    private ResponseType unmarshal(String s) {
        final StreamSource ss = new StreamSource(new StringReader(s));
        return this.handler.unmarshal(ss, ResponseType.class);
    }
}
