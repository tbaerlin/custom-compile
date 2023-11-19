/*
 * OrderedFileSnapStore.java
 *
 * Created on 28.08.12 16:58
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.BitSet;

import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRegistry;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;
import de.marketmaker.istar.feed.delay.DelayProvider;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.common.util.NumberUtil.humanReadableByteCount;

/**
 * Reads/writes snap data from/to a file.
 *
 * <p><b>Note: </b> the records are stored without compression, as we need to be able to restore
 * individual records from the file based on an offset (i.e., for bew), and compression on the
 * record level with snappy is not very effective: A test using a file with 11670653 records and an
 * uncompressed size of 10_702_174_983 bytes yielded a compressed file with 9_146_599_535 bytes,
 * i.e., a mere 15% reduction in size.
 *
 * @author oflege
 */
@ManagedResource
public class OrderedFileSnapStore implements InitializingBean, DisposableBean, Lifecycle {

  /**
   * if field definitions changed, it can be useful to recompute the dynamic neartime snap fields
   * when the snap file is read
   */
  private static final boolean RECOMPUTE_NT_SNAP = Boolean.getBoolean("snap.recompute.nt");

  private final FieldDataBuilder fdBuilder = new FieldDataBuilder(8192);

  private final FieldDataMerger fdMerger = RECOMPUTE_NT_SNAP ? new FieldDataMerger(8192) : null;

  public interface RestoreCallback {
    void restored(FeedData data, long offset);
  }

  private static final int VERSION = 2;

  private static final int RT_FLAG = 0x01;

  private static final int NT_FLAG = 0x02;

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private FeedDataRegistry registry;

  private DelayProvider delayProvider;

  /**
   * This filter will be applied on {@link #restore(RestoreCallback)} and prevents data to be
   * restored whose vendorkey does not pass the filter.
   */
  private VendorkeyFilter vendorkeyFilter = VendorkeyFilterFactory.ACCEPT_ALL;

  private File snapFile;

  private final ByteBuffer bb = ByteBuffer.allocateDirect(1024 * 1024);

  private long bbOffset = 0L;

  private boolean started;

  private boolean restoreDeletesUnknownFields = true;

  private boolean storeRecordsWithoutData = false;

  private boolean storeRealtime;

  private boolean storeDelayed;

  private long numBytesStored = 0;

  /**
   * after storing this many bytes of snap data, {@link FileChannel#force(boolean)} will be called
   * to write data through to disk. W/o invoking that method, writing a 14g snapfile might appear to
   * take only 10s, but you end up with 14g of dirty pages in memory that will be written to disk
   * soon; that massive write seems to interfere with other i/o tasks, e.g., the {@link
   * de.marketmaker.istar.feed.admin.CheckClockAge} test failed for almost a minute on a production
   * machine.
   */
  private long fsyncInterval = 100 * 1024 * 1024;

  private long nextFsyncAt = 0L;

  private long fsyncSleepMs = 100L;

  public void setFsyncInterval(long fsyncInterval) {
    this.fsyncInterval = fsyncInterval;
  }

  public void setFsyncSleepMs(long fsyncSleepMs) {
    this.fsyncSleepMs = fsyncSleepMs;
  }

  public void setRegistry(FeedDataRegistry registry) {
    this.registry = registry;
  }

  public void setDelayProvider(DelayProvider delayProvider) {
    this.delayProvider = delayProvider;
  }

  public void setVendorkeyFilter(VendorkeyFilter vendorkeyFilter) {
    this.vendorkeyFilter = vendorkeyFilter;
  }

  public void setSnapFile(File snapFile) {
    this.snapFile = snapFile;
  }

  public void setRestoreDeletesUnknownFields(boolean restoreDeletesUnknownFields) {
    this.restoreDeletesUnknownFields = restoreDeletesUnknownFields;
  }

  public void setStoreRecordsWithoutData(boolean storeRecordsWithoutData) {
    this.storeRecordsWithoutData = storeRecordsWithoutData;
  }

  public void setStoreRealtime(boolean storeRealtime) {
    this.storeRealtime = storeRealtime;
  }

  public void setStoreDelayed(boolean storeDelayed) {
    this.storeDelayed = storeDelayed;
  }

