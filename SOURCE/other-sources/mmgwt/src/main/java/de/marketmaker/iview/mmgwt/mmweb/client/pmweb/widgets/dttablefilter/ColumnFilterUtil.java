/*
 * ColumnFilterUtil.java
 *
 * Created on 17.03.2015 09:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableUtils;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.DateTimeRange;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.NumberRange;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DTCell;
import de.marketmaker.iview.pmxml.DTColumnSpec;
import de.marketmaker.iview.pmxml.HasCode;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mdick
 */
@NonNLS
public final class ColumnFilterUtil {
    private ColumnFilterUtil() {
        //nothing to do
    }

    public static List<FilterMetadata<DTCell>> buildFilterMetadataFromColumnSpecs(List<DTColumnSpec> columnSpecs) {
        final long start = System.currentTimeMillis();

        final int colSpecSize = columnSpecs.size();

        final List<FilterMetadata<DTCell>> filterMetadata = new ArrayList<>(colSpecSize);

        for (int i = 0; i < columnSpecs.size(); i++) {
            final DTColumnSpec columnSpec = columnSpecs.get(i);
            final ParsedTypeInfo typeInfo = columnSpec.getTypeInfo();
            final TiType typeId = typeInfo.getTypeId();

            final FilterMetadata<DTCell> metadata = new FilterMetadata<>();
            metadata.setColumnIndex(i);
            metadata.setColumnCaption(DTTableUtils.stripOffBlanksAndLineBreaks(columnSpec.getCaption()));
            metadata.setOriginalColumnCaption(columnSpec.getCaption());
            metadata.setFilterType(getFilterType(columnSpec));
            metadata.setPercent(typeId == TiType.TI_NUMBER && typeInfo.isNumberProcent());

            //TODO: refactor the complete filter chain stuff to use MM instead of DTCell?
            final ArrayList<DTCell> fakeCells = new ArrayList<>();
            int order = 0;
            if(typeId == TiType.TI_ENUMERATION) {
                for (MM mm : typeInfo.getEnumElements()) {
                    if(mm instanceof HasCode) {
                        final DTCell dtCell = new DTCell();
                        dtCell.setContent(((HasCode) mm).getValue());
                        dtCell.setItem(mm);
                        dtCell.setOrder(Integer.toString(order++));
                        fakeCells.add(dtCell);
                    }
                }
            }
            metadata.setValues(fakeCells);

            filterMetadata.add(metadata);
        }

        final long end = System.currentTimeMillis();
        Firebug.debug("<ColumnFilterUtil.buildFilterMetaData> took: " + (end - start) + "ms " + filterMetadata);
        return filterMetadata;
    }

    private static FilterMetadata.FilterType getFilterType(DTColumnSpec spec) {
        switch (spec.getTypeInfo().getTypeId()) {
            case TI_NUMBER:
                return FilterMetadata.FilterType.NUMBER;
            case TI_DATE:
                return FilterMetadata.FilterType.DATE_TIME;
            case TI_ENUMERATION:
                return FilterMetadata.FilterType.SELECT;
            default:
                return FilterMetadata.FilterType.TEXT;
        }
    }

    static AbstractFilterPanel<?, DTCell> createFilterPanel(FilterMetadata<DTCell> metadata) {
        switch (metadata.getFilterType()) {
            case SELECT:
                return new SelectFilterPanel();
            case NUMBER:
                return new NumberRangeFilterPanel();
            case DATE_TIME:
                return new DateTimeRangeFilterPanel();
            case TEXT:
                return new TextFilterPanel();
            default:
                throw new IllegalArgumentException("ColumnFilterPanel: Filter type not supported!");
        }
    }

    public static JSONValue toJSONValue(FilterMetadata.FilterType filterType, Object value) {
        if(value == null) {
            return JSONNull.getInstance();
        }

        switch (filterType) {
            case SELECT:
                return new JSONString((String)value);
            case TEXT:
                 return getJsonValue((TextExpression)value);
            case NUMBER:
                return getJsonValue((NumberRange) value);
            case DATE_TIME:
                return getJsonValue((DateTimeRange) value);
            default:
                throw new IllegalArgumentException("toJSONValue: Filter type not supported!");
        }
    }

    public static JSONValue getJsonValue(TextExpression value) {
        final JSONObject jsonObject = new JSONObject();
        final JSONValue jsonText = new JSONString(value.getText());
        final JSONValue jsonExactMatch = JSONBoolean.getInstance(value.isExactMatch());
        jsonObject.put("text", jsonText);
        jsonObject.put("exactMatch", jsonExactMatch);
        return jsonObject;
    }

