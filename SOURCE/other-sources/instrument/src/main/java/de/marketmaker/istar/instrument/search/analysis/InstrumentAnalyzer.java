package de.marketmaker.istar.instrument.search.analysis;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;

import de.marketmaker.istar.common.lucene.ASCIIFoldingFilterMM;
import de.marketmaker.istar.instrument.search.StandardSynonymFilter;

/**
 * Filters {@link InstrumentTokenizer} with {@link InstrumentFilter},
 * {@link InnerWordsFilter} that expands inner words, {@link LowerCaseFilter},
 * {@link ASCIIFoldingFilter} and {@link StopFilter} for the most common words in instrument names.
 */
public class InstrumentAnalyzer extends Analyzer {
    private final boolean forIndexing;

    private static final class SavedStreams {
        final InstrumentTokenizer tokenStream;

        final TokenStream filteredTokenStream;

        public SavedStreams(InstrumentTokenizer tokenStream, TokenStream filteredTokenStream) {
            this.tokenStream = tokenStream;
            this.filteredTokenStream = filteredTokenStream;
        }
    }

    /**
     * Default maximum allowed token length
     */
    public static final int DEFAULT_MAX_TOKEN_LENGTH = 255;

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "adr", "ag", "co", "corp", "inc", "kg", "kgaa", "ltd", "na", "plc", "sa", "se", "spa", "vz"
    ));

    public static InstrumentAnalyzer forQuery() {
        return new InstrumentAnalyzer(false);
    }

    public static InstrumentAnalyzer forIndexing() {
        return new InstrumentAnalyzer(true);
    }

    private InstrumentAnalyzer(boolean forIndexing) {
        this.forIndexing = forIndexing;
    }

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        return addFilters(new InstrumentTokenizer(reader));
    }

    @Override
    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
        if (overridesTokenStreamMethod) {
            // LUCENE-1678: force fallback to tokenStream() if we
            // have been subclassed and that subclass overrides
            // tokenStream but not reusableTokenStream
            return tokenStream(fieldName, reader);
        }
        SavedStreams streams = (SavedStreams) getPreviousTokenStream();
        if (streams == null) {
            streams = createSavedStreams(reader);
            setPreviousTokenStream(streams);
        }
        else {
            streams.tokenStream.reset(reader);
        }

        return streams.filteredTokenStream;
    }

    private SavedStreams createSavedStreams(Reader reader) {
        final InstrumentTokenizer tokenStream = new InstrumentTokenizer(reader);
        return new SavedStreams(tokenStream, addFilters(tokenStream));
    }

    private TokenStream addFilters(InstrumentTokenizer tokenizer) {
        TokenStream result = tokenizer;
        result = new InstrumentFilter(result);
        result = new InnerWordsFilter(this.forIndexing, result);
        result = new LowerCaseFilter(result);
        result = new ASCIIFoldingFilterMM(result);
        result = new StopFilter(false, result, STOP_WORDS);
        if (this.forIndexing) {
            result = new StandardSynonymFilter(result);
        }
        return result;
    }
}
