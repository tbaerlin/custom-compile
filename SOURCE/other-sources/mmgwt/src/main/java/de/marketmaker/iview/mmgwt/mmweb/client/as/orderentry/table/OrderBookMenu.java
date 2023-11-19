/*
 * OrderBookMenu.java
 *
 * Created on 07.11.13 14:35
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.table;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.pmxml.OrderbookDataType;

/**
 * @author Markus Dick
 */
public class OrderBookMenu<T extends OrderbookDataType> {
    private final Menu menu;
    private T data;

    private final MenuItem showOrderMenuItem;
    private final MenuItem deleteOrderMenuItem;
    private final MenuItem changeOrderMenuItem;

    public OrderBookMenu(final Callback<T> callback) {
        this.menu = new Menu();

        this.showOrderMenuItem = new MenuItem(I18n.I.detail(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                callback.onShowOrderClicked(event, data);
            }
        });
        this.menu.add(this.showOrderMenuItem);

        this.changeOrderMenuItem = new MenuItem(I18n.I.changeOrder(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                callback.onChangeOrderClicked(event, data);
            }
        });
        this.menu.add(this.changeOrderMenuItem);

        this.deleteOrderMenuItem = new MenuItem(I18n.I.deleteOrder(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                callback.onDeleteOrderClicked(event, data);
            }
        });
        this.menu.add(this.deleteOrderMenuItem);
    }

    public void setShowOrderMenuItemVisible(boolean visible) {
        this.showOrderMenuItem.setEnabled(visible);
        this.showOrderMenuItem.setVisible(visible);
    }

    public void setChangeOrderMenuItemVisible(boolean visible) {
        this.changeOrderMenuItem.setEnabled(visible);
        this.changeOrderMenuItem.setVisible(visible);
    }

    public void setDeleteOrderMenuItemVisible(boolean visible) {
        this.deleteOrderMenuItem.setEnabled(visible);
        this.deleteOrderMenuItem.setVisible(visible);
    }

    public void show(T data, Element anchor) {
        this.data = data;

        WrapperWidget w = new WrapperWidget(anchor);
        this.menu.show(w);
    }

    private static class WrapperWidget extends Widget {
        public WrapperWidget(Element element) {
            setElement(element);
        }
    }

    public interface Callback<T> {
        void onDeleteOrderClicked(ClickEvent event, T data);
        void onShowOrderClicked(ClickEvent event, T data);
        void onChangeOrderClicked(ClickEvent event, T data);
    }
}
