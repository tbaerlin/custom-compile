package de.marketmaker.itools.gwtutil.client.vml.shape;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import de.marketmaker.itools.gwtutil.client.vml.Point;
import de.marketmaker.itools.gwtutil.client.vml.VmlUtil;

/**
 * User: umaurer
 * Date: 18.11.13
 * Time: 12:46
 */
public class Circle extends Shape {
    private Point center;
    private double radius;

    public Circle(Point center, double radius) {
        set(center, radius);
    }

    public void set(Point center, double radius) {
        this.center = center;
        this.radius = radius;
        set();
    }

    private void set() {
        final Style style = getElement().getStyle();
        final Point lt = new Point(this.center.x - this.radius, this.center.y - this.radius);
        style.setProperty("left", lt.getVmlX() + "px");
        style.setProperty("top", lt.getVmlY() + "px");
        final String wh = Point.getVml(this.radius * 2) + "px";
        style.setProperty("width", wh);
        style.setProperty("height", wh);
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
        set();
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        set();
    }

    @Override
    public Element createElement() {
        return VmlUtil.createVMLElement("oval");
    }
}
