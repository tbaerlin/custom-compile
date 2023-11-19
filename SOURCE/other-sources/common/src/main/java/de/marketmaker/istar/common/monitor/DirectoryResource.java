/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */
package de.marketmaker.istar.common.monitor;

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;

import de.marketmaker.istar.common.util.FileUtil;

/**
 * This is a Resource that monitors a directory. If any files
 * are added, removed or modified in directory then it will
 * send an event indicating the change.
 */
public class DirectoryResource extends Resource {
    public static final String ADDED = "AddedFiles";

    public static final String REMOVED = "DeletedFiles";

    public static final String MODIFIED = "ModifiedFiles";

    private final File dir;

    private Map<File, Long> times;

    private FileFilter filter;

    private final boolean recursive;

    public DirectoryResource(final String resourceKey) throws Exception {
        this(resourceKey, null);
    }

    public DirectoryResource(final String resourceKey, final FileFilter filter) throws Exception {
        this(resourceKey, filter, false);
    }

    public DirectoryResource(final String resourceKey, final FileFilter filter, boolean recursive)
            throws Exception {
        super(resourceKey);
        this.dir = new File(resourceKey);
        if (!this.dir.isDirectory()) {
            throw new IllegalArgumentException(this.dir.getAbsolutePath() + " is not a directory.");
        }
        this.filter = filter;
        this.recursive = recursive;

        this.times = new HashMap<>();

        final File[] files = listFiles();
        for (final File file : files) {
            this.times.put(file, file.lastModified());
        }
        setPreviousModified(System.currentTimeMillis());
    }

    private File[] listFiles() {
        if (this.recursive) {
            final List<File> allFiles = FileUtil.listAllFiles(this.dir, this.filter);
            return allFiles.toArray(new File[allFiles.size()]);
        }
        return this.dir.listFiles(this.filter);
    }

    /**
     * Returns the set of files that currently populate the monitored directory
     * @return set of files
     */
    public synchronized Set<File> getFiles() {
        return new HashSet<>(this.times.keySet());
    }

    /**
     * Test whether this has been modified since time X
     */
    public synchronized void testModifiedAfter(final long time) {
        if (getPreviousModified() > time) {
            return;
        }

        final File[] currentFiles = listFiles();

        final Set<File> deletedFiles = new HashSet<>(this.times.keySet());
        deletedFiles.removeAll(Arrays.asList(currentFiles));
        this.times.keySet().removeAll(deletedFiles);

        final Set<File> addedFiles = new HashSet<>(Arrays.asList(currentFiles));
        addedFiles.removeAll(this.times.keySet());

        final Set<File> modifiedFiles = computeModifiedFiles(currentFiles);

        fireEvent(REMOVED, deletedFiles);
        fireEvent(ADDED, addedFiles);
        fireEvent(MODIFIED, modifiedFiles);
    }

    private void fireEvent(final String propertyName, Set<File> files) {
        if (!files.isEmpty()) {
            getEventSupport().firePropertyChange(propertyName, Collections.EMPTY_SET, files);
        }
    }

    private Set<File> computeModifiedFiles(File[] currentFiles) {
        final Set<File> result = new HashSet<>();
        for (final File file : currentFiles) {
            final long lastModified = file.lastModified();
            if (this.times.containsKey(file)) {
                final long oldLastModified = this.times.get(file);
                if (oldLastModified != lastModified) {
                    result.add(file);
                }
            }
            this.times.put(file, lastModified);
        }
        return result;
    }

    public long lastModified() {
        return getPreviousModified();
    }
}
