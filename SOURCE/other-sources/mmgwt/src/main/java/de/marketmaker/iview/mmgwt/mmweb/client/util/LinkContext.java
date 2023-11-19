/*
 * LinkContext.java
 *
 * Created on 04.06.2008 16:06:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Event;

import de.marketmaker.itools.gwtutil.client.event.KeyModifiers;
import de.marketmaker.iview.dmxml.NWSSearchElement;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiMenu;
import de.marketmaker.iview.mmgwt.mmweb.client.Token;

/**
 * Contains context information for links clicked on by a user: the listener that handles
 * a click on the link and the associated link data that can be used to handle the click
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LinkContext<D> {
    public static final LinkListener<NWSSearchElement> NEWS_LISTENER = new LinkListener<NWSSearchElement>() {
        public void onClick(LinkContext<NWSSearchElement> context, Element e) {
            PlaceUtil.goTo(getToken(context.data));
        }
    };

    private static String getToken(NWSSearchElement se) {
        return "N_D" + StringUtil.TOKEN_DIVIDER + "newsid=" + se.getNewsid(); // $NON-NLS$
    }

    private static final LinkListener<QuoteWithInstrument> PIN_LISTENER = new LinkListener<QuoteWithInstrument>() {
        public void onClick(LinkContext<QuoteWithInstrument> context, Element e) {
//            InstrumentWorkspace.INSTANCE.add((QuoteWithInstrument) context.data);
            QwiMenu.INSTANCE.show(context.data, e);
        }
    };


    private static final LinkListener<QuoteWithInstrument> QWI_LISTENER = new LinkListener<QuoteWithInstrument>() {
        public void onClick(LinkContext<QuoteWithInstrument> context, Element e) {
            context.data.goToPortrait(context.data.getHistoryContext());
        }
    };

    public static final LinkListener<QuoteWithInstrument> QWI_CHART_LISTENER = new LinkListener<QuoteWithInstrument>() {
        public void onClick(LinkContext<QuoteWithInstrument> context, Element e) {
            context.data.goToChartcenter(context.data.getHistoryContext());
        }
    };

    private static final LinkListener<Token> HISTORY_TOKEN_LISTENER = new LinkListener<Token>() {
        public void onClick(LinkContext<Token> context, Element e) {
            PlaceUtil.goTo(context.data.getToken(), context.data.getHistoryContext());
        }
    };

    public static LinkContext<NWSSearchElement> newsLink(NWSSearchElement se,
            LinkListener<NWSSearchElement> listener) {
        return new LinkContext<>(listener == null ? NEWS_LISTENER : listener, se, getToken(se));
    }

    public static LinkContext<QuoteWithInstrument> pin(QuoteWithInstrument qwi) {
        return new LinkContext<>(PIN_LISTENER, qwi, qwi.getToken(null));
    }

    public static LinkContext<QuoteWithInstrument> quoteLink(QuoteWithInstrument qwi) {
        return new LinkContext<>(QWI_LISTENER, qwi, qwi.getTokenPortrait());
    }

    public static LinkContext<Token> historyTokenLink(Token token) {
        return new LinkContext<>(HISTORY_TOKEN_LISTENER, token);
    }

    public final LinkListener<D> listener;

    public final D data;

    public final String token;

    private Event currentEvent; // in order to get the current event to check status

    public LinkContext(LinkListener<D> listener, D data) {
        this(listener, data, null);
    }

    public LinkContext(LinkListener<D> listener, D data, String token) {
        this.listener = listener;
        this.data = data;
        this.token = token;
    }

    public void onClick(Element e, Event event) {
        this.currentEvent = event;
        this.listener.onClick(this, e);
        this.currentEvent = null; // remove reference to event
    }

    public boolean isCtrlKey() {
        return null != this.currentEvent && KeyModifiers.isCtrl(this.currentEvent);
    }

    public String getToken() {
        return token;
    }

    public D getData() {
        return data;
    }
}
