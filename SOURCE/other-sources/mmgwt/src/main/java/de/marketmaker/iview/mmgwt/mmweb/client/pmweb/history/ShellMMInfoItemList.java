package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextList;

import java.util.List;

/**
 * Created on 22.02.13 15:42
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class ShellMMInfoItemList extends ContextList<ShellMMInfoItem> {

    public ShellMMInfoItemList(List<ShellMMInfoItem> list) {
        super(list);
    }

    @Override
    protected String getIdOf(ShellMMInfoItem item) {
        return item.getId();
    }
}
