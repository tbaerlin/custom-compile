package de.marketmaker.istar.analyses.analyzer.stream;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.function.Function;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.marketmaker.istar.analyses.analyzer.Agency;
import de.marketmaker.istar.analyses.analyzer.Analysis;
import de.marketmaker.istar.analyses.analyzer.Index;
import de.marketmaker.istar.analyses.analyzer.Security;
import de.marketmaker.istar.analyses.analyzer.collect.PerformanceCollector.PerformanceValues;
import de.marketmaker.istar.analyses.analyzer.collect.RatingCollector.RatingValues;
import de.marketmaker.istar.analyses.analyzer.collect.SuccessCollector.SuccessValues;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * collection of functions for accessing object properties based on various strings/names
 *
 * example queries:
 * - collect securityName, securityVwdCode, analysisId, performance, prevClose, priceCurrency, target, targetCurrency for analysis sort performance desc
 *
 */
public final class Columns {

    static final String UNKNOWN = "unknown";  // no pick-function for the field
    static final String NULL = "null"; // pick-function, but no content for the field or empty field

    static final Function<Entry<?,?>, String> UNKNOWN_FIELD_FUNCTION = entry -> UNKNOWN;

    // this defines how we map fields from the entry's key or value into th final result
    // the string template needs to be configured to optionally contain these strings
    static final Table<Class, String, Function<Object, String>> FIELDS = HashBasedTable.create();
    static {

        /*  accessing fields from the Ratings class, usually aggregated integers */
        FIELDS.put(RatingValues.class, "buy", o -> Integer.toString(((RatingValues)o).getBuy()));
        FIELDS.put(RatingValues.class, "hold", o -> Integer.toString(((RatingValues)o).getHold()));
        FIELDS.put(RatingValues.class, "sell", o -> Integer.toString(((RatingValues)o).getSell()));
        FIELDS.put(RatingValues.class, "prevBuy", o -> Integer.toString(((RatingValues)o).getPrevBuy()));
        FIELDS.put(RatingValues.class, "prevHold", o -> Integer.toString(((RatingValues)o).getPrevHold()));
        FIELDS.put(RatingValues.class, "prevSell", o -> Integer.toString(((RatingValues)o).getPrevSell()));
        FIELDS.put(RatingValues.class, "newBuy", o -> Integer.toString(((RatingValues)o).getNewBuy()));
        FIELDS.put(RatingValues.class, "newHold", o -> Integer.toString(((RatingValues)o).getNewHold()));
        FIELDS.put(RatingValues.class, "newSell", o -> Integer.toString(((RatingValues)o).getNewSell()));
        FIELDS.put(RatingValues.class, "unknownRatings", o -> Integer.toString(((RatingValues)o).getUnknown()));
        FIELDS.put(RatingValues.class, "prevUnknownRatings", o -> Integer.toString(((RatingValues)o).getPrevUnknown()));
        FIELDS.put(RatingValues.class, "totalRatings", o -> Integer.toString(((RatingValues)o).getTotal()));

        /* accessing fields from the Performance class  */
        FIELDS.put(PerformanceValues.class, "performance", o -> {
            PerformanceValues performanceValues = (PerformanceValues) o;
            BigDecimal value = performanceValues.getAverageValue();
            if (value != null) {
                return value.toString();
            }
            return NULL;
        });
        FIELDS.put(PerformanceValues.class, "prevClose", o -> {
            PerformanceValues performanceValues = (PerformanceValues) o;
            BigDecimal value = performanceValues.getPreviousClose();
            if (value != null) {
                return value.toString();
            }
            return NULL;
        });

        /* accessing fields from the Success class  */
        FIELDS.put(SuccessValues.class, "successCount", o -> Integer.toString(((SuccessValues) o).getCount()));
        FIELDS.put(SuccessValues.class, "totalCount", o -> Integer.toString(((SuccessValues) o).getTotal()));


        /*   accessing fields from the Security class */
        FIELDS.put(Security.class, "securitySymbol", o -> ((Security)o).getSymbol());
        FIELDS.put(Security.class, "securityVwdCode", o -> ((Security)o).getVwdCode());
        FIELDS.put(Security.class, "securityType", o -> {
            InstrumentTypeEnum type = ((Security) o).getType();
            if (type != null) {
                return type.getDescription();
            }
            return NULL;
        });
        FIELDS.put(Security.class, "securityCurrency", o -> ((Security)o).getCurrency());
        FIELDS.put(Security.class, "securityName", o -> ((Security)o).getName());

        /*  accessing fields from the Index class */
        FIELDS.put(Index.class, "indexQid", o -> Long.toString(((Index)o).getId()));
        FIELDS.put(Index.class, "indexName", o -> ((Index)o).getName());

        /*   accessing fields from the Agency class */
        FIELDS.put(Agency.class, "agencyId", o -> ((Agency)o).getId());
        FIELDS.put(Agency.class, "agencySymbol", o -> ((Agency)o).getSymbol());

        /*   analysis fields from the Analysis class */
        FIELDS.put(Analysis.class, "analysisId", o -> ((Analysis)o).getAnalysisIdString());
        FIELDS.put(Analysis.class, "target", o -> {
            Analysis analysis = (Analysis) o;
            BigDecimal target = analysis.getTarget();
            if (target != null) {
                return target.toString();
            }
            return NULL;
        });
        FIELDS.put(Analysis.class, "prevTarget", o -> {
            Analysis analysis = (Analysis) o;
            BigDecimal previousTarget = analysis.getPreviousTarget();
            if (previousTarget != null) {
                return previousTarget.toString();
            }
            return NULL;
        });
        FIELDS.put(Analysis.class, "targetCurrency", o -> ((Analysis)o).getTargetCurrency());
        // getting close price via attached security
        FIELDS.put(Analysis.class, "priceCurrency", o -> {
            Analysis analysis = (Analysis) o;
            Security security = analysis.getSecurity();
            if (security != null) {
                return security.getCurrency();
            }
            return NULL;
        });
        FIELDS.put(Analysis.class, "securityVwdCode", o -> {
            Analysis analysis = (Analysis) o;
            Security security = analysis.getSecurity();
            if (security != null) {
                return security.getVwdCode();
            }
            return NULL;
        });
        FIELDS.put(Analysis.class, "securityName", o -> {
            Analysis analysis = (Analysis) o;
            Security security = analysis.getSecurity();
            if (security != null) {
                return security.getName();
            }
            return NULL;
        });

        // a security property, used when streaming for industries
        FIELDS.put(String.class, "industryName", o -> ((String)o));
    }

    // try to find the right class for the selected field names
    public static Class findClassForFields(Collection<String> fieldNames) {
        for (Class clazz : new Class[] {
                RatingValues.class,
                PerformanceValues.class,
                SuccessValues.class
        }) {
            // return the first hit for one of the fields
            for (String fieldName : fieldNames) {
                if (FIELDS.row(clazz).containsKey(fieldName)) {
                    return clazz;
                }
            }
        }
        return RatingValues.class; // by default we aggregate the rating values
    }

}
