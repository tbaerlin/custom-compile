/*
 * ScreenerFieldImpl.java
 *
 * Created on 04.04.2007 08:39:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.screener;

import de.marketmaker.istar.instrument.data.screener.ScreenerFieldDescription;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ScreenerFieldImpl implements ScreenerField, Serializable {
    static final long serialVersionUID = 1L;

    private final int id;
    private final Object value;
    private String name;
    private String headline;
    private String shortText;
    private String longText;
    private String imageName;
    private Boolean star;

    public ScreenerFieldImpl(int id, Object value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return id;
    }

    public Object getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getShortText() {
        return shortText;
    }

    public void setShortText(String shortText) {
        this.shortText = shortText;
    }

    public String getLongText() {
        return longText;
    }

    public void setLongText(String longText) {
        this.longText = longText;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public Boolean isStar() {
        return star;
    }

    public void setStar(boolean star) {
        this.star = star;
    }

    public boolean isDecimal() {
        return ScreenerFieldDescription.TYPES[this.id] == ScreenerFieldDescription.TYPE_PRICE;
    }

    public boolean isDate() {
        return ScreenerFieldDescription.TYPES[this.id] == ScreenerFieldDescription.TYPE_DATE;
    }

    public boolean isString() {
        return ScreenerFieldDescription.TYPES[this.id] == ScreenerFieldDescription.TYPE_UCHAR;
    }

    public boolean isInteger() {
        return ScreenerFieldDescription.TYPES[this.id] == ScreenerFieldDescription.TYPE_UNUM4;
    }

    public String toString() {
        return "ScreenerFieldImpl[id=" + id
                + ", value=" + value
                + ", name=" + name
                + ", headline=" + headline
                + ", shortText=" + shortText
                + ", longText=" + longText
                + ", imageName=" + imageName
                + ", star=" + star
                + "]";
    }
}
