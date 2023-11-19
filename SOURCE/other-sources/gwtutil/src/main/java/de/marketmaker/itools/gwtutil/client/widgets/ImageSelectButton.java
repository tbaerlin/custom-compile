package de.marketmaker.itools.gwtutil.client.widgets;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

import de.marketmaker.itools.gwtutil.client.event.ItemAddedEvent;
import de.marketmaker.itools.gwtutil.client.event.ItemAddedHandler;
import de.marketmaker.itools.gwtutil.client.event.ItemRemovedEvent;
import de.marketmaker.itools.gwtutil.client.event.ItemRemovedHandler;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;

/**
 * @author umaurer
 * @author Markus Dick
 */
public class ImageSelectButton extends ImageButton implements HasSelectionHandlers<MenuItem> {
    private Menu menu;

    private boolean menuOpen = false;

    private boolean clickOpensMenu = false;

    private final boolean fireEventEvenIfSameItemIsSelected;

    public ImageSelectButton(Image imageDefault, Image imageDisabled, Image imageActive) {
        this(imageDefault, imageDisabled, imageActive, false);
    }

    public ImageSelectButton(Image imageDefault, Image imageDisabled, Image imageActive,
            boolean fireEventEvenIfSameItemIsSelected) {
        super(imageDefault, imageDisabled, imageActive);
        this.fireEventEvenIfSameItemIsSelected = fireEventEvenIfSameItemIsSelected;
        addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                fireClickEvent(clickEvent);
            }
        });
    }

    public ImageSelectButton(final String baseStyle, Image imageDefault, Image imageDisabled,
            Image imageActive) {
        super(baseStyle, imageDefault, imageDisabled, imageActive);
        this.fireEventEvenIfSameItemIsSelected = false;
    }

    public ImageSelectButton withMenu(Menu menu) {
        if (this.menu != null) {
            throw new IllegalStateException("menu is already initialized");
        }
        this.menu = menu;
        this.menu.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                menuOpen = false;
            }
        });

        final List<MenuItem> items = menu.getItems();
        if (!items.isEmpty()) {
            setSelectedItem(items.get(0));
        }
        for (MenuItem menuItem : items) {
            prepareItem(menuItem);
        }
        menu.addItemAddedHandler(new ItemAddedHandler<MenuItem>() {
            @Override
            public void onItemAdded(ItemAddedEvent<MenuItem> event) {
                prepareItem(event.getTarget());
            }
        });
        menu.addItemRemovedHandler(new ItemRemovedHandler<MenuItem>() {
            @Override
            public void onItemRemoved(ItemRemovedEvent<MenuItem> event) {
                ImageSelectButton.this.onItemRemoved(event.getTarget());
            }
        });
        return this;
    }

    public void setSelectedItem(MenuItem menuItem) {
        setSelectedItem(menuItem, true);
    }

    public void setSelectedItem(MenuItem menuItem, boolean fireEvent) {
        MenuItem selectedItem = this.menu.getSelectedItem();
        if (!fireEventEvenIfSameItemIsSelected && selectedItem == menuItem) {
            return;
        }
        if (selectedItem != null) {
            selectedItem.removeStyleName("selected");
            selectedItem = null;
        }
        if (menuItem != null) {
            selectedItem = menuItem;
            menuItem.addStyleName("selected");
        }
        this.menu.setSelectedItem(selectedItem);
        if (fireEvent) {
            SelectionEvent.fire(this, selectedItem);
        }
    }

    public MenuItem getSelectedItem() {
        return this.menu.getSelectedItem();
    }

    private void prepareItem(final MenuItem menuItem) {
        menuItem.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setSelectedItem(menuItem);
            }
        });
    }

    private void onItemRemoved(MenuItem menuItem) {
        if (menuItem == this.menu.getSelectedItem()) {
            this.menu.setSelectedItem(null);
            SelectionEvent.fire(this, null);
        }
    }

    @Override
    public HandlerRegistration addSelectionHandler(
            SelectionHandler<MenuItem> menuItemSelectionHandler) {
        return addHandler(menuItemSelectionHandler, SelectionEvent.getType());
    }

    public void setClickOpensMenu(boolean clickOpensMenu) {
        this.clickOpensMenu = clickOpensMenu;
    }

    protected void fireClickEvent(ClickEvent event) {
        if (event.getX() > event.getRelativeElement().getOffsetLeft() + 2 || clickOpensMenu || isEnabled()) {
            showMenu();
        }
        else if (event.getX() < event.getRelativeElement().getOffsetLeft()) {
            SelectionEvent.fire(this, this.menu.getSelectedItem());
        }
    }

    void showMenu() {
        menu.show(this);
        menuOpen = true;
    }

    @Override
    public boolean onFocusKeyClick() {
        if (this.menuOpen) {
            final MenuItem keySelectionItem = this.menu.getKeySelectionItem();
            if (keySelectionItem != null) {
                keySelectionItem.click();
            }
            else {
                this.menu.hide();
            }
            getElement().focus();
            return true;
        }
        else if (this.clickOpensMenu) {
            showMenu();
            this.menu.moveKeySelection(0);
            getElement().focus();
            return true;
        }
        else {
            return super.onFocusKeyClick();
        }
    }

    @Override
    public boolean onFocusKeyEscape() {
        this.menu.hide();
        return true;
    }

    @Override
    public boolean onFocusKeyHome() {
        if (this.menuOpen) {
            this.menu.setKeySelectionIndex(0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onFocusKeyPageUp() {
        if (this.menuOpen) {
            this.menu.moveKeySelection(-this.menu.getPopupPageItemCount());
            return true;
        }
        return false;
    }

    @Override
    public boolean onFocusKeyUp() {
        if (this.menuOpen) {
            this.menu.moveKeySelection(-1);
            return true;
        }
        return false;
    }

    @Override
    public boolean onFocusKeyDown() {
        if (!this.menuOpen) {
            showMenu();
            this.menu.moveKeySelection(0);
        }
        else {
            this.menu.moveKeySelection(1);
        }
        return true;
    }

    @Override
    public boolean onFocusKeyPageDown() {
        if (this.menuOpen) {
            this.menu.moveKeySelection(this.menu.getPopupPageItemCount());
            return true;
        }
        return false;
    }

    @Override
    public boolean onFocusKeyEnd() {
        if (this.menuOpen) {
            this.menu.setKeySelectionIndex(this.menu.getItemCount() - 1);
            return true;
        }
        return false;
    }

    @Override
    public boolean onFocusKey(char c) {
        if (this.menuOpen) {
            this.menu.setKeySelectionPrefix(c);
            return true;
        }
        return false;
    }
}
