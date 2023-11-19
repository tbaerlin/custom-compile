/*
 * MenuModel.java
 *
 * Created on 17.03.2008 14:54:06
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.tree.HasParent;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Initializer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MenuModel {
    /**
     * An item in a menu, may be a leaf item or contain subitems.
     */
    public static final class Item implements HasParent {
        private final String id;

        private final String name;

        private final String iconStyle;

        private Item parent;

        private boolean hidden = false;

        private boolean linkItem = false;

        private boolean enabled = true;

        private final String controllerId;

        private ArrayList<Item> items = null;

        private Item selected = null;

        private boolean withSeparator;

        private Command doOnClickCommand;

        protected Item(String id, String name) {
            this(id, null, name, null);
        }

        protected Item(String id, String controllerId, String name, String iconStyle) {
            this.id = id;
            this.controllerId = controllerId;
            this.name = name;
            this.iconStyle = iconStyle;
        }

        protected static Item createLinkItem(String id, String url, String name, String iconStyle) {
            final Item item = new Item(id, url, name, iconStyle);
            item.linkItem = true;
            return item;
        }

        public Item add(Item element) {
            if (element == null) {
                return this;
            }
            final String elementId = element.getId();
            if (elementId != null && !Customer.INSTANCE.isJsonMenuElementNotFalse(elementId)) {
                return this;
            }
            assert !this.hidden;
            if (this.items == null) {
                this.items = new ArrayList<>();
                if (this.controllerId == null) {
                    this.selected = element;
                }
            }
            this.items.add(element);
            element.setParent(this);
            return this;
        }

        public Item withOnClickCommand(Command command) {
            this.doOnClickCommand = command;
            return this;
        }

        public Item addAll(List<Item> elements) {
            if (elements == null || elements.isEmpty()) {
                return this;
            }
            for (Item element : elements) {
                add(element);
            }
            return this;
        }

        public Item add(Selector selector, Item element, Item alternativeItem) {
            if (selector.isAllowed()) {
                return add(element);
            }
            return add(alternativeItem);
        }

        public Item add(Selector selector, Item element) {
            if (!selector.isAllowed()) {
                return this;
            }
            return add(element);
        }

        public Item add(FeatureFlags.Feature feature, Item element) {
            if (feature.isEnabled()) {
                return add(element);
            }
            return this;
        }

        public Item add(boolean flag, Item element) {
            if (!flag) {
                return this;
            }
            return add(element);
        }

        public Item add(boolean flag, Initializer<Item> initializer) {
            if (!flag) {
                return this;
            }
            return add(initializer.init());
        }

        @SuppressWarnings("unused")
        public Item addIfAll(Selector[] selectors, Item element, Selector alternativeSelector, Item alternativeElement) {
            for (final Selector selector : selectors) {
                if (selector != null && !selector.isAllowed()) {
                    if (alternativeSelector.isAllowed()) {
                        return add(alternativeElement);
                    }
                    return this;
                }
            }
            return add(element);
        }

        public Item addIfAll(Selector[] selectors, Item element) {
            for (final Selector selector : selectors) {
                if (selector != null && !selector.isAllowed()) {
                    return this;
                }
            }
            return add(element);
        }

        public Item addIfAny(Selector[] selectors, Item element) {
            for (final Selector selector : selectors) {
                if (selector.isAllowed()) {
                    return add(element);
                }
            }
            return this;
        }

        public Item addIfJsonExists(String jsonKey, Item element) {
            return SessionData.INSTANCE.hasGuiDef(jsonKey) ? add(element) : this;
        }

        @SuppressWarnings("unused")
        public Item addIfUserPropertyTrue(String userProperty, Item element) {
            return SessionData.INSTANCE.isUserPropertyTrue(userProperty) ? add(element) : this;
        }

        public Item getParent() {
            return parent;
        }

        public void setParent(Item parent) {
            this.parent = parent;
        }

        public String getControllerId() {
            return controllerId;
        }

        public String getName() {
            return name;
        }

        public String getId() {
            return id;
        }

        public String toString() {
            return this.id + " - " + this.name;
        }

        public String getIconStyle() {
            return iconStyle;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Item setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Command getDoOnClickCommand() {
            return doOnClickCommand;
        }

        public Item hide() {
            return setHidden(true);
        }

        public Item setHidden(boolean hidden) {
            assert isLeaf() : "MenuModel.Item must be leaf: " + getId() + " - " + getName();
            this.hidden = hidden;
            return this;
        }

        public boolean isHidden() {
            return hidden;
        }

        public boolean isLeaf() {
            return this.items == null;
        }

        public boolean isLinkItem() {
            return linkItem;
        }

        public ArrayList<Item> getItems() {
            assert this.items != null;
            return this.items;
        }

        private void clearItems() {
            if(this.items != null) {
                this.items.clear();
            }
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
            this.selected = this.items.get(n);
        }

        protected void select() {
            select(this.controllerId != null ? null : this.selected);
        }

        private void select(Item e) {
            this.selected = e;
            if (this.parent != null) {
                this.parent.select(this);
            }
        }

        public boolean isWithSeparator() {
            return this.withSeparator;
        }

        public Item appendSeparator() {
            this.withSeparator = true;
            return this;
        }
    }

    public static final Item SEPARATOR = new Item("SEP", "SEP", "Separator", null); // $NON-NLS$

    private Item root = new Item("0", "ROOT"); // $NON-NLS$

    private final HashMap<String, Item> elementsById = new HashMap<>();

    public Item add(Item element) {
        return this.root.add(element);
    }

    public Item add(Initializer<Item> initializer) {
        return add(initializer.init());
    }

    public Item add(Selector selector, Item element) {
        if (selector.isAllowed()) {
            return this.root.add(element);
        }
        return element;
    }

    public Item getRootItem() {
        return this.root;
    }

    public Item getFirstItem() {
        return getFirstItem(getItems());
    }

    private Item getFirstItem(ArrayList<Item> list) {
        if (list.isEmpty()) {
            throw new NullPointerException("menu list is empty"); // $NON-NLS-0$
        }
        final MenuModel.Item item = list.get(0);
        return item.isLeaf()
                ? item
                : getFirstItem(item.getItems());
    }

    public ArrayList<Item> getItems() {
        return root.getItems();
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

    @SuppressWarnings("unused")
    public String getTopSelectedId() {
        return this.root.selected.getId();
    }

    @SuppressWarnings("unused")
    public String getSubSelectedId() {
        final Item item = this.root.selected.getSelected();
        return item != null ? item.getId() : null;
    }

    public Item getElement(String key) {
        final String[] token = StringUtil.splitToken(key);
        return this.elementsById.get(token[0]);
    }

    public boolean hasElement(String key) {
        final String[] token = StringUtil.splitToken(key);
        return this.elementsById.get(token[0]) != null;
    }

    public Item select(String key) {
        final Item element = getElement(key);
        if (element == null) {
            throw new IllegalArgumentException("MenuModel cannot select: " + key); // $NON-NLS-0$
        }
        element.select();
        return getSelectedItem();
    }

    @SuppressWarnings("unused")
    public Item createMenu(String id, String name) {
        return createMenu(id, null, name);
    }

    public Item createMenu(String id, String controllerId, String name) {
        return createItem(id, controllerId, name, null, true);
    }

    public Item createItem(String id, String controllerId, String name, String iconStyle,
                           boolean enabled) {
        final Item result = new Item(id, controllerId, name, iconStyle);
        result.setEnabled(enabled);
        if (this.elementsById.put(id, result) != null) {
            throw new IllegalArgumentException("duplicate element: " + id); // $NON-NLS-0$
        }
        return result;
    }

    public void clearItems(Item toClear) {
        if(toClear.isLeaf()) {
            return;
        }
        for (Item item : toClear.getItems()) {
            removeItem(item);
        }
        toClear.clearItems();
    }

    private void removeItem(Item toRemove) {
        toRemove.setParent(null);
        if(this.elementsById.remove(toRemove.getId()) == null) {
            Firebug.warn("<MenuModel.clearItems>: cleared element '" + toRemove.getId() + "' was never added.");
        }

        if(toRemove.isLeaf()) {
            return;
        }

        for (Item item : toRemove.getItems()) {
            removeItem(item);
        }
        toRemove.clearItems();
    }

    public Item createLinkItem(String id, String url, String name, String iconStyle,
                           boolean enabled) {
        final Item result = Item.createLinkItem(id, url, name, iconStyle);
        result.setEnabled(enabled);
        if (this.elementsById.put(id, result) != null) {
            throw new IllegalArgumentException("duplicate element: " + id); // $NON-NLS-0$
        }
        return result;
    }

    public SafeHtml getPath() {
        if (SessionData.isAsDesign()) {
            return getAsPath(getSelectedItem());
        }
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        appendPath(getSelectedItem(), sb, 0);
        return sb.toSafeHtml();
    }

    private int appendPath(Item mi, SafeHtmlBuilder sb, int count) {
        if (mi.getParent() == this.root) {
            return StringUtil.appendHeader(sb, mi.getName(), count);
        }
        else {
            final int newCount = appendPath(mi.getParent(), sb, count);
            return StringUtil.appendHeader(sb, mi.getName(), newCount);
        }
    }

    private SafeHtml getAsPath(Item mi) {
        final ArrayList<String> list = new ArrayList<>();
        while (mi.getParent() != this.root) {
            list.add(mi.getName());
            mi = mi.getParent();
        }
        final String[] texts = new String[list.size()];
        int i = list.size();
        for (String text : list) {
            i--;
            texts[i] = text;
        }
        return StringUtil.asHeader(texts);
    }
}
