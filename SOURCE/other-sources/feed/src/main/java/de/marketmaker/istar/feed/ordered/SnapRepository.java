package de.marketmaker.istar.feed.ordered;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataVkeyOnly;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * This repository can read multiple snap data files (.sd3) produced from Chicago instances and
 * internally store either the full snap data or Vendorkeys only. If full snap data is stored the
 * data can also be written out into a new snap file.
 *
 * <p>is intended to be used in two cases: 1. To merge sd3 files produced by Chicago instances with
 * market splits. (see Jira ticket DM-704) 2. To serve dpman requests for Vendorkeys. (see Jira
 * ticket DM-718)
 *
 * <p>The repository will merge snap data if multiple entries for the same vwd code are found.
 *
 * <p>The repository does not care about handling multiple days. When created put in the data for
 * one day and discard it when a new day needs to be read in.
 */
public class SnapRepository implements InitializingBean {
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  private final TreeMap<ByteString, Map<ByteString, FeedData>> marketDataMap = new TreeMap<>();

  private boolean vendorkeysOnly = false;

  /**
   * Create a new and empty repository. Fill it via fillFromFiles method.
   *
   * @param vendorkeysOnly Defines the type of data the repository will store. Either FeedData from
   *     the snap files or only the Vendorkey objects.
   */
  public SnapRepository(boolean vendorkeysOnly) {
    this.vendorkeysOnly = vendorkeysOnly;
  }

  /**
   * Add the given data to the repository while applying necessary transformations.
   *
   * <p>- If vendorkeysOnly has been set to true the data is stripped down to a FeedDataVkeyOnly
   * object. - If full snap data is stored instead a check is performed if an object is already
   * present for that market/vwdcode combination. If so the data is merged according to the
   * timestamps of the internal snap records
   *
   * @param data
   */
  public void addData(FeedData data) {
    final ByteString market = data.getMarket().getName();

    Map<ByteString, FeedData> marketData = this.marketDataMap.get(market);

    if (marketData == null) {
      // This market has not been added yet so we add a new one.
      // Since we don't have to consider merging snap data we can take a short cut
      // and simply add the data we are interested in.

      marketData = new TreeMap<>();
      final FeedData dataToStore = this.vendorkeysOnly ? new FeedDataVkeyOnly(data.getVendorkey()) : data;
      marketData.put(data.getVwdcode(), dataToStore);

      this.marketDataMap.put(market, marketData);
    } else {

      if (this.vendorkeysOnly) {
        // In case we need to store only Vendorkeys we simply put it into the map.
        // TODO do we have to check a timestamp so we only add the latest data?
        marketData.put(data.getVwdcode(), new FeedDataVkeyOnly(data.getVendorkey()));
      } else {
        final FeedData alreadyStored = marketData.get(data.getVwdcode());
        if (alreadyStored == null) {
          marketData.put(data.getVwdcode(), data);
        } else {
          // Worst case we store snap data and have to merge two records. What follows is some data
          // wrangling in order to be able to use the
          // already existing merge algorithm in the OrderedSnapRecord class
          final OrderedSnapRecord storedRecord =
              (OrderedSnapRecord) alreadyStored.getSnapData(true).toSnapRecord(0);
          final OrderedSnapRecord readRecord = (OrderedSnapRecord) data.getSnapData(true).toSnapRecord(0);

          // The merging creates a new object and does not check timestamps. So we make sure to
          // merge the newer object into the older one.
          final OrderedSnapRecord olderRecord =
              storedRecord.getLastUpdateTimestamp() <= readRecord.getLastUpdateTimestamp()
                  ? storedRecord
                  : readRecord;
          final OrderedSnapRecord newerRecord =
              storedRecord.getLastUpdateTimestamp() <= readRecord.getLastUpdateTimestamp()
                  ? readRecord
                  : storedRecord;
          final OrderedSnapRecord merged = olderRecord.merge(newerRecord);

          // Then we have to build a new FeedData object based on the old one with new snap data
          // inside.
          final OrderedSnapDataImpl snapData = new OrderedSnapDataImpl();
          snapData.init(merged);
          final OrderedFeedDataImpl newData =
              new OrderedFeedDataImpl(
                  data.getVendorkey(),
                  data.getMarket(),
                  snapData,
                  null,
                  ((OrderedFeedDataImpl) data).getOrderedTickData());

          marketData.put(data.getVwdcode(), newData);
        }
      }
    }
  }

  /**
   * @return A list of all market names in the repository.
   */
  public List<ByteString> getMarketNames() {
    return new ArrayList<>(this.marketDataMap.navigableKeySet());
  }

  /**
   * Get all stored FeedData for a market
   *
   * @param marketName The name of the market
   * @return A map of vwdcodes to FeedData objects
   */
  public Map<ByteString, FeedData> getMarketData(ByteString marketName) {
    return this.marketDataMap.get(marketName);
  }

