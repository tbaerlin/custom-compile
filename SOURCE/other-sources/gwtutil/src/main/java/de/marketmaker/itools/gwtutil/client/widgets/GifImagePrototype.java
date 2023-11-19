package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Image;

/**
 * @author umaurer
 */
public class GifImagePrototype extends AbstractImagePrototype {
    private final String iconClass;
    private String url = null;
    private int width = 0;
    private int height = 0;

    public GifImagePrototype(String iconClass, String url, int width, int height) {
        this.iconClass = iconClass;
        this.url = url;
        this.width = width;
        this.height = height;
    }

    @Override
    public void applyTo(Image image) {
        image.getElement().setAttribute("iconClass", this.iconClass);
        image.setUrl(this.url);
        image.setWidth(this.width + "px");
        image.setHeight(this.height + "px");
    }

    @Override
    public Image createImage() {
        final Image image = new Image(this.url);
        image.getElement().setAttribute("iconClass", this.iconClass);
        image.setWidth(this.width + "px");
        image.setHeight(this.height + "px");
        return image;
    }

    @Override
    public String getHTML() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<img src=\"").append(this.url)
                .append("\" iconClass=\"").append(this.iconClass)
                .append("\" width=\"").append(this.width)
                .append("\" height=\"").append(this.height)
                .append("\"/>");
        return sb.toString();
    }

    @Override
    public SafeHtml getSafeHtml() {
        return SafeHtmlUtils.fromTrustedString(getHTML());
    }

    @Override
    public void applyTo(ImagePrototypeElement imageElement) {
        imageElement.setAttribute("iconClass", this.iconClass);
        imageElement.setAttribute("src", this.url);
        imageElement.setAttribute("width", String.valueOf(this.width));
        imageElement.setAttribute("height", String.valueOf(this.height));
    }

    @Override
    public ImagePrototypeElement createElement() {
        final Element elt = Document.get().createImageElement();
        ImagePrototypeElement ipe = (ImagePrototypeElement) elt;
        applyTo(ipe);
        return ipe;
    }
}
