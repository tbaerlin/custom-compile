/*
 * OrderViewContainerView.java
 *
 * Created on 24.01.13 14:34
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.DialogButton;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSelectButton;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.history.OrderEntryHistorySupport;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

import java.util.List;

/**
 * @author Markus Dick
 */
class OrderViewContainerView implements OrderViewContainerDisplay {
    private Presenter presenter;
    private Widget content;

    private final DialogIfc dialog;

    private final DialogButton executeButton;
    private final DialogButton cancelButton;
    private FloatingToolbar toolbar;

    private boolean buttonsLocked = false;
    private boolean executeButtonEnabled = true;
    private boolean cancelButtonEnabled = true;

    private String title;

    public OrderViewContainerView(String windowTitle) {
        this.title = windowTitle;
        this.dialog = Dialog.getImpl().createDialog()
                .withStyle("as-oe-dlg")   // $NON-NLS$
                .withTitle(windowTitle);

        this.executeButton = this.dialog.addButton(I18n.I.proceed(), new Command() {
            @Override
            public void execute() {
                OrderViewContainerView.this.dialog.keepOpen();
                if(OrderViewContainerView.this.buttonsLocked) {
                    Firebug.debug("<OrderViewContainerView.executeButton.componentSelected> buttons are locked. Handler method 'onExecuteOrderClicked' not called.");
                    return;
                }
                OrderViewContainerView.this.presenter.onExecuteOrderClicked();
            }
        });

        final Command cancelCommand = new Command() {
            @Override
            public void execute() {
                OrderViewContainerView.this.dialog.keepOpen();
                if (OrderViewContainerView.this.buttonsLocked) {
                    Firebug.debug("<OrderViewContainerView.cancelButtonHasEnabled.componentSelected> buttons are locked. Handler method 'onCancelOrderClicked' not called.");
                    return;
                }
                OrderViewContainerView.this.presenter.onCancelOrderClicked();
            }
        };
        this.cancelButton = this.dialog.addButton(I18n.I.cancel(), cancelCommand);

        this.dialog.withEscapeCommand(cancelCommand);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        if(this.presenter != null) {
            throw new IllegalArgumentException("Presenter has already been set!"); //$NON-NLS$
        }
        this.presenter = presenter;
    }

    @Override
    public String getTitle() {
        return this.title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        this.dialog.withTitle(title);
    }

    @Override
    public void show() {
        this.dialog.show();
    }

    @Override
    public void hide() {
        this.dialog.closePopup();
    }

    @Override
    public OrderViewContainerDisplay withToolbar() {
        if(this.toolbar == null) {
            this.toolbar = new FloatingToolbar();
            this.dialog.withTopWidget(this.toolbar);
        }

        return this;
    }

    @Override
    public OrderViewContainerDisplay addOrderHistoryTool(List<OrderEntryHistorySupport.Item> historyItems) {
        if(historyItems == null) {
            return this;
        }

        if(this.toolbar == null) {
            throw new IllegalStateException("Cannot add history tool, because display has no toolbar!"); //$NON-NLS$
        }

        ImageSelectButton s = new ImageSelectButton(IconImage.get("select-from-history").createImage(), null, null, true); //$NON-NLS$

        if(!historyItems.isEmpty()) {
            final Menu m = new Menu();

            for(OrderEntryHistorySupport.Item item : historyItems) {
                m.add(new MenuItem(item.getLabel()).withData("h", item)); //$NON-NLS$
            }

            s.withMenu(m);
        }
        else {
            s.setEnabled(false);
        }

        s.addSelectionHandler(new SelectionHandler<MenuItem>() {
            @Override
            public void onSelection(SelectionEvent<MenuItem> event) {
                final MenuItem menuItem = event.getSelectedItem();
                final OrderEntryHistorySupport.Item item = (OrderEntryHistorySupport.Item)menuItem.getData("h"); //$NON-NLS$

                OrderViewContainerView.this.presenter.onOrderEntryHistoryItemSelected(item);
            }
        });

        this.toolbar.add(s);

        return this;
    }

    @Override
    public void setExecuteOrderButtonText(String text) {
        this.executeButton.setText(text);
    }

    @Override
    public void setExecuteOrderButtonEnabled(boolean enabled) {
        this.executeButtonEnabled = enabled;
        this.executeButton.setEnabled(this.executeButtonEnabled && !this.buttonsLocked);
    }

    @Override
    public void setCancelOrderButtonEnabled(boolean enabled) {
        this.cancelButtonEnabled = enabled;
        this.cancelButton.setEnabled(this.cancelButtonEnabled && !this.buttonsLocked);
    }

    @Override
    public void setButtonsLocked(boolean locked) {
        this.buttonsLocked = locked;
        this.executeButton.setEnabled(!locked && this.executeButtonEnabled);
        this.cancelButton.setEnabled(!locked && this.cancelButtonEnabled);
    }

    @Override
    public void setContent(ContentView view) {
        this.setContent(view.getWidget());
    }

    @Override
    public void setContent(Widget w) {
        if(w != null) {
            this.content = w;
            this.dialog.withWidget(this.content);
        }
    }

    @Override
    public Widget getContent() {
        return this.content;
    }

    @Override
    public boolean isShowing(Widget w) {
        return this.content == w;
    }
}
