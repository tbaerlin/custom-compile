package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextList;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Portfolio;

import java.util.List;

/**
 * Created on 18.01.13 08:04
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class PortfolioList extends ContextList<Portfolio> {

    public PortfolioList(List<Portfolio> list) {
        super(list);
    }

    @Override
    protected String getIdOf(Portfolio item) {
        return item.getId();
    }
}