  /**
   * Get the stored FeedData for a vendor key.
   *
   * @param vendorKey a vendor key, non-null
   * @return {@link FeedData} for the given vendor key if found
   */
  public FeedData getFeedData(Vendorkey vendorKey) {
    Objects.requireNonNull(vendorKey, "vendor key required");
    final ByteString marketName = vendorKey.getMarketName();
    final Map<ByteString, FeedData> marketData = this.marketDataMap.get(marketName);

    return marketData != null ? marketData.get(vendorKey.toVwdcode()) : null;
  }

  /**
   * Checks if this snap repository has feed data for the given vendor key.
   *
   * @param vendorKey a vendor key, non-null
   * @return true if this snap repository has feed data for the given vendor key, false otherwise
   */
  public boolean hasFeedData(Vendorkey vendorKey) {
    return getFeedData(vendorKey) != null;
  }

  /**
   * @return true if this snap repository is empty
   */
  public boolean isEmpty() {
    return this.marketDataMap.isEmpty();
  }

  @Override
  public void afterPropertiesSet() throws Exception {}

  /**
   * @return an Iterator over the markets and their stored data. This is needed when writing the
   *     data to a file.
   */
  public Iterable<FeedData> getElements() {

    return () ->
        new Iterator<FeedData>() {

          final Iterator<Map<ByteString, FeedData>> mit =
              SnapRepository.this.marketDataMap.values().iterator();
          Iterator<FeedData> it =
              this.mit.hasNext()
                  ? this.mit.next().values().iterator()
                  : Collections.emptyIterator();
          FeedData next = advance();

          FeedData advance() {
            while (this.it.hasNext() || this.mit.hasNext()) {
              while (this.it.hasNext()) {
                final FeedData fd = this.it.next();
                if (fd.isDeleted()) {
                  continue;
                }
                return fd;
              }
              this.it = this.mit.next().values().iterator();
            }
            return null;
          }

          @Override
          public boolean hasNext() {
            return this.next != null;
          }

          @Override
          public FeedData next() {
            final FeedData result = this.next;
            this.next = advance();
            return result;
          }
        };
  }

  /**
   * Parse the given files and put their data into the repository.
   *
   * @param snapFiles A list of files to parse.
   * @throws IOException In case a file cannot be read.
   */
  public void fillFromFiles(String[] snapFiles) throws IOException {
    final VolatileFeedDataRegistry vr = new VolatileFeedDataRegistry();
    vr.setDataFactory(OrderedFeedDataFactory.RT_NT);

    for (String snapFileName : snapFiles) {
      this.logger.info("Starting to read " + snapFileName);
      final File snapFile = new File(snapFileName);

      final OrderedFileSnapStore snapStore = new OrderedFileSnapStore();
      snapStore.setSnapFile(snapFile);
      snapStore.setStoreDelayed(false);
      snapStore.setRegistry(vr);

      snapStore.restore(new RestoreCallback(this));
    }
  }

  /**
   * Write vendorkeys grouped per market into the given file.
   *
   * @param outFile The file to write the data to
   */
  protected void writeVendorkeys(File outFile) throws Exception {

    try (final OutputStream outStream = new FileOutputStream(outFile)) {

      for (Map.Entry<ByteString, Map<ByteString, FeedData>> marketData :
          this.marketDataMap.entrySet()) {
        final ByteString marketName = marketData.getKey();
        final Collection<FeedData> feedData = marketData.getValue().values();
        int size = 0;
        final List<ByteBuffer> buffers = new ArrayList<>();
        for (FeedData data : feedData) {
          final ByteString vendorkeyString = data.getVendorkey().toByteString();
          final int vendorkeySize = 1 + vendorkeyString.length();
          size += vendorkeySize;
          final ByteBuffer buffer = ByteBuffer.allocate(vendorkeySize);
          vendorkeyString.writeTo(buffer, ByteString.LENGTH_ENCODING_BYTE);
          buffers.add(buffer);
        }

        final ByteBuffer marketBuffer = ByteBuffer.allocate(5 + marketName.length());
        marketName.writeTo(marketBuffer, ByteString.LENGTH_ENCODING_BYTE);
        marketBuffer.putInt(size);
        outStream.write(marketBuffer.array());

        for (ByteBuffer buffer : buffers) {
          outStream.write(buffer.array());
        }
      }
    } catch (Exception e) {
      this.logger.error("Failure to write Vendorkeys to file: " + e.getMessage());
    }
  }

  /**
   * Write the stored data to the given file. The type of file written depends on whether full snap
   * data or only vendorkeys are stored.
   *
   * @param outFile The file to write the data to
   */
  public void writeToFile(File outFile) throws Exception {
    if (this.vendorkeysOnly) {
      writeVendorkeys(outFile);
    } else {
      final OrderedFileSnapStore store = new OrderedFileSnapStore();
      store.store(outFile, true, true, getElements());
    }
  }

  /** Helper class that provides callback handling for reading in snap files. */
  private static class RestoreCallback implements OrderedFileSnapStore.RestoreCallback, Closeable {

    private final SnapRepository repository;

    public RestoreCallback(SnapRepository repository) {
      this.repository = repository;
    }

    @Override
    public void restored(FeedData feedData, long l) {
      final OrderedFeedData data = (OrderedFeedData) feedData;
      this.repository.addData(data);
    }

    @Override
    public void close() throws IOException {}
  }
}
