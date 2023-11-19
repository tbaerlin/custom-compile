/*
 * OwnerList.java
 *
 * Created on 10.06.14 10:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextList;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.AbstractOwner;

import java.util.List;

/**
 * @author Markus Dick
 */
public class OwnerList<T extends AbstractOwner> extends ContextList<T> {

    public OwnerList(List<T> list) {
        super(list);
    }

    @Override
    protected String getIdOf(T item) {
        return item.getId();
    }
}
