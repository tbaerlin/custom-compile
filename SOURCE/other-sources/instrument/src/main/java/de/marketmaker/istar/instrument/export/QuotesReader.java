/*
 * QuotesExporter.java
 *
 * Created on 27.02.12 08:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import oracle.jdbc.OracleTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domain.instrument.MmInstrumentclass;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.DomainContext;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.instrument.MinimumQuotationSizeDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.instrument.InstrumentUtil;

/**
 * Reads Quote data from an mdp ResultSet. Quotes from the ResultSet are ordered by instrumentid; this class
 * collects all the quotes for a single instrument and adds that set of quotes to a queue for
 * further processing.
 * @author oflege
 * @author mcoenen
 */
class QuotesReader extends AbstractDp2Reader {

    private class QuotesProcedure extends StoredProcedure {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        public QuotesProcedure() {
            super(dataSource, "begin ? := DPADFInterface.QuotesForIstarQ(vlistofsecurities => ?," +
                    " vforlasthours => ?, vorderby=>true, vwhichsystem => ?); end;");
            setFetchSize(fetchSize);
            setSqlReadyForUse(true);
            declareParameter(new SqlOutParameter("result", OracleTypes.CURSOR, rs -> {
                try {
                    readQuotes(rs);
                    logger.info("<extractData> finished");
                } catch (Throwable t) {
                    logger.error("<extractData> failed", t);
                    exportFailure.compareAndSet(null, t);
                    throw new DataRetrievalFailureException("QuotesProcedure failed", t);
                }
                return null;
            }));
            declareParameter(new SqlParameter("vlistofsecurities", Types.VARCHAR));
            declareParameter(new SqlParameter("vforlasthours", Types.NUMERIC));
            declareParameter(new SqlParameter("vwhichsystem", Types.VARCHAR));   // "T"/"P"
            compile();
        }
    }

    private long iid;

    private ExportParameters parameters;

    private final BlockingQueue<QuotesForInstrument> queue;

    private final AtomicReference<Throwable> exportFailure;

    private final ContentFlagsPostProcessor contentFlagsPostProcessor;

    private ResultSet rs;

    private int numEF = 0;

    private int numC = 0;

    private int numS = 0;

    private int numNull = 0;

    QuotesReader(DataSource dataSource,
            DomainContextImpl domainContext,
            ExportParameters parameters,
            BlockingQueue<QuotesForInstrument> queue,
            AtomicReference<Throwable> exportFailure,
            ContentFlagsPostProcessor contentFlagsPostProcessor,
            boolean keepDuplicates, int fetchSize) {
        super(dataSource, domainContext, keepDuplicates, fetchSize);
        this.parameters = parameters;
        this.queue = queue;
        this.exportFailure = exportFailure;
        this.contentFlagsPostProcessor = contentFlagsPostProcessor;
    }

    // for testing
    QuotesReader(DataSource dataSource,
            DomainContextImpl domainContext,
            ExportParameters parameters,
            BlockingQueue<QuotesForInstrument> queue,
            AtomicReference<Throwable> exportFailure,
            boolean keepDuplicates, int fetchSize) {
        this(dataSource, domainContext, parameters, queue, exportFailure, null, keepDuplicates,
            fetchSize);
    }

    QuotesReader(MdpExporterDp2 exporter, ExportParameters parameters, boolean keepDuplicates) {
        this(exporter.getDataSource(),
                exporter.getDomainContext(),
                parameters,
                exporter.getQuotesForInstruments(),
                exporter.exportFailure,
                exporter.getContentFlagsPostProcessor(),
                keepDuplicates, exporter.getFetchSize());
    }

    void run() {
        Thread.currentThread().setName(getClass().getSimpleName());
        try {
            new QuotesProcedure().execute(parameters.getInParameters());
            this.logger.info(String.format("ISTAR-435 null=%d, EF=%d, C=%d, S=%d"
                    , this.numNull, this.numEF, this.numC, this.numS));
        } catch (Throwable t) {
            this.logger.error("<run> failed", t);
        }
    }

