/*
 * PdfSnippet.java
 *
 * Created on 17.06.2008 12:35:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock;

import java.util.List;

import de.marketmaker.itools.gwtutil.client.widgets.SliderGraph;
import de.marketmaker.iview.dmxml.RSCAggregatedFinder;
import de.marketmaker.iview.dmxml.RSCAggregatedFinderElement;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SellHoldBuy;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Ulrich Maurer
 */
public class SellHoldBuyCoeffGraphSnippet extends
        AbstractSnippet<SellHoldBuyCoeffGraphSnippet, SnippetView<SellHoldBuyCoeffGraphSnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("SellHoldBuyCoeffGraph"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new SellHoldBuyCoeffGraphSnippet(context, config);
        }
    }

    private final SliderGraph sliderGraph;

    private DmxmlContext.Block<RSCAggregatedFinder> block;


    private SellHoldBuyCoeffGraphSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.sliderGraph = new SliderGraph(IconImage.getUrl("sell-hold-buy-coeff"), IconImage.getUrl("slider-slider")); // $NON-NLS$
        this.sliderGraph.setStyle("mm-shbcGraph"); // $NON-NLS-0$
        this.sliderGraph.setLowHighTexts("-1,0<br/>" + I18n.I.sell(), "1,0<br/>" + I18n.I.buy(), true);  // $NON-NLS-0$ $NON-NLS-1$
        setView(new View(this, sliderGraph));

        this.block = context.addBlock("RSC_Finder"); // $NON-NLS-0$
        this.block.setParameter("resultType", "aggregated"); // $NON-NLS-0$ $NON-NLS-1$
    }

    class View extends SnippetView<SellHoldBuyCoeffGraphSnippet> {
        private SliderGraph sliderGraph;

        public View(SellHoldBuyCoeffGraphSnippet snippet, SliderGraph sliderGraph) {
            super(snippet);
            setTitle(getConfiguration().getString("title", I18n.I.recommendationsCoefficiency()));  // $NON-NLS-0$
            this.sliderGraph = sliderGraph;
        }

        @Override
        protected void onContainerAvailable() {
            super.onContainerAvailable();
            this.container.setContentWidget(this.sliderGraph);
        }
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.block.setParameter("query", "symbol==" + symbol); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    public void updateView() {
        if (!this.block.isResponseOk()) {
            this.sliderGraph.setSliderVisible(false);
            this.sliderGraph.setExplainText("--"); // $NON-NLS-0$
            return;
        }

        final List<RSCAggregatedFinderElement> list = this.block.getResult().getElement();
        if (list.isEmpty()) {
            this.sliderGraph.setSliderVisible(false);
            this.sliderGraph.setExplainText("--"); // $NON-NLS-0$
            return;
        }
        final SellHoldBuy shb = new SellHoldBuy(list.get(0));
        final double coefficient = shb.getCoefficient();
        if (coefficient <= 0d) {
            this.sliderGraph.setExplainStyle(coefficient == 0d ? "mm-explain" : "mm-explain sell"); // $NON-NLS-0$ $NON-NLS-1$
            this.sliderGraph.setExplainText(Formatter.FORMAT_NUMBER_2.format(coefficient));
        }
        else if (coefficient > 0d) {
            this.sliderGraph.setExplainStyle("mm-explain buy"); // $NON-NLS-0$
            this.sliderGraph.setExplainText("+" + Formatter.FORMAT_NUMBER_2.format(coefficient)); // $NON-NLS-0$
        }
        this.sliderGraph.setValue((float) ((coefficient + 1d) * 50d));
        this.sliderGraph.setSliderVisible(true);
    }
}
