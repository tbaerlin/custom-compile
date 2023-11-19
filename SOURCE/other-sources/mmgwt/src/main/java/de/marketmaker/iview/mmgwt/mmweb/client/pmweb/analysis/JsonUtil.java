package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter.FilterData;
import de.marketmaker.iview.pmxml.DTSortSpec;
import de.marketmaker.iview.pmxml.Language;
import de.marketmaker.iview.pmxml.LanguageType;

/**
 * Created on 03.03.15
 * Copyright (c) vwd GmbH. All Rights Reserved.
 * @author mloesch
 */
public class JsonUtil {

    public static final String MAX_DIAGRAM_IDX = "MaxDiagramIdx"; //$NON-NLS$

    public static final String COLUMN_FILTER_DATA = "ColumnFilterData"; //$NON-NLS$

    public static final String DTTABLE_RENDERER_OPTIONS = "DTTableRenderer.Options"; //$NON-NLS$

    public static final String ANALYSIS_GUID = "AnalysisGuid"; //$NON-NLS$

    public static final String ANALYSIS_PARAMETERS = "AnalysisParameters"; //$NON-NLS$

    public static final String WITH_AGGREGATIONS = "withAggregations"; //$NON-NLS$

    public static final String CUSTOM_SORT = "customSort"; //$NON-NLS$

    public static final String ASCENDING = "ascending"; //$NON-NLS$

    public static final String COL_INDEX = "colIndex"; //$NON-NLS$

    public static final String SELECTED_LANGUAGE = "SelectedLanguage";// $NON-NLS$

    public static final String LANGUAGE_LONG = "LanguageLong";// $NON-NLS$

    public static final String LANGUAGE_SHORT = "LanguageShort";// $NON-NLS$

    public static final String LANGUAGE_TYPE = "LanguageType";// $NON-NLS$

    static Map<String, Object> fromJson(String json) {
        final JSONObject jsonObject = new JSONObject(JsonUtils.safeEval(json));
        final HashMap<String, Object> values = new HashMap<>();

        values.put(ANALYSIS_GUID, jsonObject.get(ANALYSIS_GUID).isString().stringValue());
        values.put(DTTABLE_RENDERER_OPTIONS, toOptions(jsonObject.get(DTTABLE_RENDERER_OPTIONS)));
        values.put(ANALYSIS_PARAMETERS, toAnalysisParametersOfMetadataForm(jsonObject.get(ANALYSIS_PARAMETERS)));
        values.put(COLUMN_FILTER_DATA, toFilterData(jsonObject.get(COLUMN_FILTER_DATA)));
        final JSONValue maxDiagramIdx = jsonObject.get(MAX_DIAGRAM_IDX);
        values.put(MAX_DIAGRAM_IDX, maxDiagramIdx == null
                ? null
                : Integer.valueOf(maxDiagramIdx.isString().stringValue()));

        values.put(SELECTED_LANGUAGE, toLanguage(jsonObject.get(JsonUtil.SELECTED_LANGUAGE)));

        return values;
    }

    private static Language toLanguage(JSONValue jsonValue) {
        if (jsonValue == null || jsonValue.isObject() == null) {
            return null;
        }

        final JSONObject jsonObject = jsonValue.isObject();
        final Language lang = new Language();
        lang.setLanguageLong(((JSONString) jsonObject.get(JsonUtil.LANGUAGE_LONG)).stringValue());
        lang.setLanguageShort(((JSONString) jsonObject.get(JsonUtil.LANGUAGE_SHORT)).stringValue());
        lang.setLanguageType(LanguageType.valueOf(((JSONString) jsonObject.get(JsonUtil.LANGUAGE_TYPE)).stringValue()));
        return lang;
    }

    private static Map<String, String> toAnalysisParametersOfMetadataForm(JSONValue jsonValue) {
        if (jsonValue == null || jsonValue.isObject() == null) {
            return null;
        }

        final JSONObject jsonObject = jsonValue.isObject();
        final HashMap<String, String> parameters = new HashMap<>();

        for (String key : jsonObject.keySet()) {
            final JSONValue value = jsonObject.get(key);
            if (value != null && value.isString() != null) {
                parameters.put(key, value.isString().stringValue());
            }
            else if (value == null || value.isNull() != null) {
                parameters.put(key, null);
            }
        }

        return parameters;
    }

