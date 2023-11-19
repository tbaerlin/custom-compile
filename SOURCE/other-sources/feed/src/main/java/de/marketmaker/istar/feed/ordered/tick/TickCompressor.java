/*
 * TickCompressorBase.java
 *
 * Created on 14.05.13 08:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.FeedMarketRepository;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

import static de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory.RT_TICKS;

/**
 * @author oflege
 */
abstract class TickCompressor extends TickFileMapper {

    interface Filter {
        boolean isIncluded(ByteString bs);
    }

    private static final int WITH_DUPLICATE = 1;

    private final int date;

    private File out = null;

    protected final ByteString marketCode;

    private final FeedMarket market;

    private final ArrayList<FeedData> items = new ArrayList<>(8192);

    private FeedData withMaxSeeks = null;

    private int maxSeeks = 0;

    private int num = 0;

    private int maxInLength = 0;

    private int maxOutLength = 0;

    private final Map<ByteString, List<FeedData>> duplicates = new HashMap<>();

    private final Filter filter;


    protected TickCompressor(File file, File out, ByteOrder byteOrder, Filter filter) {
        super(byteOrder, file);
        this.out = out;
        this.filter = filter;

        this.date = TickFile.getDay(this.file);

        final ByteString marketName = new ByteString(TickFile.getMarket(this.file));
        this.market = new FeedMarketRepository(false).getMarket(marketName);
        this.marketCode = marketName.prepend(new ByteString("."));
    }

    protected void compress() throws IOException {
        File tmp = new File(this.out.getParentFile(), this.out.getName() + ".tmp");
        if (tmp.exists() && !tmp.delete()) {
            throw new IOException("Could not delete " + tmp.getAbsolutePath());
        }
        this.logger.info(this.file.getName() + " ...");
        boolean success = false;
        try (FileChannel chIn = new RandomAccessFile(this.file, "r").getChannel();
             FileChannel fcOut = new RandomAccessFile(tmp, "rw").getChannel()) {
            success = compress(chIn, fcOut);
        } catch (Throwable t) {
            this.logger.error("FATAL error for " + this.file.getName(), t);
        } finally {
            unmapBuffers();
        }
        if (success) {
            if (this.out.exists() && !this.out.delete()) {
                this.logger.warn("<compress> failed to delete " + this.out.getName()
                        + ", find compressed data in " + tmp.getName());
                return;
            }
            if (!tmp.renameTo(this.out)) {
                this.logger.warn("<compress> failed to rename " + tmp.getName()
                        + " to " + this.out.getName());
                return;
            }
            this.logger.info(file.getName() + " finished");
        }
        else if (!tmp.delete()) {
            this.logger.warn("<compress> failed to delete corrupt file " + tmp.getName());
        }
    }

    protected boolean isIncluded(ByteString key) {
        return this.filter.isIncluded(key);
    }

    protected void addItem(ByteString vendorkey, long address, int length) {
        FeedData fd = RT_TICKS.create(toVendorkey(vendorkey), market);
        OrderedTickData td = ((OrderedFeedData) fd).getOrderedTickData();
        td.setDate(this.date);
        td.setStoreAddress(address);
        td.setLength(length);
        this.items.add(fd);
        this.maxInLength = Math.max(this.maxInLength, length);
    }

    protected void readItems(FileChannel ch) throws IOException {
        TickFileIndexReader.readEntries(ch,
                (key, position, length) -> {
                    if (isIncluded(key)) {
                        addItem(addMarket(key), position, length);
                    }
                });
    }

    private ByteString addMarket(ByteString key) {
        final int i = key.indexOf('.');
        return (i < 0) ? key.append(marketCode) : key.replace(i, i, marketCode);
    }

    protected abstract int addTicks(OrderedTickData td,
            TickDeflater deflater) throws IOException;

