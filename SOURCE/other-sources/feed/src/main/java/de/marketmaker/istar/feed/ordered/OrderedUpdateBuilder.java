/*
 * OrderedUpdateBuilder.java
 *
 * Created on 30.08.12 15:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

/**
 * @author oflege
 */
public interface OrderedUpdateBuilder {
    void process(OrderedFeedData data, OrderedUpdate update);
}
