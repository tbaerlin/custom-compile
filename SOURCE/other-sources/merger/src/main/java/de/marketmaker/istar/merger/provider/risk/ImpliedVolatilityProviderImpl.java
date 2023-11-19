/*
 * ImpliedVolatilityProvider.java
 *
 * Created on 01.12.11 13:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.risk;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.ImpliedVolatilities;
import de.marketmaker.istar.merger.provider.protobuf.ProtobufDataStream;
import de.marketmaker.istar.merger.provider.protobuf.ProviderProtos;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides implied volatility values for at-the-moneny options.
 * Main goal of implementation is to minimize memory usage, as there will be more than 10 million values
 * @author oflege
 */
public class ImpliedVolatilityProviderImpl implements InitializingBean, ImpliedVolatilityProvider {

    private class ReaderSupport {
        private Volatilities current;
        
        private final Set<Long> uids = new HashSet<>();

        private void handle(ProviderProtos.ImpliedVolatility iv) {
            if (this.current == null || this.current.iid != iv.getUnderlying()) {
                addCurrent();
                this.current = new Volatilities(iv.getUnderlying());
            }
            this.current.add(iv);
        }        
        
        void finish() {
            addCurrent();
            volatilities.keySet().retainAll(this.uids);            
        }

        private void addCurrent() {
            if (this.current != null) {
                volatilities.put(this.current.iid, this.current);
                if (!this.uids.add(this.current.iid)) {
                    throw new IllegalStateException("re-encountered underlying " + this.current.iid
                        + ", input not sorted by underlyingid");
                }
            }
        }
    }

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private File file;
    
    private ActiveMonitor activeMonitor;

    // we expect about 500 underlyings
    private ConcurrentMap<Long, Volatilities> volatilities
            = new ConcurrentHashMap<>(1 << 10);


    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        readFile();
        
        if (this.activeMonitor != null) {
            final FileResource resource = new FileResource(this.file);
            resource.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    readFile();
                }
            });
            this.activeMonitor.addResource(resource);                        
        }
    }

    private void readFile() {
        this.logger.info("<readFile> " + this.file.getAbsolutePath());

        final TimeTaker tt = new TimeTaker();

        try {
            final int numRead = doReadFile();
            this.logger.info("<readFile> #records=" + numRead
                    + ", #underlyings=" + this.volatilities.size() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readFile> failed", e);
        }
    }

    private int doReadFile() throws IOException {
        final ReaderSupport readerSupport = new ReaderSupport();

        int numRead = 0;
        ProtobufDataStream ds = null;
        try {
            ds = new ProtobufDataStream(file);
            while (ds.hasNext()) {
                final ProviderProtos.ImpliedVolatility.Builder builder
                        = ProviderProtos.ImpliedVolatility.newBuilder();
                ds.mergeNext(builder);
                readerSupport.handle(builder.build());
                numRead++;
            }
            readerSupport.finish();
        } finally {
            IOUtils.closeQuietly(ds);
        }
        return numRead;
    }

    @Override
    public ImpliedVolatilityResponse getImpliedVolatilities(ImpliedVolatilityRequest request) {
        final Volatilities underlying = this.volatilities.get(request.getUnderlyingid());
        final ImpliedVolatilities result = (underlying != null)
                ? underlying.getVolatilities(request.getFrom())
                : null;
        return new ImpliedVolatilityResponse(result);
    }

    public static void main(String[] args) throws Exception {
        ImpliedVolatilityProviderImpl ivp = new ImpliedVolatilityProviderImpl();
        ivp.setFile(new File(args.length > 0 ? args[0] : "d:/temp/istar-moneyoptions.20111215.112039.buf.gz"));
        ivp.afterPropertiesSet();

        System.out.println(ivp.volatilities.size());

        ImpliedVolatilityRequest r
                = new ImpliedVolatilityRequest(94L, null);
        ImpliedVolatilities volatilities = ivp.getImpliedVolatilities(r).getResult();

        int n = 0;
        for (Iterator<ImpliedVolatilities.Daily> itDaily = volatilities.getDailies(); itDaily.hasNext(); ) {
            ImpliedVolatilities.Daily daily = itDaily.next();
            System.out.println(daily.getDay());
            for (Iterator<ImpliedVolatilities.Item> itItem = daily.getItems(); itItem.hasNext(); ) {
                ImpliedVolatilities.Item item = itItem.next();
                if (++n < 100) {
                    System.out.printf("  %s %4.2f %s %4.2f%n",
                            item.getType(), item.getStrike(), item.getMaturity(), item.getImpliedVolatility());
                }
            }
        }
    }
}

