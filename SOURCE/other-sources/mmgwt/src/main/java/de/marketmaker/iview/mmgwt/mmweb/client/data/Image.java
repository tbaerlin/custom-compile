/*
 * Image.java
 *
 * Created on 04.09.2008 14:48:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.data;

/**
 * @author Ulrich Maurer
 */
public class Image {
    private String prefix = null;
    private final String styleName;
    private String suffix = null;

    public Image(String styleName) {
        this.styleName = styleName;
    }

    public Image withPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public boolean hasPrefix() {
        return this.prefix != null;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getStyleName() {
        return styleName;
    }

    public Image withSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public boolean hasSuffix() {
        return this.suffix != null;
    }

    public String getSuffix() {
        return suffix;
    }
}
