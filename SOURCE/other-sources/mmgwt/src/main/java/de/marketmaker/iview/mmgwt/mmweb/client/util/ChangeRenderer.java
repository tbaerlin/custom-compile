/*
 * ChangeRenderer.java
 *
 * Created on 05.06.2008 17:37:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ChangeRenderer extends WrappingRenderer<String> {
    private final String zeroValue;
    public ChangeRenderer(Renderer<String> delegate) {
        super(delegate);
        this.zeroValue = delegate.render("0"); // $NON-NLS-0$
    }

    private boolean isZeroValue(String rendered) {
        return this.zeroValue.equals(rendered);
    }

    protected String getPrefix(String raw, String rendered) {
        if (raw.startsWith("-")) { // $NON-NLS-0$
            return "<span class=\"mm-diff-negative\">"; // $NON-NLS-0$
        }
        if (isZeroValue(rendered)) {
            return "<span class=\"mm-diff-equal\">"; // $NON-NLS-0$
        }
        return "<span class=\"mm-diff-positive\">+"; // $NON-NLS-0$
    }

    protected String getSuffix(String raw, String rendered) {
        return "</span>"; // $NON-NLS-0$
    }

    public String getDiffStyle(String raw) {
        if (raw == null) {
            return null;
        }
        final String rendered = render(raw);
        return getDiffStyle(raw, rendered);
    }

    private String getDiffStyle(String raw, String rendered) {
        if (raw == null) {
            return null;
        }
        return raw.startsWith("-") ? "negative" : (isZeroValue(rendered) ? "equal" : "positive"); // $NON-NLS$
    }

    public AbstractImagePrototype getDiffIcon(String raw) {
        final String rendered = getDelegate().render(raw);
        final String style = getDiffStyle(raw, rendered);
        if (style == null) {
            return null;
        }
        return IconImage.get("mm-diff-16-" + style); // $NON-NLS$
    }

}
