/*
 * BewHistoricSnap.java
 *
 * Created on 26.10.2010 13:33:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.VolatileFeedDataRegistry;
import de.marketmaker.istar.feed.opra.OpraWriter;
import de.marketmaker.istar.feed.ordered.OrderedFeedDataFactory;
import de.marketmaker.istar.feed.ordered.OrderedFileSnapStore;

/**
 * Used to create lucene indexes based on snap files and opra files. These files usually contain
 * the prices that are valid at the end of month or year. To be able to retrieve those prices
 * fast without having to keep everything in memory, a lucene index is built for each date<p>
 * All documents in the index have an unanalyzed but indexed key field containing the vwdcode
 * (uppercase); this is the term that can be searched for. All other fields are stored but not indexed.
 * For data from snap fields, the document contains an offset field; its value can be used to read
 * a particular snap record from the snap file. All other documents contain a price field and a
 * date field that contain the valuation price and date, respectively.<p><p>
 * If baseDir's name matches a date (format yyyyMMdd) this class creates a single index in
 * baseDir, otherwise it looks for subfolders in baseDir whose name matches yyyyMMdd and creates
 * an index in each of these subfolders.<p>
 * For each index, the data is taken from a snap-file, whose name must be "snap.sd3", and from
 * an opra file, whose name must be "opra-static-data.txt.gz". Both files are optional.<p>
 * Once the index has been created, it can ba made available to a
 * {@link de.marketmaker.istar.merger.web.bew.BewHistoricPriceProviderImpl}, which expects the
 * same directory layout, with indexes already present. If a snap.sd3 file has been used, it has
 * to be made available as well, the relevant data for opra items is entirely stored in the index,
 * so the opra-file does not have to be copied.
 *
 * @author oflege
 */
