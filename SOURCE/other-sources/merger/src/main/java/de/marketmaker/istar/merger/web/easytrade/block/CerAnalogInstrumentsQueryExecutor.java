/*
 * CerAnalogInstrumentsQueryExecutor.java
 *
 * Created on 16.09.11 10:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.CertificateTypeEnum;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * @author Michael Lösch
 * @author oflege
 */
abstract class CerAnalogInstrumentsQueryExecutor {

    static String getIsoDateMinusPercent(DateTime date, int percent, int minNMonths) {
        final long range = calcRange(date, percent);
        final DateTime now = new DateTime();
        final DateTimeFormatter fmt = ISODateTimeFormat.date();
        if (date.plus(range).getMillis() > now.plusMonths(minNMonths).getMillis()) {
            return fmt.print(date.minus(range));
        }
        return fmt.print(now.plusMonths(minNMonths));
    }

    static String getIsoDatePlusPercent(DateTime date, int percent, int minNMonths) {
        final long range = calcRange(date, percent);
        final DateTimeFormatter fmt = ISODateTimeFormat.date();
        final DateTime now = new DateTime();
        if (date.plus(range).getMillis() > now.plusMonths(minNMonths).getMillis()) {
            return fmt.print(date.plus(range));
        }
        return fmt.print(now.plusYears(100)); //100 is just any big value
    }

    private static long calcRange(DateTime date, int percent) {
        final DateTime now = new DateTime();
        final DateTime expirationDateRange = date.minus(now.toDate().getTime());
        return expirationDateRange.getMillis() / 100 * percent;
    }

    static String getHalfOf(DateTime date) {
        final DateTime now = new DateTime();
        final DateTime toExpirationDate = date.minus(now.toDate().getTime());
        final DateTimeFormatter fmt = ISODateTimeFormat.date();
        return fmt.print(now.plus(toExpirationDate.getMillis() / 2));
    }

    private static class DefaultQueryExecutor extends CerAnalogInstrumentsQueryExecutor {
        static final Map<String, String> QUERY_1 = new HashMap<>();
        static final Map<String, String> QUERY_2 = new HashMap<>();

        static {
            QUERY_2.put("certificateType==", "%type");
            QUERY_2.put("issuername==", "%issuer");
            QUERY_1.putAll(QUERY_2);
            QUERY_1.put("underlyingIid==", "%iid");
        }

        private DefaultQueryExecutor() {
            //noinspection unchecked
            this("performance3m", false, QUERY_1, QUERY_2);
        }

        private DefaultQueryExecutor(String sortField, boolean isAscending, Map<String, String>... queries) {
            super(sortField, isAscending, Arrays.asList(queries));
        }

        @Override
        public void bindVars(Quote underlying, RatioDataRecord reference, String issuerName) {
            addVar("%iid", underlying == null ? "" : String.valueOf(underlying.getInstrument().getId()));
            addVar("%type", reference.getCertificateType());
            addVar("%issuer", issuerName);
        }
    }

    private static class DiscountQueryExecutor extends DefaultQueryExecutor {
        static final Map<String, String> QUERY_1 = new HashMap<>();
        static final Map<String, String> QUERY_2 = new HashMap<>();
        static final Map<String, String> QUERY_3 = new HashMap<>();

        static {
            QUERY_3.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_3.put("expirationDate>", "%fromExpDate50");

            QUERY_2.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_2.put("expirationDate>", "%fromExpDate20");
            QUERY_2.put("expirationDate<", "%toExpDate20");
            QUERY_2.put("cap>", "0");

            QUERY_1.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_1.put("expirationDate>", "%fromExpDate10");
            QUERY_1.put("expirationDate<", "%toExpDate10");
            QUERY_1.put("cap>", "0");
        }

        private DiscountQueryExecutor() {
            //noinspection unchecked
            super("unchangedYieldRelativePerYear", false, QUERY_1, QUERY_2, QUERY_3,
                    DefaultQueryExecutor.QUERY_1, DefaultQueryExecutor.QUERY_2);
        }

