package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextList;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer.FolderItem;

import java.util.List;

/**
 * Created on 18.01.13 08:04
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class FolderItemList extends ContextList<FolderItem> {

    public FolderItemList(List<FolderItem> list) {
        super(list);
    }

    @Override
    protected String getIdOf(FolderItem item) {
        return item.getId();
    }
}
