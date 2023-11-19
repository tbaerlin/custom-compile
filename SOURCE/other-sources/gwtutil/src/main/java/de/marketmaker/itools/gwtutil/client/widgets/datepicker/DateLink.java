package de.marketmaker.itools.gwtutil.client.widgets.datepicker;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.dom.client.Document;

/**
 * @author umaurer
 */
public class DateLink extends FocusWidget {
    public DateLink() {
        setElement(Document.get().createAnchorElement());
    }

    public DateLink(String text) {
        this();
        setText(text);
    }

    public void setText(String text) {
        getElement().setInnerText(text);
    }
}
