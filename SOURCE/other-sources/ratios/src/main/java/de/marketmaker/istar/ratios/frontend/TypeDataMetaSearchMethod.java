/*
 * TypeMetaDataSearchMethod.java
 *
 * Created on 18.02.15 09:07
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import de.marketmaker.istar.ratios.RatioFieldDescription;

import static de.marketmaker.istar.ratios.RatioFieldDescription.getFieldById;
import static de.marketmaker.istar.ratios.RatioFieldDescription.isInstrumentField;

/**
 * @author oflege
 */
public class TypeDataMetaSearchMethod {

    private final TypeData typeData;

    private final int groupByFieldid;

    private final int selectFieldid;

    public TypeDataMetaSearchMethod(TypeData typeData, RatioSearchMetaRequest req) {
        this.typeData = typeData;
        this.groupByFieldid = req.getGroupByFieldid();
        this.selectFieldid = req.getSelectFieldid();
    }

    RatioSearchMetaResponse invoke() {
        final RatioSearchMetaResponse response = new RatioSearchMetaResponse();
        if (this.groupByFieldid > 0) {
            response.setEnumsGroupedByLocalized(getGroupByEnumMap());
        }
        return response;
    }

    private Map<Integer, Map<Integer, Map<String, Map<String, Integer>>>> getGroupByEnumMap() {
        final RatioFieldDescription.Field selectField = getFieldById(selectFieldid);

        if (selectField.isLocalized()) {
            final Map<Integer, Map<Integer, Map<String, Map<String, Integer>>>> map = new HashMap<>();
            for (int i = 0; i < selectField.getLocales().length; i++) {
                map.put(i, getEnumGroupedBy(i));
            }
            return map;
        }
        return Collections.singletonMap(-1, getEnumGroupedBy(-1));
    }

    private Map<Integer, Map<String, Map<String, Integer>>> getEnumGroupedBy(int selectLocaleIndex) {
        final RatioFieldDescription.Field groupByField = getFieldById(groupByFieldid);

        if (groupByField.isLocalized()) {
            Map<Integer, Map<String, Map<String, Integer>>> map = new HashMap<>();
            for (int i = 0; i < groupByField.getLocales().length; i++) {
                map.put(i, getEnumGroupedBy(selectLocaleIndex, i));
            }
            return map;
        }
        return Collections.singletonMap(-1, getEnumGroupedBy(selectLocaleIndex, -1));
    }

    private Map<String, Map<String, Integer>> getEnumGroupedBy(int localeIndex,
            int groupByLocaleIndex) {
        final boolean isInstrumentField = isInstrumentField(selectFieldid);
        final boolean isInstrumentGroupByField = isInstrumentField(groupByFieldid);

        final Map<String, Object2IntMap<String>> tmp = new HashMap<>();

        for (final RatioData rd : this.typeData.getRatioDatas()) {
            if (isInstrumentGroupByField) {
                getEnumGroupBy(rd, rd.getInstrumentRatios(), groupByLocaleIndex,
                        tmp, isInstrumentField, localeIndex);
            }
            else {
                for (final QuoteRatios qdr : rd.getQuoteRatios()) {
                    getEnumGroupBy(rd, qdr, groupByLocaleIndex, tmp,
                            isInstrumentField, localeIndex);
                }
            }
        }

        final HashMap<String, Map<String, Integer>> result = new HashMap<>();
        for (String s: new ArrayList<>(tmp.keySet())) {
            result.put(s, new HashMap<>(tmp.remove(s)));
        }
        return result;
    }

    private void getEnumGroupBy(RatioData rd, Selectable dr,
            int groupByLocaleIndex, Map<String, Object2IntMap<String>> result,
            boolean instrumentField, int localeIndex) {

        final String groupBy = dr.getString(this.groupByFieldid, groupByLocaleIndex);
        if (groupBy == null) {
            return;
        }

        Object2IntMap<String> map = result.get(groupBy);
        if (map == null) {
            result.put(groupBy, map = new Object2IntOpenHashMap<>());
        }

        if (instrumentField) {
            getEnum(selectFieldid, localeIndex, rd.getInstrumentRatios(), map);
        }
        else {
            for (final QuoteRatios qdr : rd.getQuoteRatios()) {
                getEnum(selectFieldid, localeIndex, qdr, map);
            }
        }
    }

    private void getEnum(int fieldid, int localeIndex, Selectable record, Object2IntMap<String> counts) {
        final String s = getEnumAsString(fieldid, localeIndex, record);
        if (s != null) {
            counts.put(s, counts.getInt(s) + 1);
        }
    }

    private String getEnumAsString(int fieldid, int localeIndex, Selectable record) {
        final RatioFieldDescription.Field field = getFieldById(fieldid);
        if (field.type() == RatioFieldDescription.Type.STRING) {
            return record.getString(fieldid, localeIndex);
        }
        if (field.type() == RatioFieldDescription.Type.NUMBER) {
            final Long l = record.getLong(fieldid);
            return (l != null) ? Long.toString(l) : null;
        }
        return null;
    }
}
