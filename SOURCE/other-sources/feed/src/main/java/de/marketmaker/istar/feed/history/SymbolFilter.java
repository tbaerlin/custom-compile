/*
 * SymbolFilter.java
 *
 * Created on 18.03.14 09:28
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import de.marketmaker.istar.common.util.ByteString;

/**
 * @author zzhao
 */
public interface SymbolFilter {

    boolean accept(ByteString symbol);
}
