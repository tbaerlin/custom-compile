/*
 * FavouriteItem.java
 *
 * Created on 12.11.2015 18:20
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

/**
 * @author mdick
 */
public interface FavouriteItem {
    String getIdentifier();

    FavouriteItemsStore getSource();

    String getLabel();

    String getType();

    String getTypeLabel();

    int getOrder();

    String getHistoryToken();

    String getIconName();

    boolean canMove();

    boolean canRename();

    boolean canRemove();
}
