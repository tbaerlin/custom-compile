/*
 * MultiRendererImpl.java
 *
 * Created on 05.06.2008 18:02:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MultiRendererImpl<T> implements MultiRenderer<T> {
    private final String divider;

    private final Renderer<T> delegate;

    private final String defaultValue;

    public MultiRendererImpl(Renderer<T> delegate, String divider, String defaultValue) {
        this.delegate = delegate;
        this.divider = divider;
        this.defaultValue = defaultValue;
    }

    public String render(T[] values) {
        if (values == null) {
            return this.defaultValue;
        }
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(divider);
            }
            sb.append(this.delegate.render(values[i]));
        }
        return sb.toString();
    }
}
