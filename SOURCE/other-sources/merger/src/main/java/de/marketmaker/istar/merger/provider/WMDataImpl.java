/*
 * WMDataImpl.java
 *
 * Created on 02.11.11 16:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.domain.data.WMData;

/**
 * @author oflege
 */
class WMDataImpl implements WMData, Serializable {
    static class FieldImpl implements Serializable, Field {
        protected static final long serialVersionUID = 1L;

        private final String name;

        private String key;

        private String textinfo;

        private Object value;

        private WMFieldType type;

        public FieldImpl(String name) {
            this.name = name;
        }

        void setKey(String key) {
            this.key = key;
        }

        void setValue(Object value) {
            this.value = value;
        }

        public void setTextinfo(String info) {
            this.textinfo = info;
        }

        void setType(WMFieldType type) {
            this.type = type;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getTextinfo() {
            return this.textinfo;
        }

        @Override
        public WMFieldType getType() {
            return this.type;
        }

        @Override
        public Object getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.name);
            if (this.key != null) {
                sb.append("|key=").append(this.key);
            }
            if (this.textinfo != null) {
                sb.append("|info=").append(this.textinfo);
            }
            sb.append("|value=").append(this.value);
            return sb.toString();
        }
    }


    protected static final long serialVersionUID = 1L;

    private final long instrumentid;

    private Map<String, Field> fields = new HashMap<>();

    static WMData create(long id, Map<String, ? extends Field> fields) {
        return new WMDataImpl(id, fields);
    }

    public Field getField(String name) {
        return this.fields.get(name);
    }

    private WMDataImpl(long instrumentid, Map<String, ? extends Field> fields) {
        this.instrumentid = instrumentid;
        this.fields.putAll(fields);
    }

    @Override
    public long getInstrumentid() {
        return this.instrumentid;
    }

    @Override
    public Collection<Field> getFields() {
        return Collections.unmodifiableCollection(this.fields.values());
    }

    @Override
    public String toString() {
        return this.instrumentid + ".iid::" + this.fields;
    }
}
