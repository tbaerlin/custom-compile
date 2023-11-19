package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PrivFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asEnum;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asString;

/**
 * Created on 03.03.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class DmsMetadata {

    public static class Talker extends AbstractMmTalker<DatabaseIdQuery, DmsMetadata, DmsMetadata> {
        public Talker(String formula) {
            super(formula);
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        public MmTalkWrapper<DmsMetadata> createWrapper(Formula formula) {
            return DmsMetadata.createWrapper(formula);
        }


        @Override
        public DmsMetadata createResultObject(MMTalkResponse response) {
            final List<DmsMetadata> resultObject = this.wrapper.createResultObjectList(response);
            if (!resultObject.isEmpty()) {
                return resultObject.get(0);
            }
            return null;
        }
    }

    public static MmTalkWrapper<DmsMetadata> createWrapper(Formula formula) {
        final MmTalkWrapper<DmsMetadata> cols = MmTalkWrapper.create(formula, DmsMetadata.class);
        cols.appendColumnMapper(new MmTalkColumnMapper<DmsMetadata>("Typ") { // $NON-NLS$
            @Override
            public void setValue(DmsMetadata metadata, MM item) {
                metadata.type = asEnum(MMTypRefType.TRT_SHELL_MM_TYP, ShellMMType.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<DmsMetadata>("Identifier") { // $NON-NLS$
            @Override
            public void setValue(DmsMetadata metadata, MM item) {
                metadata.identifier = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<DmsMetadata>("Zone") { // $NON-NLS$
            @Override
            public void setValue(DmsMetadata metadata, MM item) {
                metadata.zone = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<DmsMetadata>("Name") { // $NON-NLS$
            @Override
            public void setValue(DmsMetadata metadata, MM item) {
                metadata.name = asString(item);
            }
        });
        return cols;
    }

    private ShellMMType type;
    private String zone;
    private String identifier;
    private String name;
    private PrivFeature priv;


    public ShellMMType getType() {
        return this.type;
    }

    public String getZone() {
        return this.zone;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getName() {
        return this.name;
    }

    public PrivFeature getPriv() {
        return this.priv;
    }

    public void setPriv(PrivFeature priv) {
        this.priv = priv;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DmsMetadata that = (DmsMetadata) o;

        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != that.type) return false;
        if (zone != null ? !zone.equals(that.zone) : that.zone != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
