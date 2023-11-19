package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Element;

/**
 * User: umaurer
 * Date: 21.10.13
 * Time: 13:47
 */
public class PopupPanelFixImplTrident extends PopupPanelFixImpl {
    public void addFrameDummy(Element e) {
        _addFrameDummy(e);
    }

    private native void _addFrameDummy(Element e) /*-{
        var frame = $doc.createElement("iframe"); // $NON-NLS$
        frame.style.position = "absolute"; // $NON-NLS$
        frame.style.top = "0"; // $NON-NLS$
        frame.style.left = "0"; // $NON-NLS$
        frame.style.width = "100%"; // $NON-NLS$
        frame.style.height = "100%"; // $NON-NLS$
        frame.style.zIndex = "-1"; // $NON-NLS$
        frame.setAttribute("frameBorder", "0"); // $NON-NLS$
        e.appendChild(frame);
    }-*/;

    @Override
    public void addFrameDummyForSuggestPopup(Element e) {
        e.getStyle().setZIndex(Integer.MAX_VALUE);
        _addFrameDummy(e);
    }
}
