package de.marketmaker.itools.gwtutil.client.vml.shape.path;

import de.marketmaker.itools.gwtutil.client.vml.Point;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 12:32
 */
public class AngleEllipseTo extends PathStep {
    public static final double ANGLE_FACTOR = 65535;
    private Point center;
    private double radius;
    private double startAngle;
    private double angle;

    public AngleEllipseTo(Point center, double radius, double startAngle, double angle) {
        this.center = center;
        this.radius = radius;
        this.startAngle = startAngle;
        this.angle = angle;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(double startAngle) {
        this.startAngle = startAngle;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public String getVmlString() {
        final Point size = new Point(this.radius, this.radius);
        return "ae" + this.center.getVml()
                + "," + size.getVml()
                + "," + (int)(this.startAngle * ANGLE_FACTOR)
                + "," + (int)(this.angle * ANGLE_FACTOR)
                ;
    }
}
