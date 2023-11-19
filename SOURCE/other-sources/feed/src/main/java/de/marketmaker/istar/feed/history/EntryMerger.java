/*
 * EntryMerger.java
 *
 * Created on 26.10.12 13:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author zzhao
 */
public interface EntryMerger {
    byte[] merge(byte[] base, byte[] delta);

    public static final EntryMerger FF = new EntryMerger() {
        @Override
        public byte[] merge(byte[] base, byte[] delta) {
            if (ArrayUtils.isEmpty(delta)) {
                return base;
            }
            if (ArrayUtils.isEmpty(base)) {
                return delta;
            }
            return ArrayUtils.addAll(delta, base);
        }
    };
}
