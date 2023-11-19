/*
* CurrencyCalculatorSnippet.java
*
* Created on 16.07.2008 13:01:17
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;


import de.marketmaker.iview.dmxml.MSCCrossRate;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Michael Lösch
 */
public class CurrencyCalculatorSnippet
        extends AbstractSnippet<CurrencyCalculatorSnippet, CurrencyCalculatorSnippetView> {

    public static class Class extends SnippetClass {
        public Class() {
            super("CurrencyCalculator"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new CurrencyCalculatorSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("width", DEFAULT_SNIPPET_WIDTH); // $NON-NLS$
        }
    }

    private DmxmlContext.Block<MSCCrossRate> block;

    private CurrencyCalculatorSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.block = createBlock("MSC_CrossRate"); // $NON-NLS-0$
        onParametersChanged();
        setView(new CurrencyCalculatorSnippetView(this));
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            final SnippetConfiguration config = getConfiguration();
            getView().showError(I18n.I.messageCurrencyCulculatorFromToNoExchangeRate(config.getString("isocodeFrom"), config.getString("isocodeTo"))); // $NON-NLS$
            return;
        }
        final MSCCrossRate crossrate = this.block.getResult();
        getView().update(crossrate.getResult(), crossrate.getRate(), crossrate.getFactor());
    }

    protected void onParametersChanged() {
        final SnippetConfiguration config = getConfiguration();
        this.block.setParameter("betrag", config.getString("betrag", "1")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.block.setParameter("isocodeFrom", config.getString("isocodeFrom", "EUR")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.block.setParameter("isocodeTo", config.getString("isocodeTo", "EUR")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
    }
}
