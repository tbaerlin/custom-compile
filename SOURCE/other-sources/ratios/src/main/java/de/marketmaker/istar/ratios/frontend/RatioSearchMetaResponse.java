/*
 * RatioSearchResponse.java
 *
 * Created on 27.10.2005 17:21:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatioSearchMetaResponse extends AbstractIstarResponse {
    static final long serialVersionUID = -75436513125L;

    private Map<Integer, Map<String, Integer>> enumFields;

    private Map<String, Map<String, Integer>> enumGroupedBy;

    public void setEnumFields(Map<Integer, Map<String, Integer>> enumFields) {
        this.enumFields = enumFields;
    }

    public Map<Integer, Map<String, Integer>> getEnumFields() {
        return enumFields;
    }

    public Map<String, Map<String, Integer>> getEnumGroupedBy() {
        return enumGroupedBy;
    }

    public void setEnumsGroupedByLocalized(
            Map<Integer, Map<Integer, Map<String, Map<String, Integer>>>> enumsGroupedByLocalized) {
        // TODO: why does this work? enumsGroupedByLocalized.values() has no defined order, to simply
        // calling next has unpredictable results?!
        this.enumGroupedBy = enumsGroupedByLocalized.values().iterator().next().values().iterator().next();
    }
}