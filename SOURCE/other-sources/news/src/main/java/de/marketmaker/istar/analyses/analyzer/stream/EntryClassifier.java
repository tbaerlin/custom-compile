package de.marketmaker.istar.analyses.analyzer.stream;

import de.marketmaker.istar.analyses.analyzer.Analysis;
import de.marketmaker.istar.analyses.analyzer.Security;

import java.util.Map.Entry;
import java.util.function.Function;

/**
 *  classifier that either uses the whole key or properties of the key
 *  to group the stream for aggregation
 *
 *  effectively this replaces the key by something else in the stream processing pipeline
 *
 *
 *  @param <K> the entry.key in an element of an entry stream
 *  @param <O> a new key that an aggregation is based upon
 */
public interface EntryClassifier<K, O> extends Function<Entry<K, Analysis>, O>  {

    EntryClassifier IDENTITY = new EntryClassifier<Object, Object>() {
        @Override
        public Object apply(Entry<Object, Analysis> entry) {
            return entry.getKey();
        }
    };

    static <K, O> EntryClassifier<K, O> identity() {
        //noinspection unchecked
        return (EntryClassifier<K, O>) IDENTITY;
    }

    // TODO: use Fields.FIELDS to configure a Classifier

    /* a security key, we pick the industry property for classification */
    class IndustryClassifier implements EntryClassifier<Security, String> {
        @Override
        public String apply(Entry<Security, Analysis> entry) {
            final String value = entry.getKey().getIndustry();
            return  value == null?"null":value;
        }
    }

}
