/*
 * PdfSnippet.java
 *
 * Created on 17.06.2008 12:35:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.estimates;

import com.google.gwt.user.client.ui.Grid;
import de.marketmaker.itools.gwtutil.client.widgets.SliderGraph;
import de.marketmaker.iview.dmxml.STKEstimates;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;

/**
 * @author Ulrich Maurer
 */
public class STKEstimatesGraphSnippet extends AbstractSnippet<STKEstimatesGraphSnippet, SnippetView<STKEstimatesGraphSnippet>> implements SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("STKEstimatesGraph"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new STKEstimatesGraphSnippet(context, config);
        }
    }

    private final SliderGraph sliderGraph;
    private final DetailsTable detailsTable;
    private final DmxmlContext.Block<STKEstimates> blockEstimates;

    private boolean showEstimatesDetails = FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() || FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled();


    private STKEstimatesGraphSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.sliderGraph = new SliderGraph(IconImage.getUrl("sell-hold-buy-coeff"), IconImage.getUrl("slider-slider")); // $NON-NLS$
        this.sliderGraph.setStyle("mm-shbcGraph"); // $NON-NLS-0$
        this.sliderGraph.setLowHighTexts(I18n.I.sell2(), I18n.I.buy2(), true);
        this.detailsTable = new DetailsTable();
        setView(new View());

        this.blockEstimates = context.addBlock("STK_Estimates"); // $NON-NLS-0$
        this.blockEstimates.setParameters("year", new String[]{"fy0", "fy1", "fy2"}); // $NON-NLS$
    }

    private class View extends SnippetView<STKEstimatesGraphSnippet> {

        public View() {
            super(STKEstimatesGraphSnippet.this);
            setTitle(getConfiguration().getString("title", I18n.I.currentEstimation()));  // $NON-NLS-0$
        }

        @Override
        protected void onContainerAvailable() {
            super.onContainerAvailable();
            if (showEstimatesDetails) {
                final Grid grid = new Grid(1, 2);
                this.container.setContentWidget(grid);
                grid.setWidget(0, 0, STKEstimatesGraphSnippet.this.sliderGraph);
                grid.setWidget(0, 1, STKEstimatesGraphSnippet.this.detailsTable);
            } else {
                this.container.setContentWidget(STKEstimatesGraphSnippet.this.sliderGraph);
            }
        }
    }

    final class DetailsTable extends Grid {
        private String[] LABELS = new String[] {
                    I18n.I.buy() + " (1,0)",           // $NON-NLS-0$
                    I18n.I.overweight() + " (1,5)",    // $NON-NLS-0$
                    I18n.I.hold() + " (2,0)",          // $NON-NLS-0$
                    I18n.I.underweight() + " (2,5)",   // $NON-NLS-0$
                    I18n.I.sell() + " (3,0)",          // $NON-NLS-0$
                    I18n.I.average()};

        DetailsTable() {
            super(8,2);
            setHTML(0,0, "");
            setHTML(1,1, I18n.I.analysts());
            getCellFormatter().setStyleName(1,1, "mm-right");  // $NON-NLS-0$
            int i = 1;
            for (String label : LABELS) {
                i += 1;
                setHTML(i,0, label);
                getCellFormatter().setStyleName(i,0, "mm-snippetTable-label");  // $NON-NLS-0$
                getCellFormatter().setStyleName(i,1, "mm-right");  // $NON-NLS-0$
            }
        }

    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.blockEstimates.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void destroy() {
        destroyBlock(this.blockEstimates);
    }

    public void updateView() {
        if (!this.blockEstimates.isResponseOk()) {
            this.sliderGraph.setSliderVisible(false);
            this.sliderGraph.setExplainText("--"); // $NON-NLS-0$
            this.detailsTable.setVisible(false);
            return;
        }

        final String recommendation = this.blockEstimates.getResult().getRecommendation();
        if (recommendation == null) {
            this.sliderGraph.setSliderVisible(false);
            this.sliderGraph.setExplainText("--"); // $NON-NLS-0$
            this.detailsTable.setVisible(false);
            return;
        }
        final float value = Float.parseFloat(recommendation);
        if (value >= 2f) {
            this.sliderGraph.setExplainStyle(value == 2f ? "mm-explain" : "mm-explain sell"); // $NON-NLS$
        }
        else {
            this.sliderGraph.setExplainStyle("mm-explain buy"); // $NON-NLS-0$
        }
        this.sliderGraph.setExplainText(Formatter.FORMAT_NUMBER_2.format(value));
        this.sliderGraph.setValue((3f - value) * 50f);
        this.sliderGraph.setSliderVisible(true);

        if (this.showEstimatesDetails) {
            this.detailsTable.setVisible(true);
            STKEstimates result = this.blockEstimates.getResult();
            detailsTable.setHTML(2, 1, result.getNumBuy());
            detailsTable.setHTML(3, 1, result.getNumOverweight());
            detailsTable.setHTML(4, 1, result.getNumHold());
            detailsTable.setHTML(5, 1, result.getNumUnderweight());
            detailsTable.setHTML(6, 1, result.getNumSell());
            detailsTable.setHTML(7, 1, result.getRecommendation());
        }
    }
}
