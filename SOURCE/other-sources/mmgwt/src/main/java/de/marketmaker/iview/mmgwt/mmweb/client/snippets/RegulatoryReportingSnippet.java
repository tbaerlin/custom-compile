/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.MSCRegulatoryReporting;
import de.marketmaker.iview.dmxml.MSCRegulatoryReportingRecord;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

public class RegulatoryReportingSnippet extends AbstractSnippet<RegulatoryReportingSnippet, RegulatoryReportingSnippetView> implements SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("RegulatoryReportingSnippet"); // $NON-NLS-0$
        }

        @Override
        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new RegulatoryReportingSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("width", DEFAULT_SNIPPET_WIDTH); // $NON-NLS$
        }
    }

    private DmxmlContext.Block<MSCRegulatoryReporting> blockRecord;

    protected RegulatoryReportingSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        this.setView(new RegulatoryReportingSnippetView(this));
        this.blockRecord = createBlock("MSC_RegulatoryReporting"); // $NON-NLS-0$

        setSymbol(null, configuration.getString("symbol", null), null); // $NON-NLS$
    }

    @Override
    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.blockRecord.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    @Override
    public void destroy() {
        destroyBlock(this.blockRecord);
    }

    @Override
    public void updateView() {
        final MSCRegulatoryReportingRecord record = this.blockRecord.isResponseOk() ? this.blockRecord.getResult().getRecord() : null;
        getView().update(record);
    }
}
