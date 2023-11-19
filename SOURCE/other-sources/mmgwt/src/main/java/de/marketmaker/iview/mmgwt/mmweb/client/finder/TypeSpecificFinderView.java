/*
 * TypeSpecificFinderView.java
 *
 * Created on 04.03.2009 10:07:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;

import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

/**
 * A FinderView that offers a button to quickly change the type of items in the search result
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract class TypeSpecificFinderView<V extends AbstractFinder> extends AbstractFinderView<V> {

    private final Listener<MenuEvent> checkItemListener = new Listener<MenuEvent>() {
        public void handleEvent(MenuEvent event) {
            final CheckMenuItem item = (CheckMenuItem) event.getItem();
            if (item.isChecked()) {
                ackTypeSelection(item);
            }
        }
    };

    private CheckMenuItem selected;

    private boolean switchTypeEnabled = true;

    private final String typeIdPrefix;

    private Button typeMenuButton;

    private final List<CheckMenuItem> typeMenuItems = new ArrayList<CheckMenuItem>();

    public TypeSpecificFinderView(V controller) {
        super(controller);
        typeIdPrefix = controller.getId() + "_"; // $NON-NLS-0$
    }

    protected void ackTypeSearch(String id) {
        this.switchTypeEnabled = false;
        for (CheckMenuItem item : this.typeMenuItems) {
            if (item.getId().endsWith(id)) {
                ackTypeSelection(item);
                break;
            }
        }
        this.switchTypeEnabled = true;
    }

    protected void addViewSelectionTo(FloatingToolbar toolbar) {
        super.addViewSelectionTo(toolbar);

        toolbar.add(new SeparatorToolItem());

        for (FinderFormElements.Item item : getTypes()) {
            typeMenuItems.add(createItem(item.value, item.item, typeMenuItems.isEmpty()));
        }

        this.typeMenuButton = new Button(""); // $NON-NLS-0$

        if (this.controller.isLiveFinder()) {
            ackTypeSelection(null);
        }
        else {
            ackTypeSelection(this.typeMenuItems.get(0));
        }

        final Menu typeMenu = new Menu();
        for (CheckMenuItem menuItem : typeMenuItems) {
            typeMenu.add(menuItem);
        }
        this.typeMenuButton.setMenu(typeMenu);

        if (!this.controller.isLiveFinder()) {
            toolbar.add(this.typeMenuButton);
        }
    }

    protected CheckMenuItem createItem(String id, String name, final boolean checked) {
        final CheckMenuItem result = new CheckMenuItem(name);
        result.setChecked(checked);
        result.setId(typeIdPrefix + id);
        result.setGroup(typeIdPrefix + "types"); // $NON-NLS-0$
        result.addListener(Events.CheckChange, this.checkItemListener);
        return result;
    }

    protected String getSelectedTypeId() {
        if (this.selected == null) {
            return null;
        }
        return this.selected.getId().substring(typeIdPrefix.length());
    }

    protected void disableTypeMenu(boolean b) {
        if (this.typeMenuButton != null) {
            this.typeMenuButton.setEnabled(!b);
        }
    }

    abstract protected ArrayList<FinderFormElements.Item> getTypes();

    abstract protected void switchType();

    private void ackTypeSelection(CheckMenuItem checkItem) {
        final CheckMenuItem tmp = selected;
        this.selected = checkItem;
        if (tmp == this.selected) {
            return;
        }
        if (this.selected != null) {
            this.selected.setChecked(true);
        }
        if (this.switchTypeEnabled && tmp != null) {
            switchType();
        }
    }
}
