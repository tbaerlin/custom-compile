package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.pmxml.ShellMMInfo;

/**
 * Created on 22.02.13 09:11
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 *         Generic type to encapsulate id, name and historyContext.
 *         This is the most common use case if "something" has to be aware in a historyContext.
 */

public class IdAndName {
    private final String id;
    private final String name;
    private HistoryContext historyContext;

    public IdAndName(String name, String id) {
        this.name = name;
        this.id = id;
    }

    public IdAndName(ShellMMInfo info) {
        this.name = info.getBezeichnung();
        this.id = info.getId();
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public IdAndName withHistoryContext(HistoryContext historyContext) {
        this.historyContext = historyContext;
        return this;
    }

    public HistoryContext getHistoryContext() {
        return this.historyContext;
    }
}