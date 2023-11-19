/*
 * PageDefinition.java
 *
 * Created on 14.12.2009 13:43:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Collection of PageDefinition objects, used to figure out which PageDefinition best matches
 * a given page (given by its history token)
 * @author oflege
 */
class PageDefinitions {

    private final List<PageDefinition> definitions = new ArrayList<>();

    void add(PageDefinition pd) {
        this.definitions.add(pd);
    }

    @Override
    public String toString() {
        return "PageDefinitions" + this.definitions;
    }

    List<PageDefinition> getDefinitions() {
        return this.definitions;
    }

    public PageDefinition getDefinitionFor(String page) {
        for (PageDefinition pd : this.definitions) {
            if (pd.matches(page)) {
                return pd;
            }
        }
        return null;
    }

    public PageDefinition getDefinitionFor(int pageDefId) {
        for (PageDefinition definition : this.definitions) {
            if (definition.getId() == pageDefId) {
                return definition;
            }
        }
        return null;
    }
}
