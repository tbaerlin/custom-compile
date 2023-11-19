/*
 * PerformanceCalculatorSnippet.java
 *
 * Created on 5/9/14 12:55 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Willenbrock
 */
public class PerformanceCalculatorSnippet extends AbstractPerformanceCalculator implements SymbolSnippet {

    public static class Class extends SnippetClass {

        public Class() {
            super("PerformanceCalculator"); // $NON-NLS-0$
        }

        @Override
        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PerformanceCalculatorSnippet(context, config);
        }
    }

    private static final Map<String, String> SHARED_INPUT_CONFIGURATION = new HashMap<>();

    private PerformanceCalculatorSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config, SHARED_INPUT_CONFIGURATION);
        setView(new PerformanceCalculatorSnippetView(this, false));
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        if (checkResetCondition(symbol)) {
            clearConfig();
            initialize();
        }

        getInputConfiguration().put(symbolKey, symbol);
        getInput().setFormParameters(getInputConfiguration());
        ackParametersChanged();
    }

    private boolean checkResetCondition(final String symbol) {
        final String lastSymbol = getInputConfiguration().get(symbolKey);
        return lastSymbol != null && !lastSymbol.equals(symbol);
    }

    @Override
    public void onControllerInitialized() {
        enableBlock();
        setSymbolState(true);
    }
}
