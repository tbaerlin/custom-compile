/*
 * GWTRPCServiceExporter.java
 *
 * Created on 05.03.2008 14:03:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server;

import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import de.marketmaker.iview.mmgwt.mmweb.client.MmwebServiceException;

/**
 * Subclass of GWT's RemoteServiceServlet which expects <em>not</em> to be registered in
 * <tt>web.xml</tt> but instead be instantiated in a spring context. The basic flow of events is that the
 * request is accepted by a spring DispatcherServlet and dispatched to an instance of
 * {@link de.marketmaker.iview.mmgwt.mmweb.server.RemoteServiceHandler}, which in turn dispatches
 * the request to us.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class GwtService extends RemoteServiceServlet
        implements ServletContextAware, BeanNameAware, InitializingBean {

    protected final Log logger = LogFactory.getLog(getClass());

    private SerializationPolicyResolver serializationPolicyResolver;

    private ServletContext servletContext;

    private boolean withCodeServer;

    private String name;

    @Override
    public void setBeanName(String s) {
        this.name = s;
    }

    public void setSerializationPolicyResolver(
            SerializationPolicyResolver serializationPolicyResolver) {
        this.serializationPolicyResolver = serializationPolicyResolver;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void afterPropertiesSet() throws Exception {
        init(new ServletConfig() {
            @Override
            public String getServletName() {
                return name + "Servlet";
            }

            @Override
            public ServletContext getServletContext() {
                return servletContext;
            }

            @Override
            public String getInitParameter(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.emptyEnumeration();
            }
        });

        this.withCodeServer = getCodeServerPolicyUrl("") != null;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    protected void doUnexpectedFailure(Throwable failure) {
        if (!causedByIEBug(failure, getThreadLocalResponse())) {
            super.doUnexpectedFailure(failure);
        }
    }

    /* IE8 sends POST requests with 0 content length and causes exception in GWT service.
       Since we can't fix this bug, we suppress error logs. */
    public static boolean causedByIEBug(Throwable failure, HttpServletResponse response) {
        boolean isIEBug = failure.getMessage() != null && failure instanceof IllegalArgumentException &&
                failure.getMessage().equals("encodedRequest cannot be empty");

        if (isIEBug) {
            if (!response.isCommitted()) {
                response.reset();
            }

            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        return isIEBug;
    }

    /**
     * @param request
     * @param moduleBaseURL
     * @param strongName
     * @return
     */
    @Override
    protected SerializationPolicy doGetSerializationPolicy(
            HttpServletRequest request, String moduleBaseURL, String strongName) {
        SerializationPolicy result = this.serializationPolicyResolver.getSerializationPolicy(strongName);
        if (result == null && !this.withCodeServer) {
            this.logger.error("<doGetSerializationPolicy> policy file not found: '"
                    + strongName + ".gwt.rpc' - request from " + request.getRemoteAddr());
            // if we would not throw this exception, the default serialization policy would be
            // used, which is not what we want
            throw new MmwebServiceException("Server error: serializationPolicyFile not found ("
                    + strongName + ")");
        }
        return result;
    }
}
