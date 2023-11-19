package de.marketmaker.istar.feed;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.FileUtil;
import de.marketmaker.istar.feed.ordered.OrderedFeedData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FeedDataRepositoryDumper {
    private final FeedDataRegistry feedDataRegistry;

    final File file;

    private boolean withType;

    private int vendorkeyType = -1;

    private int cmax = Integer.MAX_VALUE;

    private int cmin = Integer.MIN_VALUE;

    private String market;

    FeedDataRepositoryDumper(FeedDataRegistry feedDataRegistry, File file) {
        this.feedDataRegistry = feedDataRegistry;
        this.file = file;
    }

    FeedDataRepositoryDumper withMarket(String market) {
        if (StringUtils.hasText(market) && !"String".equals(market)) {
            this.market = market;
        }
        return this;
    }

    FeedDataRepositoryDumper withCreatedAfter(int cmin) {
        this.cmin = cmin;
        return this;
    }

    FeedDataRepositoryDumper withCreatedBefore(int cmax) {
        this.cmax = cmax;
        return this;
    }

    FeedDataRepositoryDumper withType(boolean withType) {
        this.withType = withType;
        return this;
    }

    FeedDataRepositoryDumper withVendorkeyType(int vendorkeyType) {
        this.vendorkeyType = vendorkeyType;
        return this;
    }

    int dump() throws IOException {
        int n = 0;
        FileUtil.deleteIfExists(file);

        final ByteBuffer bb = ByteBuffer.wrap(new byte[8192]);

        try (FileChannel ch = new RandomAccessFile(file, "rw").getChannel()) {
            for (FeedMarket m : getMarkets()) {
                final List<FeedData> elements = m.getElements(false);
                for (FeedData feedData : elements) {
                    final ByteString bs;
                    synchronized (feedData) {
                        bs = getKey((OrderedFeedData) feedData);
                    }
                    if (bs == null) {
                        continue;
                    }
                    n++;
                    if (bb.remaining() < (bs.length() + 1)) {
                        bb.flip();
                        ch.write(bb);
                        bb.clear();
                    }
                    bs.writeTo(bb, ByteString.LENGTH_ENCODING_NONE);
                    bb.put((byte) '\n');
                }
            }
            if (bb.hasRemaining()) {
                bb.flip();
                ch.write(bb);
            }
        }
        return n;
    }

    private ByteString getKey(OrderedFeedData feedData) {
        if (feedData.isDeleted()) {
            return null;
        }
        int c = feedData.getCreatedTimestamp();
        if (c < this.cmin || c > this.cmax) {
            return null;
        }
        if (this.vendorkeyType >= 0 && this.vendorkeyType != feedData.getVendorkeyType()) {
            return null;
        }
        if (this.withType) {
            return feedData.getVendorkey().toByteString();
        }
        return feedData.getVwdcode();
    }

    private List<FeedMarket> getMarkets() {
        if (StringUtils.hasText(market)) {
            final FeedMarket m = this.feedDataRegistry.getFeedMarketRepository().getMarket(new ByteString(market));
            return (m != null) ? Collections.singletonList(m) : Collections.<FeedMarket>emptyList();
        }
        return this.feedDataRegistry.getFeedMarketRepository().getMarkets();
    }
}
