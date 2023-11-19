package de.marketmaker.istar.analyses.analyzer.collect;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import de.marketmaker.istar.analyses.analyzer.Analysis;
import de.marketmaker.istar.analyses.analyzer.PriceCache;

/**
 *  calculate and collect success state for an analysis which is defined as:
 *
 *  true if the price of the analyzed security is >= the target price of the analysis withing the
 *  [start end] timeframe
 *
 */
public class SuccessCollector<K> implements Collector<Entry<K, Analysis>, SuccessCollector.SuccessValues, SuccessCollector.SuccessValues> {

    // not concurrent, not unordered
    private static final Set<Characteristics> CH_ID = Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));

    private final int yyyyMmDd;
    private final PriceCache priceCache;

    public SuccessCollector(int yyyyMmDd, PriceCache priceCache) {
        assert priceCache != null;
        this.yyyyMmDd = yyyyMmDd;
        this.priceCache = priceCache;
    }
    @Override
    public Supplier<SuccessValues> supplier() {
        return SuccessValues::new;
    }

    @Override
    public BiConsumer<SuccessValues, Entry<K, Analysis>> accumulator() {
        return (collected, entry) -> {
            // add values for entry to be collected
            final Analysis analysis = entry.getValue();
            if (priceCache.getSuccess(null, analysis, yyyyMmDd).orElse(false)) {
                collected.count += 1;
            }
            collected.total += 1;
        };
    }

    @Override
    public BinaryOperator<SuccessValues> combiner() {
        return (left, right) -> {
            left.count += right.count;
            left.total += right.total;
            return left;
        };
    }

    @Override
    public Function<SuccessValues, SuccessValues> finisher() {
        return  identity -> identity;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return CH_ID;
    }

    // this is a feature of the analysis
    public static class SuccessValues {

        int count; // success counter, can be aggregated over multiple analyses, e.g. for an agency

        int total; // success counter, can be aggregated over multiple analyses, e.g. for an agency


        public int getCount() {
            return count;
        }

        public int getTotal() {
            return total;
        }


        @Override
        public String toString() {
            return "Success{" +
                    " count=" + count +
                    ", total=" + total +
                    '}';
        }

    }

}
