/*
 * VwdPageController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.DelegatingPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AnalyserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.StaticDataSTKSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import static de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet.NO_SYMBOL;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitOverviewController extends DelegatingPageController implements MetadataAware {
    protected final String def;

    private boolean metadataNeeded = false;

    public PortraitOverviewController(ContentContainer contentContainer, String def) {
        super(contentContainer);
        this.def = def;
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String symbol = historyToken.get(1, null);
        if (symbol != null) {
            final String[] compareSymbols = getCompareSymbols(event);
            setSymbol(InstrumentTypeEnum.fromToken(historyToken.getControllerId()), symbol, compareSymbols);
        }
        super.onPlaceChange(event);
    }

    private String[] getCompareSymbols(PlaceChangeEvent event) {
        final String compareSymbols = event.getProperty("compareSymbols"); // $NON-NLS$
        if (compareSymbols == null) {
            return new String[0];
        }
        return compareSymbols.split("[,;]");
    }

    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(), def);
        if (def.equals(PortraitSTKController.DEF_OVERVIEW)) {
            final StaticDataSTKSnippet staticSnippet = (StaticDataSTKSnippet) this.delegate.getSnippet("sd"); // $NON-NLS-0$
            final PriceSnippet priceSnippet = (PriceSnippet) this.delegate.getSnippet("prc"); // $NON-NLS-0$
            staticSnippet.setPriceSnippet(priceSnippet);
        }
        for (Snippet snippet : this.delegate.getSnippets()) {
            if (snippet instanceof MetadataAware && ((MetadataAware)snippet).isMetadataNeeded()) {
                this.metadataNeeded = true;
                break;
            }
        }
    }

    public boolean isMetadataNeeded() {
        return this.metadataNeeded;
    }

    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        for (Snippet snippet : this.delegate.getSnippets()) {
            if (snippet instanceof MetadataAware && ((MetadataAware)snippet).isMetadataNeeded()) {
                ((MetadataAware)snippet).onMetadataAvailable(metadata);
            }
        }
    }

    protected void setSymbol(InstrumentTypeEnum type, String symbol, String... compareSymbols) {
        for (Snippet snippet : this.delegate.getSnippets()) {
            if (snippet instanceof SymbolSnippet
                    && !NO_SYMBOL.equals(snippet.getConfiguration().getString("symbol"))) { // $NON-NLS-0$
                ((SymbolSnippet) snippet).setSymbol(type, symbol, null, compareSymbols);
            }
        }
    }

    public String getPrintHtml() {
        // check, if page has an AnalyserSnippet -> print with flex print interface
        final AnalyserSnippet analyserSnippet = (AnalyserSnippet) this.delegate.getSnippet(AnalyserSnippet.class);
        if (analyserSnippet != null) {
            final String printHtml = analyserSnippet.getPrintHtml();
            if (!"super".equals(printHtml)) { // $NON-NLS$
                return printHtml;
            }
        }

        return super.getPrintHtml();
    }
}
