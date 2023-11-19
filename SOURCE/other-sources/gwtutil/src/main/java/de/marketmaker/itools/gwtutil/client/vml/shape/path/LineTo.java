package de.marketmaker.itools.gwtutil.client.vml.shape.path;

import de.marketmaker.itools.gwtutil.client.vml.Point;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 12:30
 */
public class LineTo extends MoveTo {
    public LineTo(Point p) {
        super(p);
    }

    @Override
    public String getVmlString() {
        return "l " + getPoint().getVml();
    }
}
