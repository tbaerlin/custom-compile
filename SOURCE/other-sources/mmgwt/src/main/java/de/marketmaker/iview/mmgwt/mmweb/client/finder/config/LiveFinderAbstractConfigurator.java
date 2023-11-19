/*
 * LiveFinderAbstractConfigurator.java
 *
 * Created on 30.09.2011 15:11
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.finder.config;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.ButtonGroup;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserTouch;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Lösch
 */
public abstract class LiveFinderAbstractConfigurator {
    private final DialogIfc dialog;
    private final FloatingToolbar toolbar = new FloatingToolbar();
    private final SimplePanel panel = new SimplePanel();
    private final List<LiveFinderConfiguratorItem> items = new ArrayList<>();
    private final ItemChooserTouch.Callback itemChooserCallback;

    public LiveFinderAbstractConfigurator(final String title, final Command onOk) {
        this.dialog = Dialog.getImpl().createDialog()
                .withTitle(title)
                .withTopWidget(this.toolbar)
                .withWidget(this.panel)
                .withCloseButton()
                /*.withDefaultButton(I18n.I.ok(), new Command() {
                    @Override
                    public void execute() {
                        for (LiveFinderConfiguratorItem item : items) {
                            item.save();
                        }
                        Scheduler.get().scheduleDeferred(onOk);
                    }
                })
                .withButton(I18n.I.cancel())*/;
        this.itemChooserCallback = new ItemChooserTouch.Callback() {
            @Override
            public void onOk() {
                for (LiveFinderConfiguratorItem item : items) {
                    item.save();
                }
                Scheduler.get().scheduleDeferred(onOk);
                hide();
            }

            @Override
            public void onCancel() {
                hide();
            }
        };
    }

    private void init() {
        this.toolbar.removeAll();
        this.items.clear();
        addItems(this.items, this.itemChooserCallback);
        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.addSelectionHandler(this::onItemSelected);

        boolean first = true;
        for (final LiveFinderConfiguratorItem item : this.items) {
            if (first) {
                first = false;
            }
            else {
                this.toolbar.addEmpty("2px");
            }
            final Button button = Button.text(item.getName()).build();
            button.setData("item", item); // $NON-NLS$
            this.toolbar.add(button);
            buttonGroup.add(button);
        }
        buttonGroup.selectFirst();
    }

    protected abstract void addItems(List<LiveFinderConfiguratorItem> items, ItemChooserTouch.Callback itemChooserCallback);

    public void onItemSelected(SelectionEvent<Button> e) {
        final Button selectedItem = e.getSelectedItem();
        if (selectedItem != null) {
            final LiveFinderConfiguratorItem item = (LiveFinderConfiguratorItem) selectedItem.getData("item"); // $NON-NLS$
            setWidget(item.getWidget());
        }
    }

    private void setWidget(ItemChooserWidget widget) {
        this.panel.setWidget(widget);
    }

    public void show() {
        init();
        this.dialog.show();
    }

    private void hide() {
        this.dialog.closePopup();
    }
}