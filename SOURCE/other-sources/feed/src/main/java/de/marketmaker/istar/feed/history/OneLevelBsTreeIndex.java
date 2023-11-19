package de.marketmaker.istar.feed.history;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;

/**
 * @author zzhao
 */
public class OneLevelBsTreeIndex<T extends Comparable<T>> {

  private final ByteBuffer chunkBuf = ByteBuffer.allocate(OneLevelBsTree.INDEX_CHUNK_SIZE);

  private final DataFile dataFile;

  private final Class<T> keyClass;

  @Getter
  private final OffsetLengthCoder olCoder;

  private final List<T> keys;

  private final LongArrayList offsets;

  private int curIndex;

  public OneLevelBsTreeIndex(DataFile dataFile, Class<T> clazz) throws IOException {
    this.dataFile = dataFile;
    this.keyClass = clazz;

    final long indexStartPos = this.dataFile.size() - 8 - 1;
    final long indexStart = this.dataFile.readLong(indexStartPos);
    this.olCoder = new OffsetLengthCoder(this.dataFile.readByte());

    final int indexSize = (int) (indexStartPos - indexStart);
    final ByteBuffer bb = ByteBuffer.allocate(indexSize);

    this.dataFile.seek(indexStart);
    this.dataFile.read(bb);
    bb.flip();

    final int keyCount = HistoryUtil.countKeys(clazz, bb.asReadOnlyBuffer());
    this.keys = new ArrayList<>(keyCount);
    this.offsets = new LongArrayList(keyCount + 2);
    do {
      this.offsets.add(bb.getLong());
      if (bb.hasRemaining()) {
        this.keys.add(Item.createKey(this.keyClass, bb));
      }
    } while (bb.hasRemaining());
    this.offsets.add(indexStart);
    this.curIndex = -1;
  }

  public void loadData(T symbol, ByteBuffer buffer) throws IOException {
    int idx = Collections.binarySearch(this.keys, symbol);
    if (idx < 0) {
      idx = -idx - 1;
    } else {
      idx = idx + 1;
    }

    if (idx != this.curIndex) {
      this.dataFile.seek(this.offsets.getLong(idx));
      this.chunkBuf.clear();
      this.dataFile.read(this.chunkBuf,
          (int) (this.offsets.getLong(idx + 1) - this.offsets.getLong(idx)));
      this.chunkBuf.flip();
      this.curIndex = idx;
    }

    final int limit = this.chunkBuf.limit();
    while (this.chunkBuf.hasRemaining()) {
      final T key = Item.createKey(this.keyClass, this.chunkBuf);
      final long entry = this.chunkBuf.getLong();
      if (symbol.equals(key)) {
        loadFromDisk(entry, buffer);
        break;
      }
    }
    this.chunkBuf.position(0);
    this.chunkBuf.limit(limit);
  }

  private void loadFromDisk(long entry, ByteBuffer buffer) throws IOException {
    this.dataFile.seek(this.olCoder.decodeOffset(entry));
    this.dataFile.read(buffer, this.olCoder.decodeLength(entry));
  }
}
