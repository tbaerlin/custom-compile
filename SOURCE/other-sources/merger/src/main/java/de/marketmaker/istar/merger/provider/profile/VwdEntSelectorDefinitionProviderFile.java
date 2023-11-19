/*
 * VwdEntSelectorDefinitionProviderFile.java
 *
 * Created on 20.07.12 15:12
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.JDOMException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/**
 * This provider may be useful for development and debugging purposes.
 * @author Markus Dick
 */
@ManagedResource
public class VwdEntSelectorDefinitionProviderFile implements SelectorDefinitionProvider, InitializingBean, PropertyChangeListener {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String filename = null;
    private ActiveMonitor monitor;

    private final VwdEntSelectorDefinitionFactory selectorDefinitionFactory = new VwdEntSelectorDefinitionFactory();

    private volatile Map<Integer, SelectorDefinition> selectorDefinitionsById = Collections.EMPTY_MAP;

    public VwdEntSelectorDefinitionProviderFile() {
        super();
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if(filename != null) {
            final FileResource fileResource = new FileResource(filename);
            fileResource.addPropertyChangeListener(this);
            if (this.monitor != null) {
                this.monitor.addResource(fileResource);
            }
            loadSelectorDefinitions();
        }
        else {
            logger.warn("<afterPropertiesSet> No selector definition filename set! Nothing to load.");
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public ActiveMonitor getActiveMonitor() {
        return monitor;
    }

    public void setActiveMonitor(ActiveMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * @return the selector definitions identified by their ID
     * @throws IllegalStateException if file name is not set
     */
    protected Map<Integer, SelectorDefinition> read() throws IllegalStateException {
        if(filename == null) throw new IllegalStateException("No selector definition file set!");

        InputStream is = null;

        try {
            is = new FileInputStream(filename);
            return selectorDefinitionFactory.read(is);
        }
        catch(JDOMException jdome) {
            logger.error(jdome.toString());
        }
        catch(IOException ioe) {
            logger.error(ioe.toString());
        }
        finally {
            IoUtils.close(is);
        }

        return null;
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        loadSelectorDefinitions();
    }

    /**
     * @param id a selector
     * @return the SelectorDefinition or null.
     */
    @Override
    public SelectorDefinition getSelectorDefinition(int id) {
        try {
            return selectorDefinitionsById.get(id);
        }
        catch(NullPointerException e) {
            return null;
        }
    }

    @ManagedOperation
    public void loadSelectorDefinitions() {
        Map<Integer, SelectorDefinition> newDefinitions = read();
        if(newDefinitions != null) {
            selectorDefinitionsById = newDefinitions;
        }
        else {
            logger.warn("No new SelectorDefinitions read!");
        }
    }
}
