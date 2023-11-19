/*
 * Selectors.java
 *
 * Created on 13.01.2010 16:29:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * @author oflege
 */
public class Selectors  implements Serializable {

    private List<SelectorElement> elements = new ArrayList<SelectorElement>();

    public void add(String name, List<String> values) {
        this.elements.add(new SelectorElement(name, values));
    }

    public List<SelectorElement> getElements() {
        return this.elements;
    }
}
