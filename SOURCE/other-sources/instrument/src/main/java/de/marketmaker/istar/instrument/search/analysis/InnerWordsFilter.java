/*
 * CapitalizedInnerWordsFilter.java
 *
 * Created on 09.01.2005 11:24:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search.analysis;

import org.apache.lucene.analysis.TokenStream;

import de.marketmaker.istar.common.lucene.TokenFilterWithStack;

/**
 * This filter looks for words that are composites of multiple words. Those words come as tokens
 * of two different types:
 * <dl>
 * <dt>ALPHANUM</dt>
 * <dd>May contain inner capital characters which are assumed to be word boundaries
 * (e.g., "HeidelbergCement")</dd>
 * <dt>COMPOSITE</dt>
 * <dd>inner words are separated by special characters <tt>@,-,+,-,_,.</tt>
 * </dl>
 * <br>
 * This filter can be used in two different modes, depending on the <tt>tokenizeParts</tt> 
 * constructor-arg. If that parameter is
 * <dl>
 * <dt>true</dt>
 * <dd>this filter emits additional tokens for each inner word and removes special characters
 * from COMPOSITE tokens; to be used during indexing</dd>
 * <dt>false</dt>
 * <dd>this filter just removes special characters from COMPOSITE tokens; to be used for parsing
 * queries</dd>
 * </dl>
 *
 * <b>Important</b>: This filter has to be used BEFORE a LowerCaseFilter is applied.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InnerWordsFilter extends TokenFilterWithStack {
    private static final String ALPHANUM_TYPE =
            InstrumentTokenizer.TOKEN_TYPES[InstrumentTokenizer.ALPHANUM];

    private static final String COMPOSITE
            = InstrumentTokenizer.TOKEN_TYPES[InstrumentTokenizer.COMPOSITE];

    /** position + 1 of the end of the n-th word (i.e., pos of start of (n+1)th word */
    private final int[] offsets = new int[100];

    private final boolean tokenizeParts;

    private char[] chars = new char[512];

    /**
     * Constructor
     * @param tokenizeParts whether word parts should be added as individual tokens
     * @param in token source
     */
    public InnerWordsFilter(boolean tokenizeParts, TokenStream in) {
        super(in);
        this.tokenizeParts = tokenizeParts;
    }

    @Override
    protected void onIncrementToken() {
        //noinspection StringEquality
        if (typeAtt.type() == COMPOSITE) {
            handleComposite(termAtt.termBuffer(), termAtt.termLength());
        }
        else //noinspection StringEquality
            if (this.tokenizeParts && typeAtt.type() == ALPHANUM_TYPE) {
            tokenizeInnerWords();
        }
    }

    private void tokenizeInnerWords() {
        final char[] buffer = termAtt.termBuffer();
        final int bufferLength = termAtt.termLength();
        int n;
        if ((n = findInnerWords(buffer, bufferLength - 1)) > 0) {
            this.offsets[n] = bufferLength;
            pushWords(buffer, n);
        }
    }

    private int findInnerWords(char[] buffer, int bufferLength) {
        int n = 0;
        for (int i = 1; i < bufferLength; i++) {
            final char c = buffer[i];
            if (Character.isUpperCase(c) && Character.isLowerCase(buffer[i - 1])) {
                this.offsets[n++] = i;
            }
        }
        return n;
    }

    private void handleComposite(char[] buffer, int bufferLength) {
        int n = 0;
        int upto = 0;
        for (int i = 0; i < bufferLength; i++) {
            final char c = buffer[i];
            if (Character.isLetter(c) || Character.isDigit(c)) {
                this.chars[upto++] = c;
            }
            else if (this.tokenizeParts) {
                this.offsets[n++] = upto;
            }
        }
        if (this.tokenizeParts) {
            this.offsets[n] = upto;
            pushWords(this.chars, n);
        }
//        this.termAtt.setTermLength(upto);
    }

    private void pushWords(char[] buffer, int n) {
        if (buffer == this.chars) {
            push(new String(buffer, 0, this.offsets[n]));
        }
        push(new String(buffer, 0, this.offsets[0]));
        for (int i = 0; i < n; i++) {
            push(new String(buffer, this.offsets[i], this.offsets[i + 1] - this.offsets[i]));
        }
    }
}
