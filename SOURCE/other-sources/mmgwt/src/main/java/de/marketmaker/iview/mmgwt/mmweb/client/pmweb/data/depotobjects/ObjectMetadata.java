/*
 * ObjectMetadata.java
 *
 * Created on 13.05.2015 10:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.List;

/**
 * @author mdick
 */
@NonNLS
public class ObjectMetadata {
    public static class DbIdTalker extends AbstractMmTalker<DatabaseIdQuery, ObjectMetadata, ObjectMetadata> {

        public DbIdTalker() {
            super(Formula.create());
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        public MmTalkWrapper<ObjectMetadata> createWrapper(Formula formula) {
            return ObjectMetadata.createWrapper(formula);
        }

        @Override
        public ObjectMetadata createResultObject(MMTalkResponse response) {
            return ObjectMetadata.createResultObject(response, this.wrapper);
        }
    }

    static ObjectMetadata createResultObject(MMTalkResponse response, MmTalkWrapper<ObjectMetadata> wrapper) {
        final List<ObjectMetadata> resultObject = wrapper.createResultObjectList(response);
        if (!resultObject.isEmpty()) {
            return resultObject.get(0);
        }
        return null;
    }

    public static MmTalkWrapper<ObjectMetadata> createWrapper(Formula formula) {
        final MmTalkWrapper<ObjectMetadata> cols = MmTalkWrapper.create(formula, ObjectMetadata.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<ObjectMetadata>("ID") {
            @Override
            public void setValue(ObjectMetadata om, MM item) {
                om.id = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<ObjectMetadata>("Name") {
            @Override
            public void setValue(ObjectMetadata om, MM item) {
                om.name = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<ObjectMetadata>("Typ") {
            @Override
            public void setValue(ObjectMetadata om, MM item) {
                om.type = MmTalkHelper.asEnum(MMTypRefType.TRT_SHELL_MM_TYP, ShellMMType.values(), item);
            }
        });
        return cols;
    }

    private String id;
    private String name;
    private ShellMMType type;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ShellMMType getType() {
        return type;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ObjectMetadata)) return false;

        final ObjectMetadata that = (ObjectMetadata) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return type == that.type;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ObjectMetadata{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
