package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * @author Ulrich Maurer
 *         Date: 26.02.13
 */
public class ImageSpec implements ImageResource {
    private final String name;
    private final String url;
    private final int left;
    private final int top;
    private final int width;
    private final int height;
    private boolean simpleImg = false;
    private boolean animated;
    private AbstractImagePrototype prototype;

    public ImageSpec(String name, String url, int width, int height) {
        this(name, url, 0, 0, width, height);
    }

    public ImageSpec(String name, String url, int left, int top, int width, int height) {
        this.name = name;
        this.url = url;
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public ImageSpec withSimpleImg(boolean simpleImg) {
        this.simpleImg = simpleImg;
        return this;
    }

    public ImageSpec withAnimated(boolean animated) {
        if (animated) {
            this.simpleImg = true;
        }
        this.animated = animated;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public SafeUri getSafeUri() {
        return UriUtils.unsafeCastFromUntrustedString(url);
    }

    @Override
    public String getURL() {
        return this.url;
    }

    @Override
    public boolean isAnimated() {
        return this.animated;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public AbstractImagePrototype asPrototype() {
        if (this.prototype == null) {
            this.prototype = this.simpleImg
                    ? new GifImagePrototype(this.name, this.url, this.width, this.height)
                    : new ClippedImagePrototype(this.name, this.url, this.left, this.top, this.width, this.height);
        }
        return this.prototype;
    }

    public void applyTo(Element element) {
        final Style style = element.getStyle();
        style.setProperty("backgroundImage", "url(\"" + this.url + "\")");
        style.setProperty("backgroundRepeat", "no-repeat");
        style.setProperty("backgroundPosition", (-this.left + "px ") + (-this.top + "px"));
        style.setPropertyPx("width", this.width);
        style.setPropertyPx("height", this.height);
    }
}
