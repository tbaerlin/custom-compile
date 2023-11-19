/*
 * Link.java
 *
 * Created on 08.08.2008 15:23:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.data;

import com.google.gwt.user.client.ui.AbstractImagePrototype;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

/**
 * @author Ulrich Maurer
 */
public class Link {
    private final LinkListener<Link> listener;
    private final String href;
    private final String target;
    private final String text;
    private final String tooltip;
    private String style = null;
    private Object data;
    private AbstractImagePrototype icon;
    private String iconTooltip;

    public Link(LinkListener<Link> listener, String text, String tooltip) {
        this.listener = listener;
        this.href = null;
        this.target = null;
        this.text = text;
        this.tooltip = tooltip;
    }

    public Link(String href, String target, String tooltip, String text) {
        this.listener = null;
        this.href = href;
        this.target = target;
        this.text = text;
        this.tooltip = tooltip;
    }

    public LinkListener<Link> getListener() {
        return listener;
    }

    public String getHref() {
        return href;
    }

    public String getTarget() {
        return target;
    }

    public String getText() {
        return text;
    }

    public String getTooltip() {
        return tooltip;
    }

    public Link withStyle(String style) {
        this.style = style;
        return this;
    }

    public String getStyle() {
        return style;
    }

    public Object getData() {
        return data;
    }

    public AbstractImagePrototype getIcon() {
        return icon;
    }

    public String getIconTooltip() {
        return iconTooltip;
    }

    public Link withData(Object data) {
        this.data = data;
        return this;
    }

    public Link withIcon(String iconMapping, String tooltip) {
        this.icon = IconImage.get(iconMapping);
        this.iconTooltip = tooltip;
        return this;
    }

    public Link withIcon(AbstractImagePrototype icon, String tooltip) {
        this.icon = icon;
        this.iconTooltip = tooltip;
        return this;
    }


}