    private void setQuoteFlagsFromMetaData() throws SQLException {
//        final ResultSetMetaData metaData = rs.getMetaData();
//        this.columAvailable = isColumnAvailable(metaData, columnName);
    }

    void readQuotes(ResultSet rs) throws Exception {
        this.rs = rs;
        doReadQuotes();
    }

    private void doReadQuotes() throws Exception {
        this.logger.info("<doReadQuotes> started");
        setQuoteFlagsFromMetaData();

        QuotesForInstrument quotes = null;
        try {
            Set<Long> quoteIds = new HashSet<>();
            //noinspection ThrowableResultOfMethodCallIgnored
            while (this.exportFailure.get() == null && rs.next()) {
                this.iid = rs.getLong("instrumentid");

                if (quotes == null || quotes.iid != iid) {
                    if (quotes != null) {
                        putQuotes(quotes);
                        quoteIds.clear();
                    }
                    quotes = new QuotesForInstrument(iid);
                }

                // instrumentclass may be null for some quotes, so we have to try multiple times
                if (quotes.getInstrumentclass() == null) {
                    assignInstrumentclass(iid, quotes);
                }

                final QuoteDp2 quote = readQuote(quoteIds);
                if (quote != null) {
                    quotes.add(quote);
                }
            }

            if (null != quotes) {
                putQuotes(quotes);
            }
        } finally {
            putQuotes(QuotesForInstrument.EOF);
        }
    }

    private void assignInstrumentclass(long iid, QuotesForInstrument quotes) throws SQLException {
        final String str = this.rs.getString("instrumentclass");
        if (str != null) {
            final MmInstrumentclass mmInstrumentclass;
            try {
                mmInstrumentclass = MmInstrumentclass.valueOf(str);
                quotes.setInstrumentclass(mmInstrumentclass);
            } catch (IllegalArgumentException e) {
                this.logger.warn("<assignInstrumentclass> Unknown instrumentclass for " + iid + ".iid:" + str);
            }
        }
    }

    private void putQuotes(QuotesForInstrument quotes) throws InterruptedException {
        if (!quotes.isEmpty() || quotes == QuotesForInstrument.EOF) {
            this.queue.put(quotes);
        }
        else {
            this.logger.warn("<putQuotes> no quotes for " + quotes.iid + ".iid");
        }
    }

    private QuoteDp2 readQuote(Set<Long> quoteIds) {
        long quoteid = -1;
        try {
            quoteid = rs.getLong("quoteid");
            if (!quoteIds.add(quoteid)) {
                this.logger.warn("<readQuote> duplicate quoteId found: " + quoteid);
                if (!keepDuplicates) {
                    return null;
                }
            }
            return doReadQuote(quoteid);
        } catch (Exception e) {
            this.logger.warn("<readQuote> failed reading quote " + quoteid, e);
            return null;
        }
    }

