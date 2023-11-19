/*
 * FeedDataChangeListener.java
 *
 * Created on 29.07.15 10:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author oflege
 */
public interface FeedDataChangeListener {
    enum ChangeType {
        CREATED, DELETED, REMOVED, RESURRECTED
    }

    void onChange(FeedData data, ChangeType type);
}
