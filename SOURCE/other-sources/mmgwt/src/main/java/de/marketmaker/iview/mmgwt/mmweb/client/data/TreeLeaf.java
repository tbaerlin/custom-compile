/*
 * TreeLeaf.java
 *
 * Created on 20.10.2009 15:06:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

/**
 * @author oflege
 */
public class TreeLeaf extends BaseTreeModel {

    private static int ID = 0;

    public TreeLeaf(String name) {
        set("id", ID++); // $NON-NLS-0$
        set("name", name); // $NON-NLS-0$
    }

    public String getName() {
        return (String) get("name"); // $NON-NLS-0$
    }

    public void setName(String name) {
        set("name", name); // $NON-NLS-0$
    }

    public Integer getId() {
        return (Integer) get("id"); // $NON-NLS-0$
    }
        
    public String toString() {
        return getParentName() + getName();
    }

    private String getParentName() {
        return getParent() != null ? (getParent().toString() + "/") : "/"; // $NON-NLS-0$ $NON-NLS-1$
    }
}
