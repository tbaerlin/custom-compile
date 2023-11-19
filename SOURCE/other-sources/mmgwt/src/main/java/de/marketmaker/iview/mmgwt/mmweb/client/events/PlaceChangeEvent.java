/*
 * GoToPageEvent.java
 *
 * Created on 04.12.2009 09:36:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.history.NullContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.HashMap;

/**
 * Fire this event to go to a different place in the application. Better than using
 * {@link com.google.gwt.user.client.History}, which cannot be used in test cases. Also, you can
 * associate properties with this event. The properties will not appear as history tokens, but
 * can be queried by controllers to initialize certain request parameters.
 *
 * @author oflege
 */
@SuppressWarnings("Convert2Diamond")
public class PlaceChangeEvent extends GwtEvent<PlaceChangeHandler> {
    private static final HistoryContext NULL_CONTEXT = NullContext.getInstance();
    private static final String[] NULL_TOKENS = new String[0];

    private static int autoIncId = 0;

    public static String[] split(String token) {
        // splitToken's result length will always be > 0, but tmp[0] will be null for an empty token
        final String[] tmp = StringUtil.splitToken(token);
        return (tmp[0] != null) ? tmp : NULL_TOKENS;
    }

    private static Type<PlaceChangeHandler> TYPE;

    private final HistoryToken historyToken;

    private HashMap<String, String> properties;

    private final HistoryContext context;
    private final boolean explicitHistoryNullContext;

    public static Type<PlaceChangeHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<PlaceChangeHandler>();
        }
        return TYPE;
    }

    public PlaceChangeEvent(PlaceChangeEvent existingEvent, String newTokens) {
        this(existingEvent, HistoryToken.Builder.fromToken(newTokens).build());
    }

    public PlaceChangeEvent(PlaceChangeEvent existingEvent, HistoryToken historyToken) {
        this(historyToken, existingEvent.getHistoryContext());
        if (existingEvent.properties != null) {
            this.properties = new HashMap<String, String>();
            this.properties.putAll(existingEvent.properties);
        }
    }

    public PlaceChangeEvent(String token) {
        this(token, AbstractMainController.INSTANCE == null // this can occur in idoc
                ? null
                : HistoryContext.Util.getWithoutBreadcrumb());
    }

    public PlaceChangeEvent(String token, HistoryContext context) {
        this(HistoryToken.Builder.fromToken(token).build(), context);
    }

    public PlaceChangeEvent(HistoryToken historyToken) {
        this(historyToken, AbstractMainController.INSTANCE == null // this can occur in idoc
                ? null
                : HistoryContext.Util.getWithoutBreadcrumb());
    }

    public PlaceChangeEvent(HistoryToken historyToken, HistoryContext context) {
        if (historyToken.getHistoryId() == null) {
            this.historyToken = HistoryToken.Builder.fromHistoryToken(historyToken)
                    .withHistoryId(getAndIncAutoId()).build();
        }
        else {
            this.historyToken = historyToken;
        }
        this.explicitHistoryNullContext = context == NULL_CONTEXT;
        this.context = this.explicitHistoryNullContext ? null : context;
    }

    public PlaceChangeEvent withProperty(String key, String value) {
        if (value != null) {
            if (this.properties == null) {
                this.properties = new HashMap<String, String>();
            }
            this.properties.put(key, value);
        }
        return this;
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
        if (this.properties == null || !this.properties.containsKey(key)) {
            return defaultValue;
        }
        return this.properties.get(key);
    }

    public Type<PlaceChangeHandler> getAssociatedType() {
        return getType();
    }

    protected void dispatch(PlaceChangeHandler handler) {
        handler.onPlaceChange(this);
    }

    public HistoryToken getHistoryToken() {
        return this.historyToken;
    }

    private static int getAndIncAutoId() {
        return autoIncId++;
    }

    public HistoryContext getHistoryContext() {
        return context;
    }

    public boolean isExplicitHistoryNullContext() {
        return this.explicitHistoryNullContext;
    }

    public static int getLastAutoIncId() {
        return autoIncId;
    }

    @Override
    @NonNLS
    public String toDebugString() {
        return super.toDebugString()
                + " token: " + this.historyToken  // $NON-NLS$
                + " token.hid: " + (this.historyToken != null ? this.historyToken.getHistoryId() : "null")  // $NON-NLS$
                + " properties: " + this.properties  // $NON-NLS$
                + " explicitHistoryNullContext? " + this.explicitHistoryNullContext;  // $NON-NLS$
    }
}