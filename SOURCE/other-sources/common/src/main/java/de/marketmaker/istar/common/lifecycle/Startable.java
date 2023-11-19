/*
 * Startable.java
 *
 * Created on 25.10.2004 14:14:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.lifecycle;

/**
 * Implement this for objects that start their own threads.
 * Use {@link de.marketmaker.istar.common.lifecycle.SpringStarter} to ensure
 * {@link #start()} is called after an ApplicationContext has been initialized and
 * {@link #stop()} is called before the context is disposed.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @deprecated use {@link org.springframework.context.Lifecycle}
 */
public interface Startable {
    void start() throws Exception;
    void stop() throws Exception;
}
