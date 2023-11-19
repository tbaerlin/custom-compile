/*
* StructPieSnippet.java
*
* Created on 18.07.2008 13:10:35
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

/**
 * @author Michael Lösch
 */
public class StructPieSnippet extends AbstractSnippet<StructPieSnippet, StructPieSnippetView> implements SymbolSnippet, IsVisible {
    public static class Class extends SnippetClass {
        public Class() {
            super("StructPie"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new StructPieSnippet(context, config);
        }

        @Override
        protected void addDefaultParameters(SnippetConfiguration config) {
            config.put("colSpan", 2); // $NON-NLS$
        }
    }

    private DmxmlContext.Block<IMGResult> block;
    private boolean isVisible = true;

    public StructPieSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        final String blockType = configuration.getString("blockType", "FND_Investments"); // $NON-NLS$
        this.block = createBlock(blockType);
        this.block.setParameter("width", configuration.getString("width", "590")); // $NON-NLS$
        if ("FND_Investments".equals(blockType)) { // $NON-NLS$
            this.block.setParameter("type", configuration.getString("allocationtype")); // $NON-NLS$
            this.block.setParameter("withConsolidatedAllocations", "true"); // $NON-NLS$
            this.block.setParameter("defaultLabel", I18n.I.remainder()); // $NON-NLS$
            this.block.setParameter("byValue", "false"); // $NON-NLS$
            this.block.setParameter("numItems", "10"); // $NON-NLS$
        }
        else if ("PF_Visualization".equals(blockType)) { // $NON-NLS$
            this.block.setParameter("type", configuration.getString("type")); // $NON-NLS$
            this.block.setParameter("portfolioid", configuration.getString("portfolioid")); // $NON-NLS$
            this.block.setParameter("userid", configuration.getString("userid")); // $NON-NLS$
        }
        else {
            throw new IllegalArgumentException(getClass().getName() + ": wrong blocktype: " + blockType); // $NON-NLS$
        }
        final StructPieSnippetView spsv = new StructPieSnippetView(this);
        spsv.setTitle(configuration.getString("title", I18n.I.fundStructure()));  // $NON-NLS$
        this.setView(spsv);
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            return;
        }
        getView().update(this.block.getResult().getRequest());
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void setPortfolioId(String portfolioId) {
        this.block.setParameter("portfolioid", portfolioId); // $NON-NLS-0$
    }

    public void destroy() {
        //
    }

    public void setEnabled(boolean value) {
        this.block.setEnabled(value);
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    @Override
    public boolean isVisible() {
        return isVisible;
    }

}
