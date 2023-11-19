/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package de.marketmaker.istar.common.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Managed Stream based Resource.  This is convenient when you want to dynamically
 * set and get the information from the resource.  For instance, the Resource does
 * not need to be actively monitored if all access to the resource goes through
 * this type of Resource.  It can notify the change as soon as the Writer or
 * OutputStream has been closed.
 */
public abstract class StreamResource extends Resource {
    /**
     * Required constructor.  The <code>String</code> location is transformed by
     * the specific resource monitor.  For instance, a FileResource will be able
     * to convert a string representation of a path to the proper File object.
     */
    public StreamResource(final String location) {
        super(location);
    }

    /**
     * Get the Resource contents as an InputStream.
     */
    public abstract InputStream getResourceAsStream()
            throws IOException;

    /**
     * Get the Resource contents as a Reader.
     */
    public abstract Reader getResourceAsReader()
            throws IOException;
}
