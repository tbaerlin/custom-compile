/*
 * NewsDetailController.java
 *
 * Created on 31.03.2008 15:50:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DzNewsRelatedOffersSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsEntrySnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsrelatedQuotesSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.PortraitChartSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PrintUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsDetailController extends DelegatingPageController {

    private static final String DEF = "nws_detail"; // $NON-NLS-0$

    private NewsEntrySnippet entry;
    private NewsrelatedQuotesSnippet related;
    private PortraitChartSnippet chart;

    private DzNewsRelatedOffersSnippet relatedDzOffers;
    private SymbolSnippet relatedDzOffersStaticData;

    public NewsDetailController(ContentContainer contentContainer) {
        super(contentContainer);
    }

/*
    @Override
    protected void beforeInvokingDelegate(PlaceChangeEvent event) {
        final String[] token = event.getTokens();
        if (token.length >= 2) {
            this.entry.ackNewsid(token[1]);
        }
    }
*/

    public void onPlaceChange(PlaceChangeEvent event) {
        this.delegate.onPlaceChange(event, false);
    }

    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(), DEF);
        this.entry = (NewsEntrySnippet) this.delegate.getSnippet("ne"); // $NON-NLS-0$
        this.related = (NewsrelatedQuotesSnippet) this.delegate.getSnippet("nrq"); // $NON-NLS-0$
        this.chart = (PortraitChartSnippet) this.delegate.getSnippet("chart"); // $NON-NLS-0$
        if (this.related == null || this.chart == null) {
            return;
        }
        this.entry.addSymbolListSnippet(related);
        this.entry.addSymbolListSnippet(chart);
        this.related.addSymbolSnippet(chart);

        this.relatedDzOffers = (DzNewsRelatedOffersSnippet) this.delegate.getSnippet("nrdzo"); // $NON-NLS$
        this.relatedDzOffersStaticData = (SymbolSnippet) this.delegate.getSnippet("nrdzos"); // $NON-NLS$
        if(this.relatedDzOffers != null && this.relatedDzOffersStaticData != null) {
            this.entry.addSymbolListSnippet(this.relatedDzOffers);
            this.relatedDzOffers.addSymbolSnippet(this.relatedDzOffersStaticData);

            EventBusRegistry.get().addHandler(VisibilityUpdatedEvent.getType(), this.related);
            EventBusRegistry.get().addHandler(VisibilityUpdatedEvent.getType(), this.relatedDzOffers);
        }

    }

    public String getPrintHtml() {
        return PrintUtil.getNewsPrintHtml(this.entry, this.related, this.chart, relatedDzOffers, relatedDzOffersStaticData);
    }
}
