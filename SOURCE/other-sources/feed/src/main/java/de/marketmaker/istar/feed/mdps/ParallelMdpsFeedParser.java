package de.marketmaker.istar.feed.mdps;

import static de.marketmaker.istar.feed.mdps.MdpsFeedUtils.toXfeedMessageType;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.ordered.OrderedFeedWriter;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * The ParallelMdpsFeedParser is an extension of the regular MdpsFeedParser that simply checks
 * incoming data and dispatches it to other MdpsFeedParser instances based on a set of
 * VendorkeyFilters. Since each parser uses a separate Thread this distributes the work of actually
 * parsing the incoming feed on multiple parsers that work in parallel. This increases the
 * throughput and allows us to handle the highest feed data volume when the US markets open without
 * dropping data.
 *
 * It is necessary to set forceNewFeedRecords to true in the SimpleMdpsRecordSource from which
 * data is read. Otherwise the RecordSource will reuse an internal ByteBuffer before the
 * MdpsFeedParsers have actually parsed the data.
 */
@ManagedResource
public class ParallelMdpsFeedParser extends MdpsFeedParser {

  private static final String GAUGE_NAME = "parallel_mdps_feed_parser_gauge";
  private static final String GAUGE_RECORDS_FAILED_NAME =
      "parallel_mdps_feed_parser_failures_gauge";
  private static final String GAUGE_RECORDS_NO_QUEUE_NAME =
      "parallel_mdps_feed_parser_no_queue_gauge";
  private static final String PARSE_TIMER_NAME = "parallel_mdps_feed_parser_timer";

  private QueuedRecordSource[] recordSources = new QueuedRecordSource[0];

  private String[] marketLists = new String[0];
  private Map<String, QueuedRecordSource> queueMap;

  public void setRecordSources(QueuedRecordSource... recordSources) {
    this.recordSources = Arrays.copyOf(recordSources, recordSources.length);
  }

  public void setMarketLists(String... marketLists) {
    this.marketLists = Arrays.copyOf(marketLists, marketLists.length);
  }

  private final Set<String> unmatchedMarkets = new HashSet<>();

  private final boolean[] applicableMessageTypes = new boolean[Byte.MAX_VALUE];
  protected final AtomicInteger numRecordsNoQueue = new AtomicInteger();

  @Override
  public void afterPropertiesSet() {
    if (this.registry == null) {
      this.registry = new VolatileFeedDataRegistry();
    }

    if (this.keyConverter == null) {
      this.keyConverter = new MdpsKeyConverter(this.processDelayedRecords);
    }

    // The MdpsFeedParser handles checking the valid message types by asking registered FeedBuilders
    // for their applicable message types. But since this class has no reference to either the
    // MdpsFeedParsers nor their FeedBuilders we have to consider the messages types directly.
    // And since only the OrderedFeedWriter is registered as a FeedBuilder we check against that.
    for (byte b : OrderedFeedWriter.APPLICABLE_MESSAGE_TYPES) {
      this.applicableMessageTypes[b] = true;
    }

    if (this.recordSources.length != this.marketLists.length) {
      throw new RuntimeException(
          "<afterPropertiesSet> Cannot start parallel feed parser! The configured lists of recordSources and marketLists need to be the same length!");
    }

    // Build a map of market names to queues since this is faster than evaluating VendorkeyFilters.
    // In the worst case we had to check all VendorkeyFilters one after the other.
    this.queueMap = new HashMap<>();
    for (int i = 0; i < this.recordSources.length; i++) {
      final QueuedRecordSource recordSource = this.recordSources[i];

      final String[] markets = this.marketLists[i].split(",");
      for (String market : markets) {
        if ("*".equals(market)) {
          this.logger.info("Setting QueuedRecordSource number " + i + " as default.");
        }
        this.queueMap.put(market, recordSource);
      }
    }

    // Try to run some warm up of the map. During initial testing it turned out that we had caused
    // a considerable gap right after the process started up. It was quickly reduced to 0 again
    // but if we can prevent that it is certainly better
    for (String market : this.queueMap.keySet()) {
      this.queueMap.get(market);
    }

    if (this.meterRegistry != null) {
      Gauge.builder(GAUGE_NAME, () -> this.numRecordsParsed)
          .tags(Tags.of("t", "num_records_parsed"))
          .register(this.meterRegistry);
      Gauge.builder(GAUGE_RECORDS_FAILED_NAME, () -> this.numParseErrors)
          .tags(Tags.of("t", "num_records_failed"))
          .register(this.meterRegistry);
      Gauge.builder(GAUGE_RECORDS_NO_QUEUE_NAME, () -> this.numRecordsNoQueue)
          .tags(Tags.of("t", "num_records_no_queue"))
          .register(this.meterRegistry);
      final Timer parseTimer =
          Timer.builder(PARSE_TIMER_NAME)
              .tags(Tags.of("t", "duration_feed_parsed"))
              .register(this.meterRegistry);
      this.setParseTimer(parseTimer);
    }
  }

  @Override
  public void parse(FeedRecord feedRecord) {
    final long start = System.nanoTime();

    final ByteBuffer buffer = feedRecord.getAsByteBuffer();
    final byte mdpsMessageType =
        buffer.get(feedRecord.getOffset() + MdpsMessageConstants.HEADER_MESSAGE_TYPE_OFFSET);

    buffer.position(feedRecord.getOffset() + MdpsMessageConstants.HEADER_LENGTH); // skip header

    final byte messageType = toXfeedMessageType(mdpsMessageType);

    if (!this.applicableMessageTypes[messageType]) {
      this.logger.warn("<parse> Ignoring message type: " + messageType);
      return;
    }

    if (!buffer.hasRemaining()) {
      return;
    }

    final short keyFieldid = buffer.getShort();
    if (keyFieldid != MdpsMessageConstants.MDPS_KEY_FID) {
      setParseProblem(
          "Illegal fieldid for mdps key: " + keyFieldid + ", message type is: " + messageType);
      return;
    }

    final int mdpsKeyLength = buffer.get() & 0xFF;
    final int typeMapping = MdpsKeyConverter.getMapping(buffer, mdpsKeyLength);
    final ByteString vwdcode = this.keyConverter.convert(buffer, mdpsKeyLength, typeMapping);

    if (vwdcode == null) {
      return;
    }

    final VendorkeyVwd vendorkeyVwd = getVendorkey(vwdcode, typeMapping);

    QueuedRecordSource queuedRecordSource = this.queueMap.get(vendorkeyVwd.getMarketName().toString());
    if (queuedRecordSource == null) {
      queuedRecordSource = this.queueMap.get("*");
    }

    if (queuedRecordSource == null) {
      final String marketName = vendorkeyVwd.getMarketName().toString();
      if (this.unmatchedMarkets.add(marketName)) {
        this.logger.warn(
            "<parse> Can't find market queue for FeedRecord with Vendorkey: "
                + vendorkeyVwd
                + " and market: "
                + marketName);
      }
      this.numRecordsNoQueue.incrementAndGet();
    } else {
      boolean success = false;
      try {
        success = queuedRecordSource.addFeedRecord(feedRecord);
      } catch (InterruptedException e) {
        success = false;
      }
      if (!success) {
        throw new RuntimeException("<parse> Failed to hand off FeedRecord to queue");
      }
      ackRecordParsed();
    }

    if (this.parseTimer != null) {
      this.parseTimer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    }
  }
}
