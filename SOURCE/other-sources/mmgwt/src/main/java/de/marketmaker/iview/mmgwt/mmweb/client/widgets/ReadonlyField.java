/*
 * ReadonlyField.java
 *
 * Created on 01.12.2015 17:51
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Element;

import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;

/**
 * A lazily vwd styled form read only field.
 * @author mdick
 */
public class ReadonlyField extends AbstractLazilyStyledLabel {
    public ReadonlyField() {
        super();
        setStyleName(Styles.get().labelReadOnlyField());
    }

    @SuppressWarnings("unused")
    public ReadonlyField(Element element) {
        super(element);
    }

    @SuppressWarnings("unused")
    public ReadonlyField(String text) {
        this();
        setText(text);
    }

    @SuppressWarnings("unused")
    public ReadonlyField(String text, boolean wordWrap) {
        super(text);
        setWordWrap(wordWrap);
    }
}