  @Override
  public void destroy() throws Exception {
    if (!this.started) {
      return;
    }
    this.fsyncSleepMs = 0L;
    try {
      store(this.snapFile, this.storeRealtime, this.storeDelayed);
    } catch (Throwable t) {
      this.logger.error("<destroy> failed to store " + this.snapFile.getAbsolutePath(), t);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    restore(null);
  }

  @Override
  public boolean isRunning() {
    return this.started;
  }

  @Override
  public void start() {
    this.started = true;
  }

  @Override
  public void stop() {
    // empty
  }

  public void restore(RestoreCallback callback) throws IOException {
    synchronized (this.bb) {
      doRestore(callback);
    }
  }

  private void doRestore(RestoreCallback callback) throws IOException {
    if (!(this.snapFile.isFile() && this.snapFile.canRead())) {
      this.logger.info("<restore> no such file " + this.snapFile.getAbsolutePath());
      return;
    }

    final TimeTaker tt = new TimeTaker();
    int num = 0;
    this.bb.clear().flip();
    this.bbOffset = 0L;
    boolean eof = false;

    InputStream inputStream = null;
    ReadableByteChannel fc = null;

    try {
      inputStream = Files.newInputStream(this.snapFile.toPath());
      if (this.snapFile.getName().endsWith(".gz")) {
        this.logger.info("Switching to GZipInputStream");
        inputStream = new GZIPInputStream(inputStream);
      }
      fc = Channels.newChannel(inputStream);

      fillBuffer(fc);
      final int version = this.bb.getInt();
      if (version > VERSION) {
        throw new IllegalStateException("cannot read file version " + version);
      }
      final boolean withCreatedTimestamp = version > 1;

      final SnapRecordTranscoder transcoder = checkFieldsAndOrders();
      this.logger.info("<restore> with " + transcoder);

      while (this.bb.hasRemaining()) {
        if (!eof && this.bb.remaining() < 10240) {
          eof = fillBuffer(fc);
        }

        final long dataOffset = this.bbOffset + this.bb.position();

        final FeedData data = readFeedData(withCreatedTimestamp, transcoder);
        if (data != null) {
          if (callback != null) {
            callback.restored(data, dataOffset);
          }
          num++;
        }
      }
    } finally {

      if (fc != null) {
        fc.close();
      }

      if (inputStream != null) {
        inputStream.close();
      }

      this.logger.info("<restore> " + num + ", took " + tt);
    }
  }

  private boolean fillBuffer(ReadableByteChannel fc) throws IOException {
    this.bbOffset += this.bb.position();
    this.bb.compact();
    int result = fc.read(this.bb);
    this.bb.flip();
    return result == -1;
  }

  private FeedData readFeedData(boolean withCreatedTimestamp, SnapRecordTranscoder transcoder) {
    final ByteString key = ByteString.readFrom(this.bb, ByteString.LENGTH_ENCODING_BYTE);
    final VendorkeyVwd vkey = VendorkeyVwd.getInstance(key, this.bb.get());

    final OrderedFeedData data =
        this.vendorkeyFilter.test(vkey) ? (OrderedFeedData) this.registry.register(vkey) : null;

    if (withCreatedTimestamp) {
      final int ts = this.bb.getInt();
      if (data != null) {
        data.setCreatedTimestamp(ts);
      }
    }

    final byte rtNtFlag = this.bb.get();
    if (isRealtime(rtNtFlag)) {
      readSnap(data, true, transcoder);
    }
    if (isNeartime(rtNtFlag)) {
      readSnap(data, false, transcoder);
      if (RECOMPUTE_NT_SNAP && data != null && data.getSnapData(false).isInitialized()) {
        recomputeNtSnap(data);
      }
    } else if (this.storeDelayed && isWithDelay(data)) {
      initNtSnapFromRt(data);
    }
    if (data != null) {
      data.setState(FeedData.STATE_UPDATED);
    }
    return data;
  }

  private void recomputeNtSnap(OrderedFeedData data) {
    byte[] rtData = data.getSnapData(true).getData(true);
    byte[] ntData = data.getSnapData(false).getData(false);
    byte[] merged = this.fdMerger.merge(new BufferFieldData(rtData), new BufferFieldData(ntData));
    if (merged == null) {
      merged = rtData;
    }
    initNtSnap(data, merged, data.getSnapData(false).getLastUpdateTimestamp());
  }

  private void initNtSnapFromRt(OrderedFeedData data) {
    OrderedSnapData rt = data.getSnapData(true);
    if (rt == null || !rt.isInitialized()) {
      return;
    }
    byte[] rtData = rt.getData(false);
    int lastUpdateTimestamp = rt.getLastUpdateTimestamp();
    initNtSnap(data, rtData, lastUpdateTimestamp);
  }

  private void initNtSnap(OrderedFeedData data, byte[] rtData, int lastUpdateTimestamp) {
    addDynamicFieldsToBuilder(new BufferFieldData(rtData));
    if (this.fdBuilder.lastOrder > 0) {
      OrderedSnapData nt = data.getSnapData(false);
      nt.init(null, this.fdBuilder.asArray());
      nt.setLastUpdateTimestamp(lastUpdateTimestamp);
    }
  }

  private void addDynamicFieldsToBuilder(final BufferFieldData fd) {
    this.fdBuilder.reset();
    for (int oid = fd.readNext(); oid > 0; oid = fd.readNext()) {
      if (VwdFieldOrder.isDynamic(oid)) {
        this.fdBuilder.addFieldToBuffer(fd);
      } else {
        fd.skipCurrent();
      }
    }
  }

  private void readSnap(OrderedFeedData data, boolean realtime, SnapRecordTranscoder transcoder) {
    final int timestamp = this.bb.getInt();
    final int length = this.bb.getShort() & 0xFFFF;
    final int end = this.bb.position() + length;
    if (data != null) {
      final OrderedSnapData sd = data.getSnapData(realtime);
      byte[] tmp = getBytes(length);
      onReadSnap(
          data, realtime, timestamp, (sd != null && tmp != null) ? transcoder.transcode(tmp) : tmp);
    }
    this.bb.position(end);
  }

  protected void onReadSnap(OrderedFeedData ofd, boolean realtime, int timestamp, byte[] data) {
    final OrderedSnapData sd = ofd.getSnapData(realtime);
    if (sd == null) {
      return;
    }
    sd.setLastUpdateTimestamp(timestamp);
    if (data != null) {
      sd.init(null, realtime ? data : removeStaticFields(data));
    }
  }

  private byte[] removeStaticFields(byte[] data) {
    // delay stores only dynamic fields; when fields were previously considered
    // dynamic and are now defined as static, this code removes those fields
    addDynamicFieldsToBuilder(new BufferFieldData(data));
    if (this.fdBuilder.lastOrder > 0 && this.fdBuilder.length() < data.length) {
      return this.fdBuilder.asArray();
    }
    return data;
  }

  private byte[] getBytes(int length) {
    if (length == 0) {
      return null;
    }
    byte[] result = new byte[length];
    this.bb.get(result);
    return result;
  }

  public static boolean isRealtime(byte rtNtFlag) {
    return (rtNtFlag & RT_FLAG) != 0;
  }

  public static boolean isNeartime(byte rtNtFlag) {
    return (rtNtFlag & NT_FLAG) != 0;
  }

  @ManagedOperation
  public void store() throws IOException {
    store(this.snapFile, this.storeRealtime, this.storeDelayed);
  }

  @ManagedOperation
  @ManagedOperationParameters({
    @ManagedOperationParameter(name = "filename", description = "filename"),
    @ManagedOperationParameter(name = "storeRt", description = "store realtime"),
    @ManagedOperationParameter(name = "storeNt", description = "store delayed")
  })
  public void store(String filename, boolean storeRt, boolean storeNt) throws IOException {
    store(new File(filename), storeRt, storeNt);
  }

  void store(File f, boolean storeRt, boolean storeNt) throws IOException {
    // prefer iterable from feedMarketRepository as it creates an iterable list per market instead
    // of a huge list for all elements
    Iterable<FeedData> it =
        this.registry.getFeedMarketRepository().isWithMarketElements()
            ? this.registry.getFeedMarketRepository().getElements()
            : this.registry.getElements();
    store(f, storeRt, storeNt, it);
  }

  public void store(File f, boolean storeRt, boolean storeNt, Iterable<FeedData> it)
      throws IOException {
    if (f == null || (!storeRt && !storeNt && !this.storeRecordsWithoutData)) {
      return;
    }
    final TimeTaker tt = new TimeTaker();
    int num;
    long numBytes;
    synchronized (this.bb) {
      this.numBytesStored = 0;
      this.nextFsyncAt = Math.max(this.bb.capacity() * 10, this.fsyncInterval);
      num = doStore(f, storeRt, storeNt, it);
      numBytes = this.numBytesStored;
    }
    this.logger.info("<store> #" + num + ", " + humanReadableByteCount(numBytes) + ", took " + tt);
  }

  private int doStore(File f, boolean storeRt, boolean storeNt, Iterable<FeedData> it)
      throws IOException {
    if (!FileUtil.backupFile(f, ".bak")) {
      this.logger.error("<store> failed to backup " + f.getAbsolutePath());
      return 0;
    }

    int num = 0;
    try (FileChannel fc = new RandomAccessFile(f, "rw").getChannel()) {
      this.bb.clear();
      this.bb.putInt(VERSION);
      addFieldsAndOrders();

      for (final FeedData feedData : it) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (feedData) {
          if (storeFeedData(fc, feedData, storeRt, storeNt)) {
            num++;
          }
        }
      }
      write(fc);
      fc.force(true);
    }
    return num;
  }

