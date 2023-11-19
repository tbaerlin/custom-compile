package de.marketmaker.itools.gwtutil.client.svg;

import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.OMSVGTransform;
import org.vectomatic.dom.svg.OMSVGTransformList;
import org.vectomatic.dom.svg.itf.ISVGTransformable;

/**
 * Author: umaurer
 * Created: 14.08.15
 */
public class SvgUtil {
    public static OMSVGTransform setRotation(final OMSVGSVGElement svg, ISVGTransformable svgElt, float angle, float cx, float cy) {
        final OMSVGTransform transform = svg.createSVGTransform();
        transform.setRotate(angle, cx, cy);
        final OMSVGTransformList transformList = svgElt.getTransform().getBaseVal();
        transformList.clear();
        transformList.appendItem(transform);
        return transform;
    }

}
