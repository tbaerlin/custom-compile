/*
 * BewFields.java
 *
 * Created on 17.05.2010 11:55:27
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.Constants;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * A list of fields, each of which knows how to extract and format a particular data item
 * from a {@link de.marketmaker.istar.merger.web.bew.ResultItem}.
 * @author oflege
 */
class BewFields {

    static final DateTimeFormatter DATE_FMT = DateTimeFormat.forPattern("dd/MM/yy");

    private static final DecimalFormat PRICE_FMT =
            (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private static final String ZEROS = "0000000000";

    private static final Set<String> PREVIOUS_MONTH_FIELDS = new HashSet<>(
            Arrays.asList("CM", "DATECM")
    );

    private static final Set<String> PREVIOUS_YEAR_FIELDS = new HashSet<>(
            Arrays.asList("CA", "DATECA")
    );

    static {
        PRICE_FMT.applyLocalizedPattern("#.#####");
    }

    private abstract static class Field {
        abstract String render(ResultItem item);
    }

    private abstract static class QuoteField extends Field {
        @Override
        String render(ResultItem item) {
            return doRender(item.getQuote());
        }

        abstract String doRender(Quote q);
    }

    private abstract static class PriceField extends Field {
        boolean isDateField() {
            return false;
        }

        String renderDate(ResultItem item, Price p) {
            if (p == null || p.getDate() == null) {
                return "";
            }
            final DateTime dt;
            if ("Q".equals(item.getQuote().getSymbolVwdfeedMarket())
                    && p.getDate().secondOfDay().get() != 0) { // only for dates with time, so especially no EoD dates
                final DateTimeZone tz = item.getTimeZone();
                dt = tz != null ? p.getDate().toDateTime(tz) : p.getDate();
            }
            else {
                dt = p.getDate();
            }


            return DATE_FMT.print(dt);
        }

        String renderPrice(ResultItem item, Price p) {
            if (!isDefined(p)) {
                return "";
            }

            final BigDecimal value = p.getValue().multiply(item.getPriceFactor(), Constants.MC);

            synchronized (PRICE_FMT) {
                return PRICE_FMT.format(value);
            }
        }

        boolean isDefined(Price p) {
            return p != null && p.getValue() != null && p.getValue().signum() != 0;
        }

        @Override
        String render(ResultItem item) {
            if (item.getPriceRecord() == null) {
                return null;
            }

            return doRender(item, item.getPriceRecord());
        }

        abstract String doRender(ResultItem item, PriceRecord priceRecord);
    }

    private static final Map<String, Field> FIELDS = new HashMap<>();

    static {
        FIELDS.put("TIC", new Field() {
            @Override
            String render(ResultItem item) {
                return item.getSymbol();
            }
        });

        FIELDS.put("TRANSLATION", new Field() {
            @Override
            String render(ResultItem item) {
                return item.getClientsideMapping();
            }
        });

        FIELDS.put("PRICE_QUALITY", new Field() {
            @Override
            String render(ResultItem item) {
                return RequestContextHolder.getRequestContext().getProfile().getPriceQuality(item.getQuote()).name();
            }
        });

        FIELDS.put("PMABO", new Field() {
            @Override
            String render(ResultItem item) {
                final String[] abos = item.getQuote().getEntitlement().getEntitlements(KeysystemEnum.MM);
                return abos == null ? "" : Arrays.toString(abos);
            }
        });

        FIELDS.put("MMWKN", new Field() {
            @Override
            String render(ResultItem item) {
                return item.getQuote().getSymbolMmwkn();
            }
        });

        FIELDS.put("IID", new Field() {
            @Override
            String render(ResultItem item) {
                return EasytradeInstrumentProvider.iidSymbol(item.getQuote().getInstrument().getId());
            }
        });

        FIELDS.put("QID", new Field() {
            @Override
            String render(ResultItem item) {
                return EasytradeInstrumentProvider.qidSymbol(item.getQuote().getId());
            }
        });

        FIELDS.put("PMEXCTEXT", new Field() {
            @Override
            String render(ResultItem item) {
                return item.getQuote().getMarket().getName();
            }
        });

        FIELDS.put("SELEKTOR", new Field() {
            @Override
            String render(ResultItem item) {
                final String[] selectors = item.getQuote().getEntitlement().getEntitlements(KeysystemEnum.VWDFEED);
                return selectors == null ? "" : Arrays.toString(selectors);
            }
        });

        FIELDS.put("SELEKTORNUM", new Field() {
            @Override
            String render(ResultItem item) {
                final String[] selectors = item.getQuote().getEntitlement().getEntitlements(KeysystemEnum.VWDFEED);
                if (selectors == null) {
                    return "";
                }
                final String[] nums = new String[selectors.length];
                for (int i = 0; i < selectors.length; i++) {
                    final String selector = selectors[i];
                    nums[i] = EntitlementsVwd.toNumericSelector(selector);
                }
                return nums == null ? "" : Arrays.toString(nums);
            }
        });

        FIELDS.put("EXC", new Field() {
            @Override
            String render(ResultItem item) {
                return item.getExchange();
            }
        });

        FIELDS.put("DOMNSIN", new QuoteField() {
            // Domestic National Security Identification Number
            @Override
            String doRender(Quote q) {
                // swiss markets: return 'CH' + valor, filled with zeros to nine char string
                // german markets: return 'DE' + WKN
                final String marketCountry = q.getMarket().getCountry().getSymbolIso();
                final String wkn = q.getInstrument().getSymbolWkn();
                final String valor = q.getInstrument().getSymbolValor();
                final String sedol = q.getInstrument().getSymbolSedol();

                if ("FONDS".equals(q.getSymbolVwdfeedMarket())) {
                    return "DE" + wkn;
                }
                else if ("CH".equals(marketCountry) && StringUtils.hasText(valor)) {
                    return "CH" + ZEROS.substring(0, 9 - valor.length()) + valor; // construct local code
                }
                else if ("DE".equals(marketCountry) && StringUtils.hasText(wkn)) {
                    return "DE" + wkn;
                }
                else if ("GB".equals(marketCountry) && StringUtils.hasText(sedol)) {
                    return "GB" + sedol;
                }
                return null;
            }
        });

        FIELDS.put("EXCH", new Field() {
            @Override
            String render(ResultItem item) {
                final String exch = item.getExchange();
                if (!"!VWD".equals(exch)) {
                    return exch;
                }

                return MarketMapping.getBewOldMarketCode(item.getQuote());
            }
        });

        FIELDS.put("VWDEXCH", new QuoteField() {
            @Override
            String doRender(Quote q) {
                return q.getSymbolVwdfeedMarket(); // Exchange Code
            }
        });

        FIELDS.put("IS_HOMEEXCHANGE", new QuoteField() {
            @Override
            String doRender(Quote q) {
                Market home = q.getInstrument().getHomeExchange();
                return (home != null && home.getId() == q.getMarket().getId()) ? "1" : "0";
            }
        });

        FIELDS.put("VWDCODE", new QuoteField() {
            @Override
            String doRender(Quote q) {
                return q.getSymbolVwdcode(); // vwd-Code
            }
        });

        FIELDS.put("VWDFEED", new QuoteField() {
            @Override
            String doRender(Quote q) {
                return q.getSymbolVwdfeed(); // vwd-Feed
            }
        });

        FIELDS.put("ISIN", new QuoteField() {
            @Override
            String doRender(Quote q) {
                return q.getInstrument().getSymbolIsin(); // ISIN
            }
        });

        FIELDS.put("WKN", new QuoteField() {
            @Override
            String doRender(Quote q) {
                return q.getInstrument().getSymbolWkn(); // WKN
            }
        });

        FIELDS.put("SEDOL", new QuoteField() {
            @Override
            String doRender(Quote q) {
                return q.getInstrument().getSymbolSedol(); // SEDOL
            }
        });

        FIELDS.put("VALOR", new QuoteField() {
            @Override
            String doRender(Quote q) {
                return q.getInstrument().getSymbolValor(); // VALOR
            }
        });

        FIELDS.put("ST", new QuoteField() {
            @Override
            String doRender(Quote q) {
                return TypeMapping.getMapping(q.getInstrument().getInstrumentType());
            }
        });

        FIELDS.put("VWDST", new QuoteField() {
            @Override
            String doRender(Quote q) {
                return q.getInstrument().getInstrumentType().name(); // Security Type
            }
        });

        FIELDS.put("TCUR", new Field() {
            @Override
            String render(ResultItem item) {
                final String currencyIso = item.getQuote().getCurrency().getSymbolIso();

                if (!item.getQuote().getMinimumQuotationSize().isUnitPercent()) {
                    final boolean gbCurrency = "GBP".equals(currencyIso) || "GBX".equals(currencyIso);
                    if (gbCurrency && "LON".equals(item.getExchange())) {
                        return "GBX";
                    }
                    if (gbCurrency && "LOG".equals(item.getExchange())) {
                        return "GBP";
                    }

                    final boolean zaCurrency = "ZAR".equals(currencyIso)
                            || "ZAC".equals(currencyIso)
                            || "ZAX".equals(currencyIso);
                    if (zaCurrency && "JNR".equals(item.getExchange())) {
                        return "ZAR";
                    }
                    if (zaCurrency && "JNB".equals(item.getExchange())) {
                        return "ZAC";
                    }
                }

                // TODO: what about XXP, %, etc.?
                return currencyIso; // Trading Currency
            }
        });

        FIELDS.put("TITLE", new QuoteField() {
            @Override
            String doRender(Quote q) {
                final String wmName = q.getSymbolWmWpNameKurz();
                return StringUtils.hasText(wmName) ? wmName : q.getInstrument().getName(); // Security Short Name
            }
        });

        // Previous Day's Ask Price Ask
        FIELDS.put("ASKP", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderPrice(item, item.getAsk());
            }
        });

