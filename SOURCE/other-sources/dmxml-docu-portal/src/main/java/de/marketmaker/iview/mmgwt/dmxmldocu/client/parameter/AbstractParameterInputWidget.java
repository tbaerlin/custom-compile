/*
 * AbstractParameterInputWidget.java
 *
 * Created on 29.03.12 15:49
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.dmxmldocu.DmxmlBlockParameterDocumentation;

/**
 * Abstract superclass of all ParameterInputWidget consisting of a single FocusWidget for the
 * actual parameter input.
 * <p/>
 * Subclasses need to provide methods to create and configure this single widget.
 * 
 * After construction, {@link #initComponents()} has to be called!
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public abstract class AbstractParameterInputWidget<W extends FocusWidget>
        implements ParameterInputWidget {
    private Button buttonHelp = new Button("?");

    private final DmxmlBlockParameterDocumentation paramDocu;

    private final CheckBox checkBox = new CheckBox();

    private final ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();

    protected List<W> inputWidgets;

    protected AbstractParameterInputWidget(DmxmlBlockParameterDocumentation paramDocu) {
        this.paramDocu = paramDocu;
    }

    public final void initComponents() {
        final HTML name = new HTML(paramDocu.getName()); // TODO: check XSS problem -> use Label instead?
        checkBox.setValue(paramDocu.isRequired());
        final W inputWidget = createInputWidget();
        inputWidget.setEnabled(paramDocu.isRequired());
        inputWidget.setWidth("15em"); // $NON-NLS$
        this.inputWidgets = new ArrayList<W>(Arrays.asList(inputWidget));
        name.setWidth("15em"); // $NON-NLS$

        if (paramDocu.getDescription() != null) {
            final DecoratedPopupPanel helpPopup = new DecoratedPopupPanel(true);
            helpPopup.ensureDebugId("cwBasicPopup-simplePopup"); // $NON-NLS$
            helpPopup.setWidget(new HTML(paramDocu.getDescription())); // TODO: check XSS problem -> use Label instead?
            buttonHelp.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    // Reposition the popup relative to the button
                    int left = buttonHelp.getAbsoluteLeft() + buttonHelp.getOffsetWidth() - 300;
                    int top = buttonHelp.getAbsoluteTop() + buttonHelp.getOffsetHeight() + 5;
                    helpPopup.setPopupPosition(left, top);
                    helpPopup.setWidth("300px"); // $NON-NLS$
                    // Show the popup
                    helpPopup.show();
                }
            });
        }

        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                setWidgetsEnabled(event.getValue());
                notifyListeners();
            }
        });

        configureInputWidget(inputWidget);
    }

    @Override
    public void addWidgets(final FlexTable table, int row) {
        table.setText(row, 0, paramDocu.getName());
        if (!paramDocu.isRequired()) {
            table.setWidget(row, 1, this.checkBox);
            getRowElement(this.checkBox).addClassName("disabled"); // add style to table row  // $NON-NLS$
        }
        table.setWidget(row, 2, this.inputWidgets.get(0));
        if (paramDocu.isMultiValued()) {
            final Label lblAddValue = new Label();
            lblAddValue.setStyleName("plus"); // $NON-NLS$
            table.setWidget(row, 4, lblAddValue);
            lblAddValue.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (!AbstractParameterInputWidget.this.checkBox.getValue()) {
                        return;
                    }
                    addInputWidget(table, lblAddValue);
                }
            });

            if(paramDocu.getSampleValue() != null) {
                final String[] sampleValue = paramDocu.getSampleValue().split(",");

                setParameterValue(this.inputWidgets.get(0), sampleValue[0].trim());

                for (int i = 1, length = sampleValue.length; i < length; i++) {
                    final W inputWidget = addInputWidget(table, lblAddValue);
                    setParameterValue(inputWidget, sampleValue[i].trim());
                }
            }
        }
        else {
            setParameterValue(this.inputWidgets.get(0), paramDocu.getSampleValue());
        }
        if (paramDocu.getDescription() != null) {
            table.setWidget(row, 5, this.buttonHelp);
        }
        updateStatus();
        setWidgetsEnabled(isEnabled());
    }

    private W addInputWidget(final FlexTable table, final Label lblAddValue) {
        final W inputWidget = createInputWidget();
        inputWidget.setWidth("15em"); // $NON-NLS$
        configureInputWidget(inputWidget);

        final int newRow = findWidgetRow(table, lblAddValue, 4) + 1;

        if (this.inputWidgets.size() == 1) {
            final Label lblRemoveValue = new Label();
            lblRemoveValue.setStyleName("minus"); // $NON-NLS$
            lblRemoveValue.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    if (!AbstractParameterInputWidget.this.checkBox.getValue()) {
                        return;
                    }
                    removeInputWidget(table, lblAddValue, lblRemoveValue);
                }
            });
            table.setWidget(newRow - 1, 3, lblRemoveValue);
        }

        final Label lblRemoveValue = new Label();
        lblRemoveValue.setStyleName("minus"); // $NON-NLS$
        lblRemoveValue.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!AbstractParameterInputWidget.this.checkBox.getValue()) {
                    return;
                }
                removeInputWidget(table, lblAddValue, lblRemoveValue);
            }
        });

        table.insertRow(newRow);
        table.setWidget(newRow, 2, inputWidget);
        table.setWidget(newRow, 3, lblRemoveValue);
        table.setWidget(newRow, 4, lblAddValue);

        this.inputWidgets.add(inputWidget);
        notifyListeners();
        return inputWidget;
    }

    private void removeInputWidget(FlexTable table, Label lblAddValue, Label lblRemoveValue) {
        final int row = findWidgetRow(table, lblRemoveValue, 3);
        if (lblAddValue == table.getWidget(row, 4)) {
            table.setWidget(row - 1, 4, lblAddValue);
        }
        final Widget inputWidget = table.getWidget(row, 2);
        final int listIndex = this.inputWidgets.indexOf(inputWidget);
        this.inputWidgets.remove(listIndex);
        if (listIndex == 0) {
            inputWidget.removeFromParent();
            table.setWidget(row, 2, table.getWidget(row + 1, 2));
            if (this.inputWidgets.size() == 1) {
                table.getWidget(row, 3).removeFromParent();
                table.setWidget(row, 4, table.getWidget(row + 1, 4));
            }
            table.removeRow(row + 1);
        }
        else {
            inputWidget.removeFromParent();
            table.removeRow(row);
            if (this.inputWidgets.size() == 1) {
                table.getWidget(row - 1, 3).removeFromParent();
            }
        }

        notifyListeners();
    }

    private int findWidgetRow(FlexTable table, Widget widget, int column) {
        for (int row = 0, size = table.getRowCount(); row < size; row++) {
            if (table.getCellCount(row) > column && table.getWidget(row, column) == widget) {
                return row;
            }
        }
        throw new RuntimeException("widget not found in table (column = " + column + ")"); // $NON-NLS$
    }

    /**
     * This method should do the following
     * <ul>
     *     <li>add an appropriate listener to the widget which calls {@link #notifyListeners()}.</li>
     * </ul>
     */
    protected abstract void configureInputWidget(W inputWidget);

    /**
     * @return a new instance of the input widget.
     */
    protected abstract W createInputWidget();

    protected void notifyListeners() {
        for (ChangeListener listener : listeners) {
            listener.onParameterValueChanged();
        }
    }
    
    protected boolean isValid(String value) {
        return true;
    }


    @Override
    public final String[] getParameterValues() {
        final List<String> values = new ArrayList<String>(this.inputWidgets.size());
        for (W w : inputWidgets) {
            final String value = getParameterValue(w);
            final boolean valid = isValid(value);
            w.setStyleName(valid ? "valid-content" : "invalid-content");
            if (valid) {
                values.add(value);
            }
        }
        return values.toArray(new String[values.size()]);
    }

    protected abstract String getParameterValue(W inputWidget);


    @Override
    public final void setParameterValue(String value) {
        if (this.inputWidgets.size() > 0) {
            setParameterValue(this.inputWidgets.get(0), value);
        }
        updateStatus();
    }

    protected abstract void setParameterValue(W inputWidget, String value);

    protected void updateStatus() {
        if (isEnabled()) {
            for (W w : inputWidgets) {
                final String value = getParameterValue(w);
                final boolean valid = isValid(value);
                w.setStyleName(valid ? "valid-content" : "invalid-content");
            }
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public DmxmlBlockParameterDocumentation getParameterDocu() {
        return this.paramDocu;
    }

    @Override
    public boolean isEnabled() {
        return this.checkBox.getValue();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.checkBox.setValue(enabled);
        setWidgetsEnabled(enabled);
    }

    private void setWidgetsEnabled(boolean enabled) {
        for (W w : this.inputWidgets) {
            w.setEnabled(enabled);
            if (enabled) {
                getRowElement(w).removeClassName("disabled"); // remove style from table row  // $NON-NLS$
            }
            else {
                getRowElement(w).addClassName("disabled"); // add style to table row  // $NON-NLS$
            }
        }
    }

    private Element getRowElement(Widget widget) {
        Element element = widget.getElement();
        while (element != null && !element.getNodeName().toUpperCase().equals("TR")) { // $NON-NLS$
            element = element.getParentElement();
        }
        return element;
    }
}
