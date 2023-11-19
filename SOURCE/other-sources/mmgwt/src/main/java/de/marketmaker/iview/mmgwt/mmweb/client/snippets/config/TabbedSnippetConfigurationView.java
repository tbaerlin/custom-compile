/*
 * TabbedSnippetConfigurationView.java
 *
 * Created on 19.06.2012 14:50:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.Activatable;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.ConfigurableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;

import java.util.ArrayList;
import java.util.HashMap;

import static de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView.SymbolParameterType;

/**
 * Uses a TabPanel to layout its children.
 *
 * @author Markus Dick
 */
public class TabbedSnippetConfigurationView implements ConfigurationPresenter {
    public static final String[] DEFAULT_FILTER_TYPES = null;

    private final HandlerManager handlerManager = new HandlerManager(this);

    private final ArrayList<TabItem> items = new ArrayList<TabItem>();

    private final HashMap<String, String> params;

    private final ConfigurableSnippet snippet;

    private final SymbolParameterType symbolParameterType;

    private Window w;

    public TabbedSnippetConfigurationView(ConfigurableSnippet s, SymbolParameterType symbolParameterType) {
        this.snippet = s;
        this.params = s.getCopyOfParameters();
        this.symbolParameterType = symbolParameterType;
    }

    public void addSelectSymbol(String[] filterTypes,
                                String filterForUnderlyingsForType,
                                Boolean filterForUnderlyingsOfLeveragProducts,
                                SelectSymbolFormControllerInterface selectSymbolFormControllerInterface,
                                boolean showQuoteDataColumns, AbstractImagePrototype icon, String label) {
        final ContentPanel panel = SelectSymbolForm.create(getParams(),
                (filterTypes != null) ? filterTypes : DEFAULT_FILTER_TYPES,
                filterForUnderlyingsForType,
                filterForUnderlyingsOfLeveragProducts,
                selectSymbolFormControllerInterface,
                this.symbolParameterType,
                showQuoteDataColumns
        );

        addContentPanel(panel, icon, label);
    }

    public void addSelectSymbol(String[] filterTypes, AbstractImagePrototype icon, String label) {
        final ContentPanel panel = SelectSymbolForm.create(getParams(),
                (filterTypes != null) ? filterTypes : DEFAULT_FILTER_TYPES, null,
                null, this.symbolParameterType);
        addContentPanel(panel, icon, label);
    }

    public void addSelectSymbol(String[] filterTypes,
                                SelectSymbolFormControllerInterface selectSymbolFormControllerInterface,
                                boolean showQuoteDataColumns, AbstractImagePrototype icon, String label) {
        addSelectSymbol(filterTypes, null, null, selectSymbolFormControllerInterface, showQuoteDataColumns, icon, label);
    }

    public void addContentPanel(ContentPanel panel, AbstractImagePrototype icon, String label) {
        addConfigurationWidget(panel, icon, label);
    }

    @Override
    public void addConfigurationWidget(Widget panel, AbstractImagePrototype icon, String label) {
        final TabItem item = new TabItem();
        item.setLayout(new FitLayout());
        item.setText(label);
        item.setIcon(icon);
        item.add(panel);

        this.items.add(item);

        if(panel instanceof HasCancellablePendingRequests) {
            addActionPerformedHandler(new CancelPendingRequests((HasCancellablePendingRequests)panel));
        }
    }

    public HashMap<String, String> getParams() {
        return this.params;
    }

    private Window createView() {
        final Window w = GuiUtil.createModalWindow(I18n.I.selection(), 500, 400, false);
        w.setLayout(new FitLayout());

        final TabPanel content = new TabPanel();
        content.setTabPosition(TabPanel.TabPosition.BOTTOM);
        w.add(content);

        for (TabItem item : this.items) {
            content.add(item);
        }
        content.setSelection(this.items.get(0));

        w.setButtonAlign(Style.HorizontalAlignment.RIGHT);
        w.addButton(new Button(I18n.I.ok(), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent buttonEvent) {
                final TabItem selectedItem = content.getSelectedItem();

                final Component component = selectedItem.getItems().get(0);
                if(component instanceof Activatable) {
                    final Activatable a = (Activatable)component;
                    a.deactivate();
                }

                TabbedSnippetConfigurationView.this.snippet.setParameters(getParams());
                w.hide();
                fireEvent(new ActionPerformedEvent(Actions.OK.name()));
            }
        }));

        w.addButton(new Button(I18n.I.cancel(), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent buttonEvent) {
                w.hide();
                fireEvent(new ActionPerformedEvent(Actions.CANCEL.name()));
            }
        }));

        return w;
    }

    private void showWindow(Window w) {
        w.show();
        BrowserSpecific.INSTANCE.fixCursorBug(w);
    }

    public void show() {
        assert this.items.size() > 0;

        if(this.w == null) {
            this.w = createView();
        }

        for (TabItem item : this.items) {
            final Component component = item.getItems().get(0);
            if(component instanceof Activatable) {
                final Activatable a = (Activatable)component;
                a.activate();
            }
        }

        showWindow(this.w);
    }

    @Override
    public void show(String presetSearchString) {
        getParams().put(SelectSymbolForm.PRESET_SEARCH_STRING, presetSearchString);
        show();
    }

    @Override
    public HandlerRegistration addActionPerformedHandler(ActionPerformedHandler handler) {
        return this.handlerManager.addHandler(ActionPerformedEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }
}