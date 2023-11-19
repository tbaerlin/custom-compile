/*
 * PortraitTimesAndSalesController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.DelegatingPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.OHLCVSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PriceTeaserSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.TimesAndSalesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * Remark: Limit/alert snippet requires quote metadata.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitTimesAndSalesController extends DelegatingPageController implements MetadataAware {
    protected static final String DEF_OHLCV_TAS = "ts_ohlcv_tas"; // $NON-NLS-0$
    protected static final String DEF_OHLCV = "ts_ohlcv"; // $NON-NLS-0$
    protected static final String DEF_FUND_TAS = "ts_fund_tas"; // $NON-NLS-0$
    protected static final String DEF_FUND = "ts_fund"; // $NON-NLS-0$

    protected PriceTeaserSnippet pt;
    protected OHLCVSnippet ohlcv;
    protected TimesAndSalesSnippet tas;
    protected final boolean asFund;

    public PortraitTimesAndSalesController(ContentContainer contentContainer) {
        this(contentContainer, false);
    }

    public PortraitTimesAndSalesController(ContentContainer contentContainer, DmxmlContext context) {
        super(contentContainer, context);
        this.asFund = false;
    }

    public PortraitTimesAndSalesController(ContentContainer contentContainer, boolean asFund) {
        super(contentContainer);
        this.asFund = asFund;
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String symbol = historyToken.get(1, null);
        if (symbol != null) {
            final InstrumentTypeEnum type = InstrumentTypeEnum.fromToken(historyToken.getControllerId());
            setSymbol(type, symbol);
        }
        super.onPlaceChange(event);
    }


    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(),
                Selector.TIMES_AND_SALES.isAllowed()
                        ? this.asFund ? DEF_FUND_TAS : DEF_OHLCV_TAS
                        : this.asFund ? DEF_FUND : DEF_OHLCV);
        this.pt = (PriceTeaserSnippet) this.delegate.getSnippet("pt"); // $NON-NLS-0$
        this.ohlcv = (OHLCVSnippet) this.delegate.getSnippet("ohlcv"); // $NON-NLS-0$
        this.tas = (TimesAndSalesSnippet) this.delegate.getSnippet("tas"); // $NON-NLS-0$
        if (this.tas != null) {
            this.ohlcv.addValueChangeHandler(this.tas);
        }
    }

    protected void setSymbol(InstrumentTypeEnum type, String symbol) {
        this.pt.setSymbol(type, symbol, null);
        this.ohlcv.setSymbol(symbol);
        if (this.tas != null) {
            this.tas.setSymbol(symbol);
        }
    }

    @Override
    public boolean isMetadataNeeded() {
        for (Snippet snippet : this.delegate.getSnippets()) {
            if(snippet instanceof MetadataAware && ((MetadataAware) snippet).isMetadataNeeded()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMetadataAvailable(MSCQuoteMetadata metadata) {
        for (Snippet snippet : this.delegate.getSnippets()) {
            if (snippet instanceof MetadataAware && ((MetadataAware)snippet).isMetadataNeeded()) {
                ((MetadataAware)snippet).onMetadataAvailable(metadata);
            }
        }
    }
}
