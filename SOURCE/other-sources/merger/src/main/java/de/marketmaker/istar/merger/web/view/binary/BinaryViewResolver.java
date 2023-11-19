/*
 * ImageViewResolver.java
 *
 * Created on 13.11.2006 12:25:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.view.binary;

import java.util.Locale;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

/**
 * Returns an ImageView whenever the requested view is {@link BinaryView#VIEW_NAME}.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BinaryViewResolver implements ViewResolver, Ordered {

    private int order = 0;

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    public View resolveViewName(String s, Locale locale) throws Exception {
        return BinaryView.VIEW_NAME.equals(s) ? BinaryView.INSTANCE : null;
    }
}
