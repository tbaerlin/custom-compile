/*
 * Filter.java
 *
 * Created on 21.01.14 15:01
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.publicnavitemspec;

/**
 * @author Markus Dick
 */
public interface Filter<T> {
    boolean isAcceptable(T t);
}
