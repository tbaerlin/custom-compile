/*
 * EasytradeHandlerMapping.java
 *
 * Created on 16.08.2006 09:11:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeansException;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.block.AtomController;

/**
 * Map requests to {@link AtomController}s. If a request has a parameter <em>controllerName</em>,
 * the value of that parameter is used to find a matching controller; otherwise, the request's
 * filename part is used (that is, both /foo/bar.html and /bar.html would be mapped to the same
 * controller).
 * <p>
 * Name to controller mappings can either be specified explicitly by calling
 * {@link #setMappings(java.util.Map)}. If that property is not set, this bean will look in its
 * application context (but <em>not</em> its parent context) for <tt>AtomController</tt> instances
 * and use the respective bean name for the mapping. If not all <tt>AtomController</tt>s in the
 * context should be used, restrictions can be specified by calling {@link #setExcludedMappings(String)}
 * and/or {@link #setIncludedMappings(String)}.<br> Regardless of the way the mappings are
 * defined, this bean will then look for alias definitions in the spring context and add
 * all aliases the the respective controller as well.
 * <p>
 * A MoleculeController makes sure that each request for an atom has a requestURI with a filename
 * that matches an atom name; so naming atom controllers with atom names does the trick.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EasytradeHandlerMapping extends AbstractHandlerMapping implements AtomControllerMapping {

    public static final String CONTROLLER_NAME = "controllerName";

    private Map<String, AtomController> mappings = new ConcurrentHashMap<>();

    private Pattern excludedMappings = Pattern.compile("");

    private Pattern includedMappings = Pattern.compile(".+");

    public void setExcludedMappings(String regex) {
        this.excludedMappings = Pattern.compile(regex);
    }

    public void setIncludedMappings(String regex) {
        this.includedMappings = Pattern.compile(regex);
    }

    public void setMappings(Map<String, AtomController> mappings) {
        this.mappings.putAll(mappings);
    }

    protected void initApplicationContext() throws BeansException {
        super.initApplicationContext();

        if (this.mappings.isEmpty()) {
            getMappingsFromContext();
        }
        addMappingAliases();

        this.logger.info("<initApplicationContext> mappings = "
            + new TreeSet<>(this.mappings.keySet()));
    }

    private void addMappingAliases() {
        for (Map.Entry<String, AtomController> e : this.mappings.entrySet()) {
            final String[] aliases = getApplicationContext().getAliases(e.getKey());
            for (String alias : aliases) {
                this.mappings.put(alias, e.getValue());
            }
        }
    }

    private void getMappingsFromContext() {
        final Map<String, AtomController> beans
                = getApplicationContext().getBeansOfType(AtomController.class);

        for (Map.Entry<String, AtomController> e : beans.entrySet()) {
            if (isAcceptable(e.getKey())) {
                this.mappings.put(e.getKey(), e.getValue());
            }
        }
    }

    private boolean isAcceptable(String name) {
        return this.includedMappings.matcher(name).matches()
                && !this.excludedMappings.matcher(name).matches();
    }

    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        return getController(request);
    }

    @Override
    public AtomController getController(HttpServletRequest request) {
        return getAtomController(getControllerName(request));
    }

    @Override
    public AtomController getAtomController(String controllerName) {
        return this.mappings.get(controllerName);
    }

    private String getControllerName(HttpServletRequest request) {
        final String controllerName = request.getParameter(CONTROLLER_NAME);
        if (controllerName != null) {
            return controllerName;
        }

        return HttpRequestUtil.getRequestName(request);
    }
}
