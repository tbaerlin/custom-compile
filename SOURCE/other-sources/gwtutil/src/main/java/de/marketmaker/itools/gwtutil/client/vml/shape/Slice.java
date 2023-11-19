package de.marketmaker.itools.gwtutil.client.vml.shape;

import de.marketmaker.itools.gwtutil.client.vml.Point;
import de.marketmaker.itools.gwtutil.client.vml.shape.path.AngleEllipseTo;
import de.marketmaker.itools.gwtutil.client.vml.shape.path.ClosePath;
import de.marketmaker.itools.gwtutil.client.vml.shape.path.MoveTo;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 17:22
 */
public class Slice extends Path {
    private Point center;
    private final MoveTo moveTo;
    private final AngleEllipseTo angleEllipseTo;

    public Slice(double cx, double cy, double radius, double startAngle, double angle) {
        this(new Point(cx, cy), radius, startAngle, angle);
    }

    public Slice(Point center, double radius, double startAngle, double angle) {
        super(3);
        this.center = center;
        this.moveTo = new MoveTo(center);
        this.angleEllipseTo = new AngleEllipseTo(center, radius, startAngle, angle);
        add(this.moveTo);
        add(this.angleEllipseTo);
        add(new ClosePath());
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(double cx, double cy) {
        setCenter(new Point(cx, cy));
    }

    public void setCenter(Point center) {
        this.center = center;
        this.moveTo.setPoint(center);
        this.angleEllipseTo.setCenter(center);
        drawSteps();
    }

    public double getStartAngle() {
        return this.angleEllipseTo.getStartAngle();
    }

    public double getAngle() {
        return this.angleEllipseTo.getAngle();
    }
}
