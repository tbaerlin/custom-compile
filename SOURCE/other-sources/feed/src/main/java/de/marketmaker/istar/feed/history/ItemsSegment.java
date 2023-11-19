package de.marketmaker.istar.feed.history;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zzhao
 */
@Slf4j
public class ItemsSegment<T extends Comparable<T>> implements Iterator<Item<T>> {

  private static final ByteBuffer EMPTY_BB = ByteBuffer.wrap(new byte[0]);

  private static final int BUF_SIZE = (1 << 20) * Integer.parseInt(
      System.getProperty("IndexSegmentBufferSizeMB", "4"));

  private static final int MIN_REMAINING_SIZE = Byte.toUnsignedInt((byte) 0xFF) + 8;

  private final DataFile df;

  private final long offset;

  private final long length;

  private final ByteBuffer buf;

  private final OffsetLengthCoder olCoder;

  private final Class<T> keyClass;

  private long bytesRead;

  private Item<T> item;

  public ItemsSegment(DataFile df, long offset, long length, OffsetLengthCoder olCoder,
      Class<T> keyClass) throws IOException {
    this.df = df;
    this.offset = offset;
    this.length = length;
    this.buf = ByteBuffer.allocateDirect(BUF_SIZE);
    this.buf.flip(); // no index items read yet
    this.olCoder = olCoder;
    this.keyClass = keyClass;
    this.item = nextItem();
  }

  public ItemsSegment() {
    this.df = null;
    this.offset = 0;
    this.length = 0;
    this.buf = EMPTY_BB;
    this.olCoder = null;
    this.keyClass = null;
    this.item = null;
  }

  private int readMore() {
    final int len = calcChunkSize(this.buf.remaining());
    if (len == 0) {
      return 0;
    }
    final long pos = this.offset + this.bytesRead;
    try {
      this.df.seek(pos);
      this.df.read(this.buf, len);
    } catch (IOException e) {
      log.error("<readMore> pos {}, len {} in {}", pos, len, this.df);
      throw new IllegalStateException("failed reading more index items", e);
    }
    this.bytesRead += len;
    return len;
  }

  private int calcChunkSize(int remaining) {
    final long available = this.length - this.bytesRead;
    if (available > remaining) {
      return remaining;
    }
    return (int) available;
  }

  @Override
  public boolean hasNext() {
    return this.item != null;
  }

  @Override
  public Item<T> next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    final Item<T> curItem = this.item;
    this.item = nextItem();

    return curItem;
  }

  private Item<T> nextItem() {
    if (this.buf.remaining() < MIN_REMAINING_SIZE) {
      final int remaining = this.buf.remaining();
      if (remaining > 0) {
        this.buf.compact();
      } else {
        this.buf.clear();
      }
      final int read = readMore();
      this.buf.position(0).limit(remaining + read);
    }

    if (!this.buf.hasRemaining()) {
      return null;
    }

    final T key = Item.createKey(this.keyClass, this.buf);
    final long oal = this.buf.getLong();

    return new Item<>(key, this.olCoder.decodeOffset(oal), this.olCoder.decodeLength(oal));
  }
}
