/*
 * ClearNews.java
 *
 * Created on 05.12.2005 07:26:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.store.FSDirectory;

import de.marketmaker.istar.news.backend.NewsQuerySupport;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsCli {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: NewsCli indexDir command [options]");
            System.exit(-1);
        }

        long now = System.currentTimeMillis();

        final FSDirectory dir = FSDirectory.open(new File(args[0]));

        if ("optimize".startsWith(args[1])) {
            System.out.println("optimize...");
            IndexWriter w = new IndexWriter(dir, new SimpleAnalyzer(), false, IndexWriter.MaxFieldLength.UNLIMITED);
            System.out.println("docCount=" + w.maxDoc());
            w.optimize();
            w.close();
        }
        else if ("merge".startsWith(args[1])) {
            System.out.println("merge...");
            IndexWriter w = new IndexWriter(dir, new SimpleAnalyzer(), !IndexReader.indexExists(dir),
                    IndexWriter.MaxFieldLength.UNLIMITED);
            IndexReader[] readers = new IndexReader[args.length - 2];
            for (int i = 2; i < args.length; i++) {
                readers[i - 2] = IndexReader.open(FSDirectory.open(new File(args[i])), true);
            }
            w.addIndexes(readers);
            w.close();
            for (IndexReader reader : readers) {
                reader.close();
            }
        }
        else if ("create".startsWith(args[1])) {
            IndexWriter iw = new IndexWriter(dir, new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
            iw.close();
        }
        else {
            final IndexReader reader = IndexReader.open(dir, true);
            System.out.println("maxDoc = " + reader.maxDoc());

            try {
                doRead(args, reader);
            } finally {
                reader.close();
            }
        }

        System.out.println(args[1] + " took: " + (System.currentTimeMillis() - now));
    }

    private static void doRead(String[] args, IndexReader reader) throws Exception {
        if ("count".startsWith(args[1])) {
            if (args.length < 4) {
                System.err.println("Usage: LuceneCli indexDir count field startTem [endTerm]");
            }
            final Term term = new Term(args[2], args[3]);
            final Term endTerm = args.length > 4 ? new Term(args[2], args[4]) : null;

            final TermEnum termEnum = reader.terms(term);
            if (termEnum.term() == null) {
                System.err.println("No terms for field " + args[2]);
                return;
            }
            int n = 0;
            do {
                n++;
            } while (termEnum.next() && termEnum.term().field().equals(term.field())
                    && (endTerm == null || endTerm.text().compareTo(termEnum.term().text()) >= 0));
            System.out.println("#terms=" + n);
        }
        else if ("search".startsWith(args[1])) {
            Term t = new Term(args[2], args[3]);
            search(reader, new TermQuery(t), copyOfRange(args, 4));
        }
        else if ("query".startsWith(args[1])) {
            final String[] options = copyOfRange(args, 3);
            if (new File(args[2]).canRead()) {
                for (String s : Files.readAllLines(new File(args[2]).toPath(), Charset.defaultCharset())) {
                    search(reader, NewsQuerySupport.parse(s), options);
                }
            }
            else {
                search(reader, NewsQuerySupport.parse(args[2]), options);
            }
        }
        else if ("print".startsWith(args[1])) {
            if (args.length < 4) {
                System.err.println("Usage: LuceneCli indexDir print field startTem [endTerm]");
            }
            System.out.println("print...");
            final Term term = new Term(args[2], args[3]);
            final Term endTerm = args.length > 4 ? new Term(args[2], args[4]) : null;

            int total = 0;

            final TermEnum termEnum = reader.terms(term);
            if (termEnum.term() == null || !termEnum.term().field().equals(term.field())) {
                System.err.println("No terms for field " + args[2]);
                return;
            }
            //noinspection StringEquality
            do {
                final int n = termEnum.docFreq();
                total += n;
                System.out.println(termEnum.term().text() + ": #" + n);
            } while (termEnum.next() && termEnum.term().field() == term.field()
                    && (endTerm == null || endTerm.text().compareTo(termEnum.term().text()) >= 0));
            System.out.println("#total = " + total);
        }
        else if ("terms".startsWith(args[1])) {
            System.out.println("terms...");

            final Collection fieldNames = reader.getFieldNames(IndexReader.FieldOption.ALL);
            for (Object fieldName : fieldNames) {
                System.out.println(" fieldName = " + fieldName);
            }
        }
    }

    private static String[] copyOfRange(String[] args, final int from) {
        if (args.length <= from) {
            return new String[0];
        }
        String[] result = new String[args.length - from];
        System.arraycopy(args, from, result, 0, result.length);
        return result;
    }

    private static void search(IndexReader reader, final Query query, String... args)
            throws IOException {
        System.out.println("query = " + query);
        long now = System.currentTimeMillis();

        final IndexSearcher searcher = new IndexSearcher(reader);

        final Sort sort = new Sort(new SortField("timestamp", SortField.INT, true));

        int n = 0;
        PrintWriter pw = null;
        if (n < args.length && "-o".equals(args[n])) {
            pw = new PrintWriter(new File(args[1]));
            n = 2;
        }

        final int numHits = n < args.length ? Integer.parseInt(args[n++]) : 1000;

        final TopFieldCollector collector =
                TopFieldCollector.create(sort, numHits, true, false, false, false);
        searcher.search(query, collector);
        final TopDocs docs = collector.topDocs();

        if (pw != null || n < args.length) {
            final String field = n >= args.length ? "id" : args[n];
            int k = 0;
            System.out.println("hits:");
            for (ScoreDoc scoreDoc : docs.scoreDocs) {
                final Document document = reader.document(scoreDoc.doc);
                String value = document.getField(field).stringValue();
                if (pw == null) {
                    System.out.print(" " + value);
                }
                else {
                    pw.println(value);
                }
                if (++k >= numHits) {
                    break;
                }
            }
            System.out.println();
        }
        System.out.println("search took: " + (System.currentTimeMillis() - now));
        System.out.println("#hits = " + docs.totalHits);
        if (pw != null) {
            pw.close();
        }
    }

}
