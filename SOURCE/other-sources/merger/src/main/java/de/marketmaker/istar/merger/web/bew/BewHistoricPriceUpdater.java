/*
 * BewHistoricPriceUpdater.java
 *
 * Created on 10.12.2010 10:17:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.IoUtils;

/**
 * @author oflege
 */
class BewHistoricPriceUpdater {

    private static class Update {

        private final String vwdcode;
        private final String price;
        private final String date;
        private Update(String vwdcode, String price, String date) {
            this.vwdcode = vwdcode.trim();
            this.price = price.trim();
            this.date = date;
        }

        Document toDocument() throws IOException {
            return BewHistoricIndexBuilder.createDocument(this.vwdcode, this.price, this.date);
        }
    }

    private final static DateTimeFormatter[] DTFS = new DateTimeFormatter[] {
            DateTimeFormat.forPattern("dd.MM.yyyy"),
            DateTimeFormat.forPattern("yyyy-MM-dd")
    };

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FSDirectory indexDir;

    BewHistoricPriceUpdater(FSDirectory indexDir) {
        this.indexDir = indexDir;
    }

    boolean update(File f) {
        try {
            final List<Update> updates = parseUpdates(f);
            this.logger.info("<update> read " + updates.size() + " updates");
            updateIndex(updates);
            return true;
        } catch (IOException e) {
            this.logger.error("<addPricesToIndex> failed", e);
            return false;
        }
    }

    private void updateIndex(List<Update> updates) throws IOException {
        IndexWriter iw = null;
        try {
            iw = new IndexWriter(this.indexDir, new WhitespaceAnalyzer(),
                    false, IndexWriter.MaxFieldLength.LIMITED);

            this.logger.info("<updateIndex> deleting old entries...");
            for (Update update : updates) {
                iw.deleteDocuments(new Term(BewHistoricIndexBuilder.KEY_TERM, update.vwdcode));
            }

            this.logger.info("<updateIndex> adding new entries...");
            for (Update update : updates) {
                iw.addDocument(update.toDocument());
            }
            this.logger.info("<updateIndex> added " + updates.size() + " documents");
            iw.commit();
            iw.optimize(true);
            this.logger.info("<updateIndex> optimized index");
        } finally {
            IoUtils.close(iw);
        }
    }

    private List<Update> parseUpdates(File f) throws IOException {
        final List<Update> updates = new ArrayList<>(100);
        Scanner sc = null;
        int lineNo = 0;
        try {
            sc = new Scanner(f);
            while (sc.hasNextLine()) {
                lineNo++;
                final String line = sc.nextLine();
                if (!StringUtils.hasText(line) || line.startsWith("#")) {
                    continue;
                }
                final String[] tokens = line.split(";");
                if (tokens.length != 3) {
                    throw new IOException("line " + lineNo + " is invalid:  '" + line + "'");
                }
                final DateTime date = getDate(tokens[2]);
                if (date == null) {
                    throw new IOException("line " + lineNo + " invalid date: " + tokens[2]);
                }
                updates.add(new Update(tokens[0], tokens[1],
                        BewHistoricPriceProviderImpl.DTF.print(date)));
            }
        } finally {
            if (sc != null) {
                sc.close();
            }
        }
        return updates;
    }

    private DateTime getDate(String s) {
        for (DateTimeFormatter dtf : DTFS) {
            try {
                return dtf.parseDateTime(s);
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
        return null;
    }

}
