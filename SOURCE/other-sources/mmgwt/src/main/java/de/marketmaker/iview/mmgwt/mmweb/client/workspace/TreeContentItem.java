/*
 * TreeContentItem.java
 *
 * Created on 30.04.2008 17:39:05
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.data.TreeModel;

/**
 * An object that is associated with a leaf node in a tree
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface TreeContentItem extends TreeModel {

    /**
     * Name of the parent folder for this item.
     * @return parent folder name
     */
    String getFolderName();

    /**
     * The history token to use when someone double clicks on the item
     * @return history token
     */
    String getHistoryToken();

    /**
     * Renames the item to s;
     * @param s new name
     */
    void rename(String s);

    String getDisplayName();

    String getNameInWorkspace();

    void setNameInWorkspace(String name);

    int getOrder();

    void setOrder(int order);
}
