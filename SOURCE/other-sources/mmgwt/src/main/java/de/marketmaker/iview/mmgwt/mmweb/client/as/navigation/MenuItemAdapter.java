package de.marketmaker.iview.mmgwt.mmweb.client.as.navigation;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.MenuModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: umaurer
 * Date: 29.04.13
 * Time: 09:32
 */
public class MenuItemAdapter implements Item {
    public static final NavTreeItemIdentifier<MenuItemAdapter> MIA_IDENTIFIER = (menuItemAdapter, id) -> {
        final MenuModel.Item item = menuItemAdapter.getMenuItem();
        return !(id == null || item.getId() == null) && id.equals(item.getId());
    };


    private final MenuModel.Item menuItem;
    private final List<MenuItemAdapter> children;
    private final boolean hasSelectionHandler;
    private int level;

    public MenuItemAdapter(MenuModel.Item rootItem) {
        this(rootItem, 0);
    }

    private MenuItemAdapter(MenuModel.Item menuItem, int level) {
        this.menuItem = menuItem;
        this.children = createChildren();
        this.hasSelectionHandler = menuItem.isEnabled() && (menuItem.isLinkItem() || menuItem.getControllerId() != null);
        this.level = level;
    }

    public MenuModel.Item getMenuItem() {
        return menuItem;
    }

    @Override
    public String getName() {
        return this.menuItem.getName();
    }

    @Override
    public boolean isLeaf() {
        return this.children == null || this.children.isEmpty();
    }

    @Override
    public List<? extends Item> getChildren() {
        return this.children;
    }

    public List<MenuItemAdapter> createChildren() {
        if (this.menuItem.isLeaf()) {
            return null;
        }
        final ArrayList<MenuModel.Item> items = this.menuItem.getItems();
        final ArrayList<MenuItemAdapter> children = new ArrayList<>(items.size());
        for (MenuModel.Item item : items) {
            if (item.isHidden() || !item.isEnabled()) {
                continue;
            }
            children.add(new MenuItemAdapter(item, this.level + 1));
        }
        return children.isEmpty() ? null : children;
    }

    @Override
    public boolean isAlwaysOpen() {
        return this.level < 1;
    }

    @Override
    public boolean isOpenByDefault() {
        return false;
    }

    @Override
    public boolean isOpenWithParent() {
        return this.level > 2;
    }

    @Override
    public boolean isOpenWithSelection() {
        return this.level == 1;
    }

    @Override
    public boolean isClosingSiblings() {
        return this.level == 1;
    }

    @Override
    public boolean isVisible() {
        return !this.menuItem.isHidden() && this.menuItem != MenuModel.SEPARATOR;
    }

    @Override
    public boolean hasSelectionHandler() {
        return this.hasSelectionHandler;
    }

    @Override
    public boolean isHasDelegate() {
        return false;
    }

    @Override
    public boolean isSelectFirstChildOnOpen() {
        return false;
    }

    @Override
    public ImageSpec getIcon() {
        // TODO: provide icon in menu???
        return null;
    }

    @Override
    public SafeHtml getIconTooltip() {
        return null;
    }

    @Override
    public Widget getEndIcon() {
        return null;
    }

    @Override
    public String getEndIconCellClass() {
        return null;
    }

    @Override
    public ImageSpec getLeftIcon() {
        return null;
    }

    @Override
    public SafeHtml getLeftIconTooltip() {
        return null;
    }
}