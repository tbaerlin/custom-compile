/*
 * EmbeddedJettyServer.java
 *
 * Created on 25.09.2008 16:54:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp.web;

import de.marketmaker.istar.common.spring.ApplicationObjectSupport;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import de.marketmaker.istar.common.util.PropertiesLoader;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;
import static org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE;

/**
 * Creates an embedded jetty servlet container and adds some servlets to it. There will be no
 * webapp classloader, it just uses the application's class loader.
 * <p>
 * If only a single servlet is needed, just set the properties <tt>servletName</tt>
 * and <tt>mappingPathSpec</tt>, and an appropriate {@link org.springframework.web.servlet.DispatcherServlet}
 * will be added. That servlet will use the same root application context that this object is
 * part of and expect its contextConfigLocation in <tt>"classpath:/"</tt> + servletName + <tt>"-servlet.xml"</tt>.
 * To use a different location (e.g., <tt>"file:${istar.home}/conf/"</tt>),
 * set {@link #contextConfigPath} accordingly
 * <p>
 * If several servlets are needed or you need to specify additional parameters for a single servlet,
 * these servlets can be specified in the property file {@link #servletPropertiesFile}.
 * The respective file has to be in the {@link java.util.Properties} file format
 * and can contain definitions for 1..n servlets as follows:
 * <pre>
 * servlet.1.name=foo
 * servlet.1.class=de.marketmaker.istar.merger.web.ZoneDispatcherServlet
 * servlet.1.mapping=*.foo
 * servlet.1.<em>param1=value1</em>
 * ...
 * servlet.n.name=bar
 * servlet.n.mapping=*.bar
 * </pre>
 * <tt>name</tt> and <tt>mapping</tt> are required for each servlet, <tt>class</tt> is optional and
 * defaults to <tt>org.springframework.web.servlet.DispatcherServlet</tt>, all other suffixes are
 * passed to the servlet as init parameters. If the parameters do not contain <tt>contextConfigLocation</tt>,
 * the default location (as defined above) will be added.<p>
 * In order to serve static content as well, just include the following servlet specification:
 * <pre>
 * servlet.i.name=default
 * servlet.i.class=org.mortbay.jetty.servlet.DefaultServlet
 * servlet.i.mapping=/
 * servlet.i.resourceBase=/path/to/htdocs/
 * </pre>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EmbeddedJettyServer extends ApplicationObjectSupport
        implements InitializingBean, SmartLifecycle {

    private int port = 8080;

    private Server webServer;

    private String contextPath = "/";

    private String servletName;

    private String contextConfigPath = "classpath:/";

    private String mappingPathSpec = "/*";

    private int maxFormContentSize = 1024 * 1024 * 8;

    private File servletPropertiesFile;

    private Properties servletProperties;

    private ServerConnector connector;

    private boolean running;

    public void setConnector(ServerConnector connector) {
        this.connector = connector;
    }

    public void setContextConfigPath(String contextConfigPath) {
        this.contextConfigPath = contextConfigPath;
        this.logger.info("<setContextConfigLocation> " + this.contextConfigPath);
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
        this.logger.info("<setContextPath> " + this.contextPath);
    }

    public void setServletPropertiesFile(File servletPropertiesFile) {
        this.servletPropertiesFile = servletPropertiesFile;
    }

    public void setPort(int port) {
        this.port = port;
        this.logger.info("<setPort> " + this.port);
    }

    public void setMaxFormContentSize(int maxFormContentSize) {
        this.maxFormContentSize = maxFormContentSize;
        this.logger.info("<setMaxFormContentSize> " + this.maxFormContentSize);
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
        this.logger.info("<setServletName> " + this.servletName);
    }

    public void setMappingPathSpec(String mappingPathSpec) {
        this.mappingPathSpec = mappingPathSpec;
        this.logger.info("<setMappingPathSpec> " + this.mappingPathSpec);
    }

    private String getContextConfigLocation(String s) {
        return this.contextConfigPath + s + "-servlet.xml";
    }

    public void afterPropertiesSet() throws Exception {
        if (this.servletPropertiesFile != null) {
            this.servletProperties = PropertiesLoader.load(this.servletPropertiesFile);
        }
        else if (this.servletName == null) {
            throw new IllegalArgumentException("undefined servletName and servletPropertiesFile");
        }

        // if we have a connector, it is possible to create everything now and start the connector
        // in the start() method; otherwise, initServer() has to be called during start() so that
        // clients cannot connect before everything has been set up
        if (this.connector != null) {
            initServer();
        }
    }

    private void initServer() throws Exception {
        if (this.connector != null) {
            this.webServer = new Server();
            this.webServer.setConnectors(new Connector[] { this.connector});
            this.port = this.connector.getPort();
        }
        else {
            this.webServer = new Server(this.port);
        }
        // Setup JMX
        MBeanContainer mbContainer = new MBeanContainer(
                ManagementFactory.getPlatformMBeanServer());
        this.webServer.addBean(mbContainer);

        this.webServer.setAttribute("org.eclipse.jetty.server.Request.maxFormContentSize",
                new Integer(this.maxFormContentSize));

        ServletHandler servletHandler = new ServletHandler();
        ServletContextHandler sch = new ServletContextHandler(webServer, this.contextPath,
                null, null, servletHandler, null, NO_SESSIONS);
        this.webServer.start();

        ServletContext servletContext = servletHandler.getServletContext();

        // Create a web application context whose parent is the
        // real application context
        GenericWebApplicationContext genericWebContext = new GenericWebApplicationContext();
        genericWebContext.setServletContext(servletContext);
        genericWebContext.setParent(getApplicationContext());
        genericWebContext.refresh();

        // Bind the created web application context to the servlet
        // context
        servletContext.setAttribute(ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, genericWebContext);

        if (this.servletName != null) {
            final Map<String, String> params =
                    Collections.singletonMap("contextConfigLocation", getContextConfigLocation(this.servletName));
            addServlet(servletHandler, 1, params, this.servletName, this.mappingPathSpec, new DispatcherServlet());
        }
        if (this.servletProperties != null) {
            addFiltersFromProperties(servletHandler);
            addServletsFromProperties(servletHandler);
        }
    }

    public void start() {
        try {
            if (this.connector != null) {
                this.connector.start();
            }
            else {
                initServer();
            }
            this.logger.info("<start> on port " + this.port);
            this.running = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isAutoStartup() {
        return false;
    }

    @Override
    public void stop(Runnable callback) {
        try {
            stop();
        } finally {
            callback.run();
        }
    }

    @Override
    public int getPhase() {
        // ensure all components we want to talk to have already started
        return Integer.MAX_VALUE - 5;
    }

    public void stop() {
        try {
            this.webServer.stop();
            if (this.connector != null) {
                this.connector.stop();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    private void addServletsFromProperties(ServletHandler servletHandler) throws Exception {
        int n = 1;
        Map<String, String> params;
        while (!(params = getServletParams(n)).isEmpty()) {
            final String name = params.remove("name");
            if (name == null) {
                throw new IllegalArgumentException("No name for servlet " + n);
            }
            final String className = params.remove("class");
            final String pathSpec = params.remove("mapping");
            if (pathSpec == null) {
                throw new IllegalArgumentException("No mapping for servlet " + n);
            }

            if (!params.containsKey("contextConfigLocation")) {
                params.put("contextConfigLocation", getContextConfigLocation(name));
            }

            final Servlet servlet = createServlet(className);
            addServlet(servletHandler, n, params, name, pathSpec, servlet);
            n++;
        }
    }

    private void addFiltersFromProperties(ServletHandler handler) throws Exception {
        int n = 1;
        Map<String, String> params;
        while (!(params = getFilterParams(n)).isEmpty()) {
            final String name = params.remove("name");
            if (name == null) {
                throw new IllegalArgumentException("No name for filter " + n);
            }
            final String className = params.remove("class");
            final String pathSpec = params.remove("mapping");
            final String servletName = params.remove("servletName");
            if (pathSpec == null && servletName == null) {
                throw new IllegalArgumentException("No servletName or pathSpec for filter " + n);
            }

            final FilterHolder holder = new FilterHolder();
            holder.setClassName(className);
            holder.setName(name);
            holder.setInitParameters(params);

            final FilterMapping mapping = new FilterMapping();
            mapping.setFilterName(name);
            if (pathSpec != null) {
                mapping.setPathSpecs(pathSpec.split(","));
            }
            if (servletName != null) {
                mapping.setServletNames(servletName.split(","));
            }

            handler.addFilter(holder, mapping);
            n++;
        }
    }

    private void addServlet(ServletHandler servletHandler, int n, Map<String, String> params,
            String name, String pathSpec, Servlet servlet) {

        final ServletHolder holder = new ServletHolder(servlet);
        holder.setName(name);
        holder.setInitOrder(n);
        holder.setInitParameters(params);
        servletHandler.addServlet(holder);

        final String[] specs = pathSpec.split(";");
        for (String spec : specs) {
            final ServletMapping mapping = new ServletMapping();
            mapping.setServletName(name);
            mapping.setPathSpec(spec);
            servletHandler.addServletMapping(mapping);
        }

        this.logger.info("<addServlet> added " + name + ", mapped to " + pathSpec);
    }

    private Map<String, String> getServletParams(int n) {
        return getParams("servlet." + n + ".");
    }

    private Map<String, String> getFilterParams(int n) {
        return getParams("filter." + n + ".");
    }

    private Map<String, String> getParams(String prefix) {
        final Map<String, String> result = new HashMap<>();
        for (Object o : this.servletProperties.keySet()) {
            final String key = (String) o;
            if (!key.startsWith(prefix)) {
                continue;
            }
            final String name = key.substring(prefix.length());
            result.put(name, this.servletProperties.getProperty(key));
        }
        return result;
    }

    private Servlet createServlet(
            String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (className == null) {
            return new DispatcherServlet();
        }
        final Class<?> clazz = Class.forName(className);
        return (Servlet) clazz.newInstance();
    }

    public static void main(String[] args) throws Exception {
        EmbeddedJettyServer jetty = new EmbeddedJettyServer();
        jetty.setPort(8080);
        jetty.setContextPath("/dmxml-1");
        jetty.setContextConfigPath("file:/Users/oflege/produktion/prog/istar-news/conf/ins/");
        jetty.setServletPropertiesFile(new File("/Users/oflege/produktion/prog/istar-news/conf/ins/servlets.properties"));
        jetty.afterPropertiesSet();
        jetty.start();
    }
}