        // Date of Previous Day's Ask
        FIELDS.put("DATEAP", new PriceField() {
            @Override
            boolean isDateField() {
                return true;
            }

            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                final Price ask = item.getAsk();
                if (isDefined(ask)) {
                    return renderDate(item, ask);
                }
                return "";
            }
        });

        // Previous Day's Bid Price
        FIELDS.put("BIDP", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderPrice(item, item.getBid());
            }
        });

        // Date of Previous Day's Bid
        FIELDS.put("DATEBP", new PriceField() {
            @Override
            boolean isDateField() {
                return true;
            }

            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                final Price bid = item.getBid();
                if (isDefined(bid)) {
                    return renderDate(item, bid);
                }
                return "";
            }
        });

        // Previous Year's Last Closing
        FIELDS.put("CA", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                if (isDefined(item.getLastYearsUltimo())) {
                    return renderPrice(item, item.getLastYearsUltimo());
                }
                return "";
            }
        });

        // Date of Previous Year's Last Closing
        FIELDS.put("DATECA", new PriceField() {
            @Override
            boolean isDateField() {
                return true;
            }

            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                if (isDefined(item.getLastYearsUltimo())) {
                    return renderDate(item, item.getLastYearsUltimo());
                }
                return "";
            }
        });

        FIELDS.put("VPSOURCE", new Field() {
            @Override
            String render(ResultItem item) {
                final String source = item.getValuationPriceSource();
                if (StringUtils.hasText(source)) {
                    return source;
                }
                return "";
            }
        });

        // Closing Price
        // CLOSE == VP for BEW/old, mail by Stefan Grossen, 2010-09-09
        FIELDS.put("CLOSE", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderPrice(item, item.getValuationPrice());
            }
        });

        // Date of Closing Price
        // CLOSE == VP for BEW/old, mail by Stefan Grossen, 2010-09-09
        FIELDS.put("DATEC", new PriceField() {
            @Override
            boolean isDateField() {
                return true;
            }

            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderDate(item, item.getValuationPrice());
            }
        });

        // Previous Month's Last Closing
        FIELDS.put("CM", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                if (isDefined(item.getLastMonthsUltimo())) {
                    return renderPrice(item, item.getLastMonthsUltimo());
                }
                return "";
            }
        });

        // Date of Previous Month's Last Closing
        FIELDS.put("DATECM", new PriceField() {
            @Override
            boolean isDateField() {
                return true;
            }

            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                if (isDefined(item.getLastMonthsUltimo())) {
                    return renderDate(item, item.getLastMonthsUltimo());
                }
                return "";
            }
        });

        // Previous Day's Settlement Price
        FIELDS.put("SETTLP", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderPrice(item, priceRecord.getSettlement());
            }
        });

        // Date Open
        FIELDS.put("DATEO", new PriceField() {
            @Override
            boolean isDateField() {
                return true;
            }

            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderDate(item, priceRecord.getOpen());
            }
        });

        // Open
        FIELDS.put("OPEN", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderPrice(item, priceRecord.getOpen());
            }
        });

        // Date Day High
        FIELDS.put("DATEH", new PriceField() {
            @Override
            boolean isDateField() {
                return true;
            }

            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderDate(item, priceRecord.getHighDay());
            }
        });

        // Day High
        FIELDS.put("HIGH", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderPrice(item, priceRecord.getHighDay());
            }
        });

        // Date Day Low
        FIELDS.put("DATEL", new PriceField() {
            @Override
            boolean isDateField() {
                return true;
            }

            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderDate(item, priceRecord.getLowDay());
            }
        });

        // Day Low
        FIELDS.put("LOW", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderPrice(item, priceRecord.getLowDay());
            }
        });

        // Date of Previous Day's Settlement Price
        FIELDS.put("TIMESP", new PriceField() {
            @Override
            boolean isDateField() {
                return true;
            }

            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderDate(item, priceRecord.getSettlement());
            }
        });

        // Valuation Price
        FIELDS.put("VP", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderPrice(item, item.getValuationPrice());
            }
        });

        // Date of Valuation Price
        FIELDS.put("DATEVP", new PriceField() {
            @Override
            boolean isDateField() {
                return true;
            }

            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                return renderDate(item, item.getValuationPrice());
            }
        });

        // NAV
        FIELDS.put("NAV", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                if (!(priceRecord instanceof PriceRecordFund)) {
                    return "";
                }
                return renderPrice(item, ((PriceRecordFund) priceRecord).getNetAssetValue());
            }
        });
        // ISSUEPRICE
        FIELDS.put("ISSUEPRICE", new PriceField() {
            @Override
            String doRender(ResultItem item, PriceRecord priceRecord) {
                if (!(priceRecord instanceof PriceRecordFund)) {
                    return "";
                }
                return renderPrice(item, ((PriceRecordFund) priceRecord).getIssuePrice());
            }
        });
    }

    static boolean isWithPrices(Collection<String> fields) {
        return fields.stream()
                .map(BewFields::toBasefield)
                .map(FIELDS::get)
                .anyMatch(field -> field instanceof PriceField);
    }

    static boolean isDateField(String fieldname) {
        final Field field = FIELDS.get(toBasefield(fieldname));
        return field instanceof PriceField && ((PriceField) field).isDateField();
    }

    private static String toBasefield(String field) {
        final int i = field.indexOf("[");
        if (i < 0) {
            return field;
        }
        return field.substring(0, i);
    }

    static boolean isWithPreviousMonthFields(Collection<String> fields) {
        return isWithFieldsFrom(fields, PREVIOUS_MONTH_FIELDS);
    }

    static boolean isWithPreviousYearFields(Collection<String> fields) {
        return isWithFieldsFrom(fields, PREVIOUS_YEAR_FIELDS);
    }

    private static boolean isWithFieldsFrom(Collection<String> fields, Set<String> from) {
        return fields.stream().map(BewFields::toBasefield).anyMatch(from::contains);
    }

    private static Field getField(String fieldName) {
        return FIELDS.get(fieldName);
    }

    static BewFields getFields(Collection<String> fieldNames) {
        final List<Field> fields =
                fieldNames.stream()
                        .map(BewFields::toBasefield)
                        .map(BewFields::getField)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        return new BewFields(fields);
    }

    private List<Field> fields;

    BewFields(List<Field> fields) {
        this.fields = fields;
    }

    void append(ResultItem item, StringBuilder sb) {
        for (final Field field : fields) {
            sb.append(';');
            final String value = field.render(item);
            if (value != null) {
                sb.append(value);
            }
        }
    }

    void appendEmptyContent(StringBuilder sb) {
        fields.stream().forEach(f -> sb.append(';'));
    }
}
