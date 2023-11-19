/*
 * RendererPipe.java
 *
 * Created on 19.02.14 08:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * Applies the first renderer to the given data item and then processes the resulting string with the other renderers.
 * @author Markus Dick
 */
public class RendererPipe<T> implements Renderer<T> {
    private final Renderer<T> first;
    private final Renderer<String>[] others;

    @SafeVarargs
    public RendererPipe(Renderer<T> first, Renderer<String>... others) {
        this.first = first;
        this.others = others;
    }

    @Override
    public String render(T t) {
        String s = this.first.render(t);
        if(this.others == null) {
            return s;
        }

        for(Renderer<String> r : this.others) {
            s = r.render(s);
        }
        return s;
    }
}
