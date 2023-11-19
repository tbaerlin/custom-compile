/*
 * DesktopIcon.java
 *
 * Created on 14.08.2008 13:31:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.desktop;

import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

/**
 * @author Ulrich Maurer
 */
public class DesktopIcon<D> {
    private final String iconStyle;
    private final String imageUrl;
    private final String[] subText;
    private final D data;
    private final LinkListener<D> listener;
    private final String href;

    public DesktopIcon(String iconStyle, String[] subText, D data, LinkListener<D> listener) {
        this(iconStyle, null, subText, data, listener);
    }

    public DesktopIcon(String iconStyle, String imageUrl, String[] subText, D data, LinkListener<D> listener) {
        this.iconStyle = iconStyle;
        this.imageUrl = imageUrl;
        this.subText = subText;
        this.data = data;
        this.listener = listener;
        this.href = null;
    }

    public DesktopIcon(String iconStyle, String[] subText, String href) {
        this.iconStyle = iconStyle;
        this.imageUrl = null;
        this.subText = subText;
        this.data = null;
        this.listener = null;
        this.href = href;
    }

    public D getData() {
        return data;
    }

    public String getIconStyle() {
        return iconStyle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LinkListener<D> getListener() {
        return listener;
    }

    public String[] getSubText() {
        return subText;
    }

    public String getHref() {
        return href;
    }
}
