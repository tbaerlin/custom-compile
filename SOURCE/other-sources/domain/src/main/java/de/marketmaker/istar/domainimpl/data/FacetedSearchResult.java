/*
 * AbstractFinderResult.java
 *
 * Created on 22.04.14 11:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * Basically a non-jaxb implementation of <code>de.marketmaker.istar.fusion.dmxml.FinderTypedMetaList</code>
 * @author oflege
 */
public class FacetedSearchResult implements Serializable {
    protected static final long serialVersionUID = 1L;

    public static class Builder {
        private static class MutableValue {

            final String id;

            final String name;

            final MutableInt count = new MutableInt();

            private MutableValue(String id, String name) {
                this.id = id;
                this.name = name;
            }

            private Value asValue() {
                return new Value(id, name, count.intValue());
            }
        }

        private static class MutableFacet {

            private Map<String, MutableValue> values = new HashMap<>();

            final String id;

            final String name;

            final boolean _enum;

            private MutableFacet(String id, String name, boolean _enum) {
                this.id = id;
                this.name = name;
                this._enum = _enum;
            }

            private Facet asFacet() {
                List<Value> vs = values();
                if (vs == null) {
                    return null;
                }
                return new Facet(this.id, this.name, this._enum, vs);
            }

            private List<Value> values() {
                ArrayList<Value> result = new ArrayList<>(this.values.size());
                for (MutableValue mv : this.values.values()) {
                    result.add(mv.asValue());
                }
                if (result.isEmpty()) {
                    return null;
                }
                return result;
            }
        }

        private final Map<String, MutableFacet> facets = new HashMap<>();

        private Builder() {
        }

        public FacetedSearchResult build() {
            return new FacetedSearchResult(this);
        }

        public Builder withEnum(String type) {
            return withFacet(type, type, true);
        }

        public Builder withFacet(String type) {
            return withFacet(type, type, false);
        }

        public Builder withFacet(String type, String name, boolean _enum) {
            this.facets.put(type, new MutableFacet(type, name, _enum));
            return this;
        }

        public Builder addValue(String facet, String key) {
            return addValue(facet, key, key);
        }

        public Builder addValue(String facet, String key, String name) {
            if (facet == null || key == null || name == null) {
                return this;
            }

            MutableFacet mf = this.facets.get(facet);
            if (mf == null) {
                throw new IllegalArgumentException("undeclared facet '" + facet + '\'');
            }
            MutableValue mv = mf.values.get(key);
            if (mv == null) {
                mf.values.put(key, mv = new MutableValue(key, name));
            }
            mv.count.increment();
            return this;
        }

        public List<Facet> getFacets() {
            ArrayList<Facet> result = new ArrayList<>(this.facets.size());
            for (MutableFacet mf : this.facets.values()) {
                Facet f = mf.asFacet();
                if (f != null) {
                    result.add(f);
                }
            }
            return result;
        }
    }


    public static class Value implements Serializable {
        protected static final long serialVersionUID = 1L;

        private final String id;

        private final String name;

        private int count;

        private Value(String id, String name, int count) {
            this.id = id;
            this.name = name;
            this.count = count;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getCount() {
            return count;
        }
    }

    public static class Facet implements Serializable {
        protected static final long serialVersionUID = 1L;

        private List<Value> values;

        private final String id;

        private final String name;

        private final boolean _enum;

        private Facet(String id, String name, boolean _enum, List<Value> values) {
            this.id = id;
            this.name = name;
            this._enum = _enum;
            this.values = values;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public boolean isEnum() {
            return this._enum;
        }

        public List<Value> getValues() {
            return (this.values != null) ? this.values : Collections.<Value>emptyList();
        }
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    private final List<Facet> facets;

    private FacetedSearchResult(Builder builder) {
        this.facets = builder.getFacets();
    }

    public FacetedSearchResult withFacetValuesSortedByName(Locale locale) {
        final Collator c = Collator.getInstance(locale);
        c.setStrength(Collator.PRIMARY);
        final Comparator<Value> cmp = new Comparator<Value>() {
            @Override
            public int compare(Value o1, Value o2) {
                return c.compare(o1.name, o2.name);
            }
        };

        for (Facet facet : this.facets) {
            if (facet.values != null && facet.values.size() > 1) {
                Collections.sort(facet.values, cmp);
            }
        }
        return this;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public Facet getFacet(String id) {
        for (Facet facet : facets) {
            if (id.equals(facet.getId())) {
                return facet;
            }
        }
        return null;
    }
}
