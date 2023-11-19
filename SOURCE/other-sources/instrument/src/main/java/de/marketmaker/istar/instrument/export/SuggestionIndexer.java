/*
 * SuggestionIndexer.java
 *
 * Created on 09.08.2010 15:28:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.instrument.protobuf.InstrumentProtos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.util.StringUtils;
import org.xerial.snappy.Snappy;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_ENTITLEMENT_VWD;
import static de.marketmaker.istar.instrument.IndexConstants.FIELDNAME_TYPE;
import static de.marketmaker.istar.instrument.SuggestIndexConstants.*;

/**
 * Builds a Lucene index that can be used to get suggestions for instruments. The index is a lot
 * smaller than the regular search index and comes with its own serialized data structure file to
 * speed up searching (as a search usually has to finish between two keystrokes of a user entering
 * the instrument name).
 * <p>
 * Data created by this class will be used by an instance of
 * {@link de.marketmaker.istar.instrument.search.SuggestionSearcherImpl}, so changes in the data
 * format have to be reflected in that class as well.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @since 1.2
 */
public class SuggestionIndexer {
    static final int MIN_VERSION = 2;

    static final int MAX_VERSION = 2;

    /**
     * for STKs whose name starts with these prefixes, the text after this prefix will be
     * indexed as a name as well (e.g., for "Deutsche Lufthansa", Lufthansa will be indexed.
     */
    private static final Set<String> STK_PREFIX_WORDS = new HashSet<>(Arrays.asList(
            "deutsche",   // Deutsche Lufthansa
            "koninklijke" // Koninklijke Philips
    ));

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void index(File dataFile, File indexDir) throws IOException {
        this.logger.info("<index> to index suggestions ...");
        final TimeTaker tt = new TimeTaker();

        final Directory dir = FSDirectory.open(indexDir);
        final IndexWriter iw = new IndexWriter(dir, new KeywordAnalyzer(), true,
                IndexWriter.MaxFieldLength.LIMITED);

        int n = 0;
        final ByteBuffer bb = mapData(dataFile);

        final int version;
        if (bb.get() == 0) {
            // version > 0: first two bytes are 0x00 0x.., the second being the version
            version = bb.get() & 0xFF;
        }
        else {
            // version == 0: file starts with number of strategies (always != 0)
            version = 0;
            bb.position(0);
        }
        if (version < MIN_VERSION || version > MAX_VERSION) {
            throw new IllegalStateException("cannot handle version " + version);
        }

        final String[] strategies = readStrings(bb);

        while (bb.hasRemaining()) {
            final int offset = bb.position();

            byte[] data = getAndUncompress(bb);
            InstrumentProtos.Suggestion suggestion
                    = InstrumentProtos.Suggestion.parseFrom(data);

            final Document d = createDocument(offset, suggestion, strategies);
            iw.addDocument(d);

            if (++n % 10000 == 0) {
                this.logger.info("<index> added " + n);
            }
        }
        this.logger.info("<index> added " + n);

        iw.optimize();
        IoUtils.close(iw);

        this.logger.info("<index> indexing suggestions took: " + tt);
    }

    private byte[] getAndUncompress(ByteBuffer bb) throws IOException {
        byte[] data = new byte[bb.getShort() & 0xFFFF];
        bb.get(data);
        return Snappy.uncompress(data);
    }

    private Document createDocument(int offset, InstrumentProtos.Suggestion s,
            String[] strategies) {
        final Document doc = new Document();
        doc.add(newField(FIELDNAME_OFFSET, Integer.toString(offset, Character.MAX_RADIX),
                Field.Store.YES));

        final InstrumentTypeEnum type = InstrumentTypeEnum.values()[s.getTypeOrd()];
        doc.add(newField(FIELDNAME_TYPE, type.name(), Field.Store.NO));

        String name = s.getName();

        addName(doc, name, type, FIELDNAME_NAME);
        if (s.hasWmWpNameKurz()) {
            addName(doc, s.getWmWpNameKurz(), type, FIELDNAME_WM_WP_NAME_KURZ);
        }

        addName(doc, s.hasPmNameCost() ? s.getPmNameCost() : s.getName(), type, FIELDNAME_NAME_COST);
        addName(doc, s.hasPmNameFree() ? s.getPmNameFree() : s.getName(), type, FIELDNAME_NAME_FREE);

        if (s.hasIsin()) {
            add(doc, FIELDNAME_ISIN, s.getIsin());
        }
        if (s.hasWkn()) {
            add(doc, FIELDNAME_WKN, s.getWkn());
        }

        for (int i = 0; i < s.getRanksCount(); i++) {
            doc.add(newField(strategies[i], "" + s.getRanks(i), Field.Store.NO));
        }

        for (int i = 0; i < s.getEntitlementsCount(); i++) {
            doc.add(newField(FIELDNAME_ENTITLEMENT_VWD,
                    EntitlementsVwd.toEntitlement(s.getEntitlements(i)).toLowerCase(), Field.Store.NO));
        }
        return doc;
    }

    private void add(Document d, String field, String s) {
        if (s == null) {
            return;
        }
        final String t = s.toLowerCase();
        for (int i = 1; i < s.length(); i++) {
            d.add(newField(field, t.substring(0, i), Field.Store.NO));
        }
        d.add(newField(field, t, Field.Store.NO));
    }

    private void addName(Document d, String s, InstrumentTypeEnum type, final String field) {
        if (s == null) {
            return;
        }

        String t = s.toLowerCase();
        int n = 0;
        do {
            add(d, field, t);
            t = removePrefix(t, type, ++n);
        } while (StringUtils.hasText(t) && t.length() >= 3);
    }

    private String removePrefix(String s, InstrumentTypeEnum type, int i) {
        final int p = s.indexOf(' ');
        if (type == InstrumentTypeEnum.FND) {
            if (i == 1 && p > 0) {
                return s.substring(p + 1);
            }
        }
        else if (type == InstrumentTypeEnum.STK) {
            if (i == 1) {
                if (p > 0) {
                    String prefix = s.substring(0, p);
                    if (prefix.endsWith(".") || STK_PREFIX_WORDS.contains(prefix)) {
                        return s.substring(p + 1);
                    }
                }
                // HACK for prefixes that don't end with a space
                if (s.startsWith("a.p.møller") || s.startsWith("s&p/")) {
                    return s.substring(4);
                }
            }
        }
        return null;
    }

    private Field newField(final String field, String value, final Field.Store store) {
        final Field result = new Field(field, value, store, Field.Index.NOT_ANALYZED_NO_NORMS);
        result.setOmitNorms(true);
        result.setOmitTermFreqAndPositions(true);
        return result;
    }

    private ByteBuffer mapData(File dataFile) throws IOException {
        final FileChannel ch = new RandomAccessFile(dataFile, "r").getChannel();
        final MappedByteBuffer bb = ch.map(FileChannel.MapMode.READ_ONLY, 0, ch.size());
        IoUtils.close(ch);
        return bb;
    }

    private String readString(ByteBuffer bb) {
        final int len = bb.get() & 0xFF;
        if (len == 0) {
            return null;
        }
        final byte[] b = new byte[len];
        bb.get(b);
        return new String(b, SuggestionExporter.CHARSET);
    }

    private String[] readStrings(ByteBuffer bb) {
        final int numEnts = bb.get();
        final String[] result = new String[numEnts];
        for (int i = 0; i < numEnts; i++) {
            result[i] = readString(bb);
        }
        return result;
    }
}
