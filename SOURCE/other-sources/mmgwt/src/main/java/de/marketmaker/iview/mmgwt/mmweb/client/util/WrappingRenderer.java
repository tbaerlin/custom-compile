/*
 * WrappingRenderer.java
 *
 * Created on 05.06.2008 18:10:20
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class WrappingRenderer<T> implements Renderer<T> {

    private final Renderer<T> delegate;

    protected WrappingRenderer(Renderer<T> delegate) {
        this.delegate = delegate;
    }

    protected Renderer<T> getDelegate() {
        return delegate;
    }

    public String render(T t) {
        if (t == null) {
            return getUndefined();
        }
        final String inner = this.delegate.render(t);
        return getPrefix(t, inner) + inner + getSuffix(t, inner);
    }

    protected abstract String getPrefix(T raw, String rendered);

    protected abstract String getSuffix(T raw, String rendered);
    
    protected String getUndefined() {
        return this.delegate.render(null);
    }
}