public class BewHistoricIndexBuilder implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IndexWriter iw;

    private File baseDir;

    static final FileFilter DIR_FILTER = new FileFilter() {
        public boolean accept(File d) {
            return d.isDirectory() && d.getName().matches("[0-9]{8}");
        }
    };

    static final String OFFSET_TERM = "offset";

    static final String PRICE_TERM = "price";

    static final String DATE_TERM = "date";

    static final String KEY_TERM = "key";

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public void afterPropertiesSet() throws Exception {
        final File[] dirs = DIR_FILTER.accept(this.baseDir)
                ? new File[]{this.baseDir}
                : this.baseDir.listFiles(DIR_FILTER);

        for (File dir : dirs) {
            createIndex(dir);
        }
    }

    private void createIndex(File dir) throws Exception {
        this.logger.info("<createIndex> in " + dir.getAbsolutePath());

        final File indexDir = new File(dir, "index");
        if (!indexDir.exists() && !indexDir.mkdir()) {
            throw new IOException("failed to create " + indexDir.getAbsolutePath());
        }

        this.iw = new IndexWriter(FSDirectory.open(indexDir),
                new WhitespaceAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
        this.iw.setRAMBufferSizeMB(96);
        this.iw.setUseCompoundFile(true);

        TimeTaker tt = new TimeTaker();

        final File snap3File = new File(dir, "snap.sd3");

        if (snap3File.canRead()) {
            addSnap3(snap3File);
            this.logger.info("<createIndex> added " + snap3File.getAbsolutePath());
        }

        final File opraFile = new File(dir, "opra-static-data-morningstar.txt.gz");
        if (opraFile.canRead()) {
            addOpra(opraFile);
            this.logger.info("<createIndex> added " + opraFile.getAbsolutePath());
        }

        final File bewOldFile = new File(dir, "bew-alt-last-years-close.txt");
        if (bewOldFile.canRead()) {
            addBewOld(bewOldFile);
            this.logger.info("<createIndex> added " + bewOldFile.getAbsolutePath());
        }

        this.iw.commit();
        this.logger.info("<createIndex> took " + tt + ", for " + iw.numDocs() + " items");

        tt = new TimeTaker();
        this.iw.optimize();
        this.logger.info("<createIndex> took " + tt + ", for optimize");
        this.iw.close();
    }

    private void addBewOld(File bewOldFile) throws IOException {
        final Scanner sc = new Scanner(new FileInputStream(bewOldFile));
        sc.nextLine(); // ignore header line
        while (sc.hasNextLine()) {
            final String line = sc.nextLine();
            if (StringUtils.hasText(line)) {
                final String[] tokens = line.split(";");
                addBew(tokens);
            }
        }
    }

    private void addBew(String[] tokens) throws IOException {
        this.iw.addDocument(createDocument(tokens[0].substring(tokens[0].indexOf('.') + 1),
                tokens[1], "2009" + tokens[2].substring(3, 5) + tokens[2].substring(0, 2)));
    }

    private void addOpra(File opraFile) throws IOException {
        int lineNo = 0;
        boolean opraWriter = false;
        try (final Scanner sc = new Scanner(new GZIPInputStream(new FileInputStream(opraFile)))) {
            while (sc.hasNextLine()) {
                final String line = sc.nextLine();
                if (0 == lineNo++ && line.startsWith(OpraWriter.HEADER)) {
                    opraWriter = true;
                    continue;
                }

                if (StringUtils.hasText(line)) {
                    final String[] tokens = line.split(";");
                    if (opraWriter) {
                        addOpraNew(tokens);
                    }
                    else {
                        addOpra(tokens);
                    }
                }
            }
        }
    }

    private void addOpraNew(String[] tokens) throws IOException {
        if (hasSettlement(tokens, 2)) {
            this.iw.addDocument(createDocument(tokens[0], tokens[2], tokens[3]));
        }
    }

    private void addOpra(String[] tokens) throws IOException {
        final int settlementIndex;
        if (hasSettlement(tokens, 8)) {
            settlementIndex = 8;
        }
        else if (hasSettlement(tokens, 10)) {
            settlementIndex = 10;
        }
        else if (hasSettlement(tokens, 12)) {
            settlementIndex = 12;
        }
        else {
            settlementIndex = 0;
        }

        if (settlementIndex > 0) {
            this.iw.addDocument(createDocument(tokens[7], tokens[settlementIndex],
                    dateFromOpraDate(tokens[settlementIndex + 1])));
        }
    }

    static Document createDocument(final String key, final String price, final String date) 
            throws IOException {
        final Document result = new Document();
        result.add(keyField(key));
        result.add(storedField(PRICE_TERM, price));
        result.add(storedField(DATE_TERM, date));
        return result;
    }

    private String dateFromOpraDate(String token) {
        // convert dd-MM-yyyy to yyyyMMdd
        return new StringBuilder(10)
                .append(token.substring(6))
                .append(token.substring(3, 5))
                .append(token.substring(0, 2)).toString();
    }

    private boolean hasSettlement(String[] s, int i) {
        return s.length > (i + 1) && StringUtils.hasText(s[i])
                && StringUtils.hasText(s[i + 1]) && !"0".equals(s[i + 1]);
    }

    private void addSnap3(File snapFile) throws Exception {
        final OrderedFileSnapStore store = new OrderedFileSnapStore();
        store.setRegistry(new VolatileFeedDataRegistry() {
            @Override
            public FeedData register(Vendorkey vkey) {
                return OrderedFeedDataFactory.RT.create(vkey, null);
            }
        });
        store.setRestoreDeletesUnknownFields(false);
        store.setSnapFile(snapFile);
        store.restore(new OrderedFileSnapStore.RestoreCallback() {
            @Override
            public void restored(FeedData data, long offset) {
                if (!data.getSnapData(true).isInitialized()) {
                    return;
                }
                try {
                    addSnap(data.getVwdcode(), offset);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void addSnap(final ByteString vwdcode, long offset) throws IOException {
        final Document d = new Document();
        d.add(keyField(vwdcode.toString()));
        d.add(storedField(OFFSET_TERM, Long.toString(offset, Character.MAX_RADIX)));
        this.iw.addDocument(d);
    }

    static Field storedField(final String name, final String value) {
        return new Field(name, value, Field.Store.YES, Field.Index.NO);
    }

    static Field keyField(final String s) {
        return new Field(KEY_TERM, s, Field.Store.NO, Field.Index.NOT_ANALYZED);
    }

    public static void main(String[] args) throws Exception {
        BewHistoricIndexBuilder builder = new BewHistoricIndexBuilder();
        builder.setBaseDir(new File(args[0]));
        builder.afterPropertiesSet();
    }
}
