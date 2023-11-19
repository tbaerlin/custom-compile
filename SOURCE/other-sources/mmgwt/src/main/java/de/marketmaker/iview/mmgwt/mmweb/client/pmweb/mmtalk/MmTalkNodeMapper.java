package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk;

import de.marketmaker.iview.pmxml.MMTable;

/**
 * Created on 28.02.13 14:27
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public abstract class MmTalkNodeMapper<O, T> {
    private final MmTalkWrapper<T> wrapper;

    public MmTalkNodeMapper(MmTalkWrapper<T> wrapper) {
        this.wrapper = wrapper;
    }

    public MmTalkWrapper<T> getWrapper() {
        return this.wrapper;
    }

    public abstract void setValue(O object, MmTalkWrapper<T> wrapper, MMTable table);
}
