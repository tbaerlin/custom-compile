/*
 * ContentFlagsPostProcessor.java
 *
 * Created on 24.04.14 15:14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

/**
 * @author oflege
 */
public interface ContentFlagsPostProcessor {
    void postProcessFlags(long iid, long qid, long[] flags);
}
