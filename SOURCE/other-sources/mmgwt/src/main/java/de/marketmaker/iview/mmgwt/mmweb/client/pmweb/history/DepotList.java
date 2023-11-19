package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextList;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Depot;

import java.util.List;

/**
 * Created on 18.01.13 08:04
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class DepotList extends ContextList<Depot> {

    public DepotList(List<Depot> list) {
        super(list);
    }

    @Override
    protected String getIdOf(Depot item) {
        return item.getId();
    }
}