        @Override
        public void bindVars(Quote underlying, RatioDataRecord reference,
                             String issuerName) {
            super.bindVars(underlying, reference, issuerName);
            addVar("%cap", String.valueOf(reference.getCap()));

            addDefaultFromToExpiration(reference);
        }
    }

    private static class BonusQueryExecutor extends DefaultQueryExecutor {
        static final Map<String, String> QUERY_1 = new HashMap<>();
        static final Map<String, String> QUERY_2 = new HashMap<>();
        static final Map<String, String> QUERY_3 = new HashMap<>();

        static {
            QUERY_3.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_3.put("expirationDate>", "%fromExpDate50");

            QUERY_2.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_2.put("expirationDate>", "%fromExpDate20");
            QUERY_2.put("expirationDate<", "%toExpDate20");
            QUERY_2.put("underlyingToCapRelative>=", "10");

            QUERY_1.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_1.put("expirationDate>", "%fromExpDate10");
            QUERY_1.put("expirationDate<", "%toExpDate10");
            QUERY_1.put("underlyingToCapRelative>=", "10");
        }

        private BonusQueryExecutor() {
            //noinspection unchecked
            super("unchangedYieldRelativePerYear", false, QUERY_1, QUERY_2, QUERY_3,
                    DefaultQueryExecutor.QUERY_1, DefaultQueryExecutor.QUERY_2);
        }

        @Override
        public void bindVars(Quote underlying, RatioDataRecord reference,
                             String issuerName) {
            super.bindVars(underlying, reference, issuerName);
            addVar("%underlyingToCapRelative", String.valueOf(reference.getUnderlyingToCapRelative()));

            addDefaultFromToExpiration(reference);
        }
    }

    private static class ReverseConvertibleQueryExecutor extends DefaultQueryExecutor {
        static final Map<String, String> QUERY_1 = new HashMap<>();
        static final Map<String, String> QUERY_2 = new HashMap<>();
        static final Map<String, String> QUERY_3 = new HashMap<>();

        static {
            QUERY_3.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_3.put("expirationDate>", "%fromExpDate50");

            QUERY_2.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_2.put("expirationDate>", "%fromExpDate20");
            QUERY_2.put("expirationDate<", "%toExpDate20");
            QUERY_2.put("gapStrikeRelative>", "0");

            QUERY_1.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_1.put("expirationDate>", "%fromExpDate10");
            QUERY_1.put("expirationDate<", "%toExpDate10");
            QUERY_1.put("gapStrikeRelative>", "0");
        }

        private ReverseConvertibleQueryExecutor() {
            //noinspection unchecked
            super("maximumYieldRelativePerYear", false, QUERY_1, QUERY_2, QUERY_3,
                    DefaultQueryExecutor.QUERY_1, DefaultQueryExecutor.QUERY_2);
        }

        @Override
        public void bindVars(Quote underlying, RatioDataRecord reference,
                             String issuerName) {
            super.bindVars(underlying, reference, issuerName);
            addVar("%gapStrikeRelative", String.valueOf(reference.getGapStrikeRelative()));

            addDefaultFromToExpiration(reference);
        }
    }

    private static class GuaranteeQueryExecutor extends DefaultQueryExecutor {
        static final Map<String, String> QUERY_1 = new HashMap<>();
        static final Map<String, String> QUERY_2 = new HashMap<>();
        static final Map<String, String> QUERY_3 = new HashMap<>();
        static final Map<String, String> QUERY_4 = new HashMap<>();
        static final Map<String, String> QUERY_5 = new HashMap<>();
        static final Map<String, String> QUERY_6 = new HashMap<>();

        static {
            QUERY_3.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_3.put("expirationDate>", "%fromExpDate50");
            QUERY_6.putAll(QUERY_3);
            QUERY_6.remove("underlyingIid");

            QUERY_2.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_2.put("expirationDate>", "%fromExpDate20");
            QUERY_2.put("expirationDate<", "%toExpDate20");
            QUERY_5.putAll(QUERY_2);
            QUERY_5.remove("underlyingIid");

            QUERY_1.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_1.put("expirationDate>", "%fromExpDate10");
            QUERY_1.put("expirationDate<", "%toExpDate10");
            QUERY_4.putAll(QUERY_1);
            QUERY_4.remove("underlyingIid");
        }

