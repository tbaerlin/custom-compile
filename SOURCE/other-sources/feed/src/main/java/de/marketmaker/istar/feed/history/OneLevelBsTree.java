/*
 * TickHistoryPersisterJMXClient.java
 *
 * Created on 26.07.12 14:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.IoUtils;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zzhao
 */
@NotThreadSafe
public class OneLevelBsTree<T extends Comparable<T>> {

  private static final Logger log = LoggerFactory.getLogger(OneLevelBsTree.class);

  public static final int INDEX_CHUNK_SIZE = 8 * 1024;

  private static final int WB_SIZE = (1 << 20) * Integer.parseInt(
      System.getProperty("OneLevelBsTreeLevelOneBufferMB", "4"));

  private final ByteBuffer itemsBuf;

  private final List<ByteBuffer> levelOneBufs;

  private final OffsetLengthCoder olCoder;

  private final Class<T> clazz;

  private final Path tempItemsFilePath;

  private final DataFile tempItemsFile;

  private long itemCount = 0;

  @SuppressWarnings("unchecked")
  public OneLevelBsTree(OffsetLengthCoder olCoder, Class<T> clazz) throws IOException {
    this.olCoder = olCoder;
    this.clazz = clazz;
    this.itemsBuf = ByteBuffer.wrap(new byte[INDEX_CHUNK_SIZE]);
    this.levelOneBufs = new ArrayList<>();
    this.tempItemsFilePath = Files.createTempFile("hist-", ".idx");
    this.tempItemsFile = new DataFile(this.tempItemsFilePath.toFile(), false);
  }

  public OneLevelBsTree(Class<T> clazz) throws IOException {
    this(defaultOffsetLengthCoder(), clazz);
  }

  private int getRequiredLength(T key) {
    return Item.getLength(key) + 8 + ((key instanceof ByteString) ? 1 : 0);
  }

  public void addItem(Item<T> item) throws IOException {
    final T key = item.getKey();
    if (this.itemsBuf.remaining() < getRequiredLength(key)) {
      addLevelOneElement(key, this.tempItemsFile.position());
      flipWriteClear(this.tempItemsFile, this.itemsBuf);
    }

    Item.addToBuffer(this.itemsBuf, key);
    this.itemsBuf.putLong(item.getEntry(this.olCoder));
    this.itemCount++;
  }

  public void finish(DataFile dataFile, long itemsStart) throws IOException {
    updateIndexPos(itemsStart);
    final long lastOffset = this.tempItemsFile.position();
    if (this.itemsBuf.position() > 0) { // write final index chunk
      flipWriteClear(this.tempItemsFile, this.itemsBuf);
    }

    final long itemsFileSize = this.tempItemsFile.size();
    if (itemsFileSize > 0) {
      // merge temp. items file with data file
      try {
        this.tempItemsFile.seek(0);
        final long tx = dataFile.transferFrom(this.tempItemsFile, itemsStart, itemsFileSize);
        if (tx != itemsFileSize) {
          throw new IllegalStateException("temporary items file is not concatenated to data file"
              + " correctly: " + tx + "!=" + itemsFileSize + ", " + this.tempItemsFilePath);
        }
      } finally {
        IoUtils.close(this.tempItemsFile);
      }
      // if exception, preserve items file
      Files.delete(this.tempItemsFilePath);

      final long indexStart = itemsStart + itemsFileSize;
      final ByteBuffer levelOneBuf = getLevelOneBuf(8 + 8);
      levelOneBuf.putLong(lastOffset + itemsStart);
      levelOneBuf.putLong(indexStart);
      dataFile.seek(indexStart);
      for (ByteBuffer buf : this.levelOneBufs) {
        flipWriteClear(dataFile, buf);
      }
    }

    if (dataFile.position() < dataFile.size()) {
      dataFile.truncate(dataFile.position());
    }

    log.info("<storeIndex> {} indices stored in {} with {}",
        this.itemCount, dataFile, this.olCoder);
  }

  private void updateIndexPos(long itemsStart) {
    this.levelOneBufs.forEach(lob -> {
      final ByteBuffer bb = lob.duplicate();
      bb.flip();
      while (bb.hasRemaining()) {
        final long offset = bb.getLong(bb.position());
        bb.putLong(offset + itemsStart);
        Item.readPassKey(this.clazz, bb);
      }
    });
  }

  private static void flipWriteClear(DataFile df, ByteBuffer buffer) throws IOException {
    buffer.flip();

    if (buffer.hasRemaining()) {
      df.write(buffer);
    }
    buffer.clear();
  }

  private void addLevelOneElement(final T key, long offset) {
    final ByteBuffer bb = getLevelOneBuf(getRequiredLength(key));
    bb.putLong(offset);
    Item.addToBuffer(bb, key);
  }

  private ByteBuffer getLevelOneBuf(int requiredLen) {
    final ByteBuffer bb = this.levelOneBufs.isEmpty()
        ? null
        : this.levelOneBufs.get(this.levelOneBufs.size() - 1);
    if (bb == null || (bb.remaining() < requiredLen)) {
      this.levelOneBufs.add(ByteBuffer.allocate(WB_SIZE));
    }

    return this.levelOneBufs.get(this.levelOneBufs.size() - 1);
  }

  public static OffsetLengthCoder defaultOffsetLengthCoder() {
    return new OffsetLengthCoder(24);
  }

  public static void addBsTreeLevel1(File protoBufFile) throws IOException {
    OffsetLengthCoder olCoder = defaultOffsetLengthCoder();
    try (final DataFile df = new DataFile(protoBufFile, false)) {
      if (df.size() <= 0) {
        return;
      }

      final long indexStartPos = df.size() - 12;
      df.seek(indexStartPos);
      final long indexStart = df.readLong();
      final int version = df.readInt();
      if (version != 1) { // file version with index
        throw new IllegalStateException("invalid proto buf file version: " + version + "," +
            " expected 1");
      }

      final ItemsSegment<Long> idxSeg = new ItemsSegment<>(df, indexStart,
          indexStartPos - indexStart, olCoder, Long.class);
      final OneLevelBsTree<Long> bsTree = new OneLevelBsTree<>(olCoder, Long.class);
      while (idxSeg.hasNext()) {
        final Item<Long> item = idxSeg.next();
        bsTree.addItem(item);
      }

      bsTree.finish(df, indexStart);
      HistoryWriter.appendLengthBits(df, olCoder.getLengthBits());
    }
  }
}
