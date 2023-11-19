/*
 * SelectFilterPanel.java
 *
 * Created on 11.08.2014 14:53
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableUtils;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DTCell;
import de.marketmaker.iview.pmxml.HasCode;
import de.marketmaker.iview.pmxml.MM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author mdick
 */
public class SelectFilterPanel extends AbstractFilterPanel<String, DTCell> {
    private final HashMap<String, String> codeToLabelMap = new HashMap<>();

    @Override
    public HasValue<String> createEditorWidget() {
        final FilterMetadata<DTCell> metadata = getMetadata();

        final FilterMetadata.FilterType filterType = metadata.getFilterType();
        if(filterType != FilterMetadata.FilterType.SELECT) {
            throw new IllegalStateException("Metadata filter type does not match! Expected: "  // $NON-NLS$
                    + FilterMetadata.FilterType.SELECT + ", found: " + filterType);  // $NON-NLS$
        }

        final SelectButtonAdaptor widget = new SelectButtonAdaptor();
        widget.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                final String value = event.getValue();

                if(StringUtil.hasText(value)) {
                    if (!isEditorValueEnabled()) {
                        setEditorValueEnabled(true);
                    }
                }
                fireChangeEvent();
            }
        });

        this.codeToLabelMap.clear();
        Collections.sort(metadata.getValues(), DTTableUtils.CELL_COMPARATOR);
        final ArrayList<HasCode> values = new ArrayList<>();
        for (DTCell cell : metadata.getValues()) {
            final MM item = cell.getItem();
            if(item instanceof HasCode) {
                final HasCode hasCode = (HasCode) item;
                values.add(hasCode);
                this.codeToLabelMap.put(hasCode.getCode(), hasCode.getValue());
            }
        }
        widget.setValues(values);

        return widget;
    }

    @Override
    public IsWidget createValueWidget(String value) {
        return new Label(this.codeToLabelMap.get(value));
    }

    @Override
    public DTTableRenderer.ColumnFilter createFilter(String value) {
        if(!StringUtil.hasText(value)) {
            return null;
        }
        return new DTTableRenderer.HasCodeColumnFilter(getMetadata().getColumnIndex(), value);
    }

    @Override
    protected boolean isValueAddable(String value) {
        return StringUtil.hasText(value);
    }

    private final static class SelectButtonAdaptor implements HasValue<String>, IsWidget {
        public static final String KEY = "code";  // $NON-NLS$

        private final HandlerManager handlerManager = new HandlerManager(this);

        private final Menu menu = new Menu();
        private final SelectButton button = (SelectButton)new SelectButton()
                .withMenu(this.menu, true)
                .withClickOpensMenu();

        private SelectButtonAdaptor() {
            menu.addFirst(new MenuItem(I18n.I.noSelectionListEntry()).withData(KEY, null));

            this.button.addSelectionHandler(new SelectionHandler<MenuItem>() {
                @Override
                public void onSelection(SelectionEvent<MenuItem> event) {
                    final MenuItem selectedItem = event.getSelectedItem();
                    final String value = selectedItem != null ? (String) selectedItem.getData(KEY) : null;
                    ValueChangeEvent.fire(SelectButtonAdaptor.this, value);
                }
            });
        }

        public void setValues(List<HasCode> values) {
            for (HasCode value : values) {
                this.menu.add(new MenuItem(value.getValue()).withData(KEY, value.getCode()));
            }
        }

        @Override
        public String getValue() {
            final MenuItem selectedItem = this.button.getSelectedItem();
            if(selectedItem == null) {
                return null;
            }
            return (String) selectedItem.getData(KEY);
        }

        @Override
        public void setValue(String value) {
            setValue(value, false);
        }

        @Override
        public void setValue(String value, boolean fireEvents) {
            this.button.setSelectedData(KEY, value, fireEvents);
        }

        @Override
        public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> handler) {
            return this.handlerManager.addHandler(ValueChangeEvent.getType(), handler);
        }

        @Override
        public void fireEvent(GwtEvent<?> event) {
            this.handlerManager.fireEvent(event);
        }

        @Override
        public Widget asWidget() {
            return this.button;
        }
    }
}
