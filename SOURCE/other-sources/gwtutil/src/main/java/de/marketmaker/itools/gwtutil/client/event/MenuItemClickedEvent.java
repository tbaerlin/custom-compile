package de.marketmaker.itools.gwtutil.client.event;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;

/**
 * Author: umaurer
 * Created: 24.08.15
 */
public class MenuItemClickedEvent extends GwtEvent<MenuItemClickedHandler> {
    private static Type<MenuItemClickedHandler> TYPE;

    public static void fire(HasMenuItemClickedHandlers source, MenuItem menuItem) {
        if (TYPE != null) {
            MenuItemClickedEvent event = new MenuItemClickedEvent(menuItem);
            source.fireEvent(event);
        }
    }

    public static Type<MenuItemClickedHandler> getType() {
        return TYPE != null ? TYPE : (TYPE = new Type<MenuItemClickedHandler>());
    }

    private final MenuItem menuItem;

    public MenuItemClickedEvent(MenuItem menuItem) {
        this.menuItem = menuItem;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Type<MenuItemClickedHandler> getAssociatedType() {
        return (Type)TYPE;
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    @Override
    protected void dispatch(MenuItemClickedHandler handler) {
        handler.onMenuItemClicked(this);
    }
}
