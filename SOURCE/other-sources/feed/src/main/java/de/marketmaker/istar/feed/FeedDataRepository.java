/*
 * FeedDataRepository.java
 *
 * Created on 25.10.2004 15:49:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.netflix.servo.annotations.Monitor;
import io.netty.util.AsciiString;
import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory;
import de.marketmaker.istar.feed.snap.SnapData;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

import static com.netflix.servo.annotations.DataSourceType.GAUGE;
import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.encodeTimestamp;
import static de.marketmaker.istar.feed.FeedDataChangeListener.ChangeType.*;
import static org.joda.time.format.ISODateTimeFormat.dateOptionalTimeParser;

/**
 * Default implementation of the FeedDataRegistry, using ByteString as key
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class FeedDataRepository implements FeedDataRegistry {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);

    @GuardedBy("this.rwl")
    private final Map<ByteString, FeedData> repository;

    private final FeedMarketRepository marketRepository;

    private FeedDataFactory dataFactory = OrderedFeedDataFactory.RT;

    private final CopyOnWriteArrayList<FeedDataChangeListener> feedDataChangeListeners = new CopyOnWriteArrayList<>();

    public FeedDataRepository() {
        this(100);
    }

    public FeedDataRepository(int expectedSize) {
        this(new FeedMarketRepository(true), expectedSize);
    }

    public FeedDataRepository(FeedMarketRepository marketRepository, int expectedSize) {
        this.marketRepository = marketRepository;
        this.repository = new HashMap<>(expectedSize * 4 / 3 + 1);
    }

    @Override
    public void setDataFactory(FeedDataFactory dataFactory) {
        this.dataFactory = dataFactory;
    }

    public void setChangeListener(FeedDataChangeListener feedDataChangeListener) {
        this.feedDataChangeListeners.add(feedDataChangeListener);
    }

    @ManagedOperation
    @Monitor(name = "numSymbols", type = GAUGE)
    public int getNumSymbols() {
        this.rwl.readLock().lock();
        try {
            return this.repository.size();
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    @ManagedOperation
    @Monitor(name = "numMarkets", type = GAUGE)
    public int getNumMarkets() {
        return this.marketRepository.size();
    }


    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "vendorkey", description = "vendorkey")
    })
    public boolean removeFeedData(String key) {
        ByteString vwdcode = new ByteString(key);
        if (VendorkeyVwd.isKeyWithTypePrefix(vwdcode)) {
            vwdcode = vwdcode.substring(vwdcode.indexOf('.') + 1);
        }
        return unregister(vwdcode) != null;
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "vendorkey", description = "vendorkey")
    })
    public boolean removeDelaySnap(String key) {
        ByteString vwdcode = new ByteString(key);
        if (VendorkeyVwd.isKeyWithTypePrefix(vwdcode)) {
            vwdcode = vwdcode.substring(vwdcode.indexOf('.') + 1);
        }
        FeedData data = get(vwdcode);
        if (data == null) {
            return false;
        }
        boolean result;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (data) {
            SnapData sd = data.getSnapData(false);
            if (sd == null) {
                return false;
            }
            result = sd.isInitialized();
            sd.init(null, null);
        }
        return result;
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "market", description = "market")
    })
    public int removeMarket(String name) {
        final FeedMarket market = removeMarket(new ByteString(name));
        if (market == null) {
            return -1;
        }
        int n = 0;
        for (FeedData fd : market.getElements(false)) {
            if (remove(fd.getVwdcode()) != null) {
                n++;
            }
        }
        this.logger.info("<removeMarket> " + name + " with " + n + " elements");
        return n;
    }

    private FeedData remove(ByteString bs) {
        this.rwl.writeLock().lock();
        try {
            return this.repository.remove(bs);
        } finally {
            this.rwl.writeLock().unlock();
        }
    }

    /**
     * Requests to remove the FeedData object with the corresponding vkey from this repository.
     * If this object's {@link FeedMarketRepository} stores FeedData objects for each market, this
     * method will only mark the FeedData object as deleted and it will only be effectively removed
     * when {@link #gc()} will be invoked.
     * @param vkey to be removed
     * @return whether the request succeeded
     */
    @Override
    public boolean unregister(Vendorkey vkey) {
        return unregister(vkey.toVwdcode()) != null;
    }

    protected FeedData unregister(ByteString key) {
        final FeedData fd;
        if (this.marketRepository.isWithMarketElements()) {
            // removing fd from its market's elements list costs O(n) with n possibly in the millions
            // so all we do now is mark fd as deleted; so all code that retrieves
            // FeedData objects from this object or any of the FeedMarkets has to filter deleted
            // objects as appropriate;
            fd = get(key);
        }
        else {
            fd = remove(key);
        }
        if (fd != null) {
            ackRemove(fd);
        }
        return fd;
    }

    private void ackRemove(FeedData fd) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (fd) {
            if (fd.isDeleted()) {
                return;
            }
            // set state so fd's FeedMarket can remove fd from its elements during gc
            // removing fd from that list now would be too expensive for large elements lists.
            fd.setState(FeedData.STATE_DELETED);
            fd.dispose();
        }
        notifyListeners(fd, REMOVED);
    }

    @Override
    public FeedData create(Vendorkey vkey) {
        // avoid to add strange vkey due to corrupt data stream
        final ByteString vwdcode = vkey.toVwdcode();
        for (int i = 0; i < vwdcode.length(); i++) {
            if ((vwdcode.byteAt(i) & 0xff) < 32) {
                return null;
            }
        }

        final FeedMarket market = this.marketRepository.getMarket(vkey);
        if (market == null) {
            return null;
        }
        final FeedData result = this.dataFactory.create(vkey, market);
        result.setState(FeedData.STATE_NEW);
        return result;
    }

    // does notify the listeners
    @Override
    public void register(FeedData data) {
        register(data, true);
    }

    private void register(FeedData data, boolean doNotifyListeners) {
        final FeedData existing;
        this.rwl.writeLock().lock();
        try {
            existing = this.repository.put(data.getVwdcode(), data);
        } finally {
            this.rwl.writeLock().unlock();
        }
        if (existing != null && existing != data) {
            data.getMarket().remove(existing);
            this.logger.warn("<register> remove " + toString(existing));
        }
        data.getMarket().add(data);
        if (doNotifyListeners) {
            notifyListeners(data, CREATED);
        }
    }

    // does NOT notify the listeners
    @Override
    public FeedData register(Vendorkey vkey) {
        final FeedData result = get(vkey.toVwdcode());
        if (result != null) {
            return result;
        }
        final FeedData data = create(vkey);
        if (data != null) {
            register(data, false);
        }
        return data;
    }

    private void notifyListeners(FeedData data, final FeedDataChangeListener.ChangeType type) {
        this.feedDataChangeListeners.forEach(it -> it.onChange(data, type));
    }

    public FeedMarket registerMarket(ByteString name) {
        return this.marketRepository.getMarket(name);
    }

    @Override
    public synchronized List<FeedData> getElements() {
        this.rwl.readLock().lock();
        try {
            return new ArrayList<>(this.repository.values());
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    @Override
    public FeedMarketRepository getFeedMarketRepository() {
        return this.marketRepository;
    }

    public List<FeedMarket> getMarkets() {
        return this.marketRepository.getMarkets();
    }

    @Override
    public FeedData get(AsciiString key) {
        return get(new ByteString(key.array(), key.arrayOffset(), key.length()));
    }

    @Override
    public FeedData get(Vendorkey key) {
        return get(key.toVwdcode());
    }

    @Override
    public FeedData get(ByteString key) {
        this.rwl.readLock().lock();
        try {
            return this.repository.get(key);
        } finally {
            this.rwl.readLock().unlock();
        }
    }

    public FeedMarket getMarket(ByteString name) {
        return this.marketRepository.getMarket(name);
    }

    private FeedMarket removeMarket(ByteString name) {
        return this.marketRepository.remove(name);
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "filename", description = "filename"),
            @ManagedOperationParameter(name = "market", description = "market, empty=all"),
            @ManagedOperationParameter(name = "withType", description = "false to dump vwdcode")
    })
    public String dumpKeys(String filename, String market, boolean withType) {
        return dumpKeys(createDumper(filename).withMarket(market).withType(withType));
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "filename", description = "filename"),
            @ManagedOperationParameter(name = "cmin", description = "min creation timestamp (dateOptionalTime)"),
            @ManagedOperationParameter(name = "cmax", description = "max creation timestamp (dateOptionalTime)"),
            @ManagedOperationParameter(name = "withType", description = "false to dump vwdcode")
    })
    public String dumpKeys(String filename, String cmin, String cmax, boolean withType) {
        return dumpKeys(createDumper(filename)
                .withCreatedAfter(encodeTimestamp(dateOptionalTimeParser().parseMillis(cmin)))
                .withCreatedBefore(encodeTimestamp(dateOptionalTimeParser().parseMillis(cmax)))
                .withType(withType));
    }

    @ManagedOperation
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "filename", description = "filename"),
            @ManagedOperationParameter(name = "vendorkeyType", description = "only keys with this type")
    })
    public String dumpKeys(String filename, int vendorkeyType) {
        return dumpKeys(createDumper(filename).withVendorkeyType(vendorkeyType));
    }

    private String dumpKeys(FeedDataRepositoryDumper dumper) {
        final TimeTaker tt = new TimeTaker();
        try {
            int n = dumper.dump();
            return "wrote " + n + " keys to " + dumper.file.getAbsolutePath() + " in " + tt;
        } catch (Exception e) {
            this.logger.warn("<dumpKeys> failed", e);
            return "failed to write keys to " + dumper.file.getAbsolutePath() + ": " + e.getMessage();
        }
    }

    private FeedDataRepositoryDumper createDumper(String filename) {
        return new FeedDataRepositoryDumper(this, new File(filename));
    }

    public void gc() {
        final TimeTaker tt = new TimeTaker();
        final int n = this.marketRepository.gc(this::delete);
        this.logger.info("<gc> removed " + n + ", took " + tt);
    }

    private void delete(FeedData fd) {
        final FeedData removed = remove(fd.getVwdcode());
        if (removed == fd || removed == null) {
            notifyListeners(fd, DELETED);
        }
        else {
            this.logger.warn("<delete> " + toString(fd) + " <> " + toString(removed));
        }
    }

    private String toString(FeedData fd) {
        return (fd == null) ? "null" : (fd.getVendorkey() + "/" + fd);
    }
}
