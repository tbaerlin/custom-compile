/*
 * ItemWithSymbols.java
 *
 * Created on 17.09.2004 16:17:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

import java.util.Comparator;

/**
 * An item which is uniquely identifiable by its id, whereas can have different symbols in different
 * systems.
 * <p>
 * The available systems are defined in KeysystemEnum.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @see de.marketmaker.istar.domain.KeysystemEnum
 */
public interface ItemWithSymbols {

    Comparator<? super ItemWithSymbols> BY_ID = new Comparator<ItemWithSymbols>() {
        @Override
        public int compare(ItemWithSymbols o1, ItemWithSymbols o2) {
            return Long.compare(o1.getId(), o2.getId());
        }
    };

    /**
     * Returns the symbol of this item in the given system.
     *
     * @param id a key system id as defined in KeysystemEnum.
     * @return a symbol string, null if there is no symbol for this item in the given system.
     */
    String getSymbol(KeysystemEnum id);

    /**
     * Returns the symbol of this item in the given system.
     * <p>
     * The system is identified by resolving the given string id to a KeysystemEnum.
     *
     * @param id a key system id as defined in KeysystemEnum.
     * @return a symbol string, null if there is no symbol for this item in the given system.
     */
    String getSymbol(String id);

    /**
     * @return the id uniquely identifying this item.
     */
    long getId();
}
