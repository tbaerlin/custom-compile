package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextList;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;

import java.util.List;

/**
 * Created on 18.01.13 08:04
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class AccountList extends ContextList<Account> {

    public AccountList(List<Account> list) {
        super(list);
    }

    @Override
    protected String getIdOf(Account item) {
        return item.getId();
    }
}
