/*
 * AbstractLazilyStyledLabel.java
 *
 * Created on 02.12.2015 08:40
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;

/**
 * @author mdick
 */
public abstract class AbstractLazilyStyledLabel extends Label {
    public AbstractLazilyStyledLabel() {
        super();
    }

    public AbstractLazilyStyledLabel(String text) {
        super(text);
    }

    public AbstractLazilyStyledLabel(Element element) {
        super(element);
    }

    @Override
    public void setWordWrap(boolean wrap) {
        final String noWrap = Styles.get().labelNoWrap();
        if (noWrap != null && wrap) {
            removeStyleName(noWrap);
        }
        else {
            addStyleName(noWrap);
        }
    }
}
