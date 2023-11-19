/*
 * ViewStateFeature.java
 *
 * Created on 12.06.2015 08:22
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mdick
 */
public class ViewStateFeature {
    private HashMap<String, String> state = new HashMap<>();
    private String stateKey;

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public String getStateKey() {
        return this.stateKey;
    }

    public Map<String, String> saveState() {
        return this.state;
    }

    public void loadState(Map<String, String> state) {
        this.state.clear();
        this.state.putAll(state);
    }

    public void putValue(String key, String value) {
        this.state.put(key, value);
    }

    public String getValue(String key) {
        return this.state.get(key);
    }
}
