/*
 * AnalyzerUtils.java
 *
 * Created on 11.08.2010 07:54:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.Version;

/**
 * @author oflege
 */
public class AnalyzerUtils {

    public static List<String> getTerms(TokenStream stream) throws IOException {
        final List<String> result = new ArrayList<>();
        TermAttribute term = addAttribute(stream, TermAttribute.class);
        while (stream.incrementToken()) {
            result.add(term.term());
        }
        return result;
    }

    @SuppressWarnings({"unchecked"})
    private static <T extends Attribute> T addAttribute(TokenStream s, Class<T> clazz) {
        return (T) s.addAttribute(clazz);
    }

    public static List<String> getTerms(Analyzer analyzer, String text) throws IOException {
        return getTerms(analyzer.tokenStream("contents", new StringReader(text)));
    }

    public static void displayTokensWithFullDetails(Analyzer analyzer, String text)
            throws IOException {
        TokenStream stream = analyzer.tokenStream("contents", new StringReader(text));
        TermAttribute term = addAttribute(stream, TermAttribute.class);
        PositionIncrementAttribute posIncr = addAttribute(stream, PositionIncrementAttribute.class);
        OffsetAttribute offset = addAttribute(stream, OffsetAttribute.class);
        TypeAttribute type = addAttribute(stream, TypeAttribute.class);
        int position = 0;
        while (stream.incrementToken()) {
            int increment = posIncr.getPositionIncrement();
            if (increment > 0) {
                position = position + increment;
                System.out.println();
                System.out.print(position + ": ");
            }
            System.out.print("[" +
                    term.term() + ":" +
                    offset.startOffset() + "->" +
                    offset.endOffset() + ":" +
                    type.type() + "] ");
        }
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        System.out.println("IstarSimpleAnalyzer");
        displayTokensWithFullDetails(new SimpleAnalyzer(),
                "The quick brown fox....");

        System.out.println("\n----");
        System.out.println("StandardAnalyzer");
        displayTokensWithFullDetails(new StandardAnalyzer(Version.LUCENE_24),
                "I'll e-mail you at xyz@example.com");
    }
}
