/*
 * NamedIdSetImpl.java
 *
 * Created on 22.06.2007 13:09:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.LocalizedString;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class IndexComposition implements Serializable {

    public static final class Builder {
        public final String id;

        public final String name;

        public final String constituentsGroup;

        private final ArrayList<Item> items = new ArrayList<>();

        public String marketStrategy;

        private Builder(String id, String name, String constituentsGroup) {
            this.id = id;
            this.name = name;
            this.constituentsGroup = constituentsGroup;
        }

        public Builder addItem(long qid, long iid, LocalizedString localizedName) {
            this.items.add(new Item(qid, iid, localizedName));
            return this;
        }

        public IndexComposition build() {
            return new IndexComposition(this);
        }
    }

    public static class Item implements Serializable {
        protected static final long serialVersionUID = 1L;

        public final long qid;

        public final long iid;

        public final String name;

        public final LocalizedString localizedName;

        private Item(long qid, long iid, LocalizedString localizedName) {
            this.qid = qid;
            this.iid = iid;
            this.localizedName = localizedName;
            this.name = localizedName != null ? localizedName.getDefault() : null;
        }

        @Override
        public String toString() {
            return this.iid + ".iid/" + this.qid + ".qid"
                    + ((this.name != null) ? ("/" + this.name) : "");
        }
    }

    public static Builder createBuilder(String id, String name, String constituentsGroup) {
        return new Builder(id, name, constituentsGroup);
    }

    public static IndexComposition createEmpty(String id) {
        return new IndexComposition(id);
    }

    protected static final long serialVersionUID = 1L;

    private final String id;

    private final String marketStrategy;

    private final String name;

    private final List<Item> items;

    private final String constituentsGroup;

    public IndexComposition(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.marketStrategy = b.marketStrategy;
        this.constituentsGroup = b.constituentsGroup;
        this.items = new ArrayList<>(b.items);
    }

    private IndexComposition(IndexComposition ic, String id, String name, String marketStrategy) {
        this.id = id;
        this.name = name;
        this.marketStrategy = marketStrategy;
        this.constituentsGroup = ic.constituentsGroup;
        this.items = ic.items;
    }

    private IndexComposition(String id) {
        this.id = id;
        this.name = id;
        this.marketStrategy = null;
        this.constituentsGroup = null;
        this.items = Collections.emptyList();
    }

    public IndexComposition with(String id, String name, String marketStrategy) {
        return new IndexComposition(this, id, name, marketStrategy);
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return (this.name != null) ? this.name : this.id;
    }

    public List<Long> getQids() {
        return this.items.stream().map(i -> i.qid).collect(Collectors.toList());
    }

    public List<Long> getIids() {
        return this.items.stream().map(i -> i.iid).collect(Collectors.toList());
    }

    /**
     * @deprecated use {@link #getLocalizedName(long, de.marketmaker.istar.domain.Language)} instead
     */
    @Deprecated
    public String getName(long qid) {
        Optional<Item> o = findByQid(qid);
        return o.isPresent() ? o.get().name : null;
    }

    public String getLocalizedName(long qid, Language lang) {
        Optional<Item> o = findByQid(qid);
        if (!o.isPresent()) {
            return null;
        }
        Item item = o.get();
        return item.localizedName != null
                ? item.localizedName.getValueOrDefault(lang)
                : item.name;
    }

    private Optional<Item> findByQid(long qid) {
        return this.items.stream().filter(item -> item.qid == qid).findFirst();
    }

    public String getMarketStrategy() {
        return marketStrategy;
    }

    public String getConstituentsGroup() {
        return this.constituentsGroup;
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(this.items);
    }

    public String toString() {
        return getClass().getSimpleName() + "[id=" + this.id
                + ", name=" + this.name
                + ", group=" + this.constituentsGroup
                + ", marketStrategy=" + this.marketStrategy
                + ", items.size()=" + this.items.size()
                + ", items=" + this.items
                + "]";
    }
}
