/*
 * CachingTickDirectoryProvider.java
 *
 * Created on 01.04.15 14:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.feed.ordered.tick.TickDirectory;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * @author oflege
 */
@ManagedResource
public class CachingTickDirectoryProvider implements TickDirectoryProvider, InitializingBean {
    private static final FileFilter DATE_DIR_FILTER
            = dir -> dir.isDirectory() && dir.getName().matches("20\\d{6}");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Path baseDirectoriesFile;

    private volatile List<File> baseDirectories = Collections.emptyList();

    private Ehcache cache;

    public void setBaseDirectoriesFile(File baseDirectoriesFile) {
        if (!baseDirectoriesFile.canRead()) {
            throw new IllegalArgumentException("cannot read " + baseDirectoriesFile.getAbsolutePath());
        }
        this.baseDirectoriesFile = baseDirectoriesFile.toPath();
    }

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    public void setBaseDirectories(String[] baseDirectoryNames) {
        this.baseDirectories = Arrays.stream(baseDirectoryNames).map(File::new).collect(toList());
    }

    @ManagedAttribute
    public String getBaseDirectoriesStr() {
        return getBaseDirectoriesStr(this.baseDirectories);
    }

    private String getBaseDirectoriesStr(List<File> dirs) {
        return dirs.stream()
                .map(f -> f.isDirectory() ? f.getAbsolutePath() : f.getAbsolutePath() + "(?)")
                .collect(joining("', '", "'", "'"));
    }

    public void afterPropertiesSet() throws Exception {
        if (this.baseDirectoriesFile != null) {
            this.baseDirectories = readBaseDirectoriesFile();
        }
        if (this.baseDirectories == null || this.baseDirectories.isEmpty()) {
            throw new IllegalStateException("neither baseDirectories nor baseDirectoriesFile set");
        }
        checkBaseDirectories(this.baseDirectories);
    }

    protected void checkBaseDirectories(final List<File> dirs) {
        final Optional<File> validDir = dirs.stream()
                .filter(d -> d.isDirectory() && d.listFiles(DATE_DIR_FILTER).length > 0).findFirst();
        if (!validDir.isPresent()) {
            throw new IllegalStateException("no valid baseDirectory in: " + getBaseDirectoriesStr(dirs));
        }
    }

    @ManagedOperation
    public String reloadBaseDirectories() throws IOException {
        if (this.baseDirectoriesFile == null) {
            throw new UnsupportedOperationException("no baseDirectoriesFile defined");
        }
        final List<File> newBaseDirectories = readBaseDirectoriesFile();
        checkBaseDirectories(newBaseDirectories);
        this.baseDirectories = newBaseDirectories;
        return getBaseDirectoriesStr(newBaseDirectories);
    }

    protected List<File> readBaseDirectoriesFile() throws IOException {
        this.logger.info("<readBaseDirectoriesFile> from '" + this.baseDirectoriesFile.toAbsolutePath() + "'");
        return Files.lines(this.baseDirectoriesFile)
                .filter(s -> StringUtils.hasText(s) && !s.startsWith("#"))
                .map(File::new)
                .peek(f -> {
                    if (!f.isDirectory()) {
                        this.logger.warn("<readBaseDirectoriesFile> not a directory: '"
                                + f.getAbsolutePath() + "'");
                    }
                })
                .collect(toList());
    }

    @Override
    public TickDirectory getDirectory(int key) {
        final Element element = this.cache.get(key);
        if (element != null) {
            final TickDirectory cached = (TickDirectory) element.getValue();
            if (cached.isValid()) {
                TickDirectory refreshed = cached.refresh();
                if (refreshed != cached) {
                    this.cache.put(new Element(key, refreshed));
                    return refreshed;
                }
                return cached;
            }
            this.cache.remove(key);
        }

        File dir = findDirectory(Integer.toString(key));
        TickDirectory result = TickDirectory.open(dir);
        if (result != null) {
            this.cache.put(new Element(key, result));
        }
        return result;
    }

    private File findDirectory(String yyyymmdd) {
        for (File baseDir : this.baseDirectories) {
            final File d = new File(baseDir, yyyymmdd);
            if (!d.exists()) {
                continue;
            }
            if (d.isDirectory()) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<findDirectory> found " + d.getAbsolutePath());
                }
                return d;
            }
            this.logger.warn("<findDirectory> not a directory " + d.getAbsolutePath());
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<findDirectory> no directory found for '" + yyyymmdd + "'");
        }
        return null;
    }

    @ManagedOperation
    public String dumpDirCache() {
        StringBuilder sb = new StringBuilder();
        for (Object key : this.cache.getKeysWithExpiryCheck()) {
            final Element e = this.cache.getQuiet(key);
            if (e != null) {
                TickDirectory f = (TickDirectory) e.getValue();
                sb.append(key)
                        .append(" -> ")
                        .append(f.getDir().getAbsolutePath())
                        .append(" | ")
                        .append(e.getHitCount())
                        .append(" | ")
                        .append(new DateTime(e.getLastAccessTime()))
                        .append('\n');
            }
        }
        return sb.toString();
    }
}
