/*
 * PageIndexReader.java
 *
 * Created on 17.01.2011 08:52:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

/**
 * @author oflege
 */
public class PageIndexReader {
    public static void main(String[] args) throws IOException {
        FSDirectory dir = FSDirectory.open(new File(args[0]));
        PrintWriter pw = args.length > 1
                ? new PrintWriter(args[1], "utf-8")
                : new PrintWriter(System.out);
        IndexReader ir = IndexReader.open(dir, true);
        for (int i = 0; i < ir.numDocs(); i++) {
            Document d = ir.document(i);
            StringBuilder sb = new StringBuilder();
            for (String x : new String[] { "pagenumber", "title", "heading", "language"}) {
                String value = d.get(x);
                if (sb.length() > 0) sb.append(";");
                if (value == null) continue;
                if (value.indexOf(";") != -1) {
                    sb.append('"').append(value).append('"');
                }
                else {
                    sb.append(value);
                }
            }
            pw.println(sb.toString());
//            if (i > 10) break;
        }
        ir.close();
        pw.close();
    }
}
