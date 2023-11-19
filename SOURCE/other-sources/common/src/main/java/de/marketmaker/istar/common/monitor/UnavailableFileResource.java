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
 * Managed File based Resource. The file does not necessarily already exist.
 * As soon as it is created or deleted, the Monitor will recognize it.
 */
public class UnavailableFileResource extends FileResource {
    private long unavailableMillis = -1;

    /**
     * Instantiate the UnavailableFileResource
     */
    public UnavailableFileResource(final String resource) {
        super(resource);
    }

    public UnavailableFileResource(final File resource) {
        super(resource);
    }

    /**
     * Determines the last time this resource was modified
     */
    public long lastModified() {
        if (this.getFile().isFile()) {
            this.unavailableMillis = -1;
            return this.getFile().lastModified();
        }
        if (this.unavailableMillis == -1) {
            this.unavailableMillis = System.currentTimeMillis();
        }
        return this.unavailableMillis;
    }
}
