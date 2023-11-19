/*
 * Selectors.java
 *
 * Created on 13.01.2010 16:29:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.Serializable;

/**
 * Set of known pages that a user may have visited.
 * @author oflege
 */
public class Pages implements Serializable {

    private HashMap<String, Page> pagesById = new HashMap<String, Page>();

    public void add(String id, String name, String module) {
        this.pagesById.put(id, new Page(name, module));
    }

    public Page getPage(String id) {
        return this.pagesById.get(id);
    }

    public ArrayList<String> getModuleNames() {
        final HashSet<String> names = new HashSet<String>();
        for (Page page : this.pagesById.values()) {
            if (page.getModule() != null) {
                names.add(page.getModule());
            }
        }
        // todo: sort names!?
        return new ArrayList<String>(names);
    }
}
