/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package de.marketmaker.istar.common.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Managed File based Resource.  This is convenient when you want to dynamically
 * set and get the information from the resource.  For instance, the Resource does
 * not need to be actively monitored if all access to the resource goes through
 * this type of Resource.  It can notify the change as soon as the Writer or
 * OutputStream has been closed.
 */
public class FileResource extends StreamResource {
    private final File file;

    /**
     * Instantiate the FileResource
     */
    public FileResource(final String resource) {
        this(new File(resource));
    }

    public FileResource(final File resource) {
        super(resource.getAbsolutePath());
        file = resource;
        setPreviousModified(lastModified());
    }

    public File getFile() {
        return file;
    }

    /**
     * Determines the last time this resource was modified
     */
    public long lastModified() {
        return file.lastModified();
    }

    /**
     * Sets the resource value with an OutputStream
     */
    public InputStream getResourceAsStream()
            throws IOException {
        return new FileInputStream(file);
    }

    /**
     * Sets the resource value with a Writer
     */
    public Reader getResourceAsReader()
            throws IOException {
        return new FileReader(file);
    }

    @Override
    public String toString() {
        return this.file.getAbsolutePath();
    }
}
