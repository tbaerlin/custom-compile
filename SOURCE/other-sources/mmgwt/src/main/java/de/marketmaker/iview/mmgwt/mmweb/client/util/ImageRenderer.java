/*
 * ImageRenderer.java
 *
 * Created on 04.09.2008 15:00:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.mmgwt.mmweb.client.data.Image;

/**
 * @author Ulrich Maurer
 */
public class ImageRenderer implements Renderer<Image> {
    public String render(Image image) {
        if (image == null) {
            return ""; // $NON-NLS-0$
        }
        final StringBuilder sb = new StringBuilder();
        if (image.hasPrefix()) {
            sb.append(image.getPrefix());
        }
        sb.append("<div class=\"").append(image.getStyleName()).append("\"/>"); // $NON-NLS-0$ $NON-NLS-1$
        if (image.hasSuffix()) {
            sb.append(image.getSuffix());
        }
        return sb.toString();
    }
}
