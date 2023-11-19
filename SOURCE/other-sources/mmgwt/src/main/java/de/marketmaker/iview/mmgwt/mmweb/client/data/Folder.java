/*
 * Folder.java
 *
 * Created on 20.10.2009 14:56:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

/**
 * @author oflege
 */
@SuppressWarnings("GwtInconsistentSerializableClass")
public class Folder extends BaseTreeModel {

    private static int ID = 0;

    public Folder(String name) {
        set("id", ID++); // $NON-NLS-0$
        set("name", name); // $NON-NLS-0$
    }

    public Folder(String name, BaseTreeModel[] children) {
        this(name);
        for (BaseTreeModel child : children) {
            add(child);
        }
    }

    public Integer getId() {
        return (Integer) get("id"); // $NON-NLS-0$
    }

    public String getName() {
        return (String) get("name"); // $NON-NLS-0$
    }

    public String toString() {
        return getName();
    }
}
