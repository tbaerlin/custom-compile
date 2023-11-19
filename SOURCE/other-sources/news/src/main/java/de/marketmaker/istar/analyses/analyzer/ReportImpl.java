package de.marketmaker.istar.analyses.analyzer;

import java.security.InvalidParameterException;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.marketmaker.istar.analyses.analyzer.collect.PerformanceCollector;
import de.marketmaker.istar.analyses.analyzer.collect.PerformanceCollector.PerformanceValues;
import de.marketmaker.istar.analyses.analyzer.collect.RatingCollector;
import de.marketmaker.istar.analyses.analyzer.collect.RatingCollector.RatingValues;
import de.marketmaker.istar.analyses.analyzer.collect.SuccessCollector;
import de.marketmaker.istar.analyses.analyzer.collect.SuccessCollector.SuccessValues;
import de.marketmaker.istar.analyses.analyzer.stream.EntryClassifier;
import de.marketmaker.istar.analyses.analyzer.stream.EntryClassifier.IndustryClassifier;
import de.marketmaker.istar.analyses.analyzer.stream.EntryOrder;
import de.marketmaker.istar.analyses.analyzer.stream.EntryOrder.BuyCountOrder;
import de.marketmaker.istar.analyses.analyzer.stream.EntryOrder.BuySellRatioOrder;
import de.marketmaker.istar.analyses.analyzer.stream.EntryOrder.HoldCountOrder;
import de.marketmaker.istar.analyses.analyzer.stream.EntryOrder.Invert;
import de.marketmaker.istar.analyses.analyzer.stream.EntryOrder.PerformanceCompare;
import de.marketmaker.istar.analyses.analyzer.stream.EntryOrder.SellCountOrder;
import de.marketmaker.istar.analyses.analyzer.stream.EntryOrder.SuccessCompare;
import de.marketmaker.istar.analyses.analyzer.stream.EntryOrder.TotalCountOrder;
import de.marketmaker.istar.analyses.analyzer.stream.EntryOrder.TotalCompare;
import de.marketmaker.istar.analyses.analyzer.stream.EntryStream;
import de.marketmaker.istar.analyses.analyzer.stream.EntryStream.AgencyStream;
import de.marketmaker.istar.analyses.analyzer.stream.EntryStream.AnalysisStream;
import de.marketmaker.istar.analyses.analyzer.stream.EntryStream.IndexStream;
import de.marketmaker.istar.analyses.analyzer.stream.EntryStream.SecurityStream;
import de.marketmaker.istar.analyses.analyzer.stream.TimeFilter;

/**
 * create a ratings aggregation for whatever by creating a sorted Map<K, R> stream
 * might be used for evaluating/collecting/aggregating buy/hold/sell counts but also for
 * performance calculation for single analyses
 *
 * @param <K> entity data for the report, usually this is the value the report is
 *           grouped by like agency, security, index
 * @param <K0> raw start values before performing grouping or aggregation operations
 * @param <R> result value, aggregated/calculated data
 */
public class ReportImpl<K, K0, R> implements Report<K, R> {

    // generates Stream<Entry<..>>
    private final EntryStream<K0, Analysis> source;
    // mostly time filtering
    private final Predicate<Analysis> filter;
    // use K0 or Analysis to classify by K
    private final EntryClassifier<K0, K> classifier;
    // collect/aggregate values and calculate the final result values
    private final Collector<Entry<K0, Analysis>, R, R> collector;
    // sort result after all the work has been done
    private final Comparator<Entry<K, R>> sorter;

    public ReportImpl(
            EntryStream<K0, Analysis> source,
            Predicate<Analysis> filter,
            EntryClassifier<K0, K> classifier,
            Collector<Entry<K0, Analysis>, R, R> collector,
            Comparator<Entry<K, R>> sorter) {
        this.source = source;
        this.filter = filter;
        this.classifier = classifier;
        this.collector = collector;
        this.sorter = sorter;
    }

