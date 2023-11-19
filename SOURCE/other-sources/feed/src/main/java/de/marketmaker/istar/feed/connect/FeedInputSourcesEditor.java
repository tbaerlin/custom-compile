/*
 * FeedInputSourceEditor.java
 *
 * Created on 13.12.2004 13:12:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.beans.PropertyEditorSupport;
import java.util.Arrays;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id: FeedInputSourcesEditor.java,v 1.1 2004/12/13 18:03:41 tkiesgen Exp $
 */
public class FeedInputSourcesEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        final String[] configs = text.split(";");

        final FeedInputSource[] result = new FeedInputSource[configs.length];

        int count=0;
        for (String singleConfig : configs) {
            final String[] tokens = singleConfig.split(",");

            final int prio = Integer.parseInt(tokens[0]);
            final String name = tokens[1];
            final String hostname = tokens[2];
            final int port = Integer.parseInt(tokens[3]);

            result[count++] = new FeedInputSource(name, prio, hostname, port);
        }
        Arrays.sort(result, FeedInputSource.COMPARATOR_BY_PRIO);

        setValue(result);
    }
}
