/*
 * EntryFactory.java
 *
 * Created on 26.10.12 16:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author zzhao
 */
public final class EntryFactory {

    private static final int PIVOT_SIZE = 512;

    private static final byte[] BA_ZERO = new byte[]{(byte) 0};

    private static final ThreadLocal<Inflater> inflaterHolder = new ThreadLocal<Inflater>() {
        @Override
        protected Inflater initialValue() {
            return new Inflater();
        }

        @Override
        public void remove() {
            get().end();
        }
    };

    private static final ThreadLocal<Deflater> deflaterHolder = new ThreadLocal<Deflater>() {
        @Override
        protected Deflater initialValue() {
            final Deflater compressor = new Deflater();
            compressor.setLevel(Deflater.BEST_SPEED);
            return compressor;
        }

        @Override
        public void remove() {
            get().end();
        }
    };

    private EntryFactory() {
        throw new AssertionError("not for instantiation or inheritance");
    }

//    public static byte[] compress(byte[] input) throws IOException {
//        if (input.length < PIVOT_SIZE) {
//            return input;
//        }
//        else {
//            final ByteBuffer bb = ByteBuffer.allocate(Snappy.maxCompressedLength(input.length) + 1);
//            bb.put(BA_ZERO);
//            final int len = Snappy.compress(input, 0, input.length, bb.array(), 1);
//            return Arrays.copyOfRange(bb.array(), 0, len + 1);
//        }
//    }
//
//    public static byte[] decompress(byte[] input) {
//        if (input[0] != 0) {
//            return input;
//        }
//        else {
//            try {
//                return Snappy.uncompress(Arrays.copyOfRange(input, 1, input.length));
//            } catch (IOException e) {
//                throw new IllegalStateException("cannot uncompress data", e);
//            }
//        }
//    }

    public static byte[] compress(byte[] input) throws IOException {
        if (input.length < PIVOT_SIZE) {
            return input;
        }
        final Deflater compressor = deflaterHolder.get();
        compressor.reset();
        compressor.setInput(input);
        compressor.finish();

        final ByteArrayOutputStream baos =
                new ByteArrayOutputStream(Math.max(64, input.length / 8));
        baos.write(BA_ZERO);

        final byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            final int count = compressor.deflate(buf);
            baos.write(buf, 0, count);
        }

        return baos.toByteArray();
    }

    public static byte[] decompress(byte[] compressedData) {
        if (compressedData.length == 0 || compressedData[0] != 0) {
            return compressedData;
        }
        final Inflater inflater = inflaterHolder.get();
        inflater.reset();
        inflater.setInput(compressedData, 1, compressedData.length - 1);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(compressedData.length * 8);

        final byte[] buf = new byte[1024];
        while (!inflater.finished()) {
            try {
                final int count = inflater.inflate(buf);
                baos.write(buf, 0, count);
            } catch (DataFormatException e) {
                throw new IllegalStateException("DataFormatException: " + e.getMessage());
            }
        }

        return baos.toByteArray();
    }

    public static <T extends MutableEntry> void fromBuffer(ByteBuffer bb, T entry) {
        if (null == entry) {
            throw new IllegalArgumentException("mutable entry required");
        }
        if (MutableEntry.class.equals(entry.getClass())) {
            entry.setDays(HistoryUtil.fromUnsignedShort(bb.getShort()));
            final int tickLen = HistoryUtil.fromUnsignedShort(bb.getShort());
            final byte[] bytes = new byte[tickLen];
            bb.get(bytes);
            entry.setData(bytes);
        }
        else if (entry instanceof MutableTickEntry) {
            final MutableTickEntry me = (MutableTickEntry) entry;
            me.setDays(HistoryUtil.fromUnsignedShort(bb.getShort()));
            me.setTickNum(HistoryUtil.fromUnsignedShort(bb.getShort()));
            final int tickLen = HistoryUtil.fromUnsignedShort(bb.getShort());
            final byte[] bytes = new byte[tickLen];
            bb.get(bytes);
            me.setData(bytes);
        }
        else {
            throw new UnsupportedOperationException("no support for: " + entry.getClass());
        }
    }

    public static <T extends MutableEntry> void toBuffer(ByteBuffer bb, T entry) {
        if (null == entry) {
            throw new IllegalArgumentException("mutable entry required");
        }
        if (MutableEntry.class.equals(entry.getClass())) {
            bb.putShort((short) entry.getDays());
            byte[] data = entry.getData();
            bb.putShort((short) data.length);
            bb.put(data);
        }
        else if (entry instanceof MutableTickEntry) {
            bb.putShort((short) entry.getDays());
            bb.putShort((short) ((MutableTickEntry) entry).getTickNum());
            byte[] data = entry.getData();
            bb.putShort((short) data.length);
            bb.put(data);
        }
        else {
            throw new UnsupportedOperationException("no support for: " + entry.getClass());
        }
    }
}
