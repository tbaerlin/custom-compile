/*
 * StackTokenFilter.java
 *
 * Created on 12.08.2005 15:49:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.lucene;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.springframework.util.StringUtils;

/**
 * A filter that allows subclasses to add Tokens to a stack
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class TokenFilterWithStack extends TokenFilter {
    private final Deque<String> stack = new ArrayDeque<>(20);

    protected final TermAttribute termAtt;

    protected final TypeAttribute typeAtt;

    protected final PositionIncrementAttribute posIncrAtt;

    private AttributeSource.State current;

    protected TokenFilterWithStack(TokenStream input) {
        super(input);
        this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
        this.typeAtt = (TypeAttribute) addAttribute(TypeAttribute.class);
        this.posIncrAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
    }

    protected final void push(String s) {
        if (StringUtils.hasText(s) && s.length() > 1) {
            if (this.stack.isEmpty()) {
                this.current = captureState();
            }
            this.stack.addLast(s);
        }
    }

    public boolean incrementToken() throws IOException {
        if (!this.stack.isEmpty()) {
            final String syn = stack.removeFirst();
            restoreState(this.current);
            this.termAtt.setTermBuffer(syn);
            this.posIncrAtt.setPositionIncrement(0);
            return true;
        }
        if (!input.incrementToken()) {
            return false;
        }
        onIncrementToken();
        return true;
    }

    protected abstract void onIncrementToken();
}