    public static JSONValue getJsonValue(NumberRange value) {
        final JSONObject jsonObject = new JSONObject();
        final JSONValue jsonMinValue = value.hasMin()
                ? new JSONString(value.getMin().toPlainString())
                : JSONNull.getInstance();
        final JSONValue jsonMaxValue = value.hasMax()
                ? new JSONString(value.getMax().toPlainString())
                : JSONNull.getInstance();
        jsonObject.put("min", jsonMinValue);
        jsonObject.put("max", jsonMaxValue);
        return jsonObject;
    }

    public static JSONValue getJsonValue(DateTimeRange value) {
        final JSONObject jsonObject = new JSONObject();
        final JSONValue jsonBeginValue = value.hasBegin()
                ? new JSONString(JsDateFormatter.formatIso8601(value.getBegin()))
                : JSONNull.getInstance();
        final JSONValue jsonEndValue = value.hasEnd()
                ? new JSONString(JsDateFormatter.formatIso8601(value.getEnd()))
                : JSONNull.getInstance();
        jsonObject.put("begin", jsonBeginValue);
        jsonObject.put("end", jsonEndValue);
        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    public static <T> T fromJSONValue(FilterMetadata.FilterType filterType, JSONValue jsonValue) {
        if (jsonValue == null || jsonValue.isNull() != null) {
            return null;
        }

        switch (filterType) {
            case SELECT:
                final JSONString jsonString = jsonValue.isString();
                return jsonString != null ? (T)jsonString.stringValue() : null;
            case TEXT:
                return (T)textExpressionFromJSONValue(jsonValue);
            case NUMBER:
                return (T)numberRangeFromJSONValue(jsonValue);
            case DATE_TIME:
                return (T)dateTimeRangeFromJSONValue(jsonValue);
            default:
                throw new IllegalArgumentException("fromJSONValue: Filter type not supported!");
        }
    }

    private static TextExpression textExpressionFromJSONValue(JSONValue jsonValue) {
        final JSONObject jsonTextExpression = jsonValue.isObject();
        if(jsonTextExpression == null) {
            return null;
        }

        final JSONString jsonText = jsonTextExpression.get("text").isString();
        final String text = jsonText != null ? jsonText.stringValue() : null;

        final JSONBoolean jsonExactMatch = jsonTextExpression.get("exactMatch").isBoolean();
        final boolean exactMatch = jsonExactMatch != null && jsonExactMatch.booleanValue();

        return new TextExpression(text, exactMatch);
    }

    private static NumberRange numberRangeFromJSONValue(JSONValue jsonValue) {
        final JSONObject jsonNumberRange = jsonValue.isObject();
        if(jsonNumberRange == null) {
            return null;
        }

        final JSONString jsonMinValue = jsonNumberRange.get("min").isString();
        final BigDecimal min = jsonMinValue != null && StringUtil.hasText(jsonMinValue.stringValue())
                ? new BigDecimal(jsonMinValue.stringValue()) : null;

        final JSONString jsonMaxValue = jsonNumberRange.get("max").isString();
        final BigDecimal max = jsonMaxValue != null && StringUtil.hasText(jsonMaxValue.stringValue())
                ? new BigDecimal(jsonMaxValue.stringValue()) : null;

        return new NumberRange(min, max);
    }

    private static DateTimeRange dateTimeRangeFromJSONValue(JSONValue jsonValue) {
        final JSONObject jsonNumberRange = jsonValue.isObject();
        if(jsonNumberRange == null) {
            return null;
        }

        final JSONString jsonBeginValue = jsonNumberRange.get("begin").isString();
        final MmJsDate begin = jsonBeginValue != null && StringUtil.hasText(jsonBeginValue.stringValue())
                ? JsDateFormatter.parseDdmmyyyy(jsonBeginValue.stringValue()) : null;

        final JSONString jsonEndValue = jsonNumberRange.get("end").isString();
        final MmJsDate end = jsonEndValue != null && StringUtil.hasText(jsonEndValue.stringValue())
                ? JsDateFormatter.parseDdmmyyyy(jsonEndValue.stringValue()) : null;

        return new DateTimeRange(begin, end);
    }

    public static DTTableRenderer.ColumnFilter createColumnFilter(List<? extends HasCreateColumnFilter> hasCreateColumnFilters) {
        final ArrayList<DTTableRenderer.ColumnFilter> filters = new ArrayList<>();
        for (HasCreateColumnFilter hasCreateColumnFilter : hasCreateColumnFilters) {
            final DTTableRenderer.ColumnFilter columnFilter = hasCreateColumnFilter.createColumnFilter();
            if(columnFilter != null) {
                filters.add(columnFilter);
            }
        }
        if(filters.isEmpty()) {
            return null;
        }
        return new DTTableRenderer.ExclusiveMultiColumnFilter(filters);
    }
}
