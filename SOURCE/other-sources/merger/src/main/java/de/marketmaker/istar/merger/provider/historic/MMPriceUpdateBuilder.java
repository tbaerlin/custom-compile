package de.marketmaker.istar.merger.provider.historic;

import de.marketmaker.istar.common.mm.MMPriceUpdate;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.merger.provider.SymbolQuote;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Bundle logic for {@link MMPriceUpdate} construction, which is required for Portfolio Manager intraday price updates.
 *
 * <ul>
 *     <li>{@link MMPriceUpdate} of istar-common does not include Java classes required for construction,
 *     thus a separate builder was created in istar-merger</li>
 *
 *     <li>Workarounds by altering {@link MMPriceUpdate}'s properties are centralized in this builder</li>
 * </ul>
 */
public abstract class MMPriceUpdateBuilder {

    public abstract MMPriceUpdate build();

    public static final MMPriceUpdateBuilder byEodTerms(EodTermRepo eodTermRepo, Quote quote, PriceRecord priceRecord) {
        return new MMPriceUpdateBuilder() {
            @Override
            public MMPriceUpdate build() {
                if (quote == null) {
                    return null;
                }

                return translate(quote.getSymbolMmwkn(), isFund(quote), quote.getQuotedef(), MMPriceUpdateBuilder.isCentWithWorkaround(quote), priceRecord);
            }

            MMPriceUpdate translate(String mmwkn, boolean isFund, int quoteDef, boolean isCent, PriceRecord pr) {
                final DateTime date = HistoricTimeseriesUtils.getDate(pr);

                if (isInvalid(mmwkn, pr, date)) {
                    return null;
                }

                return new MMPriceUpdate(mmwkn, isFund, isCent,
                        date.toLocalDate(),
                        getPrice(PriceType.OPEN, quoteDef, pr),
                        getPrice(PriceType.HIGH, quoteDef, pr),
                        getPrice(PriceType.LOW, quoteDef, pr),
                        getPrice(PriceType.CLOSE, quoteDef, pr),
                        getPrice(PriceType.KASSA, quoteDef, pr),
                        getPrice(PriceType.VOLUME, quoteDef, pr),
                        getPrice(PriceType.CONTRACT, quoteDef, pr),
                        getPrice(PriceType.OPENINTEREST, quoteDef, pr)
                );
            }

            private BigDecimal getPrice(PriceType priceType, int quoteDef, PriceRecord pr) {
                final EodTermRepo.Term term = eodTermRepo.getStandardTerm(quoteDef, priceType);
                if (term == null) {
                    return null;
                }
                Price p = term.getPrice(pr);
                return p != null ? p.getValue() : null;
            }
        };
    }

    public static final MMPriceUpdateBuilder byMapper(QuotedefMapper mapper, Quote quote, PriceRecord priceRecord) {
        return new MMPriceUpdateBuilder() {
            @Override
            public MMPriceUpdate build() {
                if (quote == null) {
                    return null;
                }

                return translate(quote.getSymbolMmwkn(), isFund(quote), quote.getQuotedef(), MMPriceUpdateBuilder.isCentWithWorkaround(quote), priceRecord);
            }

            MMPriceUpdate translate(String mmwkn, boolean isFund, int quoteDef, boolean isCent, PriceRecord pr) {
                return doTranslate(mapper, mmwkn, isFund, quoteDef, isCent, pr);
            }
        };
    }

    public static final MMPriceUpdateBuilder byMapper(QuotedefMapper mapper, SymbolQuote quote, PriceRecord priceRecord) {
        return new MMPriceUpdateBuilder() {
            @Override
            public MMPriceUpdate build() {
                if (quote == null) {
                    return null;
                }

                return translate(quote.getSymbolMmwkn(), priceRecord instanceof PriceRecordFund, quote.getQuotedef(), MMPriceUpdateBuilder.isCentWithWorkaround(quote), priceRecord);
            }

            MMPriceUpdate translate(String mmwkn, boolean isFund, int quoteDef, boolean isCent, PriceRecord pr) {
                return doTranslate(mapper, mmwkn, isFund, quoteDef, isCent, pr);
            }
        };
    }

    private static boolean isFund(Quote quote) {
        if (quote.getInstrument() == null) {
            return false;
        }
        return quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.FND && InstrumentUtil.isVwdFund(quote);
    }

    private static boolean isCentWithWorkaround(Quote quote) {
        Currency currency = quote.getCurrency();
        return currency != null ? !needsCentWorkaround(currency.getSymbolIso()) && currency.isCent() : false;
    }

    private static boolean isCentWithWorkaround(SymbolQuote quote) {
        return !needsCentWorkaround(quote.getCurrencyIso()) && quote.isCent();
    }

    /**
     * Although ratios in <emp>cent</emp> currencies like GBX and USX can be derived from calculations
     * on EOD historic data of the related base currencies (GBP and USD) sometimes the EOD data
     * is in <emph>cent</emph> and then we need to apply a workaround where we set {@link MMPriceUpdate#isCent()}
     * {@code false}.
     */
    private static boolean needsCentWorkaround(String currencyIso) {
        return "USX".equals(currencyIso);
    }

    private static MMPriceUpdate doTranslate(QuotedefMapper mapper, String mmwkn, boolean isFund, int quoteDef, boolean isCent,
                                      PriceRecord pr) {
        final DateTime date = HistoricTimeseriesUtils.getDate(pr);

        if (isInvalid(mmwkn, pr, date)) {
            return null;
        }

        return new MMPriceUpdate(mmwkn, isFund, isCent,
                date.toLocalDate(),
                mapper.getOpen(quoteDef, pr),
                mapper.getHigh(quoteDef, pr),
                mapper.getLow(quoteDef, pr),
                mapper.getClose(quoteDef, pr),
                mapper.getKassa(quoteDef, pr),
                mapper.getVolume(quoteDef, pr),
                mapper.getContracts(quoteDef, pr),
                mapper.getOpenInterest(quoteDef, pr)
        );
    }

    private static boolean isInvalid(String mmwkn, PriceRecord pr, DateTime date) {
        return mmwkn == null || pr == null || date == null;
    }
}
