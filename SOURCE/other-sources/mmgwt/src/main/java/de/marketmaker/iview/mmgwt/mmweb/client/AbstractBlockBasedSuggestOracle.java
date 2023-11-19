/*
 * AbstractBlockBasedSuggestOracle.java
 *
 * Created on 04.06.13 15:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.SuggestOracle;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Markus Dick
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractBlockBasedSuggestOracle <T extends SuggestOracle.Suggestion, V extends BlockType>
        extends SuggestOracle implements HasEnabled {

    protected abstract boolean isQueryAcceptable(String query);
    public abstract void setBlockParameters(String query, int limit);
    protected abstract List<T> getSuggestions(String query);

    private final DmxmlContext.Block<V> block;

    private boolean enabled = true;

    private List<QuerySuggestions> results = new ArrayList<QuerySuggestions>();

    protected AbstractBlockBasedSuggestOracle(DmxmlContext.Block<V> block) {
        this.block = block;
    }

    @Override
    public void requestSuggestions(Request request, Callback callback) {
        if (!this.enabled) {
            callback.onSuggestionsReady(request, new Response(Collections.<Suggestion>emptyList()));
            return;
        }

        final String query = request.getQuery().toLowerCase();

        if(!isQueryAcceptable(query)) {
            callback.onSuggestionsReady(request, new Response(Collections.<Suggestion>emptyList()));
            return;
        }

        for (QuerySuggestions result : this.results) {
            if (query.equals(result.query)
                    || (result.suggestions.isEmpty() && query.startsWith(result.query))) {
                callback.onSuggestionsReady(request, new Response(result.suggestions));
                return;
            }
        }

        setBlockParameters(query, request.getLimit());

        this.block.issueRequest(new BlockBasedSuggestCallback(request, callback));
    }

    @Override
    public boolean isDisplayStringHTML() {
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    protected DmxmlContext.Block<V> getBlock() {
        return block;
    }

    private List<? extends Suggestion> getSuggestions(Request request) {
        if (!this.block.isResponseOk()) {
            return Collections.<Suggestion>emptyList();
        }

        final String query = request.getQuery().toLowerCase();
        final List<? extends Suggestion> suggestions = getSuggestions(query);

        this.results.add(new QuerySuggestions(query, suggestions));
        if (this.results.size() > 10) {
            this.results.remove(0);
        }

        return suggestions;
    }

    class BlockBasedSuggestCallback implements AsyncCallback<ResponseType> {
        private Callback callback;

        private Request request;

        public BlockBasedSuggestCallback(Request request,
                                   Callback callback) {
            this.request = request;
            this.callback = callback;
        }

        public void onFailure(Throwable error) {
            suggestNothing();
        }

        public void onSuccess(ResponseType responseType) {
            this.callback.onSuggestionsReady(this.request, new Response(getSuggestions(this.request)));
        }

        private void suggestNothing() {
            this.callback.onSuggestionsReady(this.request, new Response(Collections.<Suggestion>emptyList()));
        }
    }

    private static class QuerySuggestions {
        private final String query;

        private final List<? extends Suggestion> suggestions;

        private QuerySuggestions(String query, List<? extends Suggestion> suggestions) {
            this.query = query;
            this.suggestions = suggestions;
        }
    }
}
