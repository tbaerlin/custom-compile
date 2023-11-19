package de.marketmaker.istar.ratios.frontend;

import static de.marketmaker.istar.ratios.frontend.RatioEnumSetFactory.SIZE_IN_BITS;
import static de.marketmaker.istar.ratios.frontend.RatioEnumSetFactory.SIZE_IN_BYTES;
import static de.marketmaker.istar.ratios.frontend.RatioEnumSetFactory.SIZE_IN_LONGS;

import de.marketmaker.istar.common.util.UnmodifiableBitSet;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Helper class to create multiple long-sized BitSet. It can store up to RatioEnumSetFactory.SIZE_IN_BITS bits.
 * {@link BitSet} is a peer type to RatioFieldDescription.Type.ENUMSET, used in {@link RatioEnumSetFactory}
 */
public class RatioEnumSet {

  public static BitSet zero() {
    return BitSet.valueOf(new long[SIZE_IN_LONGS]);
  }

  public static BitSet valueOf(long... values) {
    if (values.length > SIZE_IN_LONGS) {
      throw new IllegalArgumentException(
          "RatioEnumSet.SIZE reached. Current max: " + SIZE_IN_BITS + ", given: " + (values.length
              * Long.SIZE));
    }
    return BitSet.valueOf(values);
  }

  /**
   * Creates a zero-valued unmodifiable BitSet.
   *
   * @return BitSet
   */
  public static BitSet unmodifiableBitSet() {
    return new UnmodifiableBitSet(zero());
  }

  public static BitSet unmodifiableBitSet(BitSet bitSet) {
    return new UnmodifiableBitSet(bitSet);
  }

  /**
   * Reads SIZE_IN_BITS bits (long by long) from the buffer, in the current byte order,
   * into a new BitSet. The source buffer's position is incremented by SIZE_IN_BYTES in total.
   *
   * @param src source buffer
   * @return BitSet
   */
  public static BitSet read(ByteBuffer src) {
    final long[] longs = new long[SIZE_IN_LONGS];
    int i = 0;
    while (src.remaining() >= Long.BYTES && i < longs.length) {
      longs[i++] = src.getLong();
    }
    return BitSet.valueOf(longs);
  }

  /**
   * Writes SIZE_IN_BITS bits (long by long) containing the given {@link BitSet} value, in the current byte order,
   * into the buffer at the current position, and then increments the position by SIZE_IN_BYTES.
   * <br/>
   * So, empty bits are preserved.
   *
   * @param bitSet {@link BitSet}
   * @param dest {@link ByteBuffer}
   */
  public static void writeTo(BitSet bitSet, ByteBuffer dest) {
    final int nextPos = dest.position() + SIZE_IN_BYTES;
    final long[] longs = bitSet.toLongArray();
    try {
      dest.mark();
      for (int i = 0; i < SIZE_IN_LONGS; i++) {
        if (longs.length > i) {
          dest.putLong(longs[i]);
        } else {
          dest.putLong(0L);
        }
      }
      dest.position(nextPos);
    } catch (BufferOverflowException e) {
      dest.reset();
      throw new IllegalStateException(e);
    }
  }

  public static String toString(BitSet bitSet) {
    final StringBuilder sb = new StringBuilder();
    IntStream.range(0, bitSet.size()).mapToObj(i -> bitSet.get(i) ? '1' : '0').forEach(sb::append);
    return sb.toString();
  }

}
