/*
 * LargeLongRenderer.java
 *
 * Created on 08.09.2008 12:13:54
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Ulrich Maurer
 */
public class LargeLongRenderer implements Renderer<Long> {
    private Renderer<String> delegate;

    public LargeLongRenderer(Renderer<String> delegate) {
        this.delegate = delegate;
    }

    public String render(Long l) {
        return this.delegate.render(l == null ? null : l.toString());
    }
}
