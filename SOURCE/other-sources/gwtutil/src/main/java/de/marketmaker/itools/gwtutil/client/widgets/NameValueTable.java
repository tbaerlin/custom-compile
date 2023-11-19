package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

/**
 * @author Ulrich Maurer
 *         Date: 17.11.11
 */
public class NameValueTable extends Composite {
    final FlowPanel panel = new FlowPanel();

    public NameValueTable() {
        this.panel.setStyleName("mm-nvt");
        initWidget(this.panel);
    }

    public void addLine(String name, String value) {
        addLine(name, false, value, false);
    }

    public void addLine(String name, String value, boolean valueAsHtml) {
        addLine(name, false, value, valueAsHtml);
    }

    public void addLine(String name, boolean nameAsHtml, String value, boolean valueAsHtml) {
        final FlowPanel entry = new FlowPanel();
        entry.setStyleName("mm-nvt-entry");
        final Label lblName = createLabel(name, nameAsHtml);
        lblName.setStyleName("mm-nvt-name");
        final Label lblValue = createLabel(value, valueAsHtml);
        lblValue.setStyleName("mm-nvt-value");
        entry.add(lblName);
        entry.add(lblValue);
        this.panel.add(entry);
    }

    private Label createLabel(String text, boolean asHtml) {
        return asHtml ? new HTML(text) : new Label(text);
    }

    public int getRowCount() {
        return this.panel.getWidgetCount();
    }

    public void clear() {
        this.panel.clear();
    }
}
