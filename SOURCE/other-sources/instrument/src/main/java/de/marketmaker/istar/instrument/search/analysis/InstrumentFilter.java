package de.marketmaker.istar.instrument.search.analysis;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

/**
 * Normalizes tokens extracted with {@link InstrumentTokenizer}.
 */
public final class InstrumentFilter extends TokenFilter {

    private static final String APOSTROPHE_TYPE
            = InstrumentTokenizer.TOKEN_TYPES[InstrumentTokenizer.APOSTROPHE];

    private static final String ACRONYM_TYPE
            = InstrumentTokenizer.TOKEN_TYPES[InstrumentTokenizer.ACRONYM];

    private static final String PERCENTCOMMA_TYPE
            = InstrumentTokenizer.TOKEN_TYPES[InstrumentTokenizer.PERCENTCOMMA];

    // this filters uses attribute type

    private final TypeAttribute typeAtt;

    private final TermAttribute termAtt;

    /**
     * Construct filtering <i>in</i>.
     */
    public InstrumentFilter(TokenStream in) {
        super(in);
        this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
        this.typeAtt = (TypeAttribute) addAttribute(TypeAttribute.class);
    }

    /**
     * Returns the next token in the stream, or null at EOS.
     */
    @Override
    public final boolean incrementToken() throws java.io.IOException {
        if (!input.incrementToken()) {
            return false;
        }

        final char[] buffer = termAtt.termBuffer();
        final int bufferLength = termAtt.termLength();
        final String type = typeAtt.type();

        if (type == ACRONYM_TYPE) {
            removeChars(buffer, bufferLength, '.');
        }
        else if (type == APOSTROPHE_TYPE) {
            removeChars(buffer, bufferLength, '\'');
        }
        else if (type == PERCENTCOMMA_TYPE) {      // replace ',' by '.'
            for (int i = 0; i < bufferLength; i++) {
                if (buffer[i] == ',') {
                    buffer[i] = '.';
                    break;
                }
            }
            this.typeAtt.setType(InstrumentTokenizer.TOKEN_TYPES[InstrumentTokenizer.PERCENT]);
        }
        return this.termAtt.termLength() > 1 || incrementToken();
    }

    private void removeChars(char[] buffer, int bufferLength, final char toRemove) {
        int upto = 0;
        for (int i = 0; i < bufferLength; i++) {
            final char c = buffer[i];
            if (c != toRemove) {
                buffer[upto++] = c;
            }
        }
        this.termAtt.setTermLength(upto);
    }

}
