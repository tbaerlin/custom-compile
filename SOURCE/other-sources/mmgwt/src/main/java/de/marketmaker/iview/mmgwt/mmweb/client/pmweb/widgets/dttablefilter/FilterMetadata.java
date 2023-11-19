/*
 * FilterMetadata.java
 *
 * Created on 11.08.2014 10:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NumberUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.DTCell;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Collection;
import java.util.List;

/**
* @author mdick
*/
@NonNLS
public class FilterMetadata<T> {
    public enum FilterType {
        NUMBER, SELECT, TEXT, DATE_TIME;
    }

    private FilterType filterType;
    private List<T> values;
    private MultiWordSuggestOracle suggestOracle;
    private int columnIndex;
    private String columnCaption;
    private String originalColumnCaption;
    private boolean percent;

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public boolean isPercent() {
        return percent;
    }

    public void setPercent(boolean percent) {
        this.percent = percent;
    }

    public FilterType getFilterType() {
        return filterType;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }

    public List<T> getValues() {
        return values;
    }

    public void setSuggestOracle(MultiWordSuggestOracle suggestOracle) {
        this.suggestOracle = suggestOracle;
    }

    public MultiWordSuggestOracle getSuggestOracle() {
        return suggestOracle;
    }

    @Override
    public String toString() {
        return "FilterMetadata{" +
                "filterType=" + filterType +
                '}';
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnCaption(String columnCaption) {
        this.columnCaption = columnCaption;
    }

    public String getColumnCaption() {
        return columnCaption;
    }

    public String getOriginalColumnCaption() {
        return originalColumnCaption;
    }

    public void setOriginalColumnCaption(String originalColumnCaption) {
        this.originalColumnCaption = originalColumnCaption;
    }
}
