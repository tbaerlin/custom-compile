/*
 * FavouriteItemsStores.java
 *
 * Created on 13.11.2015 11:26
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.function.Consumer;

import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;

/**
 * @author mdick
 */
public final class FavouriteItemsStores {
    public static final String FAVOURITE_ITEM_CONFIG_CHANGED_EVENT_PREFIX = "favouriteItem";  // $NON-NLS$

    private static final FavouriteItemsStore[] FAVOURITE_ITEMS_STORES = new FavouriteItemsStore[]
            {new FavouriteInstrumentItemsStore(), new FavouritePageItemsStore()};

    private FavouriteItemsStores() {
        // do not instantiate
    }

    public static FavouriteItemsStore[] get() {
        return FAVOURITE_ITEMS_STORES;
    }

    @SuppressWarnings("unchecked")
    public static <T extends FavouriteItemsStore> T get(Class<T> c) {
        // TODO: replace return type with Optional if available in GWT
        for (FavouriteItemsStore favouriteItemsStore : FAVOURITE_ITEMS_STORES) {
            if (favouriteItemsStore.getClass() == c) {
                return (T) favouriteItemsStore;
            }
        }
        return null;
    }

    public static <T extends FavouriteItemsStore> void ifPresent(Class<T> c, Consumer<T> consumer) {
        // TODO: use get with Optional's ifPresent if Optional is available in GWT
        final T t = get(c);
        if (t != null) {
            consumer.accept(t);
        }
    }

    public static boolean isAllStoresEmpty() {
        for (FavouriteItemsStore favouriteItemsStore : FAVOURITE_ITEMS_STORES) {
            if (!favouriteItemsStore.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static boolean isFavouriteItemsConfigChangedEvent(ConfigChangedEvent event) {
        return event.getProperty() != null && event.getProperty()
                .startsWith(FAVOURITE_ITEM_CONFIG_CHANGED_EVENT_PREFIX);
    }
}
