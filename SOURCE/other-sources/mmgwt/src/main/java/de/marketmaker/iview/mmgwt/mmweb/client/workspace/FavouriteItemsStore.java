/*
 * FavouriteItemsStore.java
 *
 * Created on 12.11.2015 18:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import java.util.List;

/**
 * @author mdick
 */
public interface FavouriteItemsStore<T> {
    String getName();

    String getLabel();

    String getConfigKey();

    boolean canAddItem(T t);

    void addItem(T t);

    List<? extends FavouriteItem> getItems();

    FavouriteItem getItem(String identifier);

    void moveItem(FavouriteItem from, FavouriteItem to);

    void removeItem(FavouriteItem item);

    void renameItem(FavouriteItem item, String alias);

    boolean isEmpty();
}
