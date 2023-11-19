/*
 * SpanLabel.java
 *
 * Created on 11.09.2008 11:44:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Document;

/**
 * @author Ulrich Maurer
 */
public class BreakLabel extends FlexLabel {
    public BreakLabel() {
        super(Document.get().createBRElement());
    }

    public void setText(String text) {
        throw new UnsupportedOperationException("cannot set text for br-element");
    }
}