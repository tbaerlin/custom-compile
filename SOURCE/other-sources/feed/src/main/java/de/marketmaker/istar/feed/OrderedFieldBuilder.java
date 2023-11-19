/*
 * OrderedFieldBuilder.java
 *
 * Created on 23.11.12 15:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

/**
 * @author oflege
 */
public interface OrderedFieldBuilder {
    void setInt(int orderId, int value);

    void setPrice(int orderId, long value);

    void setString(int orderId, byte[] value, int start, int length);

    void setTime(int orderId, int value);
}
