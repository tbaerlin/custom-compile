/*
 * Formatter.java
 *
 * Created on 20.12.12 09:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.formatters;

/**
 * @author Markus Dick
 */
public interface Formatter<T> {
    public T format(final T t);
}