    private QuoteDp2 doReadQuote(long quoteid) throws SQLException {
        final QuoteDp2 quote = new QuoteDp2(quoteid);

        final long marketid = rs.getLong("marketid");
        final long currencyid = rs.getLong("currencyid");

        addSymbol(quote, KeysystemEnum.VWDFEED, getVwdfeedSymbol(true));
        addSymbol(rs, quote, "mmsymbol", KeysystemEnum.MMWKN);
        addSymbol(rs, quote, "bis_key", KeysystemEnum.BIS_KEY);
        addSymbol(rs, quote, "wm_ticker", KeysystemEnum.WM_TICKER);
        addSymbol(rs, quote, "wpk", KeysystemEnum.WM_WPK);
        addSymbol(rs, quote, "wp_name_kurz", KeysystemEnum.WM_WP_NAME_KURZ);
        addSymbol(rs, quote, "wp_name_lang", KeysystemEnum.WM_WP_NAME_LANG);
        addSymbol(rs, quote, "wp_name_zusatz", KeysystemEnum.WM_WP_NAME_ZUSATZ);
        addSymbol(rs, quote, "namesuffix_quote", KeysystemEnum.NAMESUFFIX_QUOTE);
        // TODO: The following two actually belong to the market and should be removed from the quote
        addSymbol(rs, quote, "mic", KeysystemEnum.MIC);
        addSymbol(rs, quote, "operating_mic", KeysystemEnum.OPERATING_MIC);
        String secondary = addSymbol(quote, KeysystemEnum.VWDFEED_SECONDARY, getVwdfeedSymbol(false));
        if (secondary != null) {
            this.logger.debug("<doReadQuote> secondary " + secondary + " -> " + quote);
        }
        addSymbol(rs, quote, "infrontid", KeysystemEnum.INFRONT_ID);

        final Date firstprice = rs.getDate("firstprice");
        if (!rs.wasNull()) {
            quote.setFirstHistoricPriceYyyymmdd(DateUtil.dateToYyyyMmDd(firstprice));
        }
        final int quotedef = rs.getInt("quotedef");
        quote.setQuotedef(quotedef);

        quote.setMarket(getDomainContext().getMarket(marketid));

        final Currency currency = getDomainContext().getCurrency(currencyid);
        quote.setCurrency(currency);
        quote.setMinimumQuotationSize(getMinimumQuotationSize(quote, currency));

        final long[] contentFlags = InstrumentUtil.binaryString2LongArray(getContentFlags());
        if (this.contentFlagsPostProcessor != null) {
            this.contentFlagsPostProcessor.postProcessFlags(this.iid, quoteid, contentFlags);
        }
        for (int i = 0; i < contentFlags.length; i++) {
            quote.setFlags(i, contentFlags[i]);
        }

        final String abos = rs.getString("abos");
        if (abos != null && abos.trim().length() > 0) {
            quote.addEntitlement(KeysystemEnum.MM, abos.split(","));
        }

        return quote;
    }

    private String getVwdfeedSymbol(boolean primary) throws SQLException {
        final String vwdfeed = rs.getString(primary ? "vwdfeed" : "vwdfeed_secondary");
        if (vwdfeed == null) {
            return null;
        }
        final String sectypeMdps = rs.getString("sectype_mdps");
        if (sectypeMdps == null) {
            if (primary) {
                this.numNull++;
            }
            return vwdfeed;
        }
        if (vwdfeed.startsWith("1.")) {
            if ("EF".equals(sectypeMdps)) {
                if (primary) {
                    this.numEF++;
                }
                return "18" + vwdfeed.substring(1);
            }
            if ("S".equals(sectypeMdps)) {
                if (primary) {
                    this.numS++;
                }
                return "99" + vwdfeed.substring(1);
            }
        }
        else if (vwdfeed.startsWith("8.")) {
            if ("C".equals(sectypeMdps)) {
                if (primary) {
                    this.numC++;
                }
                return "17" + vwdfeed.substring(1);
            }
        }
        return vwdfeed;
    }

    private MinimumQuotationSizeDp2 getMinimumQuotationSize(QuoteDp2 quote, Currency currency)
            throws SQLException {
        if (isQuoteWithLocalUnitSize(quote)) {
            return new MinimumQuotationSizeDp2(null,
                    QuotationUnit.evaluate(quote.getSymbolVwdcode()), currency);
        }

        // *************** WM Daten **********************************
        final boolean containsFonds = InstrumentUtil.isVwdFund(quote);

        final long xD240 = rs.getLong("XD240"); // Waehrungs ID fuer NICHT-FONDS
        final BigDecimal number = BigDecimal.valueOf(rs.getDouble("XD210B"));
        final Currency mqsCurrency = containsFonds
                ? currency
                : getDomainContext().getCurrency(xD240);

        return new MinimumQuotationSizeDp2(number,
                MinimumQuotationSize.Unit.NOTHING, mqsCurrency);
    }

    public DomainContext getDomainContext() {
        return domainContext;
    }

    private String getContentFlags() throws SQLException {
        final String cf = rs.getString("contentflags");
        return (null == cf) ? null : (cf.contains("1") ? "1" : "0") + cf; // content flags in MDP begins with flag at index 1
    }

    static boolean isQuoteWithLocalUnitSize(Quote quote) {
        return QuotationUnit.MARKETS_WITH_LOCAL_UNIT_SIZE.contains(quote.getSymbolVwdfeedMarket());
    }
}
