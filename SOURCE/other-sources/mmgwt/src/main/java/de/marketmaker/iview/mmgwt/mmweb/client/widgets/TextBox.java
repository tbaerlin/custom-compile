/*
 * TextBox.java
 *
 * Created on 25.11.2015 10:45
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;

import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;

/**
 * A lazily vwd styled text box
 * @author mdick
 */
public class TextBox extends com.google.gwt.user.client.ui.TextBox {

    private boolean valid;

    public TextBox() {
        super(Document.get().createTextInputElement());
        setStyleName(Styles.get().textBox());
    }

    public TextBox(Element element) {
        super(element);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        final String style = Styles.get().textBoxDisabled();
        if (style == null) {
            return;
        }

        if (enabled) {
            removeStyleName(style);
        }
        else {
            addStyleName(style);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);

        final String style = Styles.get().textBoxReadonly();
        if (style == null) {
            return;
        }

        if (readOnly) {
            addStyleName(style);
        }
        else {
            removeStyleName(style);
        }
    }

    public void setValid(boolean valid) {
        this.valid = valid;

        if(valid) {
            Styles.tryRemoveStyles(this, Styles.get().textBoxInvalid());
        }
        else {
            Styles.tryAddStyles(this, Styles.get().textBoxInvalid());
        }
    }

    public boolean isValid() {
        return this.valid;
    }
}
