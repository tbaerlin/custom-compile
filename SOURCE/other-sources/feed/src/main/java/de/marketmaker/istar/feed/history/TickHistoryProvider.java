/*
 * TickHistoryProvider.java
 *
 * Created on 10.08.12 13:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

/**
 * @author zzhao
 */
public interface TickHistoryProvider {

    TickHistoryResponse query(TickHistoryRequest req);
}
