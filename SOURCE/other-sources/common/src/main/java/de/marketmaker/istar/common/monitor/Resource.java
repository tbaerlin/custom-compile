/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package de.marketmaker.istar.common.monitor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Managed Resource.  All resources must have a constructor that takes a String
 * and converts it to the needed format (i.e. File).  A Managed Resource in the
 * Monitor section has only one property needed to be changed: last modified.
 * The property name for the last modified event will be the same as the resource
 * key.  Implementations may add additional properties, but for most instances the
 * last modified property will be enough.
 */
public abstract class Resource
        implements Modifiable {
    protected static final String MODIFIED = "last-modified";

    /**
     * The set of listeners for this particular resource.
     */
    private final Set<PropertyChangeListener> propertyListeners = Collections.synchronizedSet(new HashSet<PropertyChangeListener>());

    private PropertyChangeSupport eventSupport = new PropertyChangeSupport(this);

    /**
     * The resource key is the identifier of the resource.
     * ie A FileResource would have the filename as the resourceKey.
     */
    private final String resourceKey;

    private long previousModified = 0L;

    /**
     * Required constructor.  The {@link String} location is transformed by
     * the specific resource monitor.  For instance, a FileResource will be able
     * to convert a string representation of a path to the proper File object.
     */
    public Resource(final String resourceKey) {
        if (null == resourceKey) {
            throw new NullPointerException("resourceKey");
        }

        this.resourceKey = resourceKey;
    }

    /**
     * Return the key for the resource.
     */
    public final String getResourceKey() {
        return resourceKey;
    }

    /**
     * The time this was last modified.
     */
    public abstract long lastModified();

    /**
     * Test whether this has been modified since time X
     */
    public void testModifiedAfter(final long time) {
        if (getPreviousModified() > time) {
            //The next line should be uncommented for complete
            //backward compatability. Unfortunately it does not
            //make sense to add it or else you could get multiple
            //notifications about a change.
            //fireAndSetModifiedTime( lastModified );
            return;
        }

        final long lastModified = lastModified();
        if (lastModified > getPreviousModified() || lastModified > time) {
            fireAndSetModifiedTime(lastModified);
        }
    }

    /**
     * Fire a modify event and set the lastModified time as appropriate.
     * @param lastModified the time modified at
     */
    protected void fireAndSetModifiedTime(final long lastModified) {
        try {
            getEventSupport().firePropertyChange(Resource.MODIFIED, getPreviousModified(), lastModified);
        } finally {
            setPreviousModified(lastModified);
        }
    }

    /**
     * Abstract method to add the PropertyChangeListeners in another Resource to
     * this one.
     */
    public void addPropertyChangeListenersFrom(final Resource other) {
        PropertyChangeListener[] listeners = (PropertyChangeListener[])
                other.propertyListeners.toArray(new PropertyChangeListener[]{});

        for (PropertyChangeListener listener : listeners) {
            addPropertyChangeListener(listener);
        }
    }

    /**
     * This is the prefered method of registering a {@link PropertyChangeListener}.
     * It automatically registers the listener for the last modified event.
     */
    public final void addPropertyChangeListener(final PropertyChangeListener listener) {
        getEventSupport().addPropertyChangeListener(listener);
        propertyListeners.add(listener);
    }

    /**
     * This is a convenience if you want to expose other properties for the Resource.
     * It is protected now, but you may override it with public access later.
     */
    protected void addPropertyChangeListener(final String property,
            final PropertyChangeListener listener) {
        getEventSupport().addPropertyChangeListener(property, listener);
        propertyListeners.add(listener);
    }

    /**
     * This is the prefered method of unregistering a {@link PropertyChangeListener}.
     * It automatically registers the listener for the last modified event.
     */
    public final void removePropertyChangeListener(final PropertyChangeListener listener) {
        getEventSupport().removePropertyChangeListener(listener);
        propertyListeners.remove(listener);
    }

    /**
     * This is a convenience if you want to expose other properties for the Resource.
     * It is protected now, but you may override it with public access later.
     */
    protected void removePropertyChangeListener(final String property,
            final PropertyChangeListener listener) {
        getEventSupport().removePropertyChangeListener(property, listener);
        propertyListeners.remove(listener);
    }

    /**
     * This is the preferred method of determining if a Resource has listeners.
     */
    public final boolean hasListeners() {
        return getEventSupport().hasListeners(getResourceKey());
    }

    /**
     * This cleanup method removes all listeners
     */
    public void removeAllPropertyChangeListeners() {
        PropertyChangeListener[] listeners = (PropertyChangeListener[])
                propertyListeners.toArray(new PropertyChangeListener[]{});

        for (PropertyChangeListener listener : listeners) {
            removePropertyChangeListener(listener);
        }
    }

    /**
     * This is a convenience if you want to expose other properties for the Resource.
     * It is protected now, but you may override it with public access later.
     */
    protected boolean hasListeners(final String property) {
        return getEventSupport().hasListeners(property);
    }

    protected final long getPreviousModified() {
        return previousModified;
    }

    protected final void setPreviousModified(final long previousModified) {
        this.previousModified = previousModified;
    }

    protected final PropertyChangeSupport getEventSupport() {
        return eventSupport;
    }

    public String toString() {
        return resourceKey;
    }
}
