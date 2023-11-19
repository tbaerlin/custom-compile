/*
 * SymbolRetainer.java
 *
 * Created on 17.03.14 16:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

/**
 * @author zzhao
 */
public interface SymbolRetainer<T extends Comparable<T>> {

    SymbolRetainer NO = symbol -> false;

    SymbolRetainer YES = symbol -> true;

    boolean shouldRetain(T symbol);
}
