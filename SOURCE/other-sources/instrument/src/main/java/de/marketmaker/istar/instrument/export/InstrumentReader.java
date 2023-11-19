/*
 * InstrumentReader.java
 *
 * Created on 27.02.12 09:05
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

import oracle.jdbc.OracleTypes;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.StoredProcedure;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.DerivativeTypeEnum;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domainimpl.instrument.BondDp2;
import de.marketmaker.istar.domainimpl.instrument.CertificateDp2;
import de.marketmaker.istar.domainimpl.instrument.CommodityDp2;
import de.marketmaker.istar.domainimpl.instrument.CurrencyCrossrateDp2;
import de.marketmaker.istar.domainimpl.instrument.DerivativeDp2;
import de.marketmaker.istar.domainimpl.instrument.DerivativeWithStrikeDp2;
import de.marketmaker.istar.domainimpl.instrument.DetailedInstrumentTypeDp2;
import de.marketmaker.istar.domainimpl.instrument.FundDp2;
import de.marketmaker.istar.domainimpl.instrument.FutureDp2;
import de.marketmaker.istar.domainimpl.instrument.IndexDp2;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.domainimpl.instrument.MacroEconomicDataDp2;
import de.marketmaker.istar.domainimpl.instrument.OptionDp2;
import de.marketmaker.istar.domainimpl.instrument.ParticipationCertificateDp2;
import de.marketmaker.istar.domainimpl.instrument.RateDp2;
import de.marketmaker.istar.domainimpl.instrument.RealEstateDp2;
import de.marketmaker.istar.domainimpl.instrument.StockDp2;
import de.marketmaker.istar.domainimpl.instrument.UnderlyingDp2;
import de.marketmaker.istar.domainimpl.instrument.WarrantDp2;

import static de.marketmaker.istar.domain.instrument.InstrumentTypeEnum.*;

/**
 * Reads instrument data from an mdp ResultSet.
 *
 * @author oflege
 * @author mcoenen
 */
class InstrumentReader extends AbstractDp2Reader {
    static final String ID_NAME_PREFIX = "id: ";

    /**
     * types that we don't want to handle or cannot handle yet
     */
    private static final Set<InstrumentTypeEnum> TYPES_TO_IGNORE = EnumSet.of(BZG, NOT, SPR);

    private static final EnumMap<InstrumentTypeEnum, String> WM_GD_195_SUBSTITUTE_SYMBOL
            = new EnumMap<>(InstrumentTypeEnum.class);

    static {
        WM_GD_195_SUBSTITUTE_SYMBOL.put(IND, "Index");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(GNS, "Genussschein");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(WNT, "Optionsschein");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(CUR, "Devise");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(FUT, "Future");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(FND, "Fonds");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(OPT, "Option");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(STK, "Aktie");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(ZNS, "Zins_Geldmarktsatz");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(BND, "Anleihe");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(NON, "Unbekannt");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(CER, "Zertifikat");
        WM_GD_195_SUBSTITUTE_SYMBOL.put(MER, "Ware_Rohstoff_Edelmetall");
    }

    private class InstrumentsProcedure extends StoredProcedure {

        private final Logger logger = LoggerFactory.getLogger(getClass());

        public InstrumentsProcedure() {
            super(dataSource, "begin ? := DPADFInterface.QuotesForIstarS(vlistofsecurities => ?," +
                    " vforlasthours => ?, vorderby=>true, vwhichsystem => ?); end;");
            setFetchSize(fetchSize);
            setSqlReadyForUse(true);
            declareParameter(new SqlOutParameter("result", OracleTypes.CURSOR, rs -> {
                try {
                    readInstruments(rs);
                    logger.info("<extractData> finished");
                } catch (Throwable t) {
                    logger.error("<extractData> failed", t);
                    exportFailure.compareAndSet(null, t);
                    throw new DataRetrievalFailureException("InstrumentsProcedure failed", t);
                }
                return null;
            }));
            declareParameter(new SqlParameter("vlistofsecurities", Types.VARCHAR));
            declareParameter(new SqlParameter("vforlasthours", Types.NUMERIC));
            declareParameter(new SqlParameter("vwhichsystem", Types.VARCHAR));   // "t"/"p"
            compile();
        }
    }

