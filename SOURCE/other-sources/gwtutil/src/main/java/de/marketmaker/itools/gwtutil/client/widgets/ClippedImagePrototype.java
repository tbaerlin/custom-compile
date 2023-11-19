package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.user.client.ui.Image;

/**
 * Author: umaurer
 * Created: 01.07.15
 */
public class ClippedImagePrototype extends com.google.gwt.user.client.ui.impl.ClippedImagePrototype {
    private final String iconClass;

    public ClippedImagePrototype(String iconClass, String url, int left, int top, int width, int height) {
        super(url, left, top, width, height);
        this.iconClass = iconClass;
    }

    @Override
    public void applyTo(Image image) {
        image.getElement().setAttribute("iconClass", this.iconClass);
        super.applyTo(image);
    }

    @Override
    public Image createImage() {
        final Image image = super.createImage();
        image.getElement().setAttribute("iconClass", this.iconClass);
        return image;
    }

    @Override
    public void applyTo(ImagePrototypeElement imageElement) {
        imageElement.setAttribute("iconClass", this.iconClass);
        super.applyTo(imageElement);
    }

    @Override
    public ImagePrototypeElement createElement() {
        final ImagePrototypeElement imageElement = super.createElement();
        imageElement.setAttribute("iconClass", this.iconClass);
        return imageElement;
    }
}
