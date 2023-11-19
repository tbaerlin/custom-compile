/*
 * Styles.java
 *
 * Created on 25.11.2015 11:32
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.style;

import com.google.gwt.user.client.ui.Widget;

import java.util.function.Consumer;

/**
 * @author mdick
 */
public final class Styles {
    private static LazyCssResource lazyCssResource;

    public static LazyCssResource get() {
        if(lazyCssResource == null) {
            lazyCssResource = new LazyLegacyCssResource();
        }
        return lazyCssResource;
    }

    public static void set(LazyCssResource lazyCssResource) {
        Styles.lazyCssResource = lazyCssResource;
    }

    public static <W extends Widget> W trySetStyle(W w, String style) {
        tryConsumeStyles(w, w::setStyleName, style);
        return w;
    }

    public static <W extends Widget> W tryAddStyles(W w, String... styles) {
        tryConsumeStyles(w, w::addStyleName, styles);
        return w;
    }

    public static <W extends Widget> W tryRemoveStyles(W w, String... styles) {
        tryConsumeStyles(w, w::removeStyleName, styles);
        return w;
    }

    private static <W extends Widget> W tryConsumeStyles(W w, Consumer<String> c, String... styles) {
        if(styles != null && styles.length > 0) {
            for (String style : styles) {
                if(style != null) {
                    c.accept(style);
                }
            }
        }
        return w;
    }

    private Styles() {
        // prevent instantiation
    }
}
