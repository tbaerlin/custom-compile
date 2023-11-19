package de.marketmaker.istar.merger.web.easytrade.access.notifier;

import com.google.protobuf.InvalidProtocolBufferException;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.NumberUtil;
import de.marketmaker.istar.common.util.concurrent.ExecutorServiceUtil;
import de.marketmaker.istar.merger.web.easytrade.access.AccessNotifier;
import de.marketmaker.istar.merger.web.easytrade.access.io.MoleculeLocalReader;
import de.marketmaker.istar.merger.web.easytrade.access.io.MoleculeLocalWriter;
import dev.infrontfinance.dm.proto.Access.Molecule;
import dev.infrontfinance.dm.proto.Access.Molecule.Builder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * Aims for speed and throughput.
 *
 * @author zzhao
 * @deprecated use {@link KafkaNotifier}
 */
@Deprecated
@Slf4j
@ManagedResource
public class KafkaAccessNotifier implements AccessNotifier, DisposableBean, InitializingBean {

  private static final String RECOVERY_THREAD_NAME = "recovery";
  private static final String RECOVERY_RESPONSE_HANDLER_THREAD_NAME = "recovery-resp-handler";
  private static final String RECOVERY_CLEANUP_THREAD_NAME = "recovery-cleanup";
  private static final int RECOVERY_MAX_STACKED_BYTES_BEFORE_WARNING = 2_000_000_000;
  private final AtomicBoolean bufToLocal = new AtomicBoolean(false);
  private final String bootstrapServers;
  private final String topic;
  private final Path localBufDir;
  private final AtomicLong recoveredCount = new AtomicLong(0);
  private Path recordsPath;
  private Path recoveryPosPath;
  private String acks = "-1";
  private int retries = 3;
  private String compressionType = "snappy";
  private boolean enableIdempotent = true;
  private int batchSize = 16384;
  private int lingerMs = 0;
  private int bufferMemory = 33554432;
  private int maxBlockMs = 1000;
  private int maxInflightRequestsPerConnection = 5;
  private int requestTimeoutMs = 1000;
  private int deliveryTimeoutMs = 2000;
  private int recoverDelayMs = 60000; // 1 minute
  private int delayMsBetweenRecoveryChunks = 500;
  private int maxRecoveryChunkSize = 1000;
  private Producer<String, byte[]> producer;
  private ScheduledExecutorService ses;
  private ScheduledExecutorService res;
  private ScheduledExecutorService ces;
  private MoleculeLocalWriter localWriter;
  private ScheduledFuture<?> recoverAttemptResult;
  private ScheduledFuture<?> recoverResult;
  private final AtomicBoolean recoveryEnabled = new AtomicBoolean(true);
  private volatile boolean shutdown = false;
  private MeterRegistry meterRegistry;

  public KafkaAccessNotifier(String bootstrapServers, String topic, String localBufPath) {
    this.bootstrapServers = bootstrapServers;
    this.topic = topic;
    this.localBufDir = Paths.get(localBufPath);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    final Properties props = new Properties();
    props.put("bootstrap.servers", this.bootstrapServers);
    //Set acknowledgements for producer requests.
    props.put("acks", this.acks);
    //If the request fails, the producer can automatically retry,
    props.put("retries", this.retries);
    props.put("compression.type", this.compressionType);
    props.put("enable.idempotent", this.enableIdempotent);
    //Specify buffer size in config
    props.put("batch.size", this.batchSize);
    //Reduce the no of requests less than 0
    props.put("linger.ms", this.lingerMs);
    //The buffer.memory controls the total amount of memory available to the producer for buffering.
    props.put("buffer.memory", this.bufferMemory);
    props.put("max.block.ms", this.maxBlockMs);
    props.put("max.in.flight.requests.per.connection", this.maxInflightRequestsPerConnection);
    props.put("request.timeout.ms", this.requestTimeoutMs);
    props.put("delivery.timeout.ms", this.deliveryTimeoutMs);

    props.put("key.serializer", StringSerializer.class.getName());
    props.put("value.serializer", ByteArraySerializer.class.getName());

    this.producer = new KafkaProducer<>(props);
    log.info("<KafkaAccessNotifier> Kafka producer created");
    createSchedulers();
    prepareRecovery();
    Optional.ofNullable(this.meterRegistry).ifPresent(r -> {
      r.gauge("kafka.recover", Tags.of("type", "attempt"), RECOVERY_THREAD_NAME,
          f -> (this.recoverAttemptResult == null || this.recoverAttemptResult.isDone()) ? 0 : 1);
      r.gauge("kafka.recover", Tags.of("type", "inflight"), RECOVERY_THREAD_NAME,
          f -> (this.recoverResult == null || this.recoverResult.isDone()) ? 0 : 1);
      r.more().counter("kafka.recover.count", Tags.empty(), this.recoveredCount);
    });
  }

