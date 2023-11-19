package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * Created on 14.05.13 10:24
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

@NonNLS
public class Dimension {
    int width;
    int height;

    public Dimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dimension)) return false;

        final Dimension dimension = (Dimension) o;

        if (height != dimension.height) return false;
        return width == dimension.width;
    }

    @Override
    public int hashCode() {
        int result = height;
        result = 31 * result + width;
        return result;
    }

    @Override
    public String toString() {
        return "Dimension{" +
                "width=" + width +
                ", height=" + height +
                '}';
    }
}
