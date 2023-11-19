package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Ulrich Maurer
 *         Date: 05.04.11
 */
public class PmSignalsSnippet extends AbstractSnippet<PmSignalsSnippet, PmSignalsSnippetView> {
    public static class Class extends SnippetClass {
        public Class() {
            super("PmSignals", I18n.I.pmSignals()); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PmSignalsSnippet(context, config);
        }
    }

    protected PmSignalsSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        //TODO: Enable SnippetsFactory to handle both context variants (dmxml/pmxml)
        if (!(context instanceof DmxmlContext)) {
            throw new IllegalStateException("PmReportSnippet needs a DmxmlContext!"); // $NON-NLS$
        }
        this.setView(new PmSignalsSnippetView(this));
    }

    public void destroy() {
        // nothing to do
    }

    public void updateView() {
        // nothing to do
    }
}
