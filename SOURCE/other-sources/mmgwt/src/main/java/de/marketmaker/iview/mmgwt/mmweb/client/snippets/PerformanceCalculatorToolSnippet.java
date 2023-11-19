/*
 * PerformanceCalculatorToolSnippet.java
 *
 * Created on 5/9/14 1:35 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Willenbrock
 */
public class PerformanceCalculatorToolSnippet extends AbstractPerformanceCalculator {

    public static class Class extends SnippetClass {
        public Class() {
            super("PerformanceCalculatorTool"); // $NON-NLS-0$
        }

        @Override
        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PerformanceCalculatorToolSnippet(context, config);
        }
    }

    protected PerformanceCalculatorToolSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration, new HashMap<String, String>());
        setView(new PerformanceCalculatorSnippetView(this, true));
    }

    @Override
    protected void onSymbolEvent(String title, String qid) {
        clearConfig();
        initialize();
        getInput().setFormParameters(getInputConfiguration());
        super.onSymbolEvent(title, qid);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        onSymbolEvent("", null);
    }

    @Override
    public void activate() {
        createBlock();
    }

    @Override
    public void deactivate() {
        destroy();
    }
}
