/*
 * CommissionScale
 *
 * Created on 19.03.13 09:44
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
import de.marketmaker.iview.pmxml.QueryStandardSelection;

import java.util.List;

/**
 * @author Markus Dick
 */
public class ObjectName {
    public static class DbIdTalker extends AbstractMmTalker<DatabaseIdQuery, ObjectName, ObjectName> {

        public DbIdTalker() {
            super(Formula.create());
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        public MmTalkWrapper<ObjectName> createWrapper(Formula formula) {
            return ObjectName.createWrapper(formula);
        }

        @Override
        public ObjectName createResultObject(MMTalkResponse response) {
            return ObjectName.createResultObject(response, this.wrapper);
        }
    }

    public static class SecIdTalker extends AbstractMmTalker<QueryStandardSelection, ObjectName, ObjectName> {

        public SecIdTalker() {
            super(Formula.create());
        }

        @Override
        protected QueryStandardSelection createQuery() {
            return new QueryStandardSelection();
        }

        public MmTalkWrapper<ObjectName> createWrapper(Formula formula) {
            return ObjectName.createWrapper(formula);
        }

        @Override
        public ObjectName createResultObject(MMTalkResponse response) {
            return ObjectName.createResultObject(response, this.wrapper);
        }
    }

    static ObjectName createResultObject(MMTalkResponse response, MmTalkWrapper<ObjectName> wrapper) {
        final List<ObjectName> resultObject = wrapper.createResultObjectList(response);
        if (!resultObject.isEmpty()) {
            return resultObject.get(0);
        }
        return null;
    }

    public static MmTalkWrapper<ObjectName> createWrapper(Formula formula) {
        final MmTalkWrapper<ObjectName> cols = MmTalkWrapper.create(formula, ObjectName.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<ObjectName>("Name") { // $NON-NLS$
            @Override
            public void setValue(ObjectName cs, MM item) {
                cs.name = MmTalkHelper.asString(item);
            }
        });
        return cols;
    }

    private String name;

    public String getName() {
        return name;
    }


    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectName that = (ObjectName) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