    private static final DecimalFormat DF = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        DF.applyLocalizedPattern("0.#######");
    }

    private final EnumMap<InstrumentTypeEnum, MutableInt> ignoredTypeCounts
            = new EnumMap<>(InstrumentTypeEnum.class);

    private final AtomicReference<Throwable> exportFailure;

    private final ExportParameters parameters;

    private final BlockingQueue<InstrumentAssembleInfo> queue;

    private int nullTypes;

    InstrumentReader(MdpExporterDp2 exporter, ExportParameters parameters, boolean keepDuplicates) {
        super(exporter.getDataSource(), exporter.getDomainContext(), keepDuplicates,
            exporter.getFetchSize());
        this.queue = exporter.getInstrumentInfos();
        this.exportFailure = exporter.exportFailure;
        this.parameters = parameters;

        for (InstrumentTypeEnum t: TYPES_TO_IGNORE) {
            this.ignoredTypeCounts.put(t, new MutableInt());
        }
    }

    void run() {
        Thread.currentThread().setName(getClass().getSimpleName());
        try {
            new InstrumentsProcedure().execute(this.parameters.getInParameters());
        } catch (Throwable t) {
            this.logger.error("<run> failed", t);
        }
    }

    private void setInstrumentFlagsFromMetaData(ResultSetMetaData metaData) throws SQLException {
//        this.oewknAvailable = isColumnAvailable(metaData, "oewkn");
    }

    private void readInstruments(ResultSet rs) throws Exception {
        this.logger.info("<readInstruments> started");
        setInstrumentFlagsFromMetaData(rs.getMetaData());
        try {
            Set<Long> instrumentIds = new HashSet<>();
            //noinspection ThrowableResultOfMethodCallIgnored
            while (exportFailure.get() == null && rs.next()) {
                final long iid = rs.getLong("instrumentid");
                if (!instrumentIds.add(iid)) {
                    this.logger.warn("<readInstruments> duplicate instrumentId found: " + iid);
                    if (!keepDuplicates) {
                        continue;
                    }
                }
                final InstrumentAssembleInfo info = readInstrumentAssembleInfo(iid, rs);
                if (info != null) {
                    this.queue.put(info);
                }
            }
        } finally {
            this.queue.put(InstrumentAssembleInfo.EOF);
            this.logger.info("<readInstruments> ignored " + ignoredTypeCounts);
            if (this.nullTypes > 0) {
                this.logger.info("<readInstruments> # with null type " + this.nullTypes);
            }
        }
    }

    static String getWmGd195SubstituteSymbol(InstrumentTypeEnum type) {
        return WM_GD_195_SUBSTITUTE_SYMBOL.get(type);
    }


    private InstrumentAssembleInfo readInstrumentAssembleInfo(long iid, ResultSet rs)
            throws SQLException {
        final InstrumentDp2 instrument = createInstrument(iid, rs);
        if (instrument == null) {
            return null;
        }
        return new InstrumentAssembleInfo(instrument, rs.getInt("GD440"), rs.getInt("GD455A"),
                getQuotedPer(rs));
    }

    private InstrumentDp2 createInstrument(long iid, ResultSet rs) throws SQLException {
        final InstrumentTypeEnum type;
        String typeName = null;
        try {
            typeName = rs.getString("type");
            if (typeName == null) {
                this.nullTypes++;
                return null;
            }
            type = toInstrumentType(typeName);
        } catch (IllegalArgumentException e) {
            this.logger.warn("<readInstrument> invalid type for " + iid + ": " + typeName);
            return null;
        }

        if (type != null && ignoredTypeCounts.containsKey(type)) {
            this.ignoredTypeCounts.get(type).increment();
            return null;
        }

        final InstrumentDp2 instrument;
        try {
            instrument = createInstrument(iid, type, rs);
        } catch (Exception e) {
            this.logger.warn("<readInstrument> failed for " + iid, e);
            return null;
        }

        if (instrument == null) {
            return null;
        }

        try {
            if (!StringUtils.hasText(instrument.getName())) {
                final String name = rs.getString("name");
                instrument.setName(getName(name));
            }

            ensureName(instrument);

            setAdditionalNames(rs, instrument);
            setAliases(rs, instrument);
            setLei(rs, instrument);


            final long homeexchange = rs.getLong("homeexchange");
            instrument.setHomeExchange(this.domainContext.getMarket(homeexchange));

            final long country = rs.getLong("country");
            instrument.setCountry(this.domainContext.getCountry(country));

            final long sector = rs.getLong("sector");
            instrument.setSector(this.domainContext.getSector(sector));

            addSymbol(rs, instrument, "defaultmmsymbol", KeysystemEnum.DEFAULTMMSYMBOL);

            if (instrument instanceof DerivativeDp2) {
                final String s = rs.getString("subscription_ratio");
                if (StringUtils.hasText(s)) {
                    try {
                        BigDecimal sr = new BigDecimal(s.replace(',', '.'));
                        ((DerivativeDp2) instrument).setSubscriptionRatio(sr);
                    } catch (Exception e) {
                        this.logger.debug("<createInstrument> cannot set subscriptionRatio " + s + " for instrument " + iid);
                    }
                }
            }

            final DetailedInstrumentTypeDp2 dit = new DetailedInstrumentTypeDp2();
            instrument.setDetailedInstrumentType(dit);

            addSymbol(rs, dit, "wm_type_id", KeysystemEnum.WM_GD195_ID);
            addSymbol(rs, dit, "wm_type_name", KeysystemEnum.WM_GD195_NAME);
            addSymbol(rs, dit, "GD198B", KeysystemEnum.WM_GD198B_ID);
            addSymbol(rs, dit, "GD198C", KeysystemEnum.WM_GD198C_ID);

            if (instrument instanceof IndexDp2) {
                if (!StringUtils.hasText(dit.getSymbol(KeysystemEnum.WM_GD195_ID))) {
                    dit.setSymbol(KeysystemEnum.WM_GD195_ID, "950");
                    dit.setSymbol(KeysystemEnum.WM_GD195_NAME, "Sonstige Indices");
                }
            }

            // Hack for AAB to substitute deceasing GD195
            if (!StringUtils.hasText(dit.getSymbol(KeysystemEnum.WM_GD195_ID))) {
                dit.setSymbol(KeysystemEnum.WM_GD195_ID, getWmGd195SubstituteSymbol(type));
            }

            addSymbol(rs, instrument, "isin", KeysystemEnum.ISIN);
            addSymbol(rs, instrument, "wkn", KeysystemEnum.WKN);
            addSymbol(rs, instrument, "oewkn", KeysystemEnum.OEWKN);
            addSymbol(rs, instrument, "valorsymbol", KeysystemEnum.VALORSYMBOL);
            addSymbol(rs, instrument, "valor", KeysystemEnum.VALOR);
            addSymbol(rs, instrument, "sedol", KeysystemEnum.SEDOL);
            addSymbol(rs, instrument, "cusip", KeysystemEnum.CUSIP);
            addSymbol(rs, instrument, "fww", KeysystemEnum.FWW);
            addSymbol(rs, instrument, "gatrixx", KeysystemEnum.GATRIXX);
            addSymbol(rs, instrument, "ticker", KeysystemEnum.TICKER);
            addSymbol(rs, instrument, "eurexticker", KeysystemEnum.EUREXTICKER);
            addSymbol(rs, instrument, "GD664", KeysystemEnum.GD664);
            addSymbol(rs, instrument, "GD260", KeysystemEnum.GD260);
            addSymbol(rs, instrument, "lname", KeysystemEnum.MMNAME);
        } catch (Exception e) {
            this.logger.warn("<createInstrument> failed reading instrument " + iid, e);
            return null;
        }

        return instrument;
    }

    private InstrumentTypeEnum toInstrumentType(String typeName) {
        if ("NOT".equals(typeName)) {
            return null;
        }
        if ("BZG".equals(typeName) || "SPR".equals(typeName)) {
            return STK; // temporary hack
        }
        return valueOf(typeName);
    }

    private InstrumentDp2 createInstrument(long instrumentid, InstrumentTypeEnum type,
            ResultSet rs) throws SQLException {
        if (null == type) {
            return createUnion(instrumentid, rs);
        }
        switch (type) {
            case BND:
                return createBond(instrumentid, rs);
            case CER:
                return createCertificate(instrumentid, rs);
            case CUR:
                return createCurrency(instrumentid, rs);
            case FND:
                return createFund(instrumentid, rs);
            case FUT:
                return createFuture(instrumentid, rs);
            case GNS:
                return createParticipationCertificate(instrumentid, rs);
            case IMO:
                return new RealEstateDp2(instrumentid);
            case IND:
                return new IndexDp2(instrumentid);
            case MER:
                return new CommodityDp2(instrumentid);
            case MK:
                return createMacroEconomicData(instrumentid, rs);
            case NON:
                return null;
            case OPT:
                return createOption(instrumentid, rs);
            case STK:
                return createStock(instrumentid, rs);
            case UND:
                return new UnderlyingDp2(instrumentid);
            case WNT:
                return createWarrant(instrumentid, rs);
            case ZNS:
                return createRate(instrumentid, rs);
        }
        return null;
    }

    private InstrumentDp2 createUnion(long instrumentid, ResultSet rs) throws SQLException {
        return UnionDp2.create(instrumentid, rs);
    }

    private MacroEconomicDataDp2 createMacroEconomicData(long instrumentid, ResultSet rs)
            throws SQLException {
        final MacroEconomicDataDp2 result = new MacroEconomicDataDp2(instrumentid);
        addSymbol(rs, result, "longname", KeysystemEnum.MDP_SYNONYM_NAME);
        return result;
    }

    private InstrumentDp2 createWarrant(long instrumentid, ResultSet rs) throws SQLException {
        final WarrantDp2 result = new WarrantDp2(instrumentid);
        final long wntUnd = rs.getLong("wnt_und");
        if (!rs.wasNull()) {
            result.setUnderlyingId(wntUnd);
        }
        final Date wntMaturity = rs.getDate("wnt_maturity");
        if (!rs.wasNull()) {
            result.setExpirationDate(DateUtil.dateToYyyyMmDd(wntMaturity));
        }

        final double wntstrike = rs.getDouble("wnt_strike");
        if (!rs.wasNull()) {
            result.setStrike(toBigDecimal(wntstrike));
        }
        addStrikeCurrency(result, rs);
        final String wntostype = rs.getString("wnt_ostype"); // C or P
        if (!rs.wasNull()) {
            result.setType(getDerivativeType(wntostype));
        }

        result.setName(getName(rs.getString("gatrixx")));

        return result;
    }

    private InstrumentDp2 createStock(long instrumentid, ResultSet rs) throws SQLException {
        final StockDp2 result = new StockDp2(instrumentid);
        final Date date = rs.getDate("agm");
        if (!rs.wasNull()) {
            result.setGeneralMeetingDate(DateUtil.dateToYyyyMmDd(date));
        }
        return result;
    }

    private InstrumentDp2 createOption(long instrumentid, ResultSet rs) throws SQLException {
        final OptionDp2 result = new OptionDp2(instrumentid);
        final long optUnd = rs.getLong("opt_und");
        if (!rs.wasNull()) {
            result.setUnderlyingId(optUnd);
        }
        final Date optMaturity = rs.getDate("opt_maturity");
        if (!rs.wasNull()) {
            result.setExpirationDate(DateUtil.dateToYyyyMmDd(optMaturity));
        }

        final double optstrike = rs.getDouble("opt_strike");
        if (!rs.wasNull()) {
            result.setStrike(toBigDecimal(optstrike));
        }
        addStrikeCurrency(result, rs);
        final String optostype = rs.getString("opt_ostype"); // C or P
        if (!rs.wasNull()) {
            result.setType(getDerivativeType(optostype));
        }

        return result;
    }

    private InstrumentDp2 createParticipationCertificate(long instrumentid,
            ResultSet rs) throws SQLException {
        final ParticipationCertificateDp2 result = new ParticipationCertificateDp2(instrumentid);
        final Date gnsMaturity = rs.getDate("gns_maturity");
        if (!rs.wasNull()) {
            result.setExpirationDate(DateUtil.dateToYyyyMmDd(gnsMaturity));
        }
        return result;
    }

    private InstrumentDp2 createFuture(long instrumentid, ResultSet rs) throws SQLException {
        final FutureDp2 result = new FutureDp2(instrumentid);
        final long fbndUnd = rs.getLong("fbnd_und");
        final long futUnd = rs.getLong("fut_und");
        if (fbndUnd > 0) {
            result.setUnderlyingId(fbndUnd);
            result.setUnderlyingProductId(futUnd > 0 ? futUnd : fbndUnd);
        }
        else if (futUnd > 0) {
            result.setUnderlyingId(futUnd);
            result.setUnderlyingProductId(futUnd);
        }
        final Date futMaturity = rs.getDate("fut_maturity");
        if (!rs.wasNull()) {
            result.setExpirationDate(DateUtil.dateToYyyyMmDd(futMaturity));
        }
        final long contractCurrencyId = rs.getLong("future_contractcurrency");
        if (!rs.wasNull()) {
            result.setContractCurrency(this.domainContext.getCurrency(contractCurrencyId));
        }
        final double contractValue = rs.getDouble("future_contractvalue");
        if (!rs.wasNull()) {
            result.setContractValue(toBigDecimal(contractValue));
        }
        final long tickCurrencyId = rs.getLong("future_tickcurrency");
        if (!rs.wasNull()) {
            result.setTickCurrency(this.domainContext.getCurrency(tickCurrencyId));
        }
        final double tickSize = rs.getDouble("future_ticksize");
        if (!rs.wasNull()) {
            result.setTickSize(toBigDecimal(tickSize));
        }
        final double tickValue = rs.getDouble("future_tickvalue");
        if (!rs.wasNull()) {
            result.setTickValue(toBigDecimal(tickValue));
        }
        return result;
    }

    private InstrumentDp2 createFund(long instrumentid, ResultSet rs) throws SQLException {
        final FundDp2 result = new FundDp2(instrumentid);
        final Date fndMaturity = rs.getDate("fnd_maturity");
        if (!rs.wasNull()) {
            result.setExpirationDate(DateUtil.dateToYyyyMmDd(fndMaturity));
        }
        return result;
    }

    private InstrumentDp2 createCurrency(long instrumentid, ResultSet rs) throws SQLException {
        final CurrencyCrossrateDp2 result = new CurrencyCrossrateDp2(instrumentid);
        final long sourceCurrency = rs.getLong("cur_fr");
        if (!rs.wasNull()) {
            result.setSourceCurrency(this.domainContext.getCurrency(sourceCurrency));
        }
        final long targetCurrency = rs.getLong("cur_to");
        if (!rs.wasNull()) {
            result.setTargetCurrency(this.domainContext.getCurrency(targetCurrency));
        }
        final double sourceToTargetFactor = rs.getDouble("cur_fac");
        if (!rs.wasNull()) {
            result.setSourceToTargetFactor(sourceToTargetFactor);
        }
        return result;
    }

    private InstrumentDp2 createRate(long instrumentid, ResultSet rs) throws SQLException {
        final RateDp2 result = new RateDp2(instrumentid);
        final long sourceCurrency = rs.getLong("cur_fr");
        if (!rs.wasNull()) {
            result.setSourceCurrency(this.domainContext.getCurrency(sourceCurrency));
        }
        final long targetCurrency = rs.getLong("cur_to");
        if (!rs.wasNull()) {
            result.setTargetCurrency(this.domainContext.getCurrency(targetCurrency));
        }
        final double sourceToTargetFactor = rs.getDouble("cur_fac");
        if (!rs.wasNull()) {
            result.setSourceToTargetFactor(sourceToTargetFactor);
        }
        return result;
    }

    private InstrumentDp2 createCertificate(long instrumentid, ResultSet rs) throws SQLException {
        final CertificateDp2 result = new CertificateDp2(instrumentid);

        final long cerUnd = rs.getLong("cer_und");
        if (!rs.wasNull()) {
            result.setUnderlyingId(cerUnd);
        }

        final Date cerMaturity = rs.getDate("cer_maturity");
        if (!rs.wasNull()) {
            result.setExpirationDate(DateUtil.dateToYyyyMmDd(cerMaturity));
        }

        final double cerstrike = rs.getDouble("cer_strike");
        if (!rs.wasNull()) {
            result.setStrike(toBigDecimal(cerstrike));
        }
        addStrikeCurrency(result, rs);
        final String cerostype = rs.getString("cer_ostype"); // C or P
        if (!rs.wasNull()) {
            result.setType(getDerivativeType(cerostype));
        }

        result.setName(getName(rs.getString("gatrixx")));

        return result;
    }

    private InstrumentDp2 createBond(long instrumentid, ResultSet rs) throws SQLException {
        final BondDp2 result = new BondDp2(instrumentid);

        final Date bndMaturity = rs.getDate("bnd_maturity");
        if (!rs.wasNull()) {
            result.setExpirationDate(DateUtil.dateToYyyyMmDd(bndMaturity));
        }

        result.setName(rs.getString("mmnamebond"));
        return result;
    }

    private MinimumQuotationSize.Unit getQuotedPer(ResultSet rs) throws SQLException {
        final String quotedPerStr = rs.getString("quotedperunit_new");
        if ("true".equals(quotedPerStr)) {
            return MinimumQuotationSize.Unit.UNIT;
        }
        if ("false".equals(quotedPerStr)) {
            return MinimumQuotationSize.Unit.PERCENT;
        }
        return MinimumQuotationSize.Unit.NOTHING;
    }

    private void addStrikeCurrency(DerivativeWithStrikeDp2 result,
            ResultSet rs) throws SQLException {
        final long strikeCurrency = rs.getLong("strike_currencyid");
        if (!rs.wasNull()) {
            result.setStrikeCurrency(this.domainContext.getCurrency(strikeCurrency));
        }
    }

    static DerivativeTypeEnum getDerivativeType(String type) {
        if ("C".equals(type)) {
            return DerivativeTypeEnum.CALL;
        }
        if ("P".equals(type)) {
            return DerivativeTypeEnum.PUT;
        }
        return DerivativeTypeEnum.OTHER;
    }

    private void setAdditionalNames(ResultSet rs, InstrumentDp2 instrument) throws SQLException {
        addSymbol(rs, instrument, "inamekost", KeysystemEnum.PM_INSTRUMENT_NAME);
        addSymbol(rs, instrument, "inamefrei", KeysystemEnum.PM_INSTRUMENT_NAME_FREE);
    }

    private void setAliases(ResultSet rs, InstrumentDp2 instrument) throws SQLException {
        final String aliases = rs.getString("alias");
        if (StringUtils.hasText(aliases)) {
            instrument.setAliases(aliases);
        }
        // TODO: why is/was this necessary?!
        // instrument.setAliases(instrument.getName());
    }

    private void setLei(ResultSet rs, InstrumentDp2 instrument) throws SQLException {
        final String lei = rs.getString("lei");
        if (StringUtils.hasText(lei)) {
            instrument.setLei(lei);
        }
    }

    private String getName(final String input) {
        if (input == null) {
            return null;
        }
        final String name = input.replaceAll("\\(X\\)", "").trim();

        if (name.startsWith(".")) {
            return name.substring(1);
        }

        return name;
    }

    static BigDecimal toBigDecimal(double v) {
        return new BigDecimal(DF.format(v));
    }

    private void ensureName(InstrumentDp2 instrument) {
        if (StringUtils.hasText(instrument.getName())) {
            return;
        }
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<ensureName> w/o name: " + instrument.getId() + ".iid");
        }
        if (instrument.getSymbolIsin() != null) {
            instrument.setName(instrument.getSymbolIsin());
        }
        else {
            instrument.setName(ID_NAME_PREFIX + instrument.getId());
        }
    }
}
