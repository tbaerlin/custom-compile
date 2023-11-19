/*
 * PriceRenderer.java
 *
 * Created on 05.06.2008 17:04:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceStringRenderer implements Renderer<String> {
    private final String defaultValue;

    private final StringBasedNumberFormat stringBasedNumberFormat;

    public PriceStringRenderer() {
        this(StringBasedNumberFormat.DEFAULT, "--"); // $NON-NLS-0$
    }

    public PriceStringRenderer(StringBasedNumberFormat format, String defaultValue) {
        this.stringBasedNumberFormat = format;
        this.defaultValue = defaultValue;
    }

    public String render(String s) {
        return s == null ? this.defaultValue : this.stringBasedNumberFormat.format(s);
    }
}
