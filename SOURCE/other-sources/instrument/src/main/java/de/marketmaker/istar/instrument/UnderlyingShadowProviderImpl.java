/*
 * UnderlyingShadowProviderImpl.java
 *
 * Created on 14.08.2008 20:16:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.GuardedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UnderlyingShadowProviderImpl implements
        UnderlyingShadowProvider, InitializingBean, PropertyChangeListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    @GuardedBy("this")
    private Map<Long, List<Long>> map;

    @GuardedBy("this")
    private Map<Long, Long> reverseMap;

    private File file;

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void afterPropertiesSet() throws Exception {
        final FileResource resource = new FileResource(this.file);
        resource.addPropertyChangeListener(this);
        this.activeMonitor.addResource(resource);

        readData();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        try {
            readData();
        } catch (Exception e) {
            logger.error("<propertyChange> failed", e);
        }
    }

    public synchronized Long getShadowInstrumentId(long iid) {
        return this.reverseMap.get(iid);
    }

    public synchronized List<Long> getInstrumentids(long instrumentid) {
        return this.map.get(instrumentid);
    }

    private void readData() throws Exception {
        final TimeTaker tt = new TimeTaker();
        if (!this.file.exists()) {
            this.logger.warn("<readData> no file defined, return");
            map = Collections.emptyMap();
            reverseMap = Collections.emptyMap();
            return;
        }

        final Map<Long, List<Long>> tmpMap = new IstarMdpExportReader<Map<Long, List<Long>>>() {
            final Map<Long, List<Long>> result = new HashMap<>();
            {
                this.rowTag = "INSTRUMENT";
            }

            @Override
            protected void handleRow() {
                final Long shadowInstrumentId = getLong("SHADOWINSTRUMENTID");
                List<Long> ids = result.get(shadowInstrumentId);
                if (ids == null) {
                    result.put(shadowInstrumentId, ids = new ArrayList<>());
                }
                ids.add(getLong("INSTRUMENTID"));
            }

            @Override
            protected Map<Long, List<Long>> getResult() {
                return result;
            }
        }.read(this.file);

        final Map<Long, Long> tmpReverseMap = new HashMap<>();
        for (Map.Entry<Long, List<Long>> entry : tmpMap.entrySet()) {
            for (Long instrumentid : entry.getValue()) {
                if (tmpReverseMap.put(instrumentid, entry.getKey()) != null) {
                    this.logger.warn("<readData> duplicate instrumentid: " + instrumentid);
                }
            }
        }

        updateMaps(tmpMap, tmpReverseMap);
        this.logger.info("<readData> took " + tt);
    }

    private synchronized void updateMaps(Map<Long, List<Long>> tmpMap,
            Map<Long, Long> tmpReverseMap) {
        this.map = tmpMap;
        this.reverseMap = tmpReverseMap;
    }

    public static void main(String[] args) throws Exception {
        final UnderlyingShadowProviderImpl p = new UnderlyingShadowProviderImpl();
        p.setFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/istar-underlying-shadows.xml.gz"));
        p.setActiveMonitor(new ActiveMonitor());
        p.afterPropertiesSet();

        final List<Long> list = p.getInstrumentids(59893L);
        System.out.println(list);
    }
}
