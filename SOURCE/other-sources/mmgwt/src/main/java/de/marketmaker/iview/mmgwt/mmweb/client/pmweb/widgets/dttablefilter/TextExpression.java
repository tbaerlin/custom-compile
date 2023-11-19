/*
 * TextExpression.java
 *
 * Created on 31.03.2015 13:11
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
@NonNLS
public class TextExpression {
    public final String text;
    public final boolean exactMatch;

    public TextExpression(String text, boolean compareSubString) {
        this.text = text;
        this.exactMatch = compareSubString;
    }

    public String getText() {
        return this.text;
    }

    public boolean isExactMatch() {
        return this.exactMatch;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TextExpression)) return false;

        final TextExpression that = (TextExpression) o;

        if (exactMatch != that.exactMatch) return false;
        return !(text != null ? !text.equals(that.text) : that.text != null);

    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (exactMatch ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TextExpression{" +
                "text='" + text + '\'' +
                ", exactMatch=" + exactMatch +
                '}';
    }
}
