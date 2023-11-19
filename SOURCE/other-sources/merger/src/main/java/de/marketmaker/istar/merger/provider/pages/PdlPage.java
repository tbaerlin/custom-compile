/*
 * PdlPage.java
 *
 * Created on 16.06.2005 13:37:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class PdlPage {
    private final String version;

    private final String name;

    private final String title;

    /**
     * objects appearing on this page sorted from top-left to bottom-right.
     */
    private final Set<PdlObject> objects;

    private final int width;

    private final int height;

    /**
     * Creates new PdlPage
     * @param version attribute version
     * @param name the name of the page; has to be a page number
     * @param title the page title
     * @param width width of the page
     * @param height height of the page
     * @param objects objects appearing on this page
     */
    public PdlPage(String version, String name, String title, int width, int height, List<PdlObject> objects) {
        this.version = version;
        this.name = name;
        this.title = title;
        this.width = width;
        this.height = height;
        this.objects = Collections.unmodifiableSet(new TreeSet<>(objects));
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * get an iterator for the pdl objects of the page
     * @return java.util.Iterator for the pdl objects
     */
    public Iterable<PdlObject> getObjects() {
        return this.objects;
    }
}
