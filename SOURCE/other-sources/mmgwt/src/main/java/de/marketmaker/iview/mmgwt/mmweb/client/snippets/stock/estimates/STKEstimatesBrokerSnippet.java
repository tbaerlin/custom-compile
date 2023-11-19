/*
 * PdfSnippet.java
 *
 * Created on 17.06.2008 12:35:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.estimates;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.iview.dmxml.STKEstimates;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Thomas Kiesgen
 */
public class STKEstimatesBrokerSnippet extends
        AbstractSnippet<STKEstimatesBrokerSnippet, SnippetView<STKEstimatesBrokerSnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("STKEstimatesBroker"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new STKEstimatesBrokerSnippet(context, config);
        }
    }

    private DmxmlContext.Block<STKEstimates> blockEstimates;

    private HTML brokernames = new HTML();

    private STKEstimatesBrokerSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        setView(new View(this, this.brokernames));

        this.blockEstimates = context.addBlock("STK_Estimates"); // $NON-NLS$
        this.blockEstimates.setParameters("year", new String[]{"fy0", "fy1", "fy2"}); // $NON-NLS$
    }

    class View extends SnippetView<STKEstimatesBrokerSnippet> {
        private HTML brokernames;

        public View(STKEstimatesBrokerSnippet snippet, HTML brokernames) {
            super(snippet);
            setTitle(getConfiguration().getString("title", I18n.I.analystCompanies()));  // $NON-NLS$
            this.brokernames = brokernames;
        }

        @Override
        protected void onContainerAvailable() {
            super.onContainerAvailable();
            this.container.setContentWidget(this.brokernames);
        }
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.blockEstimates.setParameter("symbol", symbol); // $NON-NLS$
    }

    public void destroy() {
        destroyBlock(this.blockEstimates);
    }

    public void updateView() {
        if (!this.blockEstimates.isResponseOk()) {
            this.brokernames.setText("");
            return;
        }

        final STKEstimates estimates = this.blockEstimates.getResult();
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        String komma = "";
        for (final String s : estimates.getBrokerName()) {
            sb.appendEscaped(komma).appendEscaped(s);
            komma = ", ";
        }
        sb.appendHtmlConstant("<br/>"); // $NON-NLS$
        if (Selector.FACTSET.isAllowed()) {
            sb.appendHtmlConstant("<br/>"); // $NON-NLS$
            sb.appendEscaped(I18n.I.brokerEstimatesHintFactset());
        }

        if (estimates.getReferenceDate() != null) {
            sb.appendHtmlConstant("<br/>"); // $NON-NLS$
            sb.appendEscaped(I18n.I.lastUpdate()).appendEscaped(": ").appendEscaped(estimates.getReferenceDate());
        }

        this.brokernames.setHTML(sb.toSafeHtml());
    }
}
