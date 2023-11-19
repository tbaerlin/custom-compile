package de.marketmaker.istar.merger.web.easytrade.access.io;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.ThrowingBiConsumer;
import de.marketmaker.istar.merger.web.easytrade.RequestParserMethod;
import dev.infrontfinance.dm.proto.Access.Molecule;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;

/**
 * Reader to read chunks of {@link Molecule}s from a path. A path to the offset, from where the read
 * starts, is required. It serves as progress pointer. In case of failure:
 * <ol>
 *   <li>if message corrupted, ignore and continue</li>
 *   <li>if failed to be consumed, current position is stored in offset file, i.e. if started again,
 *   this reader can resume from the last position</li>
 * </ol>
 * This also means, in case of failure, client must close the reader, wait for some time and create
 * the reader and try again.
 *
 * @author zzhao
 * @deprecated use {@link dev.infrontfinance.dm.kafka.utils.io.MemoryMappedFileReader} instead
 */
@Slf4j
@NotThreadSafe
@Deprecated
public class MoleculeLocalReader implements Closeable {

  private static final ByteBuffer BUF = ByteBuffer.allocateDirect(
      RequestParserMethod.MAX_CONTENT_LENGTH * 2); // 256k

  private final int maxChunkSize;

  private final List<Molecule> dataList;

  private final DataFile recordsFile;

  private final DataFile posFile;

  private long curPos;

  public MoleculeLocalReader(Path posPath, Path recordsPath, int maxChunkSize) throws IOException {
    this.posFile = new DataFile(posPath.toFile(), false);
    this.recordsFile = new DataFile(recordsPath.toFile(), true);
    this.maxChunkSize = maxChunkSize;
    this.dataList = new ArrayList<>(this.maxChunkSize);
  }

  public boolean hasNext() throws IOException {
    // read until we have at least one data
    while (this.dataList.isEmpty()) {
      if (readChunk()) { // or to the end of file
        break; // end of file
      }
    }
    return !this.dataList.isEmpty();
  }

  /**
   * Read one chunk regardless of the messages are corrupted or not.
   * When a corrupted message is detected, this method simply start scanning to the right, byte by byte, until a valid message is found.
   *
   * @return true if we hit the end of file, otherwise false.
   */
  private boolean readChunk() throws IOException {
    this.curPos = readPos();
    if (this.curPos + 4 >= this.recordsFile.size()) { // 4 for length
      return true; // file ends
    }

    this.recordsFile.seek(this.curPos);
    BUF.clear();
    this.recordsFile.read(BUF);
    BUF.flip();

    long skipped = 0;
    while (BUF.remaining() > 4 && this.dataList.size() < this.maxChunkSize) { // 4 for length
      final int len = BUF.getInt();
      if (len > 0 && BUF.remaining() >= len) {
        final byte[] bytes = new byte[len];
        BUF.get(bytes);
        final Molecule m = getMolecule(bytes);
        if (m != null) {
          this.dataList.add(m);
        } else { // corrupted message
          // recovery mode start scanning byte by byte
          // revert the position: -4 bytes for message size, +1 for the next byte, -len for the message body
          BUF.position(BUF.position() - 3 - len);
          skipped++;
        }
      } else if(len <= 0 || len >= BUF.limit()) { // invalid message size
        // recovery mode start scanning byte by byte
        // revert the position: -4 bytes for message size, +1 for the next byte, no message body here
        BUF.position(BUF.position() - 3);
        skipped++;
      } else {
        break;
      }
    }
    // forward skipped bytes
    if (skipped > 0) {
      log.warn("<hasNext> corrupted file segment between: [{}-{}], lost bytes: {}", this.curPos, this.curPos + skipped, skipped);
      this.curPos += skipped;
      writePos(this.curPos);
    }
    return false;
  }

  public int getChunkSize() {
    return this.dataList.size();
  }

  private long readPos() throws IOException {
    if (this.posFile.size() < 8) {
      return 0;
    }
    return this.posFile.readLong(0);
  }

  private void writePos(long pos) throws IOException {
    this.posFile.writeLong(0, pos);
  }

  public void nextChunk(ThrowingBiConsumer<String, byte[], Exception> consumer) throws Exception {
    int posAdvance = 0;
    final Iterator<Molecule> it = this.dataList.iterator();
    try {
      while (it.hasNext()) {
        final Molecule m = it.next();
        consumer.accept(m.getUid(), m.toByteArray());
        it.remove();
        posAdvance += (4 + m.getSerializedSize());
      }
    } finally {
      if (posAdvance > 0) {
        writePos(this.curPos + posAdvance); // secure pos
      }
    }
  }

  private Molecule getMolecule(byte[] bytes) {
    try {
      return Molecule.parseFrom(bytes);
    } catch (Throwable t) {
      return null;
    }
  }

  @Override
  public void close() throws IOException {
    IoUtils.close(this.posFile);
    IoUtils.close(this.recordsFile);
  }
}
