package de.marketmaker.itools.gwtutil.client.vml.shape.path;

import de.marketmaker.itools.gwtutil.client.vml.Point;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 12:14
 */
public class MoveTo extends PathStep {
    private Point p;

    public MoveTo(Point p) {
        this.p = p;
    }

    public Point getPoint() {
        return p;
    }

    public void setPoint(Point p) {
        this.p = p;
    }

    @Override
    public String getVmlString() {
        return "m " + p.getVml();
    }
}
