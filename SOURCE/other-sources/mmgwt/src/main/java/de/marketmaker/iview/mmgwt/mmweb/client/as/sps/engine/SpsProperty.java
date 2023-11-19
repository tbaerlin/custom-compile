package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 24.03.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public abstract class SpsProperty implements HasChangeHandlers {
    private final String bindKey;
    private final SpsProperty parent;
    private boolean changed = false;
    private HandlerManager handlerManager = new HandlerManager(this);

    protected SpsProperty(String bindKey, SpsProperty parent) {
        this.bindKey = bindKey;
        this.parent = parent;
    }

    public BindToken getBindToken() {
        if (isRoot()) {
            return BindToken.EMPTY_ROOT_TOKEN;
        }
        final List<SpsProperty> list = new ArrayList<>();
        SpsProperty p = this;
        while (p != null) {
            list.add(p);
            p = p.parent;
        }
        boolean isList = false;
        BindKey first = null;
        BindKey parent = null;
        BindKey child;
        for (int i = list.size() - 1; i >= 0; i--) {
            final SpsProperty sp = list.get(i);
            if (first == null && !StringUtil.hasText(sp.bindKey)) {
                continue;
            }
            child = isList
                    ? new BindKeyIndexed(getIndex((SpsListProperty) sp.getParent(), sp))
                    : new BindKeyNamed(sp.getBindKey());
            if (parent == null) {
                first = child;
            }
            else {
                parent.setNext(child);
            }
            child.setPrev(parent);
            parent = child;
            isList = sp instanceof SpsListProperty;
        }
        return new BindToken(first, true, false);
    }

    private int getIndex(SpsListProperty lp, SpsProperty p) {
        return lp.getChildren().indexOf(p);
    }

    public String getBindKey() {
        return bindKey;
    }

    public SpsProperty getParent() {
        return this.parent;
    }

    public boolean hasChanged() {
        return this.changed;
    }

    public void setChanged() {
        this.changed = true;
        if (isRoot()) {
            // if it has no parent, it is the root property. hence, fire change to inform interested parties
            // about a change somewhere in the properties tree.
            // TODO: replace this special handling with a special event to avoid unwanted interference
            fireChanged();
        } else {
            this.parent.setChanged();
        }
    }

    /**
     * Can be called to reset the state when the properties have been saved.
     */
    public void resetChanged() {
        this.changed = false;
        if(this instanceof SpsCompositeProperty) {
            for (SpsProperty child : ((SpsCompositeProperty) this).getChildren()) {
                child.resetChanged();
            }
        }
    }

    @Override
    public String toString() {
        return getBindToken().toString();
    }

    @Override
    public HandlerRegistration addChangeHandler(ChangeHandler handler) {
        return this.handlerManager.addHandler(ChangeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.handlerManager.fireEvent(event);
    }

    public void fireChanged() {
        ChangeEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
    }

    public boolean isRoot() {
        return this.parent == null;
    }
}
