/*
 * LinkPanel.java
 *
 * Created on 14.08.2008 15:57:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.desktop;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Ulrich Maurer
 */
public class LinkPanel extends ComplexPanel {
    private Element anchorElem;

    public LinkPanel(String href, String target) {
        setElement(Document.get().createDivElement());
        DOM.appendChild(getElement(), anchorElem = DOM.createAnchor());
        if (href != null) {
            this.anchorElem.setAttribute("href", href); // $NON-NLS-0$
        }
        if (target != null) {
            this.anchorElem.setAttribute("target", target); // $NON-NLS-0$
        }
    }


    /**
     * Adds a new child widget to the panel.
     *
     * @param w the widget to be added
     */
    @Override
    public void add(Widget w) {
      super.add(w, this.anchorElem);
    }


    /**
     * Inserts a widget before the specified index.
     *
     * @param w the widget to be inserted
     * @param beforeIndex the index before which it will be inserted
     * @throws IndexOutOfBoundsException if <code>beforeIndex</code> is out of
     *           range
     */
    public void insert(Widget w, int beforeIndex) {
      super.insert(w, this.anchorElem, beforeIndex, true);
    }

}
