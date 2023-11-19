/*
 * EodIterator.java
 *
 * Created on 14.01.13 09:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.util.Iterator;

/**
 * @author zzhao
 */
interface EodIterator<T extends EodItem> extends Iterator<T> {

    long getQuote();
}
