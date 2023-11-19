/*
 * RawTickAggregator.java
 *
 * Created on 10.04.13 10:37
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import static de.marketmaker.istar.feed.tick.AbstractTickRecord.TickItem.Encoding.AGGREGATED;
import static org.joda.time.DateTimeConstants.SECONDS_PER_DAY;

import de.marketmaker.istar.domain.data.TickType;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregates daily ticks, new style (i.e., out-of-order ticks will be added to the appropriate
 * aggregation)
 *
 * @author oflege
 */
public class RawTickAggregator implements RawTickProcessor<AbstractTickRecord.TickItem> {

  private final Logger logger = LoggerFactory.getLogger(getClass());

  private final TickWithInterval twi;

  protected final int endSec;

  protected final boolean checkLast;

  private final TickType type;

  protected final int aggregationIntervalInSeconds;

  private final boolean aggregateOnlyPositivePrices;

  private final RawAggregatedTick[] result;

  private final int date;

  private int numTicks = 0;

  private int maxTickTime = -1;

  public RawTickAggregator(
      AbstractTickRecord.TickItem item,
      DateTime lastTickDateTime,
      int aggregationIntervalInSeconds,
      TickType type,
      boolean aggregateOnlyPositivePrices) {
    this.date = item.getDate();
    this.type = type;
    this.aggregationIntervalInSeconds = aggregationIntervalInSeconds;
    this.aggregateOnlyPositivePrices = aggregateOnlyPositivePrices;

    this.result = new RawAggregatedTick[SECONDS_PER_DAY / aggregationIntervalInSeconds];
    this.endSec = item.getEndSec(lastTickDateTime);
    this.checkLast = (endSec != SECONDS_PER_DAY) && item.contains(lastTickDateTime);
    this.twi = new TickWithInterval(type);
  }

  int getMaxTickTime() {
    return this.maxTickTime;
  }

  @Override
  public boolean process(RawTick tick) {
    if (!tick.isRequiredType(type)) {
      return true;
    }
    if (checkLast && tick.getTime() > endSec) {
      return false;
    }
    this.twi.rawTick = tick;
    if (this.aggregateOnlyPositivePrices && twi.getPrice() <= 0) {
      return true;
    }

    addTick(twi.getSecondsInDay(), twi.getPrice(), twi.getVolume());
    return true;
  }

  protected void addTick(final int secondsInDay, final long price, final long volume) {
    this.maxTickTime = Math.max(maxTickTime, secondsInDay);
    final int index = secondsInDay / this.aggregationIntervalInSeconds;

    if (index >= result.length) {
      this.logger.warn(
          "<addTick> invalid index {} calculated for {} seconds in day. Tick will be ignored.",
          index,
          secondsInDay);
    } else {
      final RawAggregatedTick at = result[index];
      if (at == null) {
        this.numTicks++;
        this.result[index] = new RawAggregatedTick();
        this.result[index].resetTick(index * aggregationIntervalInSeconds, price, volume);
      } else {
        at.addTick(price, volume);
      }
    }
  }

  public int getNumTicks() {
    return this.numTicks;
  }

  public AbstractTickRecord.TickItem getResult() {
    if (this.numTicks == 0) {
      return new AbstractTickRecord.TickItem(this.date, new byte[0], 0, AGGREGATED);
    }

    final AggregatedTickEncoder ate = new AggregatedTickEncoder();
    final ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
    final AggregatedTickData atd = new AggregatedTickData();

    for (int i = 0; i < result.length; i++) {
      if (result[i] == null) {
        continue;
      }
      atd.resetOhlc(i * aggregationIntervalInSeconds, result[i]);
      addEncodedTick(ate.encode(atd), baos);
    }

    return new AbstractTickRecord.TickItem(
        this.date, this.maxTickTime, baos.toByteArray(), ate.getNumEncoded(), AGGREGATED);
  }

  private static void addEncodedTick(ByteBuffer bb, ByteArrayOutputStream baos) {
    baos.write(bb.array(), 0, bb.remaining());
  }

  public TickType getType() {
    return type;
  }
}
