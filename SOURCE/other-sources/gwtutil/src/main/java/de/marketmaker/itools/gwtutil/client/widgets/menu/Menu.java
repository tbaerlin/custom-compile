package de.marketmaker.itools.gwtutil.client.widgets.menu;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.event.HasItemAddedHandlers;
import de.marketmaker.itools.gwtutil.client.event.HasItemRemovedHandlers;
import de.marketmaker.itools.gwtutil.client.event.HasMenuItemClickedHandlers;
import de.marketmaker.itools.gwtutil.client.event.ItemAddedEvent;
import de.marketmaker.itools.gwtutil.client.event.ItemAddedHandler;
import de.marketmaker.itools.gwtutil.client.event.ItemRemovedEvent;
import de.marketmaker.itools.gwtutil.client.event.ItemRemovedHandler;
import de.marketmaker.itools.gwtutil.client.event.MenuItemClickedEvent;
import de.marketmaker.itools.gwtutil.client.event.MenuItemClickedHandler;
import de.marketmaker.itools.gwtutil.client.widgets.floating.FloatingPopupPanel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ulrich Maurer
 *         Date: 01.10.12
 */
public class Menu implements HasItemAddedHandlers<MenuItem>, HasItemRemovedHandlers<MenuItem>, HasMenuItemClickedHandlers, HasWidgets {
    private static final String ATT_KEY_SELECTION_INDEX = "keySelectIndex";
    private static final String STYLE_KEY_SELECT = "keySelect";
    private final FloatingPopupPanel popupPanel = new FloatingPopupPanel(true);
    private final FlowPanel panel = new FlowPanel();
    private boolean separateNext = false;
    private final List<MenuItem> listMenuItems = new ArrayList<>();
    private MenuItem selectedItem = null;
    private MenuItem noSelectionItem = null;

