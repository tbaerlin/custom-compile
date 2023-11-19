/*
 * VwdEntSelectorDefinitionProvider.java
 *
 * Created on 23.07.12 10:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTimeConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpMethod;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.marketmaker.istar.common.monitor.ActiveMonitor;

/**
 * @author Markus Dick
 */
@ManagedResource
public class VwdEntSelectorDefinitionProvider implements SelectorDefinitionProvider,
        InitializingBean, PropertyChangeListener {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final URI uri;

    private ActiveMonitor monitor;

    private int updatePeriodInMinutes = DateTimeConstants.MINUTES_PER_DAY;

    private final VwdEntSelectorDefinitionFactory factory = new VwdEntSelectorDefinitionFactory();

    private volatile Map<Integer, SelectorDefinition> selectorDefinitionsById = Collections.emptyMap();

    private RestTemplate restTemplate;

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public VwdEntSelectorDefinitionProvider() {
        this.uri = UriComponentsBuilder.fromHttpUrl(
                "http://vwd-ent:1968/vwdPermissions.asmx/SelectorDefinitions"
        ).build().toUri();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final PeriodicallyUpdatedResource resource = new PeriodicallyUpdatedResource("VwdEnt-SelectorDefinition-Service", updatePeriodInMinutes);
        resource.addPropertyChangeListener(this);
        if (this.monitor != null) {
            this.monitor.addResource(resource);
        }

        loadSelectorDefinitions();
    }

    public ActiveMonitor getActiveMonitor() {
        return monitor;
    }

    public void setActiveMonitor(ActiveMonitor monitor) {
        this.monitor = monitor;
    }

    public int getUpdatePeriodInMinutes() {
        return updatePeriodInMinutes;
    }

    public void setUpdatePeriodInMinutes(int updatePeriodInMinutes) {
        this.updatePeriodInMinutes = updatePeriodInMinutes;
    }

    protected Map<Integer, SelectorDefinition> readHttp() {
        try {
            return this.restTemplate.execute(this.uri, HttpMethod.GET, null, this.factory);

        } catch (Throwable t) {
            this.logger.warn("<readHttp> failed", t);
        }
        return null;
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     * and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.info("<propertyChange> Reloading SelectorDefinitions");
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
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * @return The number of loaded selector definitions.
     */
    @ManagedOperation
    public int loadSelectorDefinitions() {
        Map<Integer, SelectorDefinition> newDefinitions = readHttp();
        if (newDefinitions != null) {
            selectorDefinitionsById = newDefinitions;
            return selectorDefinitionsById.size();
        }
        else {
            logger.warn("<loadSelectorDefinitions> No new SelectorDefinitions read!");
            return -1;
        }
    }
}
