/*
 * VwdPageController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.VisibilityUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.DzNewsRelatedOffersSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsEntrySnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.NewsHeadlinesSnippet;
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
public class NewsTopicController extends DelegatingPageController {
    private static final String DEF = "nws_topics"; // $NON-NLS$

    public static final String USER_MSG_DEF = "nws_user_msg"; // $NON-NLS$

    private NewsHeadlinesSnippet headlines;

    private NewsEntrySnippet entry;

    private NewsrelatedQuotesSnippet related;

    private PortraitChartSnippet chart;

    private DzNewsRelatedOffersSnippet relatedDzOffers;

    private SymbolSnippet relatedDzOffersStaticData;

    private String def;

    private String defaultTopic;

    public NewsTopicController(ContentContainer contentContainer) {
        this(contentContainer, DEF);
    }

    public NewsTopicController(ContentContainer contentContainer, String def) {
        super(contentContainer);
        this.def = def;
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final String topic = event.getHistoryToken().get("topic"); // $NON-NLS$
        this.headlines.setTopic(topic == null ? this.defaultTopic : topic);
        this.delegate.onPlaceChange(event, false);
    }

    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(), this.def);
        this.headlines = (NewsHeadlinesSnippet) this.delegate.getSnippet("nhd"); // $NON-NLS$
        this.defaultTopic = this.headlines.getConfiguration().getString("topic"); // $NON-NLS$
        this.entry = (NewsEntrySnippet) this.delegate.getSnippet("ne"); // $NON-NLS$
        this.chart = (PortraitChartSnippet) this.delegate.getSnippet("chart"); // $NON-NLS$

        if (this.chart != null) {
            this.entry.addSymbolListSnippet(chart);
        }
        this.related = (NewsrelatedQuotesSnippet) this.delegate.getSnippet("nrq"); // $NON-NLS$
        if (this.related != null) {
            this.entry.addSymbolListSnippet(this.related);
            this.related.addSymbolSnippet(this.chart);
        }

        this.relatedDzOffers = (DzNewsRelatedOffersSnippet) this.delegate.getSnippet("nrdzo"); // $NON-NLS$
        this.relatedDzOffersStaticData = (SymbolSnippet) this.delegate.getSnippet("nrdzos"); // $NON-NLS$
        if(this.relatedDzOffers != null && this.relatedDzOffersStaticData != null) {
            this.entry.addSymbolListSnippet(this.relatedDzOffers);
            this.relatedDzOffers.addSymbolSnippet(this.relatedDzOffersStaticData);

            EventBusRegistry.get().addHandler(VisibilityUpdatedEvent.getType(), this.related);
            EventBusRegistry.get().addHandler(VisibilityUpdatedEvent.getType(), this.relatedDzOffers);
        }

    }

    @Override
    public String getPrintHtml() {
        return PrintUtil.getNewsPrintHtml(this.entry, this.related, this.chart, this.relatedDzOffers, this.relatedDzOffersStaticData);
    }
}
