/*
 * StringRendererFormatter.java
 *
 * Created on 20.12.12 14:39
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.formatters;

import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Markus Dick
 */
public class StringRendererFormatter implements Formatter<String> {
    private final Renderer<String> renderer;

    public StringRendererFormatter(Renderer<String> renderer) {
        this.renderer = renderer;
    }

    @Override
    public String format(String s) {
        if(StringUtil.hasText(s)) {
            try {
                return this.renderer.render(renderer.render(s));
            }
            catch(NumberFormatException e) {
                return s;
            }
        }
        return s;
    }
}
