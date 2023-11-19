/*
 * AbstractFilterPanel.java
 *
 * Created on 11.08.2014 09:56
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.itools.gwtutil.client.widgets.input.CheckBox;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author mdick
 */
public abstract class AbstractFilterPanel<T, M> extends Composite implements HasChangeHandlers, HasCreateColumnFilter {
    private final Label caption = new Label();
    private final FlexTable table = new FlexTable();
    private final IconImageIcon addButton = IconImage.getIcon("sps-plus");  // $NON-NLS$

    private final List<T> values = new ArrayList<>();
    private final List<Boolean> valuesEnabled = new ArrayList<>();

    private FilterMetadata<M> metadata;
    private HasValue<T> editorWidget;
    private boolean editorValueEnabled = true;
    private CheckBox editorCheckBox;

    public AbstractFilterPanel() {
        final FlowPanel panel = new FlowPanel();
        initWidget(panel);

        panel.add(this.caption);
        panel.add(this.table);
        this.addButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onAddButtonClicked();
            }
        });

        this.editorCheckBox = new CheckBox(this.editorValueEnabled);
        this.editorCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                setEditorValueEnabled(event.getValue());
                if(isValueAddable(AbstractFilterPanel.this.editorWidget.getValue())) {
                    fireChangeEvent();
                }
            }
        });

        panel.setStyleName("as-filter");
        this.caption.setStyleName("as-filter-caption");
        this.table.setStyleName("as-filter-entries");
    }

    @Override
    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return addHandler(handler, ChangeEvent.getType());
    }

    private void onAddButtonClicked() {
        final T value = this.editorWidget.getValue();
        if(!isValueAddable(value)) {
            return;
        }
        if(this.values.indexOf(value) < 0) {
            addValueRow(value, true);
            fireChangeEvent();
        }
    }

    protected void fireChangeEvent() {
        ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
    }

    private void addValueRow(final T value, final boolean enabled) {
        this.values.add(value);
        this.valuesEnabled.add(enabled);

        final int index = this.table.getRowCount() - 1;
        this.table.insertRow(index);

        final CheckBox checkBox = new CheckBox(enabled);
        checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                setEnabled(value, event.getValue(), true);
            }
        });

        this.table.setWidget(index, 0, checkBox);
        this.table.setWidget(index, 1, createValueWidget(value));
        this.table.setWidget(index, 2, IconImage.getIcon("sps-minus").withClickHandler(new ClickHandler() { // $NON-NLS$
            @Override
            public void onClick(ClickEvent event) {
                onDeleteButtonClicked(value);
            }
        }));
    }

    private void setEnabled(T value, Boolean enabled, boolean fireChangeEvent) {
        final int index = this.values.indexOf(value);
        if(index > -1 && this.valuesEnabled.get(index) != enabled) {
            this.valuesEnabled.set(index, enabled);
            if(fireChangeEvent) {
                fireChangeEvent();
            }
        }
    }

    protected List<T> getValues() {
        return this.values;
    }

    protected List<Boolean> getValuesEnabled() {
        return valuesEnabled;
    }

    protected HasValue<T> getEditorWidget() {
        return editorWidget;
    }

    protected boolean isEditorValueEnabled() {
        return this.editorValueEnabled;
    }

    protected void setEditorValueEnabled(boolean enabled) {
        this.editorCheckBox.setChecked(enabled, false);
        this.editorValueEnabled = enabled;
    }

    private void onDeleteButtonClicked(T value) {
        final int index = this.values.indexOf(value);
        this.values.remove(index);
        this.valuesEnabled.remove(index);
        this.table.removeRow(index);
        fireChangeEvent();
    }

    protected FilterMetadata<M> getMetadata() {
        return this.metadata;
    }

    public void updateMetadata(FilterMetadata<M> metadata) {
        if (metadata == this.metadata) {
            return;
        }
        this.metadata = metadata;
        reset();
    }

    private void reset() {
        this.table.clear();

        this.caption.setText(this.metadata.getColumnCaption());

        this.editorWidget = createEditorWidget();

        this.table.setWidget(0, 0, this.editorCheckBox);
        this.table.setWidget(0, 1, (IsWidget) this.editorWidget);
        this.table.setWidget(0, 2, this.addButton);
    }

    public DTTableRenderer.ColumnFilter createColumnFilter() {
        final ArrayList<DTTableRenderer.ColumnFilter> valueFilters = new ArrayList<>();

        for(int i = 0; i < this.valuesEnabled.size(); i++) {
            if(!this.valuesEnabled.get(i)) {
                continue;
            }
            final T value = this.values.get(i);
            final DTTableRenderer.ColumnFilter filter = createFilter(value);
            if(filter != null) {
                valueFilters.add(filter);
            }
        }
        if(this.editorValueEnabled) {
            final T value = this.editorWidget.getValue();
            if(isValueAddable(value)) {
                final DTTableRenderer.ColumnFilter filter = createFilter(value);
                if (filter != null) {
                    valueFilters.add(filter);
                }
            }
        }

        if(valueFilters.isEmpty()) {
            return null;
        }

        return new DTTableRenderer.InclusiveMultiColumnFilter(valueFilters);
    }

    @Override
    public boolean isColumnFilterDefined() {
        boolean result = false;

        for(int i = 0; i < this.valuesEnabled.size(); i++) {
            if(!this.valuesEnabled.get(i)) {
                continue;
            }
            if(isValueAddable(this.values.get(i))) {
                result = true;
                break;
            }
        }
        if(this.editorValueEnabled) {
            result |= isValueAddable(this.editorWidget.getValue());
        }
        return result;
    }

    public FilterData<T> getData() {
        final FilterData<T> data = new FilterData<>();
        data.setMetadataColumnIndex(getMetadata().getColumnIndex());
        data.setOriginalColumnCaption(getMetadata().getOriginalColumnCaption());
        data.setFilterType(getMetadata().getFilterType());
        data.setEditorValueEnabled(this.editorValueEnabled);
        data.setEditorValue(this.editorWidget.getValue());
        data.setValuesEnabled(Collections.unmodifiableList(this.valuesEnabled));
        data.setValues(Collections.unmodifiableList(this.values));
        return data;
    }

    public void updateData(FilterData<T> data) {
        reset();

        if(data == null
                || data.getMetadataColumnIndex() != getMetadata().getColumnIndex()
                || data.getFilterType() != getMetadata().getFilterType()
                || !StringUtil.equals(data.getOriginalColumnCaption(), getMetadata().getOriginalColumnCaption())) {
            return;
        }

        setEditorValueEnabled(data.isEditorValueEnabled());
        if(this.editorValueEnabled) {
            this.editorWidget.setValue(data.getEditorValue());
        }

        final List<T> values = data.getValues();
        final List<Boolean> valuesEnabled = data.getValuesEnabled();
        for (int i = 0; i < values.size(); i++) {
            addValueRow(values.get(i), valuesEnabled.get(i));
        }
    }

    /**
     * @return a widget that implements HasValue and IsWidget
     */
    protected abstract HasValue<T> createEditorWidget();

    /**
     * @return a widget that is not editable
     */
    protected abstract IsWidget createValueWidget(T value);

    /**
     * @return creates a suitable filter for the selected values and the current value of the editor widget
     */
    protected abstract DTTableRenderer.ColumnFilter createFilter(T value);

    protected abstract boolean isValueAddable(T value);
}
