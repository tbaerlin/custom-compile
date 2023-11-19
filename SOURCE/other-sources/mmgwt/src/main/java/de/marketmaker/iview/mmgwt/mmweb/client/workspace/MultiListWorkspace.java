/*
 * MultiListWorkspace.java
 *
 * Created on 16.12.2008 10:59:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.UserListUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.UserListUpdatedHandler;

/**
 * An AbstractListWorkspace that supports multiple lists being displayed; adds a Toolbar for
 * list selection.
 *
 * @author Michael Lösch
 */
abstract class MultiListWorkspace<D extends BlockType, T> extends AbstractListWorkspace<D,T> {

    protected final Button button;

    protected final Button buttonOpenList;

    protected String currentListId = null;

    protected final Menu menu;

    protected final ToolBar toolbar = new ToolBar();

    protected MultiListWorkspace(final String heading) {
        super(heading, false, false);

        this.button = new Button();
        this.menu = new Menu();
        this.button.setMenu(this.menu);

        this.buttonOpenList = new Button();
        this.buttonOpenList.addListener(Events.Select, new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                openCurrentList();
            }
        });

        setTopComponent(this.toolbar);
        populateToolbar();
    }

    private void populateToolbar() {
        this.toolbar.add(this.button);
        this.toolbar.add(new FillToolItem());
        this.toolbar.add(this.buttonOpenList);
    }

    protected abstract String getCurrentListId();

    protected abstract String getCurrentName();

    protected abstract void openCurrentList();

    protected abstract void reorganizeMenu();

    protected void updateView() {
        if (!this.block.isResponseOk()) {
            this.currentListId = null;
            setColumnHeader("n/a"); // $NON-NLS-0$
        }
        else {
            this.currentListId = getCurrentListId();
            setColumnHeader(getCurrentName());
        }
        super.updateView();
    }

    protected void addListsChangeListener(final boolean forWatchlists) {
        EventBusRegistry.get().addHandler(UserListUpdatedEvent.getType(), new UserListUpdatedHandler() {
            public void onUpdate(UserListUpdatedEvent event) {
                if (forWatchlists == event.isWatchlists()) {
                    if (event.isStructureChanged()) {
                        reorganizeMenu();
                    }
                    if (block.getResult() != null || block.isToBeRequested()) {
                        forceRefresh();
                    }
                }
            }
        });
    }
}
