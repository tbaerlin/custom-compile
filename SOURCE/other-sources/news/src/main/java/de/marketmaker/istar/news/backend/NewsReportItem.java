/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.backend;

import java.beans.PropertyEditorSupport;
import java.util.Collections;
import java.util.List;

/**
 * Configuration bean for news agencies or suppliers of {@link NewsReport}.
 */
public class NewsReportItem {

    public enum Option {
        USE_COMBINED_KEY,
        NAME_IS_PREFIX;
    }

    public static class OptionEditor extends PropertyEditorSupport {

        public void setAsText(String text) {
            setValue(Option.valueOf(text));
        }
    }

    private String displayName;

    private String name;

    private String field;

    private Option option;

    private List<String> keys = Collections.emptyList();

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }
}