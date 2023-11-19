/*
 * AbstractFavouriteItemsStore.java
 *
 * Created on 17.11.2015 08:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * <ul>
 * <li><code>T</code>: type of the objects to be added</li>
 * <li><code>I</code>: type of the concrete workspace item</li>
 * </ul>
 * @author mdick
 */
public abstract class AbstractFavouriteItemsStore<T, I, A extends AbstractFavouriteItemsStore.AbstractFavouriteItem>
        implements FavouriteItemsStore<T> {

    public static final String FAVOURITE_ITEM_RENAMED = FavouriteItemsStores.FAVOURITE_ITEM_CONFIG_CHANGED_EVENT_PREFIX + ".renamed"; // $NON-NLS$

    public static final String FAVOURITE_ITEM_REMOVED = FavouriteItemsStores.FAVOURITE_ITEM_CONFIG_CHANGED_EVENT_PREFIX + ".removed"; // $NON-NLS$

    public static final String FAVOURITE_ITEM_MOVED = FavouriteItemsStores.FAVOURITE_ITEM_CONFIG_CHANGED_EVENT_PREFIX + ".moved"; // $NON-NLS$

    public static final String FAVOURITE_ITEM_ADDED = FavouriteItemsStores.FAVOURITE_ITEM_CONFIG_CHANGED_EVENT_PREFIX + ".added"; // $NON-NLS$

    /**
     * @return a list with our without items, but never null.
     */
    protected abstract List<I> getRawItems();

    protected abstract A adaptRawItem(I item);

    public List<A> getItems() {
        final List<I> rawItems = getRawItems();
        if (rawItems.isEmpty()) {
            return Collections.emptyList();
        }

        final ArrayList<A> adaptedItems = new ArrayList<>(rawItems.size());
        for (I item : rawItems) {
            adaptedItems.add(adaptRawItem(item));
        }
        Collections.sort(adaptedItems, (o1, o2) -> o1.getOrder() - o2.getOrder());
        return adaptedItems;
    }

    @Override
    public FavouriteItem getItem(String identifier) {
        for (FavouriteItem favouriteItem : getItems()) {
            if (StringUtil.equals(identifier, favouriteItem.getIdentifier())) {
                return favouriteItem;
            }
        }
        return null;
    }

    @Override
    public boolean isEmpty() {
        return getRawItems().isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    @NonNLS
    public void moveItem(FavouriteItem from, FavouriteItem to) {
        final A from1 = (A) from;
        final A to1 = (A) to;

        from1.setOrder(to1.getOrder());
        for (A item : getItems()) {
            if (item.item != from1.item && item.getOrder() >= to1.getOrder()) {
                item.setOrder(item.getOrder() + 1);
            }
        }
        fireItemMovedEvent(from1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeItem(FavouriteItem item) {
        if (getRawItems().remove(((AbstractFavouriteItem) item).item)) {
            fireItemRemovedEvent(item);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void renameItem(FavouriteItem item, String alias) {
        ((AbstractFavouriteItem) item).setAlias(alias);
        fireItemRenamedEvent(item);
    }

    @Override
    public String getName() {
        return getConfigKey();
    }

    protected void fireItemAddedEvent(FavouriteItem item) {
        firePropertyChange(FAVOURITE_ITEM_ADDED, null, item);
    }

    protected void fireItemMovedEvent(FavouriteItem item) {
        firePropertyChange(FAVOURITE_ITEM_MOVED, null, item);
    }

    protected void fireItemRemovedEvent(FavouriteItem item) {
        firePropertyChange(FAVOURITE_ITEM_REMOVED, null, item);
    }

    protected void fireItemRenamedEvent(FavouriteItem item) {
        firePropertyChange(FAVOURITE_ITEM_RENAMED, null, item);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (EventBusRegistry.isSet()) { // will not be available on server side
            EventBusRegistry.get().fireEvent(new ConfigChangedEvent(propertyName, oldValue, newValue));
        }
    }

    public abstract class AbstractFavouriteItem implements FavouriteItem {
        protected abstract void setAlias(String alias);

        protected abstract void setOrder(int order);

        protected final I item;

        public AbstractFavouriteItem(I item) {
            this.item = item;
        }

        @Override
        public FavouriteItemsStore getSource() {
            return AbstractFavouriteItemsStore.this;
        }

        @Override
        public boolean canMove() {
            return true;
        }

        @Override
        public boolean canRename() {
            return true;
        }

        @Override
        public boolean canRemove() {
            return true;
        }
    }
}
