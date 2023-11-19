/*
 * EconodaySearchRequest.java
 *
 * Created on 30.03.12 13:31
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import de.marketmaker.istar.merger.provider.IstarQueryListRequest;

/**
 * @author zzhao
 */
public class EconodaySearchRequest extends IstarQueryListRequest {
    static final long serialVersionUID = -3929360815173611758L;

    private final String[] countries;

    private final String[] eventCodes;

    private final String from;

    private final String to;

    private EconodaySearchRequest(int offset, int count, String sortBy, boolean ascending,
            String query, String[] countries, String[] eventCodes, String from, String to) {
        super(offset, count, sortBy, ascending, query);
        this.countries = countries;
        this.eventCodes = eventCodes;
        this.from = from;
        this.to = to;
    }

    public static EconodaySearchRequest byQuery(int offset, int count, String sortBy,
            boolean ascending, String query) {
        return new EconodaySearchRequest(offset, count, sortBy, ascending, query, null, null, null, null);
    }

    public static EconodaySearchRequest byParameter(int offset, int count, String sortBy,
            boolean ascending, String[] countries, String[] eventCodes, String from, String to) {
        return new EconodaySearchRequest(offset, count, sortBy, ascending, null, countries, eventCodes, from, to);
    }

    public String[] getCountries() {
        return countries;
    }

    public String[] getEventCodes() {
        return eventCodes;
    }

    public String getFrom() {
        return from;
    }


    public String getTo() {
        return to;
    }
}