  private boolean storeFeedData(FileChannel fc, FeedData feedData, boolean storeRt, boolean storeNt)
      throws IOException {
    OrderedFeedData ofd = (OrderedFeedData) feedData;

    OrderedSnapData rt = null;
    OrderedSnapData nt = null;
    byte[] rtData = null;
    byte[] ntData = null;

    if (storeRt) {
      rt = ofd.getSnapData(true);
      rtData = getSnapData(ofd, true);
    }
    if (doStoreDelayed(feedData, storeNt)) {
      nt = ofd.getSnapData(false);
      ntData = getSnapData(ofd, false);
    }

    if (rtData == null && ntData == null && !this.storeRecordsWithoutData) {
      return false;
    }

    final ByteString vwdcode = feedData.getVwdcode();
    if (this.bb.remaining() < vwdcode.length() + 7 + length(rtData) + length(ntData)) {
      write(fc);
    }
    vwdcode.writeTo(bb, ByteString.LENGTH_ENCODING_BYTE);
    this.bb.put((byte) feedData.getVendorkeyType());
    this.bb.putInt(ofd.getCreatedTimestamp());
    this.bb.put(
        encodeRtNt(
            storeRt && (rt != null || rtData != null), storeNt && (nt != null || ntData != null)));
    append(rt, rtData);
    append(nt, ntData);
    return true;
  }

