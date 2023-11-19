package de.marketmaker.istar.analyses.analyzer;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import it.unimi.dsi.fastutil.longs.Long2ObjectArrayMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.analyses.backend.Protos;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PeriodEditor;

/**
 * collection of analyses data for a single provider like AWP or dpa-afx
 * this is supposed to be a data container, try not to process any data here
 */
public class ReportContextImpl implements ReportContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportContextImpl.class);

    public static final String DEFAULT_TIMEFRAME = "12M";

    // can not be shared with other providers since security contains links to analyses which are provider specific
    protected final Long2ObjectMap<Security> securities = new Long2ObjectArrayMap<>();

    // mapping agencyId to agency data
    protected final Map<String, Agency> agencies = new HashMap<>();

    // mapping qid to index data
    protected final Long2ObjectMap<Index> indices = new Long2ObjectArrayMap<>();

    // mapping analysisId to analysis data
    protected final Long2ObjectMap<Analysis> analyses = new Long2ObjectArrayMap<>();

    // dynamic data derived from prices, like previous close and success for analysis
    private final PriceCache priceCache;



    public ReportContextImpl(PriceCache priceCache) {
        this.priceCache = priceCache;
    }


    /**
     * adding a single analyses to the data model
     * called with open DB connection while looping through a result set
     * so this should be as fast as possible
     */
    @Override
    public void addAnalyses(Protos.Analysis protoAnalysis) {
        try {
            doAddAnalyses(protoAnalysis);
        } catch (Exception ignored) {
            LOGGER.info("<addAnalyses> exception while adding an analysis, we have to ignore this analysis, "
                    + " reason is probably a missing or invalid value, the analysis id is: "
                    + Long.toString(protoAnalysis.getId(), Character.MAX_RADIX)
                    + "/" + protoAnalysis.getId()
                    + " stacktrace coming up ", ignored
            );
        }
    }


    @Override
    public Collection<Agency> getAgencies() {
        return agencies.values();
    }

    @Override
    public Collection<Security> getSecurities() {
        return securities.values();
    }

    @Override
    public Collection<Analysis> getAnalyses() {
        return analyses.values();
    }

    @Override
    public Collection<Index> getIndices() {
        return indices.values();
    }

    @Override
    public PriceCache getPriceCache() {
        return priceCache;
    }

    // used in the collector
    public Optional<Index> getIndex(long indexQid) {
        return Optional.ofNullable(indices.get(indexQid));
    }

    // used in the collector
    public void addIndex(Index index) {
        indices.put(index.getId(), index);
    }

    /**
     * this method must be idempotent, adding the same analysis again must not change any data
     *
     * @param protoAnalysis an analysis that might already be stored
     */
    private void doAddAnalyses(Protos.Analysis protoAnalysis) {
        // hashed id value
        final long analysisId = protoAnalysis.getId();

        // a single iid
        if (protoAnalysis.getIidCount() != 1) {
            LOGGER.warn("<doAddAnalyses> analysis with id '" + Long.toString(analysisId, Character.MAX_RADIX)
                    + "' has none or multiple iids, ignoring in analyzer");
            return;
        }
        final long iid = protoAnalysis.getIid(0);

        // a specified source
        final String source = protoAnalysis.getSource();
        if (StringUtils.isEmpty(source)) {
            LOGGER.warn("<doAddAnalyses> analysis with empty source '" + Long.toString(analysisId, Character.MAX_RADIX)
                    + "', ignoring in analyzer");
            return;
        }

        // -- at this point we have primary ids: iid for security, source for agency, analysisId for analysis
        final Security security = storedSecurity(iid, protoAnalysis);
        final Agency agency = storedAgency(source, protoAnalysis);
        final Analysis analysis = storedAnalysis(analysisId, security, agency, protoAnalysis);
        agency.put(analysis);
        security.put(analysis);
    }

    // return the stored Analysis or create a new one, store it and return it
    private Analysis storedAnalysis(long analysisId, Security security, Agency agency, Protos.Analysis protoAnalysis) {
        if (!analyses.containsKey(analysisId)) {
            final Analysis analysis = new Analysis(analysisId, agency, security);

            long agencyDate = protoAnalysis.getAgencyDate(); // ms since 1/1/1970
            String timeFrame = protoAnalysis.getTimeframe(); // string encoded duration

            if (StringUtils.isEmpty(timeFrame) || "n/a".equalsIgnoreCase(timeFrame)) {
                timeFrame = DEFAULT_TIMEFRAME;
            }

            DateTime start = new DateTime(agencyDate);
            Period period = PeriodEditor.fromText("P" + timeFrame.toUpperCase());
            DateTime end = start.plus(period);

            analysis.setStartDate(DateUtil.toYyyyMmDd(start));
            analysis.setEndDate(DateUtil.toYyyyMmDd(end));

            // we might not have any price data
            if (!StringUtils.isEmpty(protoAnalysis.getTarget())) {
                try {
                    analysis.setTarget(new BigDecimal(protoAnalysis.getTarget()));
                } catch (NumberFormatException ex) {
                    LOGGER.info("<storedAnalysis> can't parse number for price target: '"
                            + protoAnalysis.getTarget() + "' ignoring field");
                }
            }
            if (!StringUtils.isEmpty(protoAnalysis.getPreviousTarget())) {
                try {
                    analysis.setPreviousTarget(new BigDecimal(protoAnalysis.getPreviousTarget()));
                } catch (NumberFormatException ex) {
                    LOGGER.info("<storedAnalysis> can't parse number for previous price target: '"
                            + protoAnalysis.getPreviousTarget() + "' ignoring field");
                }
            }
            analysis.setTargetCurrency(protoAnalysis.getCurrency());

            analysis.setRating(protoAnalysis.getRating());
            analysis.setPreviousRating(protoAnalysis.getPreviousRating());
            analyses.put(analysisId,analysis);
        }
        return analyses.get(analysisId);
    }

    // source must not be null, protoAnalysis.getInstituteIsin() seems to be empty
    private Agency storedAgency(String source, Protos.Analysis protoAnalysis) {
        if (!agencies.containsKey(source)) {
            agencies.put(source, new Agency(source));
        }
        final Agency result = agencies.get(source);
        // hack for incomplete feed data: update symbol if available from protobuf
        if (StringUtils.isEmpty(result.getSymbol())
                && !StringUtils.isEmpty(protoAnalysis.getInstituteSymbol())) {
            result.setSymbol(protoAnalysis.getInstituteSymbol());
        }
        return result;
    }

    private Security storedSecurity(long iid, Protos.Analysis protoAnalysis) {
        final String symbol = protoAnalysis.getSymbol(0);
        if (!securities.containsKey(iid)) {
            securities.put(iid, new Security(iid, symbol));
        }
        return securities.get(iid);
    }

}
