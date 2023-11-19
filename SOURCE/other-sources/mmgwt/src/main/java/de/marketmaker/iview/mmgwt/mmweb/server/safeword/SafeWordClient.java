package de.marketmaker.iview.mmgwt.mmweb.server.safeword;

/**
 * Created on 02.08.12 10:43
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface SafeWordClient {
    public boolean auth(String user, String password);
}