        private GuaranteeQueryExecutor() {
            //noinspection unchecked
            super("performance3m", false, QUERY_1, QUERY_2, QUERY_3, QUERY_4, QUERY_5, QUERY_6,
                    DefaultQueryExecutor.QUERY_1, DefaultQueryExecutor.QUERY_2);
        }

        @Override
        public void bindVars(Quote underlying, RatioDataRecord reference,
                             String issuerName) {
            super.bindVars(underlying, reference, issuerName);
            addDefaultFromToExpiration(reference);
        }
    }

    private static class SprintQueryExecutor extends DefaultQueryExecutor {
        static final Map<String, String> QUERY_1 = new HashMap<>();
        static final Map<String, String> QUERY_2 = new HashMap<>();
        static final Map<String, String> QUERY_3 = new HashMap<>();

        static {
            QUERY_3.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_3.put("expirationDate>", "%fromExpDate50");

            QUERY_2.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_2.put("expirationDate>", "%fromExpDate20");
            QUERY_2.put("expirationDate<", "%toExpDate20");
            QUERY_2.put("strike>", "price10");

            QUERY_1.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_1.put("expirationDate>", "%fromExpDate10");
            QUERY_1.put("expirationDate<", "%toExpDate10");
            QUERY_1.put("strike>", "price10");
        }

        private SprintQueryExecutor() {
            //noinspection unchecked
            super("maximumYieldRelativePerYear", false, QUERY_1, QUERY_2, QUERY_3,
                    DefaultQueryExecutor.QUERY_1, DefaultQueryExecutor.QUERY_2);
        }

        @Override
        public void bindVars(Quote underlying, RatioDataRecord reference,
                             String issuerName) {
            super.bindVars(underlying, reference, issuerName);

            final int price10 = Math.round(reference.getPrice().getValue().floatValue() * 1.1f);
            addVar("%price10", String.valueOf(price10));

            addDefaultFromToExpiration(reference);
        }
    }


    private static class KnockoutQueryExecutor extends DefaultQueryExecutor {
        static final Map<String, String> QUERY_1 = new HashMap<>();
        static final Map<String, String> QUERY_2 = new HashMap<>();
        static final Map<String, String> QUERY_3 = new HashMap<>();

        static {
            QUERY_3.putAll(DefaultQueryExecutor.QUERY_2);
            QUERY_3.put("isknockout==", "false");

            QUERY_2.putAll(DefaultQueryExecutor.QUERY_1);
            QUERY_2.put("isknockout==", "false");

            QUERY_1.putAll(QUERY_2);
            QUERY_1.put("leverageType==", "%leverageType");
        }

        private KnockoutQueryExecutor() {
            // CORE-14035:
            // Here is a special case where DefaultQueryExecutor's Queries will explicitly not be added
            // because that would not check for isknockout==false which checks wether a certificate has
            // already been invalidated by crossing the knock-out value. In the rare cases where
            // this minimum requirement cannot retrieve enough alternatives as requested we will
            // simply return fewer alternatives instead of returning invalid certificates
            //noinspection unchecked
            super("performance3m", false, QUERY_1, QUERY_2, QUERY_3);
        }

        @Override
        public void bindVars(Quote underlying, RatioDataRecord reference, String issuerName) {
            super.bindVars(underlying, reference, issuerName);

            addVar("%leverageType", String.valueOf(reference.getLeverageTypeGatrixx()));
        }
    }

