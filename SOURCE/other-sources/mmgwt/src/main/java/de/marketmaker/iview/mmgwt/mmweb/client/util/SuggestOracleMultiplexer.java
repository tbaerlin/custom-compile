/*
 * SuggestOracleMultiplexer.java
 *
 * Created on 04.06.13 16:30
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.SuggestOracle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
* @author Markus Dick
*/
public class SuggestOracleMultiplexer<K> extends SuggestOracle {
    private final Map<K, SuggestOracle> oracles;
    private SuggestOracle currentOracle;
    private K currentOracleKey;
    private boolean enabled;

    public SuggestOracleMultiplexer() {
        this.oracles = new HashMap<>();
        this.enabled = true;
    }

    public void put(K key, SuggestOracle oracle) {
        this.oracles.put(key, oracle);
    }

    @Override
    public boolean isDisplayStringHTML() {
        return this.currentOracle != null && this.currentOracle.isDisplayStringHTML();
    }

    @Override
    public void requestSuggestions(Request request, Callback callback) {
        if (!this.enabled || this.currentOracle == null) {
            callback.onSuggestionsReady(request, new Response(Collections.<Suggestion>emptyList()));
            return;
        }

        this.currentOracle.requestSuggestions(request, callback);
    }

    public void setActiveOracle(K oracleKey) {
        assert oracleKey != null;

        final SuggestOracle oracle = this.oracles.get(oracleKey);
        if(oracle == null) {
            this.currentOracleKey = null;
            this.currentOracle = null;
            return;
        }

        this.currentOracleKey = oracleKey;
        this.currentOracle = oracle;
    }

    public void setOracleEnabled(K oracleKey, boolean enabled) {
        final SuggestOracle suggestOracle = this.oracles.get(oracleKey);
        if(suggestOracle instanceof HasEnabled) {
            ((HasEnabled) suggestOracle).setEnabled(enabled);
        }
    }

    @SuppressWarnings("unused")
    public boolean isOracleEnabled(K oracleKey) {
        final SuggestOracle oracle = this.oracles.get(oracleKey);
        return oracle instanceof HasEnabled && ((HasEnabled) oracle).isEnabled();
    }

    @SuppressWarnings("unused")
    private K getCurrentOracleKey() {
        return this.currentOracleKey;
    }

    @SuppressWarnings("unused")
    private SuggestOracle getCurrentOracle() {
        return this.currentOracle;
    }

    @SuppressWarnings("unused")
    private boolean isEnabled() {
        return this.enabled;
    }

    @SuppressWarnings("unused")
    private void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
