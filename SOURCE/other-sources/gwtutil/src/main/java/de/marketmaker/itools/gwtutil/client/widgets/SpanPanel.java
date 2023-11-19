package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Node;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AttachDetachException;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;

/**
 * Author: umaurer
 * Created: 13.06.14
 */
public class SpanPanel extends ComplexPanel {
    public SpanPanel() {
        setElement(DOM.createSpan());
    }

    public void add(Widget w) {
        add(w, getElement());
    }

    public void insert(Widget w, int beforeIndex) {
        insert(w, getElement(), beforeIndex, true);
    }

    @Override
    public void clear() {
        try {
            doLogicalClear();
        } finally {
            // Remove all existing child nodes.
            Node child = getElement().getFirstChild();
            while (child != null) {
                getElement().removeChild(child);
                child = getElement().getFirstChild();
            }
        }
    }

    private AttachDetachException.Command orphanCommand;
    void doLogicalClear() {
        // Only use one orphan command per panel to avoid object creation.
        if (orphanCommand == null) {
            orphanCommand = new AttachDetachException.Command() {
                public void execute(Widget w) {
                    orphan(w);
                }
            };
        }
        try {
            AttachDetachException.tryCommand(this, orphanCommand);
        } finally {
            clear(getChildren());
        }
    }

    private void clear(WidgetCollection children) {
        for (int i = children.size() - 1; i >= 0; i--) {
            children.remove(i);
        }
    }

}
