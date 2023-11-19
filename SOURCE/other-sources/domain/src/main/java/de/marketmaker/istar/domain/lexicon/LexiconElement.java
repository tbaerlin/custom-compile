/*
 * LexiconElement.java
 *
 * Created on 02.08.2006 22:03:32
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.lexicon;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface LexiconElement {
    String getId();

    String getInitial();

    String getSource();

    String getItem();

    String getText();

    DateTime getDate();
}