    final boolean compress(FileChannel ch, FileChannel out) throws IOException {
        TimeTaker tt = new TimeTaker();
        TickDeflater deflater = new TickDeflater(out, 0L);

        readItems(ch);
        if (items.size() == 0) {
            return false;
        }

        sortItems();
        this.logger.info(file.getName() + " read " + items.size() + " items");

        final short[] seeks = new short[this.items.size()];

        mapFile(ch);

        // define for vwdcode known to cause an error to speed up analysis
        final String vwdcode = System.getProperty("vwdcode");

        List<String> failed = new ArrayList<>();
        for (FeedData item : items) {
            OrderedTickData td = ((OrderedFeedData) item).getOrderedTickData();

            if (vwdcode != null && !item.getVwdcode().toString().equals(vwdcode)) {
                td.setLength(0);
                continue;
            }

            deflater.reset();

            int numSeeks;
            try {
                numSeeks = addTicks(td, deflater);
                if (item.getState() == WITH_DUPLICATE) {
                    for (FeedData duplicate : this.duplicates.get(item.getVwdcode())) {
                        numSeeks += addTicks(((OrderedFeedData) duplicate).getOrderedTickData(), deflater);
                    }
                }
            } catch (Throwable e) {
                this.logger.error(file.getName() + " compress failed for " + item.getVwdcode(), e);
                failed.add(item.getVwdcode().toString());
                td.setLength(0);
                continue;
            }

            if (numSeeks < 0) {
                td.setLength(0);
                this.logger.warn(file.getName() + " no data for " + item.getVwdcode());
                continue;
            }
            seeks[num] = (short) numSeeks;

            if (numSeeks > this.maxSeeks) {
                this.withMaxSeeks = item;
                this.maxSeeks = numSeeks;
            }

            deflater.flushCompressedTicks();
            td.setStoreAddress(deflater.getFileAddress());
            td.setLength(deflater.getNumTickBytes());
            this.maxOutLength = Math.max(this.maxOutLength, td.getLength());

            ++num;
        }
        deflater.flushWriteBuffer();

        if (failed.size() == items.size()) {
            this.logger.warn(this.file.getName() + " failed for all");
            return false;
        }

        TickFileIndexWriter w = new TickFileIndexWriter(out, deflater.getNumBytesOut());
        w.append(this.items, this.date);

        Arrays.sort(seeks);

        long size = file.length();
//        numBytesOut += (size - indexOffset);

        if (!failed.isEmpty()) {
            this.logger.warn(this.file.getName() + " failed for " + failed + ", #" + failed.size());
        }
        this.logger.info(file.getName() + ", took " + tt + ", #keys=" + this.items.size());
        this.logger.info(String.format("%s f=%8d  in=%8d out=%8d ratio=%4.2f, in(max)=%8d, out(max)=%8d",
                this.file.getName(), size, deflater.getNumBytesIn(), deflater.getNumBytesOut(),
                ((double) deflater.getNumBytesOut()) / size, this.maxInLength, this.maxOutLength));
        this.logger.info(String.format("%s seeks: min=%4d, mean=%4d, max=%4d (%s)", this.file.getName(),
                seeks[0], seeks[seeks.length / 2],
                seeks[seeks.length - 1], (withMaxSeeks != null) ? withMaxSeeks.getVwdcode() : "null"));
        return true;
    }

    private void sortItems() {
        final boolean[] withDuplicateKeys = new boolean[1];
        this.items.sort((o1, o2) -> {
            final int cmp = o1.getVwdcode().compareTo(o2.getVwdcode());
            if (cmp != 0) {
                return cmp;
            }
            withDuplicateKeys[0] = true;
            OrderedTickData td1 = ((OrderedFeedData) o1).getOrderedTickData();
            OrderedTickData td2 = ((OrderedFeedData) o2).getOrderedTickData();
            return -Long.compare(td1.getStoreAddress(), td2.getStoreAddress());
        });

        if (withDuplicateKeys[0]) {
            handleDuplicates();
        }
    }

    protected void handleDuplicates() {
        Iterator<FeedData> it = this.items.iterator();
        FeedData last = it.next();
        while (it.hasNext()) {
            FeedData next = it.next();
            if (last.getVwdcode().equals(next.getVwdcode())) {
                last.setState(WITH_DUPLICATE);
                List<FeedData> feedDatas = duplicates.get(last.getVwdcode());
                if (feedDatas == null) {
                    duplicates.put(last.getVwdcode(), feedDatas = new ArrayList<>(4));
                }
                feedDatas.add(next);
                it.remove();
            }
            else {
                last = next;
            }
        }
        this.logger.info(this.file.getName() + " handleDuplicates for " + this.duplicates.keySet());
    }

    VendorkeyVwd toVendorkey(ByteString vendorkey) {
        return VendorkeyVwd.isKeyWithTypePrefix(vendorkey)
                ? VendorkeyVwd.getInstance(vendorkey)
                : VendorkeyVwd.getInstance(vendorkey, 1);
    }

}
