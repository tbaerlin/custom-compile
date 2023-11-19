/*
 * FilterData.java
 *
 * Created on 23.03.2015 09:42
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mdick
 */
@NonNLS
public class FilterData<T> {
    private int metadataColumnIndex;
    private String originalColumnCaption;
    private FilterMetadata.FilterType filterType;

    private boolean editorValueEnabled;
    private List<Boolean> valuesEnabled;

    private T editorValue;
    private List<T> values;

    public JSONValue toJSONValue() {
        final JSONObject jsonObject = new JSONObject();

        jsonObject.put("metadata_columnIndex", new JSONString(Integer.toString(this.metadataColumnIndex)));
        jsonObject.put("metadata_originalColumnCaption", new JSONString(this.originalColumnCaption));
        jsonObject.put("metadata_filterType", new JSONString(this.filterType.name()));
        jsonObject.put("values_size", new JSONString(Integer.toString(this.values.size())));
        jsonObject.put("valuesEnabled_size", new JSONString(Integer.toString(this.valuesEnabled.size())));

        jsonObject.put("editorValueEnabled", JSONBoolean.getInstance(this.editorValueEnabled));

        final JSONArray jsonEnabled = new JSONArray();
        for (int i = 0; i < this.valuesEnabled.size(); i++) {
            jsonEnabled.set(i, JSONBoolean.getInstance(this.valuesEnabled.get(i)));
        }
        jsonObject.put("valuesEnabled", jsonEnabled);

        jsonObject.put("editorValue", ColumnFilterUtil.toJSONValue(this.filterType, this.editorValue));

        final JSONArray jsonValues = new JSONArray();
        for (int i = 0; i < this.values.size(); i++) {
            jsonValues.set(i, ColumnFilterUtil.toJSONValue(this.filterType, this.values.get(i)));
        }
        jsonObject.put("values", jsonValues);

        return jsonObject;
    };

    public static <T> FilterData<T> fromJSONValue(JSONValue jsonValue) {
        if(jsonValue == null || jsonValue.isObject() == null) {
            return null;
        }
        final JSONObject jsonObject = jsonValue.isObject();
        final FilterData<T> data = new FilterData<>();

        data.metadataColumnIndex = Integer.parseInt(getString(jsonObject, "metadata_columnIndex"));
        data.originalColumnCaption = getString(jsonObject, "metadata_originalColumnCaption");
        data.filterType = FilterMetadata.FilterType.valueOf(getString(jsonObject, "metadata_filterType"));
        final int valuesSize = Integer.parseInt(getString(jsonObject, "values_size"));
        final int valuesEnabledSize = Integer.parseInt(getString(jsonObject, "valuesEnabled_size"));

        data.editorValueEnabled = jsonObject.get("editorValueEnabled").isBoolean().booleanValue();

        final JSONArray jsonValuesEnabled = jsonObject.get("valuesEnabled").isArray();
        data.valuesEnabled = new ArrayList<>(valuesEnabledSize);
        for (int i = 0; i < jsonValuesEnabled.size(); i++) {
            data.valuesEnabled.add(jsonValuesEnabled.get(i).isBoolean().booleanValue());
        }

        data.editorValue = ColumnFilterUtil.fromJSONValue(data.filterType, jsonObject.get("editorValue"));

        final JSONArray jsonValues = jsonObject.get("values").isArray();
        data.values = new ArrayList<>(valuesSize);
        for (int i = 0; i < jsonValues.size(); i++) {
            final T value = ColumnFilterUtil.fromJSONValue(data.filterType, jsonValues.get(i));
            data.values.add(value);
        }

        return data;
    }

    public static String getString(JSONObject jsonObject, String key) {
        final JSONValue jsonValue = jsonObject.get(key);
        if(jsonValue == null || jsonValue.isString() == null) {
            return null;
        }
        return jsonValue.isString().stringValue();
    }

    public int getMetadataColumnIndex() {
        return metadataColumnIndex;
    }

    public void setMetadataColumnIndex(int metadataColumnIndex) {
        this.metadataColumnIndex = metadataColumnIndex;
    }

    public String getOriginalColumnCaption() {
        return originalColumnCaption;
    }

    public void setOriginalColumnCaption(String originalColumnCaption) {
        this.originalColumnCaption = originalColumnCaption;
    }

    public FilterMetadata.FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterMetadata.FilterType filterType) {
        this.filterType = filterType;
    }

    public boolean isEditorValueEnabled() {
        return editorValueEnabled;
    }

    public void setEditorValueEnabled(boolean editorValueEnabled) {
        this.editorValueEnabled = editorValueEnabled;
    }

    public List<Boolean> getValuesEnabled() {
        return valuesEnabled;
    }

    public void setValuesEnabled(List<Boolean> valuesEnabled) {
        this.valuesEnabled = valuesEnabled;
    }

    public T getEditorValue() {
        return editorValue;
    }

    public void setEditorValue(T editorValue) {
        this.editorValue = editorValue;
    }

    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilterData)) return false;

        final FilterData that = (FilterData) o;

        if (editorValueEnabled != that.editorValueEnabled) return false;
        if (metadataColumnIndex != that.metadataColumnIndex) return false;
        if (editorValue != null ? !editorValue.equals(that.editorValue) : that.editorValue != null) return false;
        if (filterType != that.filterType) return false;
        if (originalColumnCaption != null ? !originalColumnCaption.equals(that.originalColumnCaption) : that.originalColumnCaption != null)
            return false;
        if (values != null ? !values.equals(that.values) : that.values != null) return false;
        if (valuesEnabled != null ? !valuesEnabled.equals(that.valuesEnabled) : that.valuesEnabled != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = metadataColumnIndex;
        result = 31 * result + (originalColumnCaption != null ? originalColumnCaption.hashCode() : 0);
        result = 31 * result + (filterType != null ? filterType.hashCode() : 0);
        result = 31 * result + (editorValueEnabled ? 1 : 0);
        result = 31 * result + (valuesEnabled != null ? valuesEnabled.hashCode() : 0);
        result = 31 * result + (editorValue != null ? editorValue.hashCode() : 0);
        result = 31 * result + (values != null ? values.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FilterData{" +
                "metadataColumnIndex=" + metadataColumnIndex +
                ", originalColumnCaption='" + originalColumnCaption + '\'' +
                ", filterType=" + filterType +
                ", editorValueEnabled=" + editorValueEnabled +
                ", valuesEnabled=" + valuesEnabled +
                ", editorValue=" + editorValue +
                ", values=" + values +
                '}';
    }
}
