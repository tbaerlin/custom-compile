/*
 * ContextList.java
 *
 * Created on 18.01.13 08:00
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.history;

import java.util.List;

/**
 * @author Michael Lösch
 */
public abstract class ContextList<T extends ContextItem> {
    private final List<T> list;
    private T selected;

    protected ContextList(List<T> list) {
        this.list = list;
    }

    public List<T> getList() {
        return this.list;
    }

    public List<? extends ContextItem> getContextItems() {
        return this.list;
    }

    public void setSelected(T item) {
        this.selected = item;
    }

    public void setSelected(String id) {
        for (T t : this.list) {
            if (getIdOf(t).equals(id)) {
                this.selected = t;
            }
        }
    }

    public T getSelected() {
        return this.selected;
    }

    protected abstract String getIdOf(T item);

}
