package de.marketmaker.iview.mmgwt.mmweb.client.data;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil;

/**
 * Created on 14.07.2010 16:11:38
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class Message extends FlowPanel {
    String id;
    private final int displayTimeSeconds;

    public Message(String id, SafeHtml safeHtml, String style, int displayTimeSeconds) {
        final HTML w = new HTML(safeHtml);
        w.setStyleName(style);
        launderLinks(w.getElement());
        add(w);
        
        this.id = id;
        this.displayTimeSeconds = displayTimeSeconds;
    }

    private void launderLinks(Element element) {
        final JavaScriptObject list = DOMUtil.getElementsByTagNameImpl(element, "a"); // $NON-NLS$
        final int n = DOMUtil.getLength(list);
        for (int i = 0; i < n; i++) {
            final Element anchor = DOMUtil.getElementFromList(i, list);
            final String hash = getHash(anchor);
            if (hash != null && hash.startsWith("#") && AbstractMainController.INSTANCE.isValidToken(hash.substring(1))) { // $NON-NLS$
                DOMUtil.launderFrameLinkForHistory(anchor);
            }
        }
    }

    public void addCloseButton(ClickHandler clickHandler) {
        final Label lblClose = new Label();
        lblClose.setStyleName("mm-message-close"); // $NON-NLS$
        lblClose.addClickHandler(clickHandler);
        lblClose.addMouseOverHandler(new MouseOverHandler() {
            public void onMouseOver(MouseOverEvent event) {
                lblClose.setStyleName("mm-message-close-hover"); // $NON-NLS$
            }
        });
        lblClose.addMouseOutHandler((new MouseOutHandler() {
            public void onMouseOut(MouseOutEvent event) {
                lblClose.setStyleName("mm-message-close"); // $NON-NLS$
            }
        }));
        insert(lblClose, 0);
    }

    private native String getHash(Element anchor) /*-{
        return anchor.hash;
    }-*/;

    public String getId() {
        return id;
    }

    public int getDisplayTimeSeconds() {
        return displayTimeSeconds;
    }
}
