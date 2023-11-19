/*
 * FeedInputSourceEditor.java
 *
 * Created on 13.12.2004 13:12:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.beans.PropertyEditorSupport;

/**
 * A FeedInputSource can be specified as [name,]host,port<br>
 * If name is missing, host will be used as name
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FeedInputSourceEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        final String[] tokens = text.split(",");

        int n = 0;
        final String name = tokens.length > 2 ? tokens[n++] : tokens[0];
        final String hostname = tokens[n++];
        final int port = Integer.parseInt(tokens[n]);

        setValue(new FeedInputSource(name, 0, hostname, port));
    }
}