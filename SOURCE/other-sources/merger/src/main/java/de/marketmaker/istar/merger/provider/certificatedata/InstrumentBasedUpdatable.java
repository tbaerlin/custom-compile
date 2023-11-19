/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface InstrumentBasedUpdatable<T> {
    void addOrReplace(long instrumentid, T data);
}
