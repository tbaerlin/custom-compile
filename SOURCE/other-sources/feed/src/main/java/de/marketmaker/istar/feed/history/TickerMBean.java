/*
 * TickHistoryPersisterMBean.java
 *
 * Created on 26.07.12 14:43
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

/**
 * @author zzhao
 */
public interface TickerMBean {

    boolean tick(String path);
}
