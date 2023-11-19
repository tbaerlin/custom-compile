/*
 * Page.java
 *
 * Created on 15.01.2010 16:07:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.io.Serializable;

/**
 * A page the user visited or an action performed by the user.
 * @author oflege
*/
class Page implements Serializable {
    private String name;
    private String module;

    public Page() { // required for GWT serialization
    }

    public Page(String name, String module) {
        this.name = name;
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public String getModule() {
        return module;
    }
}