    @Override
    public Stream<Entry<K, R>> generate(ReportContext container) {
        return source.generate(container, filter) // stream of Entry<K, Analysis>
                .collect(Collectors.groupingBy(classifier, collector))
                .entrySet()
                .stream()
                .sorted(sorter);
    }


    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    static class Builder {

        private EntryStream<?, Analysis> source;

        private Predicate<Analysis> filter;

        private EntryClassifier classifier;

        private Collector collector;

        private Comparator sorter;

        // config details
        private String sourceStream; // one of {INDUSTRY, SECURITY, INDEX, AGENCY, ANALYSIS}

        private Class valueClass;    // one of {Rating.class, Performance.class, Success.class},

        private String sort, order;  // a fieldname and asc/desc

        private int yyyyMmDd;           // for filtering active analysis or historic queries

        // dynamic data for success and performance calculation
        private PriceCache priceCache;

        // sourceStream defines the grouping by semantics of the result
        void setSourceStream(String sourceStream) {
            this.sourceStream = sourceStream;
        }

        // affects: analysis active, previous day
        void setDate(int yyyyMmDd) {
            this.yyyyMmDd = yyyyMmDd;
        }

        void setSortOrder(String sort, String order) {
            this.sort = sort;
            this.order = order;
        }

        void setValueClass(Class valueClass) {
            this.valueClass = valueClass;
        }

        void setPriceCache(PriceCache priceCache) {
            this.priceCache = priceCache;
        }

        ReportImpl build() {
            setupSource();
            setupClassifier();
            setupOrder();
            setupCollector();
            filter = new TimeFilter(yyyyMmDd);
            return new ReportImpl<>(
                    source,
                    filter,
                    classifier,
                    collector,
                    sorter);
        }

        // configure what kind of data we want to process
        private void setupSource() {
            switch (sourceStream) {  // the keyword behind 'for' in the query
                case QueryParser.INDUSTRY: // industry is a property of security
                    source = new SecurityStream();  // streams (Security, Analysis) pairs
                    break;
                case QueryParser.SECURITY:
                    source = new SecurityStream();  // streams (Security, Analysis) pairs
                    break;
                case QueryParser.INDEX:
                    source = new IndexStream();  // streams (Index, Analysis) pairs
                    break;
                case QueryParser.AGENCY:
                    source = new AgencyStream();  // streams (Agency, Analysis) pairs
                    break;
                case QueryParser.ANALYSIS:
                    source = new AnalysisStream();  // streams (Analysis, Analysis) pairs
                    break;
                default:
                    throw new InvalidParameterException("<setSourceStream> source is unknown: '" + sourceStream + "'");
            }
        }

        /**
         * configure what kind of aggregation we want to have:
         * - RatingCollector: the static buy/hold/sell values from the analyses
         * - PerformanceCollector: comparing the target price in the analyses with the previous day price
         * - SuccessCollector: counting the successful analyses (where the target price was met)
         */
        private void setupCollector() {
            if (RatingValues.class.equals(valueClass)) {
                // gets the value from the buy/hold/sell ratings in the analysis only
                collector = new RatingCollector<>();
            }
            else if (PerformanceValues.class.equals(valueClass)) {
                // uses the previous day to determine the performance
                collector = new PerformanceCollector<>(this.yyyyMmDd, this.priceCache);
            }
            else if (SuccessValues.class.equals(valueClass)) {
                // check if the predicted target was met
                collector = new SuccessCollector<>(this.yyyyMmDd, this.priceCache);
            }
            else {
                throw new InvalidParameterException("<setupCollector> unknown collector for class " + valueClass);
            }
        }

        /**
         * classifier is used to define the value to aggregate with
         * e.g. all pairs with the same industry will be aggregated
         */
        private void setupClassifier() {
            if (RatingValues.class.equals(valueClass)) {
                switch (sourceStream) {
                    case QueryParser.INDUSTRY:
                        // using a property of the Security class
                        classifier = new IndustryClassifier();
                        break;
                    case QueryParser.SECURITY:
                    case QueryParser.INDEX:
                    case QueryParser.AGENCY:
                    case QueryParser.ANALYSIS:
                        classifier = EntryClassifier.identity();
                        break;
                    default:
                        throw new InvalidParameterException("<setupClassifier> unknown sourceStream: " + sourceStream);
                }
            }
            else if (PerformanceValues.class.equals(valueClass)) {
                classifier = EntryClassifier.identity();
            }
            else if (SuccessValues.class.equals(valueClass)) {
                classifier = EntryClassifier.identity();
            }
            else {
                throw new InvalidParameterException("<setupClassifier> unknown valueClass: " + valueClass);
            }
        }

        // available order options depend on what we aggregate into the valueCLass
        private void setupOrder() {
            if (sorter == null && RatingValues.class.equals(valueClass)) {
                sorter = setupRatingValueOrder();
            }

            if (sorter == null && PerformanceValues.class.equals(valueClass)) {
                sorter = setupPerformanceValueOrder();
            }

            if (sorter == null && SuccessValues.class.equals(valueClass)) {
                sorter = setupSuccessValueOrder();
            }

            if (sorter == null && "security".equals(sourceStream)) {
                sorter = setupSecurityOrder();
            }

            if (sorter == null) {
                throw new InvalidParameterException("<setupOrder> unknown sorter for valueClass: "
                        + valueClass + " sort was '" + sort + " " + order + "'");
            }
        }


        private boolean isInvertedOrder() {
            switch(order) {
                case "asc":
                    return true;
                case "desc":
                    return false;
                default:
                    return false;
            }
        }

        private EntryOrder<Security,?>  setupSecurityOrder() {
            EntryOrder<Security,?> result;
            boolean invert = isInvertedOrder();

            switch (sort) {
                case "securityName":
                    result = new EntryOrder.SecurityNameCompare<>(invert);
                    break;
                case "securitySymbol":
                    result = new EntryOrder.SecuritySymbolCompare<>(invert);
                    break;
                default:
                    return null;
            }
            return result;
        }

        private EntryOrder<?,SuccessValues> setupSuccessValueOrder() {
            EntryOrder<?,SuccessValues> result;
            switch (sort) {
                case "successCount":
                    result = new SuccessCompare<>();
                    break;
                case "totalCount":
                    result = new TotalCompare<>();
                    break;
                default:
                    return null;
            }
            switch (order) {
                case "asc":
                    return new Invert<>(result);
                case "desc":
                default:
                    return result;
            }
        }

        private EntryOrder<?,PerformanceValues> setupPerformanceValueOrder() {
            PerformanceCompare<?> result;
            boolean invert = isInvertedOrder();
            switch (sort) {
                case "performance":
                    return new PerformanceCompare<>(invert);
                default:
                    return null;
            }
        }

        // pick order from rating properties if possible
        private EntryOrder<?,?> setupRatingValueOrder() {
            EntryOrder<?,?> result;
            switch (sort) {
                case "totalRatings":
                    result = new TotalCountOrder<>();
                    break;
                case "buy":
                    result = new BuyCountOrder<>();
                    break;
                case "hold":
                    result = new HoldCountOrder<>();
                    break;
                case "sell":
                    result = new SellCountOrder<>();
                    break;
                case "buySellRatio":
                    result = new BuySellRatioOrder<>();
                    break;
                default:
                    return null;
            }
            switch (order) {
                case "asc":
                    return new Invert<>(result);
                case "desc":
                default:
                    return result;
            }
        }

    }

}