    static CerAnalogInstrumentsQueryExecutor getQueryExecutor(CertificateTypeEnum anEnum) {
        switch (anEnum) {
            case CERT_DISCOUNT:
                return new DiscountQueryExecutor();
            case CERT_BONUS:
                return new BonusQueryExecutor();
            case CERT_REVERSE_CONVERTIBLE:
                return new ReverseConvertibleQueryExecutor();
            case CERT_GUARANTEE:
                return new GuaranteeQueryExecutor();
            case CERT_SPRINT:
                return new SprintQueryExecutor();
            case CERT_KNOCKOUT:
                return new KnockoutQueryExecutor();
            default:
                return new DefaultQueryExecutor();
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<Map<String, String>> queries;

    private final Map<String, String> vars;

    private final String sortField;

    private final boolean isAscending;

    private CerAnalogInstrumentsQueryExecutor(String sortField, boolean isAscending, List<Map<String, String>> queries) {
        this.queries = new ArrayList<>(queries);
        this.vars = new HashMap<>();
        this.sortField = sortField;
        this.isAscending = isAscending;
    }

    public abstract void bindVars(Quote underlying, RatioDataRecord reference, String issuerName);

    protected void addDefaultFromToExpiration(RatioDataRecord reference) {
        if (reference.getExpirationDate() != null) {
            addDefaultFromToExpiration(reference.getExpirationDate());
        }
    }

    private void addDefaultFromToExpiration(final DateTime expirationDate) {
        addVar("%fromExpDate10", getIsoDateMinusPercent(expirationDate, 10, 1));
        addVar("%toExpDate10", getIsoDatePlusPercent(expirationDate, 10, 1));
        addVar("%fromExpDate20", getIsoDateMinusPercent(expirationDate, 20, 2));
        addVar("%toExpDate20", getIsoDatePlusPercent(expirationDate, 20, 2));
        addVar("%fromExpDate50", getHalfOf(expirationDate));
    }

    protected void addVar(String key, String value) {
        if (value != null) {
            this.vars.put(key, value);
        }
    }

    public List<RatioDataResult> execute(RatiosProvider ratiosProvider,
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields,
            Quote underlying, RatioDataRecord reference, String issuerName, int count) {

        bindVars(underlying, reference, issuerName);
        List<RatioDataResult> result = new ArrayList<>(count);

        final Set<Long> iids = new HashSet<>();
        iids.add(reference.getInstrumentId());

        for (Map<String, String> queryMap : this.queries) {
            final String query = queryMapToQueryString(resolve(queryMap));
            final Map<String, String> parameters = AbstractFindersuchergebnis.parseQuery(query, fields);
            final RatioSearchRequest rsr = AbstractFindersuchergebnis.createRequest(InstrumentTypeEnum.CER);
            rsr.addParameters(parameters);
            rsr.addParameter("sort1", this.sortField);
            rsr.addParameter("sort1:D", Boolean.toString(!this.isAscending));
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<execute> " + rsr);
            }
            final RatioSearchResponse sr = ratiosProvider.search(rsr);
            if (sr.isValid() && !((DefaultRatioSearchResponse) sr).getElements().isEmpty()) {
                for (RatioDataResult rdr: ((DefaultRatioSearchResponse) sr).getElements()) {
                    if (!iids.contains(rdr.getInstrumentid())) {
                        result.add(rdr);
                        iids.add(rdr.getInstrumentid());
                        if (result.size() == count) {
                            return result;
                        }
                    }
                }
            }
        }
        return result;
    }

    private String queryMapToQueryString(Map<String, String> queryMap) {
        final StringBuilder sb = new StringBuilder();
        for (String key : queryMap.keySet()) {
            if (sb.length() > 0) {
                sb.append(" && ");
            }
            sb.append(key).append("'").append(queryMap.get(key)).append("'");
        }
        if (sb.length() > 0) {
            sb.append(" && price>'0.009'"); // add some sanity, exclude knocked out certs etc.
        }
        return sb.toString();
    }

    private Map<String, String> resolve(Map<String, String> query) {
        final Map<String, String> result = new HashMap<>();
        for (String key : query.keySet()) {
            final String value = query.get(key);
            final String resolved = value.startsWith("%") ? this.vars.get(value) : value;
            if (resolved != null) {
                result.put(key, resolved);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        final DateTime dateTime = new DateTime();
        final DateTime dt = dateTime.plusDays(24);

        System.out.println("from: " + getIsoDateMinusPercent(dt, 50, 1));
        System.out.println("to: " + getIsoDatePlusPercent(dt, 50, 1));
        System.out.println("half: " + getHalfOf(dt));
    }
}
