package de.marketmaker.istar.feed.mdps;

import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.RecordSource;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.util.concurrent.ArrayBlockingQueue;
import org.springframework.beans.factory.InitializingBean;

/**
 * The QueuedRecordSource is used to provide pre parsed FeedRecords from the ParallelMdpsFeedParser
 * to regular MdpsFeedParsers in a multithreaded setup.
 *
 * <p>It differs from other RecordSource implementations by accepting FeedRecords instead of
 * ByteBuffer instances.
 *
 * <p>Internally the FeedRecords are stored in an ArrayBlockingQueue which may cause the thread that
 * feeds data into it to pause and wait for room in the queue. This behavior can be tuned by setting
 * the queueSize parameter to an appropriate value.
 */
public class QueuedRecordSource implements RecordSource, InitializingBean {

  private ArrayBlockingQueue<FeedRecord> queue;

  private String metricsPostfix = null;
  private MeterRegistry meterRegistry;

  private Integer queueSize;

  private static final int DEFAULT_QUEUE_SIZE = 100000;

  public void setMeterRegistry(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public void setMetricsPostfix(String metricsPostfix) {
    this.metricsPostfix = metricsPostfix;
  }

  public void setQueueSize(Integer queueSize) {
    this.queueSize = queueSize;
  }

  private static final String GAUGE_QUEUE_SIZE_NAME = "queued_record_source_gauge";

  @Override
  public void afterPropertiesSet() {
    if (this.queueSize == null || this.queueSize == 0) {
      this.queueSize = DEFAULT_QUEUE_SIZE;
    }

    this.queue = new ArrayBlockingQueue<>(this.queueSize);

    String postfix = "";
    if (this.metricsPostfix != null) {
      postfix = "_" + this.metricsPostfix;
    }

    if (this.meterRegistry != null) {
      Gauge.builder(GAUGE_QUEUE_SIZE_NAME + postfix, () -> this.queue.size())
          .tags(Tags.of("t", "queue_size"))
          .register(this.meterRegistry);
    }
  }

  @Override
  public FeedRecord getFeedRecord() throws InterruptedException {
    return this.queue.poll();
  }

  public boolean addFeedRecord(FeedRecord feedRecord) throws InterruptedException {
    this.queue.put(feedRecord);
    return true;
  }
}
