package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: umaurer
 * Created: 17.02.14
 */
public class SpsListProperty extends SpsProperty implements SpsCompositeProperty {
    private final List<SpsProperty> list;

    public SpsListProperty(String bindKey, SpsProperty parent) {
        super(bindKey, parent);
        this.list = new ArrayList<>();
    }

    public void add(SpsProperty property, boolean setChangeIndicator) {
        add(property, setChangeIndicator, true);
    }

    public void add(SpsProperty property, boolean setChangeIndicator, boolean fireEvent) {
        this.list.add(property);
        if (setChangeIndicator) {
            setChanged();
        }
        if (fireEvent) {
            fireChanged();
        }
    }

    public void remove(int index, boolean setChangeIndicator, boolean fireEvent) {
        this.list.remove(index);
        if(setChangeIndicator) {
            setChanged();
        }
        if(fireEvent) {
            fireChanged();
        }
    }

    public void remove(SpsProperty spsProperty, boolean setChangeIndicator, boolean fireEvent) {
        for (int i = 0; i < this.list.size(); i++) {
            if (this.list.get(i) == spsProperty) {
                remove(i, setChangeIndicator, fireEvent);
                return;
            }
        }
        throw new IllegalStateException("cannot remove property: not found"); // $NON-NLS$
    }

    public void clear(boolean fireEvent) {
        if (this.list.isEmpty()) {
            return;
        }
        this.list.clear();
        setChanged();
        if (fireEvent) {
            fireChanged();
        }
    }

    public void clearAndAdd(SpsProperty property, boolean fireEvent) {
        this.list.clear();
        this.list.add(property);
        setChanged();
        if (fireEvent) {
            fireChanged();
        }
    }

    public int getChildCount() {
        return this.list.size();
    }

    public SpsProperty get(int index) {
        return this.list.get(index);
    }

    public SpsProperty get(BindKey bindKey) {
        if (bindKey == null) {
            return this;
        }
        if (!(bindKey instanceof BindKeyIndexed)) {
            throw new IllegalArgumentException("SpsListProperty '" + this.getBindToken() + "' child '" + BindToken.getConcatBindKey(bindKey, getParent() == null) + "' has invalid bindKey type: " + bindKey.getClass().getSimpleName() + "; expected: BindKeyIndexed"); // $NON-NLS$
        }
        final BindKeyIndexed bki = (BindKeyIndexed) bindKey;

        if(this.list.size() <= bki.getIndex()) {
            throw new IllegalStateException("SpsListProperty '" + this.getBindToken() + "' index (" + bki.getIndex() + ") > list size (" + this.list.size() + ") indexed bindKey: " + BindToken.getConcatBindKey(bindKey, getParent() == null)); // $NON-NLS$
        }

        final SpsProperty spsProperty = this.list.get(bki.getIndex());
        if (spsProperty == null) {
            throw new SpsPropertyIsNullException("SpsListProperty '" + this.getBindToken() + "' child not found for indexed bindKey", BindToken.getConcatBindKey(bindKey, getParent() == null)); // $NON-NLS$
        }
        if (bindKey.getNext() == null) {
            return spsProperty;
        }
        if (!(spsProperty instanceof SpsCompositeProperty)) {
            throw new IllegalStateException("SpsListProperty '" + this.getBindToken() + "' expected list or group property for bindKey '" + BindToken.getConcatBindKey(bindKey, getParent() == null) + "' but was " + spsProperty.getClass().getSimpleName()); // $NON-NLS$
        }
        return ((SpsCompositeProperty) spsProperty).get(bindKey.getNext());
    }

    @Override
    public List<SpsProperty> getChildren() {
        return new ArrayList<>(this.list);
    }
}
