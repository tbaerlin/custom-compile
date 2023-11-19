package de.marketmaker.itools.gwtutil.client.vml;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 12:19
 */
public class Point {
    private static final double RADIANS_FACTOR = Math.PI / 180d;

    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static String getVml(double v) {
        return String.valueOf((int)v);
    }

    public String getVmlX() {
        return getVml(this.x);
    }

    public String getVmlY() {
        return getVml(this.y);
    }

    public String getVml() {
        return getVmlX() + "," + getVmlY();
    }

    public static Point onCircleDegree(Point center, double radius, double angle) {
        return onCircleRadians(center, radius, angle * RADIANS_FACTOR);
    }

    public static Point onCircleRadians(Point center, double radius, double angle) {
        return new Point(
                center.x + Math.sin(angle) * radius,
                center.y - Math.cos(angle) * radius
        );
    }
}
