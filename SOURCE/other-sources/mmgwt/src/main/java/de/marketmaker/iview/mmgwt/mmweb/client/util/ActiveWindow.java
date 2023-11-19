package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;

/**
 * @author umaurer
 */
public class ActiveWindow {
    final Document doc;

    public ActiveWindow() {
        this(createWindow("", "_blank", "dependent=yes,width=800,height=800,location=no,menubar=yes,resizable=yes,scrollbars=yes,status=no,toolbar=yes")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
    }

    public ActiveWindow(String uri, String windowName, String options) {
        this(createWindow(uri, windowName, options));
    }

    public ActiveWindow(Document doc) {
        this.doc = doc;
    }

    private static native Document createWindow(String uri, String windowName, String options) /*-{
        var win = $wnd.open(uri, windowName, options);
        return win.document;
    }-*/;

    public void loadStylesheet(String uri) {
        DOMUtil.loadStylesheet(this.doc, uri);
    }

    public void setTitle(String title) {
        this.doc.setTitle(title);
    }

    public void add(String text, boolean asHtml) {
        add(text, asHtml, null);
    }

    public void add(String text, boolean asHtml, String style) {
        final DivElement div = this.doc.createDivElement();
        if (style != null) {
            div.setClassName(style);
        }
        if (asHtml) {
            div.setInnerHTML(text);
        }
        else {
            div.setInnerText(text);
        }
        this.doc.getBody().insertFirst(div);
    }

    public void clear() {
        final BodyElement body = this.doc.getBody();
        for (int i = body.getChildCount() - 1; i >= 0; i--) {
            body.removeChild(body.getChild(i));
        }
    }

}
