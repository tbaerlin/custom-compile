/*
 * HistoryWriter.java
 *
 * Created on 27.09.12 15:52
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.IoUtils;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zzhao
 */
public class HistoryWriter<T extends Comparable<T>> implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(HistoryWriter.class);

    private final DataFile dataFile;

    private final ByteBuffer writeBuffer;

    private final ByteBuffer dataBuffer;

    private final OffsetLengthCoder olCoder;

    private final OneLevelBsTree<T> bsTree;

    private long position;

    private T curKey;

    public HistoryWriter(File file, int lengthBits, Class<T> clazz) throws IOException {
        this(file, new OffsetLengthCoder(lengthBits), clazz);
    }

    public HistoryWriter(File file, OffsetLengthCoder coder, Class<T> clazz) throws IOException {
        this.olCoder = coder;
        this.dataFile = new DataFile(file, false);
        this.position = 0;
        this.dataBuffer = ByteBuffer.allocate(this.olCoder.maxLength());
        this.writeBuffer = ByteBuffer.allocate(this.olCoder.maxLength() * 2);
        this.bsTree = new OneLevelBsTree<>(coder, clazz);
    }

    public void withEntry(T key, byte[] data) throws IOException {
        withEntry(key, data, 0, data.length);
    }

    public void withEntry(T key, byte[] data, int offset, int length) throws IOException {
        start(key);
        withData(key, data, offset, length);
        finish(key);
    }

    public void start(T key) {
        if (null != this.curKey) {
            throw new IllegalArgumentException("not finished with: " + this.curKey);
        }
        this.curKey = key;
        this.dataBuffer.clear();
    }

    private void withData(T key, byte[] data, int offset, int length) throws IOException {
        checkKeyAndCapacity(key, length);
        this.dataBuffer.put(data, offset, length);
    }

    public void withData(T key, byte data) throws IOException {
        checkKeyAndCapacity(key, 1);
        this.dataBuffer.put(data);
    }

    public void withData(T key, int data) throws IOException {
        checkKeyAndCapacity(key, 4);
        this.dataBuffer.putInt(data);
    }

    private void checkKeyAndCapacity(T key, int requiredLen) {
        checkKey(key);
        if (this.dataBuffer.remaining() < requiredLen) {
            throw new IllegalStateException("data buffer overflow: [" + key + "] " + requiredLen);
        }
    }

    public void withData(T key, byte[] data) throws IOException {
        withData(key, data, 0, data.length);
    }

    public void finish(T key) throws IOException {
        checkKey(key);
        this.dataBuffer.flip();
        if (this.dataBuffer.hasRemaining()) {
            // has data
            final byte[] data = Arrays.copyOfRange(this.dataBuffer.array(),
                    this.dataBuffer.position(), this.dataBuffer.limit());
            finishWithKey(key, data);
        }
        this.curKey = null;
    }

    private void checkKey(T key) {
        if (!this.curKey.equals(key)) {
            throw new IllegalArgumentException("expected key: " + this.curKey + ", is: " + key);
        }
    }

    private void finishWithKey(T key, byte[] data) throws IOException {
        if (this.writeBuffer.remaining() < data.length) {
            this.writeBuffer.flip();
            this.dataFile.write(this.writeBuffer);
            this.writeBuffer.compact();
        }
        this.writeBuffer.put(data);
        this.olCoder.encode(this.position, data.length); // ensure we can encode
        this.bsTree.addItem(new Item<>(key, this.position, data.length));
        this.position += data.length;
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
        } finally {
            IoUtils.close(this.dataFile);
            log.info("<close> cleared and closed history writer");
        }
    }

    private void flush() throws IOException {
        while (this.writeBuffer.position() != 0) {
            this.writeBuffer.flip();
            this.dataFile.write(this.writeBuffer);
            this.writeBuffer.compact();
        }
        this.bsTree.finish(this.dataFile, this.dataFile.position());
        appendLengthBits(this.dataFile, this.olCoder.getLengthBits());
    }

    public static void appendLengthBits(DataFile df, int lengthBits) throws IOException {
        df.seek(df.size());
        df.write(ByteBuffer.wrap(new byte[]{(byte) lengthBits}));
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: file/folder LengthBits");
            System.exit(1);
        }

        Path path = Paths.get(args[0]);
        int lengthBits = Integer.parseInt(args[1]);
        if (Files.isDirectory(path)) {
            try (final DirectoryStream<Path> ds = Files.newDirectoryStream(path,
                    new DirectoryStream.Filter<Path>() {
                        @Override
                        public boolean accept(Path entry) throws IOException {
                            try {
                                HistoryUnit.fromExt(entry.toFile());
                                return true;
                            } catch (IllegalArgumentException e) {
                                return false;
                            }
                        }
                    }
            )) {
                for (Path huPath : ds) {
                    appendLengthBitsToFile(huPath, lengthBits);
                    System.out.print("appended lengthBits(" + lengthBits + ") to " + huPath);
                }
            }
        }
        else {
            appendLengthBitsToFile(path, lengthBits);
        }
    }

    private static void appendLengthBitsToFile(Path path, int lengthBits) throws IOException {
        try (final DataFile df = new DataFile(path.toFile(), false)) {
            df.seek(df.size());
            appendLengthBits(df, lengthBits);
            System.out.println("bits encoding length: " + lengthBits + " appended to: " + path);
        }
    }
}
