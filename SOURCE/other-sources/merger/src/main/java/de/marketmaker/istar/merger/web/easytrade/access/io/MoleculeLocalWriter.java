package de.marketmaker.istar.merger.web.easytrade.access.io;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;

import com.google.protobuf.CodedOutputStream;
import dev.infrontfinance.dm.proto.Access.Molecule;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writer to store {@link Molecule} onto local disk. Can be generalised to handle all serializable
 * messages.
 * <p>
 * A message's length is written first as integer, then the message itself.
 * </p>
 *
 * @author zzhao
 * @deprecated use {@link dev.infrontfinance.dm.kafka.utils.io.RollingFileWriter} with
 * {@link de.marketmaker.istar.merger.web.easytrade.access.notifier.MoleculeDataWriter} instead
 */
@Deprecated
public class MoleculeLocalWriter implements Closeable, Flushable {

  private final OutputStream stream;

  private final CodedOutputStream out;

  private final ByteBuffer lenBuf = ByteBuffer.allocate(4);

  public MoleculeLocalWriter(Path path, int bufSize) throws IOException {
    this.stream = Files.newOutputStream(path, CREATE, APPEND, WRITE);
    this.out = CodedOutputStream.newInstance(stream, bufSize);
  }

  public MoleculeLocalWriter(Path path) throws IOException {
    this(path, CodedOutputStream.DEFAULT_BUFFER_SIZE);
  }

  public void write(Molecule molecule) throws IOException {
    this.lenBuf.putInt(0, molecule.getSerializedSize());
    this.out.writeRawBytes(this.lenBuf);
    molecule.writeTo(this.out);
  }

  @Override
  public void flush() throws IOException {
    this.out.flush();
    this.stream.flush();
  }

  @Override
  public void close() throws IOException {
    flush();
    this.stream.close();
  }
}
