/*
 * Holder.java
 *
 * Created on 05.03.2010 14:44:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.prices;

import java.util.HashSet;

/**
 * @author oflege
 */
abstract class Holder {
    private final HashSet<String> blockIds = new HashSet<String>();

    void removeBlock(String cid) {
        this.blockIds.remove(cid);
    }

    void addBlock(String cid) {
        this.blockIds.add(cid);
    }

    boolean isStale() {
        return this.blockIds.isEmpty();
    }

    protected abstract void removePrevious();
}
