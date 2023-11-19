package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.explorer;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.List;

/**
 * Created on 18.04.13 09:17
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class FolderItem implements ContextItem {

    public static class Talker extends AbstractMmTalker<DatabaseIdQuery, List<FolderItem>, FolderItem> {
        public Talker(String formula) {
            super(formula);
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        public MmTalkWrapper<FolderItem> createWrapper(Formula formula) {
            return FolderItem.createWrapper(formula);
        }


        @Override
        public List<FolderItem> createResultObject(MMTalkResponse response) {
            return this.wrapper.createResultObjectList(response);
        }
    }

    public static MmTalkWrapper<FolderItem> createWrapper(Formula formula) {
        final MmTalkWrapper<FolderItem> cols = MmTalkWrapper.create(formula, FolderItem.class);
        cols.appendColumnMapper(new MmTalkColumnMapper<FolderItem>("Id") { // $NON-NLS$
            @Override
            public void setValue(FolderItem folderItem, MM item) {
                folderItem.id = MmTalkHelper.asMMNumber(item).getValue();
            }
        }).appendColumnMapper(new MmTalkColumnMapper<FolderItem>("Typ") { // $NON-NLS$
            @Override
            public void setValue(FolderItem folderItem, MM item) {
                folderItem.type = MmTalkHelper.asEnum(MMTypRefType.TRT_SHELL_MM_TYP, ShellMMType.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<FolderItem>("Name") { // $NON-NLS$
            @Override
            public void setValue(FolderItem folderItem, MM item) {
                folderItem.name = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<FolderItem>("Zone") { // $NON-NLS$
            @Override
            public void setValue(FolderItem folderItem, MM item) {
                folderItem.zone = MmTalkHelper.asString(item);
            }
        });
        return cols;
    }

    private String id;
    private ShellMMType type;
    private String name;
    private String zone;
    private HistoryContext historyContext;

    public String getId() {
        return this.id;
    }

    public ShellMMType getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public String getZone() {
        return this.zone;
    }

    public FolderItem withHistoryContext(HistoryContext context) {
        this.historyContext = context;
        return this;
    }

    public HistoryContext getHistoryContext() {
        return this.historyContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FolderItem)) return false;

        final FolderItem that = (FolderItem) o;

        if (!id.equals(that.id)) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (zone != null ? !zone.equals(that.zone) : that.zone != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        return result;
    }
}