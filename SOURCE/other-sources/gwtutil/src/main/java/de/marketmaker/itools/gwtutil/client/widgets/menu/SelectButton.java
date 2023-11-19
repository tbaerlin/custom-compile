package de.marketmaker.itools.gwtutil.client.widgets.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiChild;
import de.marketmaker.itools.gwtutil.client.event.ItemAddedEvent;
import de.marketmaker.itools.gwtutil.client.event.ItemAddedHandler;
import de.marketmaker.itools.gwtutil.client.event.ItemRemovedEvent;
import de.marketmaker.itools.gwtutil.client.event.ItemRemovedHandler;
import de.marketmaker.itools.gwtutil.client.event.MenuItemClickedEvent;
import de.marketmaker.itools.gwtutil.client.event.MenuItemClickedHandler;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;

import java.util.List;

/**
 * @author Ulrich Maurer
 *         Date: 01.10.12
 */
public class SelectButton extends MenuButton implements HasSelectionHandlers<MenuItem> {
    private SafeHtml noSelectionText = SafeHtmlUtils.fromTrustedString("");
    private SelectionTextStrategy selectionTextStrategy = new DefaultSelectionTextStrategy();

    public interface SelectionTextStrategy {
        SafeHtml getHtml(MenuItem menuItem);
    }

    private class DefaultSelectionTextStrategy implements SelectionTextStrategy {
        @Override
        public SafeHtml getHtml(MenuItem menuItem) {
            return menuItem == null
                    ? noSelectionText
                    : SafeHtmlUtils.fromSafeConstant(menuItem.getHtml());
        }
    }

    public SelectButton() {

    }

    public SelectButton(RendererType rendererType) {
        super(rendererType);
    }

    @Override
    @UiChild(tagname = "menu")
    public SelectButton withMenu(Menu menu) {
        return withMenu(menu, true);
    }

    public SelectButton withMenu(final Menu menu, boolean selectFirstEntry) {
        super.withMenu(menu);
        final List<MenuItem> items = menu.getItems();
        if (!items.isEmpty() && selectFirstEntry) {
            setSelectedItem(items.get(0));
        }
        else {
            setHTML(this.selectionTextStrategy.getHtml(null));
        }
        if (selectFirstEntry) {
            menu.addItemAddedHandler(new ItemAddedHandler<MenuItem>() {
                @Override
                public void onItemAdded(ItemAddedEvent<MenuItem> event) {
                    if (menu.getSelectedItem() == null) {
                        setSelectedItem(event.getTarget(), false);
                    }
                }
            });
        }
        menu.addItemRemovedHandler(new ItemRemovedHandler<MenuItem>() {
            @Override
            public void onItemRemoved(ItemRemovedEvent<MenuItem> event) {
                SelectButton.this.onItemRemoved(event.getTarget());
            }
        });
        menu.addMenuItemClickedHandler(new MenuItemClickedHandler() {
            @Override
            public void onMenuItemClicked(MenuItemClickedEvent event) {
                setSelectedItem(event.getMenuItem());
            }
        });
        return this;
    }

    public SelectButton withNoSelectionText(SafeHtml noSelectionText) {
        this.noSelectionText = noSelectionText;
        if (getMenu() != null && getMenu().getSelectedItem() == null) {
            setHTML(this.selectionTextStrategy.getHtml(null));
        }
        return this;
    }

    @Override
    public SelectButton withTooltip(String tooltip) {
        Tooltip.addQtip(this, tooltip);
        return this;
    }

    @Override
    public SelectButton withClickOpensMenu() {
        super.withClickOpensMenu();
        return this;
    }

    public SelectButton withSelectionTextStrategy(SelectionTextStrategy selectionTextStrategy) {
        this.selectionTextStrategy = selectionTextStrategy;
        return this;
    }

    private void onItemRemoved(MenuItem menuItem) {
        if (menuItem == getMenu().getSelectedItem()) {
            getMenu().setSelectedItem(null);
            setHTML(this.noSelectionText);
            SelectionEvent.fire(this, null);
        }
    }

    public void setSelectedItem(MenuItem menuItem) {
        setSelectedItem(menuItem, true);
    }

    public void setSelectedItem(MenuItem menuItem, boolean fireEvent) {
        MenuItem selectedItem = getMenu().getSelectedItem();
        if (menuItem == selectedItem) {
            return;
        }
        if (selectedItem != null) {
            selectedItem.removeStyleName("selected");
            selectedItem = null;
        }
        if (menuItem == null) {
            setHTML(this.selectionTextStrategy.getHtml(null));
        }
        else {
            selectedItem = menuItem;
            menuItem.addStyleName("selected");
            setHTML(this.selectionTextStrategy.getHtml(menuItem));
        }
        getMenu().setSelectedItem(selectedItem);
        if (fireEvent) {
            SelectionEvent.fire(this, selectedItem);
        }
    }

    public MenuItem getSelectedItem() {
        return getMenu().getSelectedItem();
    }

    @Override
    public SelectButton withIcon(String iconMapping) {
        super.withIcon(iconMapping);
        return this;
    }

    @Override
    public SelectButton withClickHandler(ClickHandler clickHandler) {
        super.withClickHandler(clickHandler);
        return this;
    }

    public HandlerRegistration addSelectionHandler(SelectionHandler<MenuItem> handler) {
        return addHandler(handler, SelectionEvent.getType());
    }

    public SelectButton withSelectionHandler(SelectionHandler<MenuItem> handler) {
        addSelectionHandler(handler);
        return this;
    }

    @Override
    protected void fireClickEvent(ClickEvent event) {
        if (!isEnabled()) {
            return;
        }
        if (event.getX() > getRightContentX() + 2 || isClickOpensMenu() || isActive()) {
            showMenu();
        }
        else if (event.getX() < getRightContentX()) {
            super.fireClickEvent(event);
            SelectionEvent.fire(this, getMenu().getSelectedItem());
        }
    }

    @Override
    public void setData(String key, Object value) {
        throw new UnsupportedOperationException("setData not allowed for SelectButton");
    }

    @Override
    public Object getData(String key) {
        final MenuItem selectedItem = getMenu().getSelectedItem();
        return selectedItem == null ? null : selectedItem.getData(key);
    }

    public boolean setSelectedData(String key, Object value) {
        return setSelectedData(key, value, true);
    }

    public boolean setSelectedData(String key, Object value, boolean fireEvent) {
        for (MenuItem menuItem : getMenu().getItems()) {
            if ((value == null && menuItem.getData(key) == null)
                    || (value != null && value.equals(menuItem.getData(key)))) {
                setSelectedItem(menuItem, fireEvent);
                return true;
            }
        }
        return false;
    }
}
