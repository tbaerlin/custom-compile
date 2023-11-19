package de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter;

import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Attribute;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.xmltree.Element;

/**
 * @author Ulrich Maurer
 *         Date: 09.05.12
 */
public interface XmlTreeSelectionListener {
    void onElementClicked(Element element);
    void onAttributeClicked(Attribute attribute);
}
