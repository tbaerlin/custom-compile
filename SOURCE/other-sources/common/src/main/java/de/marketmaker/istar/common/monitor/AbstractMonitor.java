/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package de.marketmaker.istar.common.monitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AbstractMonitor class is a useful base class which all Monitors
 * can extend. The particular monitoring policy is defined by the particular
 * implementation.
 */
abstract class AbstractMonitor {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The set of resources that the monitor is monitoring.
     */
    private final Map<String, Resource> resources = new HashMap<>();

    /**
     * Add an array of resources to monitor.
     * @param resources the resources to monitor
     */
    public final void addResources(final Resource[] resources) {
        for (Resource resource : resources) {
            addResource(resource);
        }
    }

    public final void setResources(final Resource[] resources) {
        synchronized (this.resources) {
            this.resources.clear();
        }
        for (Resource resource : resources) {
            addResource(resource);
        }
    }

    /**
     * Add a resource to monitor.  The resource key referenced in the other
     * interfaces is derived from the resource object.
     */
    public final void addResource(final Resource resource) {
        synchronized (this.resources) {
            final String resourceKey = resource.getResourceKey();
            if (resources.containsKey(resourceKey)) {
                final Resource original = this.resources.get(resourceKey);
                original.addPropertyChangeListenersFrom(resource);
            }
            else {
                resources.put(resourceKey, resource);
            }
        }
    }

    /**
     * Find a monitored resource.  If no resource is available, return null
     */
    public Resource getResource(final String key) {
        synchronized (this.resources) {
            return this.resources.get(key);
        }
    }

    /**
     * Remove a monitored resource by key.
     */
    public final void removeResource(final String key) {
        synchronized (this.resources) {
            final Resource resource = this.resources.remove(key);
            resource.removeAllPropertyChangeListeners();
        }
    }

    /**
     * Remove a monitored resource by reference.
     */
    public final void removeResource(final Resource resource) {
        removeResource(resource.getResourceKey());
    }

    /**
     * Return an array containing all the resources currently monitored.
     * @return an array containing all the resources currently monitored.
     */
    protected Resource[] getResources() {
        synchronized (this.resources) {
            final Collection collection = resources.values();
            return (Resource[]) collection.toArray(new Resource[collection.size()]);
        }
    }

    /**
     * Scan through all resources to determine if they have changed.
     */
    protected void scanAllResources() {
        final Resource[] resources = getResources();
        for (Resource resource : resources) {
            scanResource(resource);
        }
    }

    private void scanResource(Resource resource) {
        try {
            resource.testModifiedAfter(System.currentTimeMillis());
        } catch (Throwable t) {
            this.logger.error("<scanResource> failed for " + resource, t);
        }
    }
}
