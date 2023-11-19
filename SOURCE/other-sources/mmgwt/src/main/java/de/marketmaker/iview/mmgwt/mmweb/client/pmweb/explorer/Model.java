/*
 * Model.java
 *
 * Created on 17.04.2013 14:54:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.ImageSpec;
import de.marketmaker.itools.gwtutil.client.widgets.tree.HasParent;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Michael Lösch
 */

class Model {
    static final class Item implements HasParent, de.marketmaker.iview.mmgwt.mmweb.client.as.navigation.Item {
        private final String id;

        private final String name;

        private final String iconStyle;

        private Item parent;

        private ArrayList<Item> children = null;

        private Item selected = null;

        private boolean moreChildren;

        private boolean openableWorkspace = false;

        protected Item(String id, String name) {
            this(id, name, null);
        }

        protected Item(String id, String name, String iconStyle) {
            this.id = id;
            this.name = name;
            this.iconStyle = iconStyle;
        }

        public Item add(Item element) {
            if (element == null) {
                return this;
            }
            final String elementId = element.getId();
            if (elementId != null && !Customer.INSTANCE.isJsonMenuElementNotFalse(elementId)) {
                return this;
            }
            if (this.children == null) {
                this.children = new ArrayList<>();
                this.selected = element;
            }
            this.children.add(element);
            element.setParent(this);
            return this;
        }

        public void clearChildren() {
            this.children = null;
            this.selected = null;
        }

        @Override
        public Item getParent() {
            return parent;
        }

        public void setParent(Item parent) {
            this.parent = parent;
        }

        @Override
        public String getName() {
            return name;
        }

        public String getId() {
            return this.id;
        }

        public String getDatabaseId() {
            if (this.id.contains("-")) {
                return this.id.substring(0, this.id.indexOf("-"));
            }
            else {
                return this.id;
            }
        }

        @Override
        public String toString() {
            return this.id + " - " + this.name;
        }

        Item getSelected() {
            return this.selected != null ? this.selected.getSelected() : this;
        }

        Item[] getSelectedItems(final int depth) {
            final Item[] elements = this.selected != null
                    ? this.selected.getSelectedItems(depth + 1)
                    : new Item[depth + 1];
            elements[depth] = this;
            return elements;
        }

        protected void select(int n) {
            this.selected = this.children.get(n);
        }

        protected void deepSelect() {
            select(this.selected);
        }

        protected void select() {
            select(null);
        }

        private void select(Item e) {
            this.selected = e;
            if (this.parent != null) {
                this.parent.select(this);
            }
        }

        public boolean hasMoreChildren() {
            return this.moreChildren;
        }

        public void setMoreChildren(boolean moreChildren) {
            this.moreChildren = moreChildren;
        }

        public void setOpenableWorkspace(boolean openableWorkspace) {
            this.openableWorkspace = openableWorkspace;
        }

        public boolean hasOpenableWorkspace() {
            return this.openableWorkspace;
        }

        // Implementation of as.navigation.Item

        @Override
        public boolean isLeaf() {
            return this.children == null;
        }

        @Override
        public List<Item> getChildren() {
            return this.children;
        }

        @Override
        public boolean isAlwaysOpen() {
            return false;
        }

        @Override
        public boolean isOpenByDefault() {
            return false;
        }

        @Override
        public boolean isOpenWithParent() {
            return false;
        }

        @Override
        public boolean isOpenWithSelection() {
            return false;
        }

        @Override
        public boolean isClosingSiblings() {
            return this.parent != null && this.parent.parent == null;
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
        public boolean isVisible() {
            return true;
        }

        @Override
        public boolean hasSelectionHandler() {
            return this.openableWorkspace;
        }

        @Override
        public ImageSpec getIcon() {
            if (this.iconStyle == null) {
                return null;
            }
            return IconImage.getImageResource(this.iconStyle);
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

    private Item root = new Item("0", "ROOT"); // $NON-NLS$

    private final HashMap<String, Item> itemById = new HashMap<>();
    private final HashMap<String, Integer> idCount = new HashMap<>();

    public Item add(Item element) {
        return this.root.add(element);
    }

    public Item getRootItem() {
        return this.root;
    }

    public Item getFirstItem() {
        return getFirstItem(getItems());
    }

    private Item getFirstItem(List<Item> list) {
        if (list != null) {
            for (Item item : list) {
                final Item firstItem = getFirstItem(item);
                if (firstItem != null) {
                    return firstItem;
                }
            }
        }
        return null;
    }

    private Item getFirstItem(Item item) {
        final List<Model.Item> subList = item.getChildren();
        if ((subList == null || subList.isEmpty()) && item.hasOpenableWorkspace()) {
            return item;
        }
        else if (subList != null && !subList.isEmpty()) {
            return getFirstItem(subList);
        }
        else {
            return null;
        }
    }

    public List<Item> getItems() {
        return root.getChildren();
    }

    public Item getSelectedItem() {
        return this.root.getSelected();
    }

    public Item[] getSelectedItems() {
        return this.root.selected.getSelectedItems(0);
    }

    public String[] getSelectedIds() {
        final Item[] elements = getSelectedItems();
        final String[] ids = new String[elements.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = elements[i].getId();
        }
        return ids;
    }

    public Item getItem(String id) {
        return this.itemById.get(id);
    }

    public Item select(String key) {
        final Item element = getItem(key);
        if (element == null) {
            final Item firstItem = getFirstItem();
            if(firstItem != null) {
                firstItem.deepSelect();
            }
            return firstItem;
        }
        element.select();

        return getSelectedItem();
    }

    public Item createItem(String id, String name, String iconStyle) {
        return createItem(id, name, iconStyle, false, false);
    }

    public Item createItem(String id, String name, String iconStyle,
                           boolean hasMoreChildren, boolean openableWorkspace) {
        final Item result;
        if (this.idCount.containsKey(id)) {
            Integer count = this.idCount.get(id);
            this.idCount.put(id, ++count);
            result = new Item(id + "-" + count, name, iconStyle);
        }
        else {
            this.idCount.put(id, 0);
            result = new Item(id, name, iconStyle);
        }
        result.setMoreChildren(hasMoreChildren);
        result.setOpenableWorkspace(openableWorkspace);
        if (this.itemById.put(result.getId(), result) != null) {
            throw new IllegalArgumentException("duplicate element: " + result.getId()); // $NON-NLS-0$
        }
        return result;
    }

    public boolean hasData() {
        return getItems() != null;
    }

}