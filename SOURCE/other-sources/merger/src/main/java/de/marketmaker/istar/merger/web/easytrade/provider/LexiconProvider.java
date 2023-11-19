/*
 * LexiconProvider.java
 *
 * Created on 02.08.2006 22:23:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.provider;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.lexicon.LexiconElement;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.lexicon")
public interface LexiconProvider {

    List<String> getInitials(String lexiconId);

    List<LexiconElement> getElements(String lexiconId, String initial);

    LexiconElement getElement(String lexiconId, String id);
}
