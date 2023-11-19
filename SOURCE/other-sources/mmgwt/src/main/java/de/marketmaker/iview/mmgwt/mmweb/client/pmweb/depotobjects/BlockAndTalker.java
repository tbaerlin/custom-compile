/*
 * BlockAndTalker.java
 *
 * Created on 12.02.13 14:26
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.pmxml.DatabaseId;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.MMTalkRequest;
import de.marketmaker.iview.pmxml.MMTalkResponse;

import java.util.List;

/**
 * @author Michael Lösch
 */
public class BlockAndTalker<T extends AbstractMmTalker<DatabaseIdQuery, O, W>, O, W> {
    private T mmtalker;
    private DmxmlContext.Block<MMTalkResponse> block;
    private final MMTalkRequest request;

    public BlockAndTalker(DmxmlContext context, T mmtalker) {
        this.mmtalker = mmtalker;
        this.block = context.addBlock("PM_MMTalk"); // $NON-NLS$
        this.request = this.mmtalker.createRequest();
        this.block.setParameter(this.request);
    }

    public DmxmlContext.Block<MMTalkResponse> getBlock() {
        return block;
    }

    public void setPrivacyModeActive(boolean privacyModeActive) {
        this.request.setCustomerDesktopActive(privacyModeActive);
    }

    public void setDatabaseId(String databaseId) {
        final DatabaseIdQuery query = this.mmtalker.getQuery();
        query.getIds().clear();
        final DatabaseId dbid = new DatabaseId();
        dbid.setId(databaseId);
        query.getIds().add(dbid);
        this.block.setToBeRequested();
    }

    public String getDatabaseId() {
        final List<DatabaseId> ids = this.mmtalker.getQuery().getIds();
        if (ids.isEmpty()) {
            return null;
        }
        return ids.get(0).getId();
    }

    public O createResultObject() {
        return this.mmtalker.createResultObject(this.block.getResult());
    }

    public T getMmtalker() {
        return this.mmtalker;
    }
}
