/*
 * InstrumentMetadata.java
 *
 * Created on 08.05.13 07:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.Key;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.QueryStandardSelection;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.List;

/**
 * @author Michael Lösch
 */
public class InstrumentMetadata {
    public static class Talker extends AbstractMmTalker<QueryStandardSelection, InstrumentMetadata, InstrumentMetadata> {
        public Talker() {
            super(Formula.create());
        }

        @Override
        protected QueryStandardSelection createQuery() {
            final QueryStandardSelection query = new QueryStandardSelection();
            query.setDataItemType(ShellMMType.ST_WP);
            return query;
        }

        @Override
        protected MmTalkWrapper<InstrumentMetadata> createWrapper(Formula formula) {
            return InstrumentMetadata.createWrapper(formula);
        }

        @Override
        public InstrumentMetadata createResultObject(MMTalkResponse response) {
            final List<InstrumentMetadata> result = this.wrapper.createResultObjectList(response);
            if (!result.isEmpty()) {
                return result.get(0);
            }
            return null;
        }

        public void setSecurityId(String securityId) {
            getQuery().getKeys().clear();
            final Key key = new Key();
            key.setKey(securityId);
            getQuery().getKeys().add(key);
        }
    }

    public static MmTalkWrapper<InstrumentMetadata> createWrapper(Formula formula) {
        final MmTalkWrapper<InstrumentMetadata> cols = MmTalkWrapper.create(formula, InstrumentMetadata.class);
        cols.appendColumnMapper(new MmTalkColumnMapper<InstrumentMetadata>("Id") { // $NON-NLS$
            @Override
            public void setValue(InstrumentMetadata i, MM item) {
                i.id = MmTalkHelper.asMMNumber(item).getValue();
            }
        }).appendColumnMapper(new MmTalkColumnMapper<InstrumentMetadata>("SecurityId") { // $NON-NLS$
            @Override
            public void setValue(InstrumentMetadata i, MM item) {
                i.securityId = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<InstrumentMetadata>("Typ") { // $NON-NLS$
            @Override
            public void setValue(InstrumentMetadata i, MM item) {
                i.type = MmTalkHelper.asEnum(MMTypRefType.TRT_SHELL_MM_TYP, ShellMMType.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<InstrumentMetadata>("Name") { // $NON-NLS$
            @Override
            public void setValue(InstrumentMetadata i, MM item) {
                i.name = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<InstrumentMetadata>("ISIN") { // $NON-NLS$
            @Override
            public void setValue(InstrumentMetadata i, MM item) {
                i.isin = MmTalkHelper.asString(item);
            }
        });
        return cols;
    }

    private String id;
    private String securityId;
    private ShellMMType type;
    private String name;
    private String isin;

    public String getId() {
        return this.id;
    }

    public String getSecurityId() {
        return this.securityId;
    }

    public ShellMMType getType() {
        return this.type;
    }

    public String getName() {
        return this.name;
    }

    public String getIsin() {
        return isin;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstrumentMetadata)) return false;

        final InstrumentMetadata that = (InstrumentMetadata) o;

        if (!id.equals(that.id)) return false;
        if (isin != null ? !isin.equals(that.isin) : that.isin != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (securityId != null ? !securityId.equals(that.securityId) : that.securityId != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (securityId != null ? securityId.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (isin != null ? isin.hashCode() : 0);
        return result;
    }
}