    public Menu() {
        this.popupPanel.setStyleName("mm-menu");
        this.popupPanel.setWidget(this.panel);
        this.popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                removeKeySelection();
            }
        });
    }

    public HandlerRegistration addCloseHandler(CloseHandler<PopupPanel> handler) {
        return this.popupPanel.addCloseHandler(handler);
    }

    @Override
    public HandlerRegistration addItemAddedHandler(ItemAddedHandler<MenuItem> handler) {
        return this.popupPanel.addHandler(handler, ItemAddedEvent.getType());
    }

    @Override
    public HandlerRegistration addItemRemovedHandler(ItemRemovedHandler<MenuItem> handler) {
        return this.popupPanel.addHandler(handler, ItemRemovedEvent.getType());
    }

    @Override
    public HandlerRegistration addMenuItemClickedHandler(MenuItemClickedHandler handler) {
        return this.popupPanel.addHandler(handler, MenuItemClickedEvent.getType());
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.popupPanel.fireEvent(event);
    }

    public void add(final MenuItem menuItem) {
        if (this.separateNext && this.panel.getWidgetCount() > 0) {
            final Label sep = new Label();
            sep.setStyleName("mm-menuSeparator");
            this.panel.add(sep);
            this.separateNext = false;
        }
        this.listMenuItems.add(menuItem);
        this.panel.add(menuItem);
        menuItem.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onMenuItemClicked(menuItem);
            }
        });
        menuItem.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                setKeySelectionItem(menuItem);
            }
        });
        ItemAddedEvent.fire(this, menuItem);
    }

    public MenuItem add(final String text) {
        final MenuItem menuItem = new MenuItem(text);
        add(menuItem);
        return menuItem;
    }

    private void onMenuItemClicked(MenuItem menuItem) {
        hide();
        // it is perfectly valid to select the <no selection> item
        MenuItemClickedEvent.fire(this, menuItem);
    }

    public void addNoSelectionItem(MenuItem noSelectionItem) {
        this.noSelectionItem = noSelectionItem;
        addFirst(noSelectionItem);
    }

    public boolean isNoSelectionItem(MenuItem menuItem) {
        return menuItem == this.noSelectionItem;
    }

    public void addFirst(final MenuItem menuItem) {
        insert(menuItem, 0);
    }

    public void insert(final MenuItem menuItem, int index) {
        this.listMenuItems.add(index, menuItem);
        this.panel.insert(menuItem, index);
        menuItem.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                onMenuItemClicked(menuItem);
            }
        });
        ItemAddedEvent.fire(this, menuItem);
    }

    public void addSeparator() {
        this.separateNext = true;
    }

    public void setMinWidth(int minWidth) {
        if (minWidth > 0) {
            this.panel.getElement().getStyle().setPropertyPx("minWidth", minWidth);
        }
        else {
            this.panel.getElement().getStyle().clearProperty("minWidth");
        }
    }

    public void hide() {
        this.popupPanel.hide();
    }

    public void show(final Widget widget) {
        this.popupPanel.showNearby(widget, this.selectedItem);
    }

    public List<MenuItem> getItems() {
        return this.listMenuItems;
    }

    public int getItemCount() {
        return this.listMenuItems.size();
    }

    public int getPopupPageItemCount() {
        return 10; // TODO
    }

    public void removeAll() {
        removeAll(true);
    }

    public void removeAll(boolean fireEvent) {
        this.panel.clear();
        this.panel.getElement().removeAttribute(ATT_KEY_SELECTION_INDEX);
        this.separateNext = false;
        final ArrayList<MenuItem> listMenuItems = new ArrayList<>(this.listMenuItems);
        this.listMenuItems.clear();
        if(fireEvent) {
            for (MenuItem menuItem : listMenuItems) {
                ItemRemovedEvent.fire(this, menuItem);
            }
        }
        this.selectedItem = null;
    }

    public void remove(MenuItem menuItem) {
        this.panel.remove(menuItem);
        this.listMenuItems.remove(menuItem);
        ItemRemovedEvent.fire(this, menuItem);
        if (menuItem == this.selectedItem) {
            this.selectedItem = null;
        }
    }

    public MenuItem getSelectedItem() {
        return this.selectedItem;
    }

    public void setSelectedItem(MenuItem selectedItem) {
        this.selectedItem = selectedItem;
    }

    private int getSelectedIndex() {
        for (int i = 0; i < this.listMenuItems.size(); i++) {
            if (this.listMenuItems.get(i) == this.selectedItem) {
                return i;
            }
        }
        return 0;
    }

    public void removeKeySelection() {
        final String sIndex = this.panel.getElement().getAttribute(ATT_KEY_SELECTION_INDEX);
        if (sIndex != null && !sIndex.isEmpty()) {
            final int selectedIndex = Integer.parseInt(sIndex);
            if (selectedIndex < this.listMenuItems.size()) {
                this.listMenuItems.get(selectedIndex).removeStyleName(STYLE_KEY_SELECT);
            }
            this.panel.getElement().removeAttribute(ATT_KEY_SELECTION_INDEX);
        }
    }

    public void moveKeySelection(int delta) {
        final int keySelectionIndex = getKeySelectionIndex();
        setKeySelectionIndex(keySelectionIndex == -1 ? getSelectedIndex() : keySelectionIndex + delta);
    }

    public MenuItem getKeySelectionItem() {
        final int index = getKeySelectionIndex();
        return index == -1 ? null : this.listMenuItems.get(index);
    }

    public int getKeySelectionIndex() {
        final String sIndex = this.panel.getElement().getAttribute(ATT_KEY_SELECTION_INDEX);
        if (sIndex != null && !sIndex.isEmpty()) {
            final int selectedIndex = Integer.parseInt(sIndex);
            if (selectedIndex >= 0 && selectedIndex < this.listMenuItems.size()) {
                return selectedIndex;
            }
        }
        return -1;
    }

    public void setKeySelectionIndex(int newIndex) {
        final int keySelectionIndex = getKeySelectionIndex();
        if (keySelectionIndex != -1) {
            this.listMenuItems.get(keySelectionIndex).removeStyleName(STYLE_KEY_SELECT);
        }
        final int itemCount = getItemCount();
        if (newIndex < 0) {
            newIndex = 0;
        }
        else if (newIndex >= itemCount) {
            newIndex = itemCount - 1;
        }
        this.panel.getElement().setAttribute(ATT_KEY_SELECTION_INDEX, String.valueOf(newIndex));
        final MenuItem newKeySelectItem = this.listMenuItems.get(newIndex);
        newKeySelectItem.addStyleName(STYLE_KEY_SELECT);
        this.popupPanel.scrollToElement(newKeySelectItem.getElement());
    }

    private void setKeySelectionItem(MenuItem menuItem) {
        for (int i = 0, max = this.listMenuItems.size(); i < max; i++) {
            if (menuItem == this.listMenuItems.get(i)) {
                setKeySelectionIndex(i);
                return;
            }
        }
    }

    public void setKeySelectionPrefix(char c) {
        for (int index = getKeySelectionIndex() + 1, itemCount = getItemCount(); index < itemCount; index++) {
            if (this.listMenuItems.get(index).hasPrefixChar(c)) {
                setKeySelectionIndex(index);
                return;
            }
        }
        for (int index = 0, max = getKeySelectionIndex(); index < max; index++) {
            if (this.listMenuItems.get(index).hasPrefixChar(c)) {
                setKeySelectionIndex(index);
                return;
            }
        }
    }

    @Override
    public void add(Widget widget) {
        if (!(widget instanceof MenuItem)) {
            throw new IllegalArgumentException("Menu.add() only supports MenuItems");
        }
        add((MenuItem) widget);
    }

    @Override
    public void clear() {
        removeAll();
    }

    @Override
    public Iterator<Widget> iterator() {
        throw new RuntimeException("Don't use Menu.iterator()! Only implemented to support HasWidgets");
    }

    @Override
    public boolean remove(Widget widget) {
        throw new RuntimeException("Don't use Menu.remove(Widget)! Only implemented to support HasWidgets");
    }
}
