package de.marketmaker.istar.merger.web.easytrade.access.notifier;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;
import com.google.protobuf.util.JsonFormat;
import de.marketmaker.istar.merger.web.easytrade.access.AccessNotifier;
import dev.infrontfinance.dm.kafka.utils.DmKafkaUtils;
import dev.infrontfinance.dm.kafka.utils.io.RollingFileCycle;
import dev.infrontfinance.dm.kafka.utils.io.RollingFileWriter;
import dev.infrontfinance.dm.kafka.utils.io.RollingFileWriter.Filename;
import dev.infrontfinance.dm.proto.Access.Molecule;
import dev.infrontfinance.dm.proto.Access.Molecule.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalDiskNotifier implements AccessNotifier {

  private final RollingFileWriter<Molecule> fileWriter;

  /**
   * Metrics
   */
  private final AtomicLong writeCount = new AtomicLong(0);

  public LocalDiskNotifier(MeterRegistry meterRegistry, Path folder, String filename, String extension, RollingFileCycle cycle) {
    this(meterRegistry, folder, filename, extension, cycle, 1 << 20); // 1MB
  }

  public LocalDiskNotifier(MeterRegistry meterRegistry, Path folder, String filename, String extension, RollingFileCycle cycle,
      int bufferSize) {
    log.info("Saving file...");
    try {
      log.info("Path: {} \nFile: {}.{}\nRollingFileCycle: {}", folder.toString(), filename, extension, cycle.toString());
      this.fileWriter =
          DmKafkaUtils.newRollingFileWriter(folder, Filename.of(filename, extension), cycle,
              new MoleculeDataWriter(), bufferSize);
      log.info("File saved successfully");
    } catch (IOException e) {
      log.error("Could not save file {}.{}", filename, extension);
      throw new RuntimeException(e);
    }
    Optional.ofNullable(meterRegistry).ifPresent(r -> {
      r.more().counter("dmxml.access", Tags.of("type", "write"), this.writeCount);
    });
  }

  @Override
  public synchronized void notify(Builder builder) {
    try {
      this.fileWriter.write(builder.build()); // not thread-safe, always should be syncronized
      this.writeCount.incrementAndGet();
    } catch (Exception e) {
      try {
        final String json = JsonFormat.printer()
            .omittingInsignificantWhitespace()
            .print(builder);
        log.error("cannot write molecule: {}", json, e);
      } catch (InvalidProtocolBufferException ex) {
        log.error("cannot convert proto to json: {}",
            TextFormat.printer().shortDebugString(builder), ex);
      }
    }
  }
}
