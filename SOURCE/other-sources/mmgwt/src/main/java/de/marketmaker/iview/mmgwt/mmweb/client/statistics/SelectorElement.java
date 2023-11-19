/*
 * SelectorElement.java
 *
 * Created on 15.01.2010 16:08:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.io.Serializable;
import java.util.List;

/** 
 * @author oflege
*/
class SelectorElement implements Serializable {
    private String name;
    private List<String> values;

    public SelectorElement() { // required for GWT serialization
    }

    public SelectorElement(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }
}
