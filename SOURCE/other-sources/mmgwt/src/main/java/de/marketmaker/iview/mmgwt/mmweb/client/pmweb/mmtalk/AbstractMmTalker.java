package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk;

import de.marketmaker.iview.pmxml.MMTalkRequest;
import de.marketmaker.iview.pmxml.ObjectQuery;

/**
 * Created on 06.03.13 09:36
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public abstract class AbstractMmTalker<T extends ObjectQuery, O, W> implements MmTalker<T, O> {

    private final T query;
    protected final MmTalkWrapper<W> wrapper;

    protected AbstractMmTalker(Formula formula) {
        this.query = createQuery();
        this.wrapper = createWrapper(formula);
    }

    protected AbstractMmTalker(String formula) {
        this(Formula.create(formula));
    }

    public MMTalkRequest createRequest() {
        final MMTalkRequest mmtalk = new MMTalkRequest();
        mmtalk.setQuery(this.query);
        mmtalk.setRootnode(this.wrapper.getNode());
        mmtalk.getParameter().addAll(this.wrapper.getParameters());
        return mmtalk;
    }

    public Formula getFormula() {
        return this.wrapper.getFormula();
    }

    @Override
    public T getQuery() {
        return this.query;
    }

    protected abstract MmTalkWrapper<W> createWrapper(Formula formula);
    protected abstract T createQuery();
}