  private void createSchedulers() {
    this.ses = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, RECOVERY_THREAD_NAME));
    this.res = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, RECOVERY_RESPONSE_HANDLER_THREAD_NAME));
    this.ces = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, RECOVERY_CLEANUP_THREAD_NAME));
    log.info("<createSchedulers> {} scheduler created", RECOVERY_THREAD_NAME);
    log.info("<createSchedulers> {} scheduler created", RECOVERY_RESPONSE_HANDLER_THREAD_NAME);
    log.info("<createSchedulers> {} scheduler created", RECOVERY_CLEANUP_THREAD_NAME);
  }

  private void prepareRecovery() throws IOException {
    this.recordsPath = this.localBufDir.resolve("recovery.dat");
    this.recoveryPosPath = this.localBufDir.resolve("recovery.pos");
    resetLocalRecords();
    startCleanupThread();
  }

  private void resetLocalRecords() throws IOException {
    if (!(Files.exists(this.recordsPath) || Files.exists(this.recoveryPosPath))) {
      return;
    }
    final long offset = Files.exists(this.recoveryPosPath) && Files.size(this.recoveryPosPath) >= 8
        ? ByteBuffer.wrap(Files.readAllBytes(this.recoveryPosPath)).getLong() : 0;
    final long recordsFileSize = Files.exists(this.recordsPath)
        ? Files.size(this.recordsPath) : 0;

    if (recordsFileSize <= offset) {
      log.info("<resetLocalRecords> ");
      Files.deleteIfExists(this.recordsPath);
      Files.deleteIfExists(this.recoveryPosPath);
    } else {
      scheduleRecoveryAttempt();
    }
  }

  /**
   * Runs every day
   */
  private void startCleanupThread() {
    this.ces.scheduleAtFixedRate(this::cleanupLocalRecords, 1, 1, TimeUnit.DAYS);
  }

  private void cleanupLocalRecords() {
    if (this.shutdown) {
      return;
    }

    if (this.recoverAttemptResult == null || this.recoverAttemptResult.isDone()) { // only when no recovery attempt
      log.info("<cleanupLocalRecords> try cleaning local records");
      try {
        synchronized (this.localBufDir) {
          final long left = bytesLeftToRecover();
          if (left == 0) {
            if (this.localWriter != null) { // maybe we still have something in buffer
              this.localWriter.flush();
            }
            final long flushedRemaining = bytesLeftToRecover() - left;
            if (flushedRemaining > 0) {
              // read remaining into a temp file
              final Path tmp = this.localBufDir.resolve("recovery.tmp");
              log.info("<cleanupLocalRecords> remaining bytes in writer's buffer is detected, writing into a temp file: {}", tmp);
              try (
                  final MoleculeLocalReader reader = new MoleculeLocalReader(this.recoveryPosPath, this.recordsPath, this.maxRecoveryChunkSize);
                  final MoleculeLocalWriter tmpWriter = new MoleculeLocalWriter(tmp)
              ) {
                while (reader.hasNext()) {
                  reader.nextChunk((uid, bytes) -> tmpWriter.write(Molecule.parseFrom(bytes)));
                }
              }
              Files.deleteIfExists(this.recoveryPosPath);
              Files.deleteIfExists(this.recordsPath);
              Files.move(tmp, this.recordsPath); // swap tmp file
              log.info("<cleanupLocalRecords> temp file is swapped successfully");
            } else {
              Files.deleteIfExists(this.recordsPath);
              Files.deleteIfExists(this.recoveryPosPath);
            }
            this.localWriter = new MoleculeLocalWriter(this.recordsPath);

            log.info("<cleanupLocalRecords> cleanup successful");
          }
        }
      } catch (Exception e) {
        log.error("<cleanupLocalRecords> cannot clean local records", e);
      }

    } else {
      log.info("<cleanupLocalRecords> recovery attempt inflight, abort cleanup");
    }
  }

  @Override
  public void notify(Builder builder) {
    final Molecule molecule = builder.build();
    if (this.bufToLocal.get()) {
      sendToLocal(molecule);
    } else {
      sendToKafka(molecule);
    }
  }

  private void sendToKafka(Molecule molecule) {
    final String uid = molecule.getUid();
    try {
      // use get to trigger exception if sending failed
      // toByteArray will increase GC
      // kafka transaction expensive, enable.idempotent already set to true
      toKafka(molecule.getUid(), molecule.toByteArray());
    } catch (Exception e) {
      log.warn("<notify> sending access record failed: {}", uid, e);
      if (this.bufToLocal.compareAndSet(false, true)) {  // only here set to true
        scheduleRecoveryAttempt();
      }
      sendToLocal(molecule);
    }
  }

  private void scheduleRecoveryAttempt() {
    attemptRecovery(false);
  }

  private synchronized void attemptRecovery(boolean forceParallel) {
    if (this.shutdown) {
      return;
    }

    if (!this.recoveryEnabled.get()) {
      log.info("<scheduleRecoveryAttempt> recovery is disabled");
      return;
    }

    if (this.recoverAttemptResult == null || this.recoverAttemptResult.isDone()) {
      log.info("<scheduleRecoveryAttempt>");
      final boolean noRecoveryInflight = this.recoverResult == null || this.recoverResult.isDone();
      if (forceParallel || noRecoveryInflight) {
        this.recoverAttemptResult =
            this.ses.schedule(this::tryToRecover, this.recoverDelayMs, TimeUnit.MILLISECONDS);
      } else {
        log.info("<scheduleRecoveryAttempt> recovery inflight");
        // if a recovery is inflight and takes too long we don't want to keep local buffer forever.
        // So, set it back to false
        this.bufToLocal.compareAndSet(true, false);
      }
    } else {
      log.info("<scheduleRecoveryAttempt> recovery attempt inflight");
    }
  }

  private void toKafka(String uid, byte[] dat) throws Exception {
    if (this.meterRegistry != null) {
      final Sample sample = Timer.start();
      try {
        kafkaSend(uid, dat);
      } finally {
        sample.stop(this.meterRegistry.timer("access.notify", Tags.of("mode", "kafka")));
      }
    } else {
      kafkaSend(uid, dat);
    }
  }

  private void kafkaSend(String uid, byte[] dat) throws Exception {
    try {
      this.producer.send(new ProducerRecord<>(this.topic, uid, dat))
          .get(this.maxBlockMs + this.requestTimeoutMs, TimeUnit.MILLISECONDS);
    } catch (Throwable t) {
      throw new Exception(t);
    }
  }

  private CompletableFuture<Void> kafkaSendAsync(String uid, byte[] dat) {
    return CompletableFuture.runAsync(() -> { // run in fork join pool, multiple threads
      try {
        kafkaSend(uid, dat);
      } catch (Throwable t) {
        throw new CompletionException(t);
      }
    }).handleAsync((unused, throwable) -> {
      if (throwable != null) {
        log.warn("<kafkaSendAsync> kafka access problem, writing local", throwable);
        try {
          localSend(Molecule.parseFrom(dat));
        } catch (InvalidProtocolBufferException e) {
          log.warn("<kafkaSendAsync> cannot parse molecule with uid: {}, message lost", uid);
        } finally {
          this.recoveredCount.decrementAndGet();
        }
      }
      return null;
    }, this.res);
  }

  private void tryToRecover() {
    log.info("<tryToRecover> ");
    this.bufToLocal.compareAndSet(true, false);
    recoverRecords();
  }

  private void sendToLocal(Molecule molecule) {
    if (this.meterRegistry != null) {
      this.meterRegistry.timer("access.notify", Tags.of("mode", "local"))
          .record(() -> localSend(molecule));
    } else {
      localSend(molecule);
    }
  }

  private void localSend(Molecule molecule) {
    try {
      synchronized (this.localBufDir) {
        if (this.localWriter == null) {
          this.localWriter = new MoleculeLocalWriter(this.recordsPath);
        }
        this.localWriter.write(molecule);
      }
    } catch (Throwable t) {
      log.error("<sendToLocal> {} lost", molecule.getUid(), t);
    }
  }

  private void localFlush() throws IOException {
    synchronized (this.localBufDir) {
      if (this.localWriter != null) {
        this.localWriter.flush();
      }
    }
  }

  private void recoverRecords() {
    if (this.bufToLocal.get()) {
      // new request triggered kafka exception in between, postpone recovery
      // since after bufToLocal flag toggle, another tryToRecover must have been already scheduled
      log.info("<recoverRecords> buffer to local again, postpone recovery");
      return;
    }

    try {
      localFlush();
      this.recoverResult = this.ses.schedule(this::recover, 1000, TimeUnit.MILLISECONDS);
    } catch (Throwable t) {
      log.warn("<recoverRecords> failed", t);
      // recoverRecords is called during tryToRecover, (this also applies to method recover)
      // since scheduleRecoveryAttempt checks the scheduled future's done status
      // have to schedule the attempt-schedule in order to let the current schedule future
      // to transition to status done, otherwise it bites its own tail and won't schedule at all
      this.ses.schedule(this::scheduleRecoveryAttempt, 1000, TimeUnit.MILLISECONDS);
    }
  }

  private void recover() {
    try (final MoleculeLocalReader reader = new MoleculeLocalReader(
        this.recoveryPosPath, this.recordsPath, this.maxRecoveryChunkSize)) {
      log.info("<recover> start recovering ...");
      final long start = this.recoveredCount.get();
      while (!this.shutdown && this.recoveryEnabled.get() && reader.hasNext()) { // if shutdown, delayed for chunk processing
        final int chunkSize = reader.getChunkSize();
        final List<CompletableFuture<Void>> futures = new ArrayList<>(chunkSize);
        reader.nextChunk((String uid, byte[] bytes) ->
            futures.add(kafkaSendAsync(uid, bytes))
        );
        this.recoveredCount.addAndGet(chunkSize);
        // wait all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        localFlush(); // flush failed messages if any
        TimeUnit.MILLISECONDS.sleep(this.delayMsBetweenRecoveryChunks); // take a breath
      }
      log.info("<recover> recovery done, molecules recovered: {}", this.recoveredCount.get() - start);

      final long left = bytesLeftToRecover();
      if (left > RECOVERY_MAX_STACKED_BYTES_BEFORE_WARNING) {
        log.error("<recover> local messages are stacked up to {}. Highly possible there is a problem on recovery, please check!",
            NumberUtil.humanReadableByteCount(left));
      } else if (left > 0) {
        attemptRecovery(true);
      }
    } catch (Throwable t) {
      log.warn("<recover> failed", t);
      attemptRecovery(true);
    }
  }

  private long bytesLeftToRecover() throws IOException {
    final long offset = Files.exists(this.recoveryPosPath) && Files.size(this.recoveryPosPath) >= 8
        ? ByteBuffer.wrap(Files.readAllBytes(this.recoveryPosPath)).getLong() : 0;
    final long recordsFileSize = Files.exists(this.recordsPath)
        ? Files.size(this.recordsPath) : 0;

    return Math.max(0, recordsFileSize - offset);
  }

  @Override
  public void destroy() throws Exception {
    this.shutdown = true;
    if (this.producer != null) {
      this.producer.close(Duration.ofMinutes(1));
    }
    log.info("<destroy> Kafka producer closed");
    if (this.recoverAttemptResult != null && !this.recoverAttemptResult.isDone()) {
      this.recoverAttemptResult.cancel(false);
    }
    log.info("<destroy> to shutdown {} scheduler ...", RECOVERY_THREAD_NAME);
    ExecutorServiceUtil.shutdownAndAwaitTermination(this.ses, 120);
    ExecutorServiceUtil.shutdownAndAwaitTermination(this.res, 120);
    ExecutorServiceUtil.shutdownAndAwaitTermination(this.ces, 120);
    log.info("<destroy> ... {} scheduler shutdown complete", RECOVERY_THREAD_NAME);
    log.info("<destroy> ... {} scheduler shutdown complete", RECOVERY_RESPONSE_HANDLER_THREAD_NAME);
    log.info("<destroy> ... {} scheduler shutdown complete", RECOVERY_CLEANUP_THREAD_NAME);
    IoUtils.close(this.localWriter);
    log.info("<destroy> local writer closed");
  }

  public void setAcks(String acks) {
    this.acks = acks;
  }

  public void setRetries(int retries) {
    this.retries = retries;
  }

  public void setCompressionType(String compressionType) {
    this.compressionType = compressionType;
  }

  public void setEnableIdempotent(boolean enableIdempotent) {
    this.enableIdempotent = enableIdempotent;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public void setLingerMs(int lingerMs) {
    this.lingerMs = lingerMs;
  }

  public void setBufferMemory(int bufferMemory) {
    this.bufferMemory = bufferMemory;
  }

  public void setMaxBlockMs(int maxBlockMs) {
    this.maxBlockMs = maxBlockMs;
  }

  public void setMaxInflightRequestsPerConnection(int maxInflightRequestsPerConnection) {
    this.maxInflightRequestsPerConnection = maxInflightRequestsPerConnection;
  }

  public void setRequestTimeoutMs(int requestTimeoutMs) {
    this.requestTimeoutMs = requestTimeoutMs;
  }

  public void setDeliveryTimeoutMs(int deliveryTimeoutMs) {
    this.deliveryTimeoutMs = deliveryTimeoutMs;
  }

  public void setRecoverDelayMs(int recoverDelayMs) {
    this.recoverDelayMs = recoverDelayMs;
  }

  public void setDelayMsBetweenRecoveryChunks(int delayMsBetweenRecoveryChunks) {
    this.delayMsBetweenRecoveryChunks = delayMsBetweenRecoveryChunks;
  }

  public void setMaxRecoveryChunkSize(int maxRecoveryChunkSize) {
    this.maxRecoveryChunkSize = maxRecoveryChunkSize;
  }

  public void setMeterRegistry(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  /*
   * JMX Operations are below
   */

  @ManagedOperation(description = "Kafka statistics local buffer mode")
  public boolean getBufToLocal() {
    return this.bufToLocal.get();
  }

  @ManagedOperation(description = "Use to switch local buffer mode for Kafka statistics messages")
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "mode", description = "true or false")
  })
  public void setLocalMode(boolean mode) {
    this.bufToLocal.set(mode);
  }

  @ManagedOperation(description = "Temporarily disable/enable recovery for Kafka statistics")
  @ManagedOperationParameters({
      @ManagedOperationParameter(name = "enabled", description = "true or false")
  })
  public void setRecoveryEnabled(boolean enabled) {
    this.recoveryEnabled.set(enabled);
    if (enabled) { // if re-enabled then try a recovery
      scheduleRecoveryAttempt();
    }
  }

}
