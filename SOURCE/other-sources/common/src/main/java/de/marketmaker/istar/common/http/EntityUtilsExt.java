package de.marketmaker.istar.common.http;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.util.Args;
import org.apache.http.util.ByteArrayBuffer;

/**
 * Extended utility for dealing with {@link HttpEntity}.
 *
 * @author zzhao
 */
public final class EntityUtilsExt {

  private static final int DEFAULT_BUFFER_SIZE = 4096;

  private EntityUtilsExt() {
    throw new AssertionError("not for instantiation or inheritance");
  }

  /**
   * Reads from given {@link HttpEntity} upto given maximum bytes. Adopted from {@link
   * org.apache.http.util.EntityUtils#toByteArray(HttpEntity)} and use that directly for consuming
   * all bytes upto {@link Integer#MAX_VALUE} from given {@link HttpEntity}.
   *
   * @param entity an {@link HttpEntity}
   * @param maxBytesToRead maximum bytes to read
   * @return a byte array containing the bytes read. May be null if {@link HttpEntity#getContent()}
   * is null.
   * @throws IOException if an error occurs reading the input stream
   * @throws IllegalArgumentException if entity is null or if content length &gt; {@link
   * Integer#MAX_VALUE} or given max bytes to read &lt;= 0
   */
  public static byte[] toByteArray(final HttpEntity entity, int maxBytesToRead) throws IOException {
    Args.notNull(entity, "Entity");
    Args.check(maxBytesToRead > 0, "max bytes to read must be positive");
    final InputStream inStream = entity.getContent();
    if (inStream == null) {
      return null;
    }
    try {
      Args.check(entity.getContentLength() <= Integer.MAX_VALUE,
          "HTTP entity too large to be buffered in memory");
      int capacity = Math.min((int) entity.getContentLength(), maxBytesToRead);
      capacity = capacity > 0 ? capacity : maxBytesToRead; // content length can be negative
      final ByteArrayBuffer buffer = new ByteArrayBuffer(capacity);
      final byte[] tmp = new byte[DEFAULT_BUFFER_SIZE];
      int l;
      while ((l = inStream.read(tmp)) != -1 && buffer.length() < capacity) {
        buffer.append(tmp, 0, Math.min(l, buffer.capacity() - buffer.length()));
      }
      return buffer.toByteArray();
    } finally {
      inStream.close();
    }
  }
}
