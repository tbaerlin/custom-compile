/*
 * ItemWithNamesDp2.java
 *
 * Created on 02.12.2010 14:10:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl;

import de.marketmaker.istar.domain.ItemWithNames;
import de.marketmaker.istar.domain.Language;

/**
 * @author oflege
 */
public abstract class ItemWithNamesDp2 extends ItemWithSymbolsDp2 implements ItemWithNames {
    static final long serialVersionUID = 1L;

    private final String[] names = new String[Language.getNumLanguages()];

    public ItemWithNamesDp2() {
    }

    public ItemWithNamesDp2(long id) {
        super(id);
    }

    public String getName(Language language) {
        return language.resolve(this.names);
    }

    public String getNameOrDefault(Language language) {
        String name = getName(language);
        if (name == null) {
            name = getName(Language.en);
            if (name == null) {
                name = getName(Language.de);
            }
        }
        return name;
    }

    public void setNames(Language language, String name) {
        this.names[language.ordinal()] = name;
    }
}
