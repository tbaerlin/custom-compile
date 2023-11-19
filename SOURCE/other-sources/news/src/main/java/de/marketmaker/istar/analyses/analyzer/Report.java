package de.marketmaker.istar.analyses.analyzer;

import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * base class fo evaluating analyzer data, performs evaluation on a set of
 * analyses data
 *
 * @param <K> the business entity for which we want to create a report,
 *           usually one of Agency, Analysis, Security, String (for index)
 * @param <V> aggregatable, statistic or other evaluation data,
 *           usually one of Ratings, Performance
 */
interface Report<K, V> {

    // generate a stream of tuples, retrieve data from the container
    Stream<Entry<K, V>> generate(ReportContext container);

}
