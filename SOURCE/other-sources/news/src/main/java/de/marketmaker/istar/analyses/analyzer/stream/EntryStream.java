package de.marketmaker.istar.analyses.analyzer.stream;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Stream;

import de.marketmaker.istar.analyses.analyzer.Agency;
import de.marketmaker.istar.analyses.analyzer.Analysis;
import de.marketmaker.istar.analyses.analyzer.Index;
import de.marketmaker.istar.analyses.analyzer.ReportContext;
import de.marketmaker.istar.analyses.analyzer.Security;

/**
 * we use entity streams to aggregate data
 *  entity.key is the object we want to gather data about
 *  entity.value contains the collected data
 *
 * @param <V> : value, mosty Analysis to pick up buy,hold,sell values and store them in a Rating key
 * @param <K> : key to collect data, mostly Analyses, might be replaced by something else later in the
 *           stream processing
 *
 * the same key is used for multiple values in the stream, it's not a map...
 */
public interface EntryStream<K, V> {

    Stream<Entry<K, V>> generate(ReportContext container, Predicate<? super Analysis> filter);

    /**
     * create a stream of Entry<Agency, Analysis>
     */
    class AgencyStream implements EntryStream<Agency, Analysis> {

        @Override
        public Stream<Entry<Agency, Analysis>> generate(ReportContext container, Predicate<? super Analysis> filter) {
            return container.getAgencies().stream() // stream agency
                    .flatMap(agency -> agency.getAnalyses().stream() // stream contained analyses
                            .filter(filter)
                            .map(analysis -> new SimpleImmutableEntry<>(agency, analysis)));
        }

    }

    /**
     * create a stream of Entry<Index, Analysis>
     */
    class IndexStream implements EntryStream<Index, Analysis> {

        @Override
        public Stream<Entry<Index, Analysis>> generate(ReportContext container, Predicate<? super Analysis> filter) {
            //noinspection Convert2Diamond
            return container.getIndices().stream()
                    .flatMap(index -> index.getSecurities().stream()  // stream 1:n securities
                            .map(security -> new SimpleImmutableEntry<Index, Security>(index, security)))
                    // stream of Entry<Index, Security>
                    .flatMap(entry -> entry.getValue().getAnalyses().stream()  // stream 1:n analyses
                            .filter(filter)
                            .map(analysis -> new SimpleImmutableEntry<>(entry.getKey(), analysis)));
                    // stream of Entry<Index, Analysis>
        }

    }

    /**
     * create a stream of Entry<Security, Analysis>
     */
    class SecurityStream implements EntryStream<Security, Analysis> {

        @Override
        public Stream<Entry<Security, Analysis>> generate(ReportContext container, Predicate<? super Analysis> filter) {
           return container.getSecurities().stream()
                    .flatMap(security -> security.getAnalyses().stream()  // stream 1:n analyses
                            .filter(filter)
                            .map(analysis -> new SimpleImmutableEntry<>(security, analysis)));
        }

    }

    /**
     * create a stream of Entry<Analysis, Analysis> this is mainly to be consistent with
     * the rest of the API since we process stream of Entry<Analysis, Analysis> it
     * would be more complicated to implement the same processing for a special
     * case for stream of Analysis
     */
    class AnalysisStream implements EntryStream<Analysis, Analysis> {

        @Override
        public Stream<Entry<Analysis, Analysis>> generate(ReportContext container, Predicate<? super Analysis> filter) {
            return container.getAnalyses().stream() // stream agency
                            .filter(filter)
                            .map(analysis -> new SimpleImmutableEntry<>(analysis, analysis));
        }

    }

}
