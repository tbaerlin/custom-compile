/*
 * QuoteMetadataPostprocessor.java
 *
 * Created on 31.10.12 22:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.mdpsexport;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author mwilke
 */
public class QuoteMetadataPostprocessor implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File full;

    private File update;

    private Map<String, String> updateMap = new HashMap<>();

    public void setFull(File full) {
        this.full = full;
    }

    public void setUpdate(File update) {
        this.update = update;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        File u = getNewestFileFromDir(this.update);
        this.logger.info("<afterPropertiesSet> reading " + u);
        read(u);

        removeUnchanged(getNewestFileFromDir(this.full));

        File tmp = new File(this.update.getAbsolutePath() + ".tmp");
        try (PrintWriter pw = new PrintWriter(tmp)) {
            for (Map.Entry<String, String> e : updateMap.entrySet()) {
                pw.println(e.getKey() + ";" + e.getValue());
            }
        }
        u.renameTo(new File(u.getAbsolutePath() + ".in"));
        tmp.renameTo(u);
    }

    private void read(File file) throws IOException {
        try (Scanner scanner = createScanner(file)) {
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(";");
                this.updateMap.put(line[0], line[1].intern());
            }
            this.logger.info("<read> read " + this.updateMap.size() + " items");
        }
    }

    private void removeUnchanged(File file) throws IOException {
        try (Scanner scanner = createScanner(file)) {
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(";");
                final String existing = this.updateMap.get(line[0]);
                if (existing != null && line[1].equals(existing)) {
                    this.updateMap.remove(line[0]);
                }
            }
            this.logger.info("<removeUnchanged> kept " + updateMap.size() + " items");
        }
    }

    private Scanner createScanner(File file) throws IOException {
        this.logger.info("<createScanner> for " + file.getAbsolutePath());
        return new Scanner(file.getName().endsWith("gz") ?
                new GZIPInputStream(new FileInputStream(file)) : new FileInputStream(file));
    }

    private File getNewestFileFromDir(File dir) {
        File[] list = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith("mm-mdps-quotemetadata");
            }
        });

        Arrays.sort(list, new Comparator() {
            public int compare(Object f1, Object f2) {
                return -Long.valueOf(((File) f1).lastModified()).compareTo(
                        ((File) f2).lastModified());
            }

        });
        return list[0];
    }
}
