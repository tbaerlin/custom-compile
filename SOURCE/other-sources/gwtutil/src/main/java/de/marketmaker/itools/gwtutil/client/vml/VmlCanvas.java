package de.marketmaker.itools.gwtutil.client.vml;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.vml.shape.VectorObject;

import java.util.ArrayList;
import java.util.List;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 11:34
 */
public class VmlCanvas extends Widget {
    private final Element root;
    private List<VectorObject> children = new ArrayList<VectorObject>();

    public VmlCanvas(int width, int height) {
        addNamespaceAndStyle(VmlUtil.VML_NS_PREFIX, VmlUtil.VML_ELEMENT_CLASSNAME);
        final DivElement container = Document.get().createDivElement();
        setElement(container);
        container.getStyle().setProperty("position", "relative");
        container.getStyle().setProperty("overflow", "hidden");
        container.getStyle().setProperty("width", width, Style.Unit.PX);
        container.getStyle().setProperty("height", height, Style.Unit.PX);
        disableSelection(container);

        final Element container2 = Document.get().createDivElement();
        container2.getStyle().setProperty("position", "absolute");
        container2.getStyle().setProperty("overflow", "hidden");
        container2.getStyle().setProperty("width", width, Style.Unit.PX);
        container2.getStyle().setProperty("height", height, Style.Unit.PX);
        container.appendChild(container2);

        this.root = VmlUtil.createVMLElement("group");
        this.root.getStyle().setWidth(width, Style.Unit.PX);
        this.root.getStyle().setHeight(height, Style.Unit.PX);
        this.root.getStyle().setPosition(Style.Position.ABSOLUTE);
        this.root.setPropertyString("coordorigin", "0,0");
        this.root.setPropertyString("coordsize", new Point(width, height).getVml());
        container2.appendChild(this.root);
    }

    private native void addNamespaceAndStyle(String ns, String classname) /*-{
        if (!$doc.namespaces[ns]) {
            $doc.namespaces.add(ns, "urn:schemas-microsoft-com:vml");
            // IE8's standards mode doesn't support * selector
            $doc.createStyleSheet().cssText = "." + classname + "{behavior:url(#default#VML); position: absolute; display:inline-block; }";
        }
    }-*/;

    private native void disableSelection(Element element) /*-{
        element.onselectstart = function () {
            return false
        };
    }-*/;

    public VectorObject add(VectorObject vo) {
        this.root.appendChild(vo.getElement());
        vo.setParent(this);
        this.children.add(vo);
        return vo;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.gwt.user.client.ui.Widget#doAttachChildren()
     */
    @Override
    protected void doAttachChildren() {
        for (VectorObject vo : children) {
            vo.onAttach();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.gwt.user.client.ui.Widget#doDetachChildren()
     */
    @Override
    protected void doDetachChildren() {
        for (VectorObject vo : children) {
            vo.onDetach();
        }
    }
}
