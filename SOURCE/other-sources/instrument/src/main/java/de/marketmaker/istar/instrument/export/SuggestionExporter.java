/*
 * SuggestionExporter.java
 *
 * Created on 09.08.2010 15:27:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategies;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.protobuf.SuggestionSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.xerial.snappy.Snappy;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static de.marketmaker.istar.instrument.export.SuggestionIndexer.MAX_VERSION;
import static de.marketmaker.istar.instrument.export.SuggestionIndexer.MIN_VERSION;

/**
 * Creates a suggestion data file based on instruments and their indexes, processed by
 * {@link SuggestionRankings}.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @since 1.2
 */
public class SuggestionExporter implements InitializingBean {
    public static final Charset CHARSET = Charset.forName("UTF-8");

    /**
     * Types of instruments that will be exported; to export instruments of other types,
     * their iid needs to be added to {@link #allowedIids}
     */
    private static final Set<InstrumentTypeEnum> TYPES = EnumSet.of(
            InstrumentTypeEnum.IND,
            InstrumentTypeEnum.FND,
            InstrumentTypeEnum.STK
    );

    private static final int MAX_ENTITLEMENT = EntitlementsVwd.toValue("20Z");

    private static final int DEFAULT_FILE_VERSION = MAX_VERSION;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Set<Long> allowedIids = new HashSet<>();

    private SuggestionRankings rankings;

    private int fileVersion = DEFAULT_FILE_VERSION;

    public void setFileVersion(int fileVersion) {
        this.fileVersion = fileVersion;
    }

    public void setAllowedIids(String[] allowedIids) {
        for (String allowedIid : allowedIids) {
            this.allowedIids.add(Long.parseLong(allowedIid));
        }
    }

    public void setRankings(SuggestionRankings rankings) {
        this.rankings = rankings;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.rankings, "suggestion rankings required");
        if (this.fileVersion < MIN_VERSION || this.fileVersion > MAX_VERSION) {
            throw new IllegalArgumentException("illegal version: " + this.fileVersion);
        }
    }

    public void export(File dataFile, File instrumentIndexDir, InstrumentDao instrumentDao,
            File tmpDir) throws Exception {
        this.logger.info("<export> to export suggestions ...");
        final TimeTaker tt = new TimeTaker();


        this.rankings.setInstrumentDao(instrumentDao);
        this.rankings.setIndexDir(instrumentIndexDir);
        this.rankings.initialize();

        final File tmpDataFile = InstrumentSystemUtil.getFile(tmpDir,
                dataFile.getName() + System.currentTimeMillis(), true);

        createDataFile(tmpDataFile, instrumentDao);

        InstrumentSystemUtil.replace(dataFile, tmpDataFile);

        this.logger.info("<export> exporting suggestions took: " + tt);
    }

    private void createDataFile(File suggestFile, InstrumentDao idf) throws Exception {
        final ByteBuffer bb = ByteBuffer.allocate(96 * 1024);
        if (suggestFile.exists() && !suggestFile.delete()) {
            throw new IllegalStateException("Could not delete " + suggestFile.getAbsolutePath());
        }

        final FileChannel ch = new RandomAccessFile(suggestFile, "rw").getChannel();

        if (this.fileVersion > 0) {
            bb.put((byte) 0);
            bb.put((byte) this.fileVersion);
        }

        final String[] strategies = this.rankings.getStrategyNames();
        write(bb, strategies);

        final Set<String> ents = new HashSet<>();

        SuggestionSerializer serializer = new SuggestionSerializer(this.rankings);

        int n = 0;
        for (Instrument instrument : idf) {
            if (!isAcceptable(instrument)) {
                continue;
            }

            collectEntitlements(ents, instrument);

            if (this.fileVersion < 2) {
                for (int i = 0; i < strategies.length; i++) {
                    bb.putShort(this.rankings.getOrder(instrument, i));
                }

                bb.putLong(instrument.getId());
                bb.put((byte) instrument.getInstrumentType().ordinal());

                write(bb, InstrumentNameStrategies.DEFAULT.getName(instrument));
                if (this.fileVersion > 0) {
                    write(bb, InstrumentNameStrategies.WM_WP_NAME_KURZ.getName(instrument));
                }
                write(bb, instrument.getSymbolIsin());
                write(bb, instrument.getSymbolWkn());

                write(bb, ents);
            }
            else {
                byte[] serialized = serializer.serialize(instrument, ents);
                byte[] compressed = Snappy.compress(serialized);
                bb.putShort((short) compressed.length).put(compressed);
            }

            if (bb.remaining() < 1024) {
                bb.flip();
                ch.write(bb);
                bb.clear();
            }

            if (++n % 10000 == 0) {
                this.logger.info("<createDataFile> added " + n);
            }
        }

        bb.flip();
        ch.write(bb);

        IoUtils.close(ch);

        this.logger.info("<createDataFile> exported " + n + " instruments as suggestions");
    }

    private boolean hasValidQuote(Instrument instrument) {
        for (Quote quote : instrument.getQuotes()) {
            if (isValidQuote(quote)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAcceptable(Instrument instrument) {
        if (instrument.getId() < 0) {
            return false;
        }
        if (!TYPES.contains(instrument.getInstrumentType())
                && !this.allowedIids.contains(instrument.getId())) {
            return false;
        }
        if (!StringUtils.hasText(instrument.getName())
                || instrument.getName().startsWith(InstrumentReader.ID_NAME_PREFIX)) {
            return false;
        }
        if (!hasValidQuote(instrument)) {
            return false;
        }
        if (instrument.getInstrumentType() == InstrumentTypeEnum.STK
                && instrument.getQuotes().size() < 2) {
            return false;
        }
        return instrument.getInstrumentType() != InstrumentTypeEnum.FND
                || this.rankings.hasFundRank(instrument.getId());
    }

    private boolean isValidQuote(Quote quote) {
        return StringUtils.hasText(quote.getSymbolVwdcode())
                && StringUtils.hasText(quote.getSymbolMmwkn());
    }

    private void collectEntitlements(Set<String> ents, Instrument instrument) {
        ents.clear();
        for (Quote quote : instrument.getQuotes()) {
            final String[] strings = getEntitlements(quote);
            if (strings != null) {
                for (String string : strings) {
                    if (EntitlementsVwd.toValue(string) <= MAX_ENTITLEMENT) {
                        ents.add(string);
                    }
                }
            }
        }
    }

    private String[] getEntitlements(Quote q) {
        return isValidQuote(q) ? q.getEntitlement().getEntitlements(KeysystemEnum.VWDFEED) : null;
    }

    private void write(ByteBuffer bb, Set<String> strings) {
        bb.put((byte) strings.size());
        for (String s : strings) {
            write(bb, s);
        }
    }

    private void write(ByteBuffer bb, String[] strings) {
        bb.put((byte) strings.length);
        for (String s : strings) {
            write(bb, s);
        }
    }

    private void write(ByteBuffer bb, String s) {
        if (!StringUtils.hasText(s)) {
            bb.put((byte) 0);
            return;
        }
        final byte[] bytes = s.getBytes(CHARSET);
        bb.put((byte) bytes.length);
        bb.put(bytes);
    }
}