    private static DTTableRenderer.Options toOptions(JSONValue jsonValue) {
        if (jsonValue == null || jsonValue.isObject() == null) return null;

        final JSONObject jsonObject = jsonValue.isObject();
        final DTTableRenderer.Options options = new DTTableRenderer.Options();

        final JSONValue withAggregationsValue = jsonObject.get(WITH_AGGREGATIONS);
        if (withAggregationsValue != null && withAggregationsValue.isBoolean() != null) {
            options.withAggregations(withAggregationsValue.isBoolean().booleanValue());
        }

        final JSONValue customSortListValue = jsonObject.get(CUSTOM_SORT);
        if (customSortListValue != null && customSortListValue.isArray() != null) {
            final JSONArray customSortListArray = customSortListValue.isArray();
            final List<DTSortSpec> dtSortSpecs = new ArrayList<>(customSortListArray.size());
            options.withCustomSort(dtSortSpecs);

            for (int i = 0; i < customSortListArray.size(); i++) {
                final JSONObject sortSpecJsonObject = customSortListArray.get(i).isObject();

                final DTSortSpec dtSortSpec = new DTSortSpec();
                dtSortSpec.setAscending(sortSpecJsonObject.get(ASCENDING).isBoolean().booleanValue());
                dtSortSpec.setColIndex(sortSpecJsonObject.get(COL_INDEX).isString().stringValue());

                dtSortSpecs.add(dtSortSpec);
            }
        }
        return options;
    }

    static String toJson(DTTableRenderer.Options options, Map<String, String> formParameters, String guid,
            Map<Integer, FilterData> filterData, Integer maxDiagramIdx, Language selectedLanguage) {
        final JSONObject jsonObject = new JSONObject();

        final JSONValue layoutGuid = new JSONString(guid);
        jsonObject.put(ANALYSIS_GUID, layoutGuid);

        final JSONValue optionsValue = toJsonValue(options);
        jsonObject.put(DTTABLE_RENDERER_OPTIONS, optionsValue);

        final JSONValue formParametersValue = toJsonValue(formParameters);
        jsonObject.put(ANALYSIS_PARAMETERS, formParametersValue);

        final JSONValue filterDataValue = filterDataToJson(filterData);
        jsonObject.put(COLUMN_FILTER_DATA, filterDataValue);

        jsonObject.put(MAX_DIAGRAM_IDX, (maxDiagramIdx == null
                ? null
                : new JSONString(String.valueOf(maxDiagramIdx))));

        jsonObject.put(SELECTED_LANGUAGE, toJsonValue(selectedLanguage));

        return jsonObject.toString();
    }

    private static JSONValue toJsonValue(Language language) {
        Firebug.warn("<JsonUtil.toJsonValue> language = " + (language != null ? language.getLanguageLong() : "null"));
        if (language == null) {
            return null;
        }
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(LANGUAGE_LONG, new JSONString(language.getLanguageLong()));
        jsonObject.put(LANGUAGE_SHORT, new JSONString(language.getLanguageShort()));
        jsonObject.put(LANGUAGE_TYPE, new JSONString(language.getLanguageType().name()));
        return jsonObject;
    }

    private static JSONValue toJsonValue(Map<String, String> formParameters) {
        if (formParameters == null) return JSONNull.getInstance();

        final JSONObject jsonObject = new JSONObject();

        for (Map.Entry<String, String> entry : formParameters.entrySet()) {
            final String value = entry.getValue();
            if (value != null) {
                jsonObject.put(entry.getKey(), new JSONString(value));
            }
            else {
                jsonObject.put(entry.getKey(), JSONNull.getInstance());
            }
        }

        return jsonObject;
    }

    private static JSONValue toJsonValue(DTTableRenderer.Options options) {
        if (options == null) return JSONNull.getInstance();

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(WITH_AGGREGATIONS, JSONBoolean.getInstance(options.isWithAggregations()));

        if (options.isWithCustomSort()) {
            final JSONArray customSortValue = new JSONArray();
            jsonObject.put(CUSTOM_SORT, customSortValue);

            for (DTSortSpec dtSortSpec : options.getCustomSort()) {
                final JSONObject dtSortSpecObject = new JSONObject();
                dtSortSpecObject.put(ASCENDING, JSONBoolean.getInstance(dtSortSpec.isAscending()));
                dtSortSpecObject.put(COL_INDEX, new JSONString(dtSortSpec.getColIndex()));
                customSortValue.set(customSortValue.size(), dtSortSpecObject);
            }
        }

        return jsonObject;
    }

    private static JSONValue filterDataToJson(Map<Integer, FilterData> filterData) {
        if (filterData == null || filterData.isEmpty()) {
            return JSONNull.getInstance();
        }
        final JSONObject jsonObject = new JSONObject();
        for (Map.Entry<Integer, FilterData> entry : filterData.entrySet()) {
            jsonObject.put(Integer.toString(entry.getKey()), entry.getValue().toJSONValue());
        }

        return jsonObject;
    }

    private static Map<Integer, FilterData> toFilterData(JSONValue jsonValue) {
        if (jsonValue == null || jsonValue.isObject() == null) {
            return null;
        }

        final JSONObject jsonObject = jsonValue.isObject();
        final HashMap<Integer, FilterData> filterData = new HashMap<>();

        for (String key : jsonObject.keySet()) {
            filterData.put(Integer.decode(key), FilterData.fromJSONValue(jsonObject.get(key)));
        }

        return filterData;
    }
}