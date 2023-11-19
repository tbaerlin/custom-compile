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
import de.marketmaker.istar.analyses.analyzer.collect.RatingCollector.RatingValues;
import de.marketmaker.istar.analyses.backend.Protos.Analysis.Rating;

/**
 *  calculate/accumulate/collect rating values from analyses,
 *  rating values are:
 *   buy, hold, sell, unknown, prevBuy, prevHold, prevSell, prevUnknown, total
 */
public class RatingCollector<K> implements Collector<Entry<K, Analysis>, RatingValues, RatingValues> {

    // not concurrent, not unordered
    private static final Set<Characteristics> CH_ID = Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));

    @Override
    public Supplier<RatingValues> supplier() {
        return RatingValues::new;
    }

    @Override
    public BiConsumer<RatingValues, Entry<K, Analysis>> accumulator() {
        return (collected, entry) -> {
            Rating rating;
            Rating previousRating;
            collected.total++;
            final Analysis analysis = entry.getValue();

            rating = analysis.getRating();
            if (rating != null) {
                switch (rating) {
                    case BUY:
                        collected.buy++;
                        break;
                    case HOLD:
                        collected.hold++;
                        break;
                    case SELL:
                        collected.sell++;
                        break;
                    default:
                        collected.unknown++;
                }
            } else {
                collected.unknown++;
            }

            previousRating = analysis.getPreviousRating();
            if (previousRating != null) {
                switch (previousRating) {
                    case BUY:
                        collected.prevBuy++;
                        break;
                    case HOLD:
                        collected.prevHold++;
                        break;
                    case SELL:
                        collected.prevSell++;
                        break;
                    default:
                        collected.prevUnknown++;
                }
            } else {
                collected.prevUnknown++;
            }

            if ((rating == Rating.BUY) && (previousRating != Rating.BUY)) {
                collected.newBuy++;
            }
            if ((rating == Rating.HOLD) && (previousRating != Rating.HOLD)) {
                collected.newHold++;
            }
            if ((rating == Rating.SELL) && (previousRating != Rating.SELL)) {
                collected.newSell++;
            }
        };
    }

    @Override
    public BinaryOperator<RatingValues> combiner() {
        return (left, right) -> {
            left.buy += right.buy;
            left.hold += right.hold;
            left.sell += right.sell;

            left.prevBuy += right.prevBuy;
            left.prevHold += right.prevHold;
            left.prevSell += right.prevSell;

            left.newBuy += right.newBuy;
            left.newHold += right.newHold;
            left.newSell += right.newSell;

            left.unknown += right.unknown;
            left.prevUnknown += right.prevUnknown;
            left.total += right.total;

            return left;
        };
    }

    @Override
    public Function<RatingValues, RatingValues> finisher() {
        return  identity -> identity;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return CH_ID;
    }


    public static class RatingValues {

        private int buy;
        private int hold;
        private int sell;

        private int newBuy;
        private int newHold;
        private int newSell;

        private int prevBuy;
        private int prevHold;
        private int prevSell;

        private int unknown;
        private int prevUnknown;
        private int total;

        public int getBuy() {
            return buy;
        }

        public int getHold() {
            return hold;
        }

        public int getSell() {
            return sell;
        }

        public int getPrevBuy() {
            return prevBuy;
        }

        public int getPrevHold() {
            return prevHold;
        }

        public int getPrevSell() {
            return prevSell;
        }

        public int getNewBuy() {
            return newBuy;
        }

        public int getNewHold() {
            return newHold;
        }

        public int getNewSell() {
            return newSell;
        }

        public int getUnknown() {
            return unknown;
        }

        public int getPrevUnknown() {
            return prevUnknown;
        }

        public int getTotal() {
            return total;
        }

        @Override
        public String toString() {
            return "Ratings{" +
                    "buy=" + buy +
                    ", hold=" + hold +
                    ", sell=" + sell +

                    ", newBuy=" + newBuy +
                    ", newHold=" + newHold +
                    ", newSell=" + newSell +

                    ", prevBuy=" + prevBuy +
                    ", prevHold=" + prevHold +
                    ", prevSell=" + prevSell +

                    ", unknown=" + unknown +
                    ", prevUnknown=" + prevUnknown +
                    ", total=" + total +
                    '}';
        }

    }

}
