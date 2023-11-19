/*
 * FileRatioDataStore.java
 *
 * Created on 28.10.2005 08:37:13
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * Stores ratio data in files per InstrumentTypeEnum and also allows to restore ratio data
 * from those files.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FileRatioDataStore implements RatioDataStore {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File baseDir;

    static final int VERSION = 8;

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    private File getFile(InstrumentTypeEnum type) {
        return new File(this.baseDir, "ratio-" + type.name().toLowerCase() + ".rda");
    }

    public void store(TypeData data) throws IOException {
        if (this.baseDir == null) {
            this.logger.info("<store> not feasible, no baseDir defined");
            return;
        }

        new FileRatioDataWriter(getFile(data.getType()), data, VERSION).write();
    }

    public void restore(InstrumentTypeEnum type, Consumer<RatioData> consumer) throws Exception {
        final File file = getFile(type);
        if (!file.canRead()) {
            this.logger.warn("<restore> cannot read " + file.getAbsolutePath() + ", returning");
            return;
        }

        final int version = readVersion(file);
        if (version != VERSION) {
            throw new IllegalArgumentException("cannot read version " + version);
        }

        new FileRatioDataReader(file, type, consumer).read();
    }

    private int readVersion(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             DataInputStream dis = new DataInputStream(fis)) {
            return Integer.reverseBytes(dis.readInt());
        }
    }

    public static void main(String[] args) throws Exception {
        final FileRatioDataStore store = new FileRatioDataStore();
        File dir = args.length > 0
                ? new File(args[0])
                : LocalConfigProvider.getProductionDir("var/data/ratios/");
        store.setBaseDir(dir);

        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(args[i]);
                restoreAndStore(store, type);
            }
        }
        else if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                final String s = f.getName();
                if (s.endsWith(".rda")) {
                    final String typeStr = s.substring(s.indexOf('-') + 1, s.lastIndexOf('.'));
                    InstrumentTypeEnum type = InstrumentTypeEnum.valueOf(typeStr.toUpperCase());
                    restoreAndStore(store, type);
                }
            }
        }
    }

    private static void restoreAndStore(FileRatioDataStore store,
            InstrumentTypeEnum t) throws Exception {
        final TypeData td = new TypeData(t, 1);
        store.restore(t, td);
        store.store(td);
    }
}
