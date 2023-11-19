package de.marketmaker.itools.gwtutil.client.vml;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 11:37
 */
public abstract class VmlUtil {
    public static final String VML_NS_PREFIX = "vml";
    public static final String VML_ELEMENT_CLASSNAME = "vml-element";

    public static Element createVMLElement(String name) {
        Element element = Document.get().createElement(VML_NS_PREFIX + ":" + name);
        element.setClassName(VML_ELEMENT_CLASSNAME);
        return element;
    }

    public static Element getOrCreateChildElementWithTagName(Element element, String name) {
        Element e = getChildElementWithTagName(element, name);
        if (e != null) {
            return e;
        }
        return element.appendChild(createVMLElement(name));
    }

    private static Element getChildElementWithTagName(Element element, String name) {
        NodeList<Node> nodes = element.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.getItem(i);
            if (node.getNodeName().equals(name)) {
                return Element.as(node);
            }
        }
        return null;
    }

}
