/*
 * PlaceUtil.java
 *
 * Created on 26.05.2008 09:52:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceQueryEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil.TOKEN_DIVIDER;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PlaceUtil {
    private static final String DEFAULT_PORTRAIT_VIEW = "U"; // $NON-NLS-0$
    public static final String UNDEFINDED_PORTRAIT_VIEW = "UNDEF"; // $NON-NLS-0$
    private static final String CHARTCENTER_PORTRAIT_VIEW = "U"; // $NON-NLS-0$

    public static void goToPortrait(InstrumentData instrumentData) {
        goToPortrait(instrumentData, DEFAULT_PORTRAIT_VIEW);
    }

    public static void goToChartcenter(InstrumentData instrumentData, QuoteData quoteData) {
        goTo(getPortraitPlace(instrumentData, quoteData, CHARTCENTER_PORTRAIT_VIEW));
    }

    public static void goToPortrait(final InstrumentData instrumentData, final QuoteData quoteData) {
        goTo(getPortraitPlace(instrumentData, quoteData, DEFAULT_PORTRAIT_VIEW), true);
    }

    public static void goToPortrait(final InstrumentData instrumentData, final QuoteData quoteData, HistoryContext historyContext) {
        goTo(getPortraitPlace(instrumentData, quoteData, DEFAULT_PORTRAIT_VIEW), historyContext);
    }

    //TODO: This method name sucks! Find a better one...
    public static void goToPortraitUndefView(final InstrumentData instrumentData, final QuoteData quoteData, HistoryContext historyContext) {
        goTo(getPortraitPlace(instrumentData, quoteData, UNDEFINDED_PORTRAIT_VIEW), historyContext);
    }

    public static void goToPortrait(final InstrumentData instrumentData, String view) {
        goTo(getPortraitPlace(instrumentData, null, view));
    }

    public static void changeQuoteInView(final QuoteData quoteData) {
        final String p = PlaceQueryEvent.getCurrentPlace();
        final String[] tokens = StringUtil.splitToken(p);
        goTo(StringUtil.joinTokens(tokens[0], quoteData.getQid(),
                tokens.length > 2 ? tokens[2] : DEFAULT_PORTRAIT_VIEW), true);
    }

    public static String getPortraitPlace(QuoteWithInstrument qwi, String view) {
        return getPortraitPlace(qwi.getInstrumentData(), qwi.getQuoteData(), view);
    }

    public static String getPortraitPlace(InstrumentData id, QuoteData qd, String view) {
        final String idToken = (qd != null ? qd.getQid() : id.getIid());
        return getPortraitPlace(id.getType(), idToken, view);
    }

    public static String getPortraitPlace(String type, String id, String view) {
        final String viewToken = view != null ? view : DEFAULT_PORTRAIT_VIEW;
        return "P_" + type + TOKEN_DIVIDER + id + TOKEN_DIVIDER + viewToken; // $NON-NLS-0$
    }

    public static PlaceChangeEvent createEvent(String place) {
        assert AbstractMainController.INSTANCE.getHistoryThreadManager() != null : "HistoryThreadManager needed";
        assert AbstractMainController.INSTANCE.getHistoryThreadManager().getActiveThreadHistoryItem() != null : "ActiveHistoryItem needed";
        assert AbstractMainController.INSTANCE.getHistoryThreadManager().getActiveThreadHistoryItem().getPlaceChangeEvent() != null : "PlaceChangeEvent needed";

        return new PlaceChangeEvent(place);
    }

    public static void goTo(String place) {
        goTo(place, null);
    }

    public static void goTo(String place, boolean useCurrentContext) {
        if (useCurrentContext) {
            fire(createEvent(place));
        }
        else {
            fire(new PlaceChangeEvent(place));
        }
    }

    public static void goTo(String place, HistoryContext context) {
        if (context != null) {
            fire(new PlaceChangeEvent(place, context));
        }
        else {
            fire(createEvent(place));
        }
    }

    public static void fire(PlaceChangeEvent event) {
        EventBusRegistry.get().fireEvent(event);
    }
}
