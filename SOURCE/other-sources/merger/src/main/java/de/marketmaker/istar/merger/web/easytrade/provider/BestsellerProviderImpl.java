/*
 * BestsellerProviderImpl.java
 *
 * Created on 19.07.2006 22:16:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.provider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.DirectoryResource;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.instrument.InstrumentServer;
import de.marketmaker.istar.instrument.search.SearchRequestStringBased;
import de.marketmaker.istar.instrument.search.SearchResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BestsellerProviderImpl implements BestsellerProvider, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyyMMdd");
    private ActiveMonitor activeMonitor;

    private InstrumentServer instrumentServer;
    private File baseDir;
    private boolean readSavingplans;
    private InstrumentTypeEnum[] types = new InstrumentTypeEnum[0];

    private final Map<String, Bestseller> bestseller = new ConcurrentHashMap<>();
    private static final String SAVINGPLANS_KEY = "sparplaene";

    public void setTypes(String[] types) {
        this.types = new InstrumentTypeEnum[types.length];
        for (int i = 0; i < types.length; i++) {
            this.types[i]= InstrumentTypeEnum.valueOf(types[i]);
        }
    }

    public void setReadSavingplans(boolean readSavingplans) {
        this.readSavingplans = readSavingplans;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
        if (!this.baseDir.isDirectory()) {
            throw new IllegalArgumentException("not a directory: " + this.baseDir);
        }
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setInstrumentServer(InstrumentServer instrumentServer) {
        this.instrumentServer = instrumentServer;
    }

    public void afterPropertiesSet() throws Exception {
        readBestseller();

        final DirectoryResource resource = new DirectoryResource(this.baseDir.getAbsolutePath());
        resource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    readBestseller();
                }
                catch (Exception e) {
                    logger.error("<propertyChange> failed", e);
                }
            }
        });

        this.activeMonitor.addResource(resource);
    }

    private void readBestseller() throws Exception {
        for (final InstrumentTypeEnum type : this.types) {
            final File[] files =getFiles(type.name().toLowerCase()+"_bestseller");

            if (files.length == 0) {
                this.logger.warn("<initialize> no files for type: " + type.name());
                continue;
            }

            sort(files);

            this.bestseller.put(type.name(), createBestseller(type, files[0]));
        }

        readSavingplans();

        this.logger.info("<readBestseller> bestseller: " + this.bestseller);
    }

    private void readSavingplans() throws Exception {
        if(!this.readSavingplans) {
            return;
        }

        final File[] files = getFiles("fnd_sparplaene");

        if (files.length == 0) {
            this.logger.warn("<initialize> no files for type: FND_sparplaene");
            return;
        }

        sort(files);

        this.bestseller.put(SAVINGPLANS_KEY, createBestseller(InstrumentTypeEnum.FND, files[0]));
    }

    private File[] getFiles(final String prefix) {
        return this.baseDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().startsWith(prefix);
            }
        });
    }

    private void sort(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            // names are foo_yyyyMMdd.suffix, in order to sort in descending yyyyMMdd order,
            // we just have to compare the names and invert the result
            public int compare(File o1, File o2) {
                return -o1.getName().compareTo(o2.getName());
            }
        });
    }

    private Bestseller createBestseller(InstrumentTypeEnum type, File file) throws Exception {
        final DateTime dateTime = getDateTime(file);
        final Bestseller bestseller = new Bestseller(type, dateTime);

        final InputStream is = file.getName().endsWith(".gz")
                ? new GZIPInputStream(new FileInputStream(file))
                : new FileInputStream(file);

        final Scanner scanner = new Scanner(is);
        // skip header
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] tokens = line.split(";");
            if (tokens.length != 3) {
                continue;
            }
            final String isin = tokens[1].trim();
            final SearchRequestStringBased ir = new SearchRequestStringBased();
            ir.setMaxNumResults(1);
            ir.setSearchExpression(KeysystemEnum.ISIN.name().toLowerCase() + ":" + isin);
            final SearchResponse sr = this.instrumentServer.search(ir);
            final List<Instrument> instruments = sr.getInstruments();
            if (instruments.isEmpty()) {
                this.logger.warn("<createBestseller> no instrument for isin " + isin);
                continue;
            }

            bestseller.add(instruments.get(0).getId());
        }
        scanner.close();
        is.close();
        return bestseller;
    }

    private DateTime getDateTime(File file) {
        final String ts = file.getName().substring(file.getName().lastIndexOf("_") + 1, file.getName().indexOf(".csv"));
        return DTF.parseDateTime(ts);
    }

    public Bestseller getBestseller(InstrumentTypeEnum type) {
        return this.bestseller.get(type.name());
    }

    public Bestseller getSavingplans() {
        return this.bestseller.get(SAVINGPLANS_KEY);
    }
}
