/*
 * GenobrokerWp2ReverseMapper.java
 *
 * Created on 09.02.13 22:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.LocalConfigProvider;

/**
 * @author tkiesgen
 */
public class GenobrokerWp2ReverseMapper implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File file;

    private ActiveMonitor activeMonitor;

    private final Map<String, String> mappings = new HashMap<>();

    private static final Map<String, Map<String, String>> NON_UNIQUE_MAPPINGS = Collections.synchronizedMap(new HashMap<String, Map<String, String>>());

    static {
        final Map<String, String> usotc = new HashMap<>();
        usotc.put("QB", "NQB");
        usotc.put("BB", "NAT");
        usotc.put("PC", "NPS");
        usotc.put("QX", "NQX");
        usotc.put(null, "NAT");
        NON_UNIQUE_MAPPINGS.put("USOTC", usotc);

        final Map<String, String> n = new HashMap<>();
        n.put("_not_yet_defined_1", "NAR");
        n.put("_not_yet_defined_2", "NYS");
        n.put(null, "NYS");
        NON_UNIQUE_MAPPINGS.put("N", n);

        final Map<String, String> to = new HashMap<>();
        to.put("_not_yet_defined_1", "NCC");
        to.put("_not_yet_defined_2", "TOR");
        to.put(null, "TOR");
        NON_UNIQUE_MAPPINGS.put("TO", to);
    }

    public File getFile() {

        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        readMappings();

        final FileResource resource = new FileResource(this.file);
        resource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readMappings();
            }
        });
        this.activeMonitor.addResource(resource);
    }

    private void readMappings() {
        try {
            doReadMappings();
        } catch (IOException e) {
            this.logger.error("<readMappings> failed", e);
        }
    }

    private void doReadMappings() throws IOException {
        final Properties properties = new Properties();
        final FileReader fr = new FileReader(this.file);
        properties.load(fr);
        fr.close();

        final Map<String, String> map = new HashMap<>();

        @SuppressWarnings("unchecked")
        final Enumeration<String> en = (Enumeration<String>) properties.propertyNames();
        while (en.hasMoreElements()) {
            final String key = en.nextElement();
            final String value = properties.getProperty(key);

            final String[] tokens = value.split(Pattern.quote(","));
            for (final String token : tokens) {
                final String put = map.put(token, key);
                if (put != null) {
                    final Map<String, String> num = NON_UNIQUE_MAPPINGS.get(token);
                    if (!num.containsValue(key)) {
                        this.logger.warn("<doReadMappings> duplicate value for " + put + " on insert of " + token + "=>" + key);
                    }
                }
            }
        }

        synchronized (this) {
            this.mappings.clear();
            this.mappings.putAll(map);
        }
    }

    synchronized public String getMapping(String vwdMarket) {
        return this.mappings.get(vwdMarket);
    }

    synchronized public String getMapping(String vwdMarket, String marketSegment) {
        final Map<String, String> num = NON_UNIQUE_MAPPINGS.get(vwdMarket);
        if (num == null) {
            return getMapping(vwdMarket);
        }
        final String byMarketSegment = num.get(marketSegment);
        return byMarketSegment != null ? byMarketSegment : num.get(null);
    }

    public static void main(String[] args) throws Exception {
        final GenobrokerWp2ReverseMapper m = new GenobrokerWp2ReverseMapper();
        m.setFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/web/fiduciaMarketMappings.prop"));
        m.setActiveMonitor(new ActiveMonitor());
        m.afterPropertiesSet();

        System.out.println(m.getMapping("DTB"));
        System.out.println(m.getMapping("DTB", "xxx"));
        System.out.println(m.getMapping("USOTC", "BB"));
        System.out.println(m.getMapping("USOTC", "xx"));
    }
}
