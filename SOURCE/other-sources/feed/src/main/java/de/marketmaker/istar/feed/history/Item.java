/*
 * Item.java
 *
 * Created on 24.09.12 15:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.ByteString;
import java.nio.ByteBuffer;

/**
 * @author zzhao
 */
public class Item<T extends Comparable<T>> implements Comparable<Item<T>> {
    private final T key;

    private final long offset;

    private final int length;

    public Item(T key, long offset, int length) {
        if (offset < 0 || length < 0) {
            throw new IllegalArgumentException("invalid offset: " + offset + " a/o length: " + length);
        }
        this.key = key;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public int compareTo(Item<T> o) {
        return this.key.compareTo(o.key);
    }

    @Override
    public String toString() {
        return "{" + key + ";" + getOffset() + ";" + getLength() + "}";
    }

    public T getKey() {
        return key;
    }

    public long getEntry(OffsetLengthCoder olCoder) {
        return olCoder.encode(this.offset, this.length);
    }

    public long getOffset() {
        return this.offset;
    }

    public int getLength() {
        return this.length;
    }

    public static <T> void addToBuffer(ByteBuffer bb, T key) {
        if (key instanceof ByteString) {
            ((ByteString) key).writeTo(bb, ByteString.LENGTH_ENCODING_BYTE);
        }
        else if (key instanceof Long) {
            bb.putLong(Long.class.cast(key));
        }
        else if (key instanceof Character) {
            bb.put((byte) ((Character) key).charValue());
        }
        else {
            throw new UnsupportedOperationException("no support for: " + key.getClass());
        }
    }

    public static <T> int getLength(T key) {
        if (key instanceof ByteString) {
            return ((ByteString) key).length();
        }
        else if (key instanceof Long) {
            return 8;
        }
        else if (key instanceof Character) {
            return 1;
        }
        else {
            throw new UnsupportedOperationException("no support for: " + key.getClass());
        }
    }

    public static <T> T createKey(Class<T> clazz, ByteBuffer bb) {
        if (Long.class.equals(clazz)) {
            return clazz.cast(bb.getLong());
        }
        else if (ByteString.class.equals(clazz)) {
            return clazz.cast(ByteString.readFrom(bb, ByteString.LENGTH_ENCODING_BYTE));
        }
        else if (Character.class.equals(clazz)) {
            return clazz.cast((char) bb.get());
        }
        else {
            throw new UnsupportedOperationException("no support for: " + clazz);
        }
    }

    public static <T> void readPassKey(Class<T> clazz, ByteBuffer bb) {
        if (Long.class.equals(clazz)) {
            bb.position(bb.position() + 8);
        } else if (ByteString.class.equals(clazz)) {
            final int keyLen = bb.get() & 0xFF;
            bb.position(bb.position() + keyLen);
        } else if (Character.class.equals(clazz)) {
            bb.position(bb.position() + 1);
        } else {
            throw new UnsupportedOperationException("no support for: " + clazz);
        }
    }
}