  protected byte[] getSnapData(OrderedFeedData ofd, boolean realtime) {
    final OrderedSnapData osd = ofd.getSnapData(realtime);
    return (osd != null && osd.isInitialized()) ? osd.getData(false) : null;
  }

  private boolean doStoreDelayed(FeedData data, boolean storeDelayed) {
    return storeDelayed && isWithDelay(data);
  }

  private boolean isWithDelay(FeedData data) {
    return this.delayProvider == null || this.delayProvider.getDelayInSeconds(data) > 0;
  }

  private void append(OrderedSnapData sd, byte[] data) {
    if (sd != null) {
      this.bb.putInt(sd.getLastUpdateTimestamp());
      if (data != null && data.length > 0) {
        this.bb.putShort((short) data.length).put(data);
      } else {
        this.bb.putShort((short) 0);
      }
    } else if (data != null) {
      this.bb.putInt(0); // dummy
      this.bb.putShort((short) data.length).put(data);
    }
  }

  private void addFieldsAndOrders() {
    assert this.bb.position() == 4;
    this.bb.position(6);
    BitSet fields = VwdFieldDescription.getFieldIds();
    int n = 0;
    for (int i = fields.nextSetBit(0); i >= 0; i = fields.nextSetBit(i + 1)) {
      VwdFieldDescription.Field f = VwdFieldDescription.getField(i);
      if (VwdFieldOrder.getOrder(f.id()) != 0) {
        bb.putShort((short) f.id()).putShort((short) VwdFieldOrder.getOrder(f.id()));
        n++;
      }
    }
    bb.putShort(4, (short) n);
  }

  private SnapRecordTranscoder checkFieldsAndOrders() {
    final SnapRecordTranscoder.Builder builder = new SnapRecordTranscoder.Builder();
    for (int i = 0, n = bb.getShort(); i < n; i++) {
      final int fid = bb.getShort();
      final int order = bb.getShort();
      VwdFieldDescription.Field field = VwdFieldDescription.getField(fid);
      if (field == null) {
        if (this.restoreDeletesUnknownFields) {
          builder.add(order, 0);
        }
        continue;
      }
      int newOrder = VwdFieldOrder.getOrder(fid);
      if (order != newOrder) {
        if (newOrder != -1) {
          builder.add(order, newOrder);
        } else if (this.restoreDeletesUnknownFields) {
          builder.add(order, 0);
        }
      }
    }
    return builder.build();
  }

  private void write(FileChannel fc) throws IOException {
    this.bb.flip();
    this.numBytesStored += this.bb.remaining();
    while (this.bb.hasRemaining()) {
      fc.write(this.bb);
    }
    this.bb.clear();
    if (this.fsyncInterval > 0L && this.numBytesStored > this.nextFsyncAt) {
      fsync(fc);
    }
  }

  private void fsync(FileChannel fc) throws IOException {
    final long then = System.currentTimeMillis();
    fc.force(false);
    final long took = System.currentTimeMillis() - then;
    if (took > 5000) {
      this.logger.warn("<fsync> took " + took + "ms");
    }
    this.nextFsyncAt += this.fsyncInterval;
    if (this.fsyncSleepMs > 0) {
      try {
        Thread.sleep(this.fsyncSleepMs);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException("interrupted!?", e);
      }
    }
  }

  private byte encodeRtNt(boolean storeRt, boolean storeNt) {
    return (byte) ((storeRt ? RT_FLAG : 0) + (storeNt ? NT_FLAG : 0));
  }

  private int length(byte[] data) {
    return 6 + (data != null ? data.length : 0);
  }
}
