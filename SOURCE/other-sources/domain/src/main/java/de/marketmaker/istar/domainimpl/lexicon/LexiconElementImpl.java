/*
 * LexiconElementImpl.java
 *
 * Created on 02.08.2006 22:04:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.lexicon;

import de.marketmaker.istar.domain.lexicon.LexiconElement;
import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LexiconElementImpl implements Serializable,LexiconElement {
    protected static final long serialVersionUID = 1L;

    final String id;
    final String source;
    final String initial;
    final String item;
    final DateTime date;
    final String text;

    public LexiconElementImpl(String id, String source, String initial, String item, DateTime date, String text) {
        this.id = id;
        this.source = source;
        this.initial = initial;
        this.item = item;
        this.date = date;
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public String getInitial() {
        return initial;
    }

    public String getItem() {
        return item;
    }

    public DateTime getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public String toString() {
        return "LexiconElementImpl[id=" + id
                + ", source=" + source
                + ", initial=" + initial
                + ", item=" + item
                + ", date=" + date
                + ", text=" + text
                + "]";
    }
}
