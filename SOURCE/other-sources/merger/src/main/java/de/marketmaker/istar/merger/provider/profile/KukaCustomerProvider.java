/*
 * KukaCustomerProvider.java
 *
 * Created on 15.07.11 10:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;

/**
 * Maintains data read from kuka files.
 * @author oflege
 */
public class KukaCustomerProvider implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor monitor;

    private File kukaFile;

    private volatile Map<String, KukaCustomer> customers = new HashMap<>();

    public void setKukaFile(File kukaFile) {
        this.kukaFile = kukaFile;
    }

    public void setMonitor(ActiveMonitor monitor) {
        this.monitor = monitor;
    }

    public void afterPropertiesSet() throws Exception {
        readKuka();
        if (this.monitor != null) {
            final FileResource r = new FileResource(this.kukaFile);
            r.addPropertyChangeListener(evt -> readKuka());
            this.monitor.addResource(r);
        }
    }

    /** to be called by external scheduler... */
    public void refresh() {
        readKuka();
    }

    private void readKuka() {
        try {
            final KukaReader reader = new KukaReader();
            reader.read(this.kukaFile);
            this.customers = reader.getResult();
            this.logger.info("<readKuka> read data for " + this.customers.size()
                    + " customers from " + this.kukaFile.getAbsolutePath());
        } catch (Exception e) {
            this.logger.error("<readKuka> failed for " + this.kukaFile.getAbsolutePath(), e);
        }
    }

    KukaCustomer getCustomer(String kennung) {
        return this.customers.get(kennung);
    }
}
