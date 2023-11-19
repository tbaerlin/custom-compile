/*
 * Limiter.java
 *
 * Created on 20.12.12 09:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.limiters;

/**
 * @author Markus Dick
 */
public interface Limiter<T> {
    public Limiter<T> attach(T t);
    public Limiter<T> detach(T t);
}
