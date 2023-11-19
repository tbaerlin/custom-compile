/*
 * InstrumentTokenizer.java
 *
 * Created on 2010-08-05 11:24:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;

/**
 * A grammar-based tokenizer constructed with JFlex
 *
 * <p> This should be a good tokenizer for most European-language documents:
 *
 * <ul>
 * <li>Splits words at punctuation characters, removing punctuation. However, a
 * dot that's not followed by whitespace is considered part of a token.
 * <li>Splits words at hyphens, unless there's a number in the token, in which case
 * the whole token is interpreted as a product number and is not split.
 * <li>Recognizes email addresses and internet hostnames as one token.
 * </ul>
 */

public final class InstrumentTokenizer extends Tokenizer {
    /**
     * A private instance of the JFlex-constructed scanner
     */
    private final InstrumentTokenizerImpl scanner;

    public static final int ALPHANUM = 0;

    public static final int APOSTROPHE = 1;

    public static final int ACRONYM = 2;

    public static final int COMPOSITE = 3;

    public static final int PERCENT = 4;

    public static final int PERCENTCOMMA = 5;

    /**
     * String token types that correspond to token type int constants
     */
    public static final String[] TOKEN_TYPES = new String[]{
            "<ALPHANUM>",
            "<APOSTROPHE>",
            "<ACRONYM>",
            "<COMPOSITE>",
            "<PERCENT>",
            "<PERCENTCOMMA>"
    };


    // this tokenizer generates three attributes:
    // offset, positionIncrement and type

    private TermAttribute termAtt;

    private OffsetAttribute offsetAtt;

    private PositionIncrementAttribute posIncrAtt;

    private TypeAttribute typeAtt;

    /**
     * Creates a new instance.  Attaches
     * the <code>input</code> to the newly created JFlex scanner.
     * @param input The input reader
     *
     */
    public InstrumentTokenizer(Reader input) {
        super();
        this.scanner = new InstrumentTokenizerImpl(input);
        init(input);
    }

    /**
     * Creates a new StandardTokenizer with a given {@link AttributeSource}.
     */
    public InstrumentTokenizer(AttributeSource source, Reader input) {
        super(source);
        this.scanner = new InstrumentTokenizerImpl(input);
        init(input);
    }

    /**
     * Creates a new StandardTokenizer with a given {@link org.apache.lucene.util.AttributeSource.AttributeFactory}
     */
    public InstrumentTokenizer(AttributeFactory factory, Reader input) {
        super(factory);
        this.scanner = new InstrumentTokenizerImpl(input);
        init(input);
    }

    private void init(Reader input) {
        this.input = input;
        termAtt = (TermAttribute) addAttribute(TermAttribute.class);
        offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
        posIncrAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
        typeAtt = (TypeAttribute) addAttribute(TypeAttribute.class);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.lucene.analysis.TokenStream#next()
    */

    @Override
    public final boolean incrementToken() throws IOException {
        clearAttributes();
        int posIncr = 1;

        while (true) {
            int tokenType = scanner.getNextToken();

            if (tokenType == InstrumentTokenizerImpl.YYEOF) {
                return false;
            }

            if (scanner.yylength() <= InstrumentAnalyzer.DEFAULT_MAX_TOKEN_LENGTH) {
                posIncrAtt.setPositionIncrement(posIncr);
                scanner.getText(termAtt);
                final int start = scanner.yychar();
                offsetAtt.setOffset(correctOffset(start), correctOffset(start + termAtt.termLength()));
                typeAtt.setType(InstrumentTokenizer.TOKEN_TYPES[tokenType]);
                return true;
            }
            else
                // When we skip a too-long term, we still increment the
                // position increment
                posIncr++;
        }
    }

    @Override
    public final void end() {
        // set final offset
        int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    /*
    * (non-Javadoc)
    *
    * @see org.apache.lucene.analysis.TokenStream#reset()
    */

    @Override
    public void reset() throws IOException {
        super.reset();
        scanner.yyreset(input);
    }

    @Override
    public void reset(Reader reader) throws IOException {
        super.reset(reader);
        reset();
    }
}
