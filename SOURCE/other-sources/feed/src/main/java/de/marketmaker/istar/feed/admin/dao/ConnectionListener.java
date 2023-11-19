/*
 * ConnectionListener.java
 *
 * Created on 22.08.2006 14:05:57
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.admin.dao;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ConnectionListener {
    void registered(String name, ConnectionInfo info) throws Exception;

    void unregistered(String name);
}
