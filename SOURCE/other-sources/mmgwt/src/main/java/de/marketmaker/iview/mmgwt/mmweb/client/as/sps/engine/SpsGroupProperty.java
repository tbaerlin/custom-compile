package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: umaurer
 * Created: 17.02.14
 */
public class SpsGroupProperty extends SpsProperty implements SpsCompositeProperty {
    private String nodeGUID;
    private final Map<String, SpsProperty> map;

    public SpsGroupProperty(String bindKey, SpsProperty parent) {
        super(bindKey, parent);
        this.map = new HashMap<>();
    }

    public SpsProperty put(String bindKey, final SpsProperty property, boolean setChangeIndicator) {
        this.map.put(bindKey, property);
        if (setChangeIndicator) {
            setChanged();
        }
        fireChanged();
        return property;
    }

    public SpsProperty get(String bindKey) {
        return this.map.get(bindKey);
    }

    public SpsProperty get(BindKey bindKey) {
        if (bindKey == null) {
            return this;
        }
        if (!(bindKey instanceof BindKeyNamed)) {
            throw new IllegalArgumentException("SpsGroupProperty '" + this.getBindToken() + "' child '" + BindToken.getConcatBindKey(bindKey, getParent() == null) + "' has invalid bindKey type: " + bindKey.getClass().getSimpleName() + "; expected: BindKeyNamed"); // $NON-NLS$
        }
        final BindKeyNamed bkn = (BindKeyNamed) bindKey;
        final SpsProperty spsProperty = this.map.get(bkn.getName());
        if (spsProperty == null) {
            throw new SpsPropertyIsNullException("SpsGroupProperty '" + this.getBindToken() + "' child not found for bindKey", BindToken.getConcatBindKey(bindKey, getParent() == null)); // $NON-NLS$
        }
        if (bindKey.getNext() == null) {
            return spsProperty;
        }
        if (!(spsProperty instanceof SpsCompositeProperty)) {
            throw new IllegalStateException("SpsGroupProperty '" + this.getBindToken() + "' expected list or group property for bindKey '" + BindToken.getConcatBindKey(bindKey, getParent() == null) + "' but was " + spsProperty.getClass().getSimpleName()); // $NON-NLS$
        }
        return ((SpsCompositeProperty) spsProperty).get(bindKey.getNext());
    }

    public void setNodeGUID(String nodeGUID) {
        this.nodeGUID = nodeGUID;
    }

    public String getNodeGUID() {
        return this.nodeGUID;
    }

    @Override
    public Collection<SpsProperty> getChildren() {
        return this.map.values();
    }
}