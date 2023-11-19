package de.marketmaker.iview.mmgwt.mmweb.client.history;

import java.util.Map;

/**
 * Created on 13.11.12 08:12
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface ThreadStateHandler {
    Map<String, String> saveState(HistoryItem item);

    void loadState(HistoryItem item, Map<String, String> data);

    String getStateKey(HistoryItem item) throws GetStateKeyException;
}