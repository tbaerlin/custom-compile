/*
 * MdpsExporter.java
 *
 * Created on 17.08.2008 11:33:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.mdpsexport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateFormatter;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DefaultDataRecordStrategy;
import de.marketmaker.istar.ratios.frontend.EnumFlyweightFactory;
import de.marketmaker.istar.ratios.frontend.FileRatioDataStore;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.MinMaxAvgVisitor;
import de.marketmaker.istar.ratios.frontend.QuoteRatios;
import de.marketmaker.istar.ratios.frontend.QuoteRatiosFND;
import de.marketmaker.istar.ratios.frontend.RatioData;
import de.marketmaker.istar.ratios.frontend.RatioEnumSetFactory;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.SearchParameterParser;

import static de.marketmaker.istar.common.util.LocalConfigProvider.getIstarSrcDir;
import static de.marketmaker.istar.common.util.LocalConfigProvider.getProductionDir;
import static de.marketmaker.istar.ratios.RatioFieldDescription.getLocaleIndex;
import static java.util.Collections.singletonList;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *         <p/>
 *         ftp-Account: ftp2.vwd.de
 *         Login: KennzMN
 *         Passwort: 6adeZu&A
 */
public class MdpsExporter implements InitializingBean {

    private static final Map<Integer, RatioFieldDescription.Field> FTREFF_FIELDS_STK = new LinkedHashMap<>();

    private static final Map<Integer, RatioFieldDescription.Field> FTREFF_FIELDS_FND = new LinkedHashMap<>();

    static {
        FTREFF_FIELDS_STK.put(0, RatioFieldDescription.factsetCurrency);
        FTREFF_FIELDS_STK.put(1, RatioFieldDescription.factsetCurrentPriceEarningRatio1Y);
        FTREFF_FIELDS_STK.put(2, RatioFieldDescription.factsetCurrentPriceEarningRatio2Y);
        FTREFF_FIELDS_STK.put(3, RatioFieldDescription.factsetCurrentPriceCashflowRatio1Y);
        FTREFF_FIELDS_STK.put(4, RatioFieldDescription.factsetCurrentPriceCashflowRatio2Y);
        FTREFF_FIELDS_STK.put(5, RatioFieldDescription.factsetFiscalYear);
        FTREFF_FIELDS_STK.put(6, RatioFieldDescription.factsetCurrentPriceSalesRatio1Y);
        FTREFF_FIELDS_STK.put(7, RatioFieldDescription.factsetCurrentPriceSalesRatio2Y);
        FTREFF_FIELDS_STK.put(8, RatioFieldDescription.factsetDividendyield1Y);
        FTREFF_FIELDS_STK.put(9, RatioFieldDescription.factsetDividendyield2Y);
        FTREFF_FIELDS_STK.put(10, RatioFieldDescription.marketCapitalization);
        FTREFF_FIELDS_STK.put(11, RatioFieldDescription.factsetEps1Y);
        FTREFF_FIELDS_STK.put(12, RatioFieldDescription.factsetEps2Y);
        FTREFF_FIELDS_STK.put(13, RatioFieldDescription.factsetCashflow1Y);
        FTREFF_FIELDS_STK.put(14, RatioFieldDescription.factsetCashflow2Y);
        FTREFF_FIELDS_STK.put(15, RatioFieldDescription.factsetSales1Y);
        FTREFF_FIELDS_STK.put(16, RatioFieldDescription.factsetSales2Y);
        FTREFF_FIELDS_STK.put(17, RatioFieldDescription.factsetDividend1Y);
        FTREFF_FIELDS_STK.put(18, RatioFieldDescription.factsetDividend2Y);
        FTREFF_FIELDS_STK.put(19, RatioFieldDescription.trCurrentPriceEarningRatio1Y);
        FTREFF_FIELDS_STK.put(20, RatioFieldDescription.trCurrentPriceEarningRatio2Y);
        FTREFF_FIELDS_STK.put(21, RatioFieldDescription.trCurrentPriceCashflowRatio1Y);
        FTREFF_FIELDS_STK.put(22, RatioFieldDescription.trCurrentPriceCashflowRatio2Y);
        FTREFF_FIELDS_STK.put(23, RatioFieldDescription.trFiscalYear);
        FTREFF_FIELDS_STK.put(24, RatioFieldDescription.trCurrentPriceSalesRatio1Y);
        FTREFF_FIELDS_STK.put(25, RatioFieldDescription.trCurrentPriceSalesRatio2Y);
        FTREFF_FIELDS_STK.put(26, RatioFieldDescription.trDividendyield1Y);
        FTREFF_FIELDS_STK.put(27, RatioFieldDescription.trDividendyield2Y);
        FTREFF_FIELDS_STK.put(28, RatioFieldDescription.trEps1Y);
        FTREFF_FIELDS_STK.put(29, RatioFieldDescription.trEps2Y);
        FTREFF_FIELDS_STK.put(30, RatioFieldDescription.trCashflow1Y);
        FTREFF_FIELDS_STK.put(31, RatioFieldDescription.trCashflow2Y);
        FTREFF_FIELDS_STK.put(32, RatioFieldDescription.trSales1Y);
        FTREFF_FIELDS_STK.put(33, RatioFieldDescription.trSales2Y);
        FTREFF_FIELDS_STK.put(34, RatioFieldDescription.trDividend1Y);
        FTREFF_FIELDS_STK.put(35, RatioFieldDescription.trDividend2Y);
        FTREFF_FIELDS_STK.put(36, RatioFieldDescription.trCurrency);

        FTREFF_FIELDS_FND.put(1, RatioFieldDescription.bviperformance1y);
        FTREFF_FIELDS_FND.put(2, RatioFieldDescription.bviperformance2_1y);
        FTREFF_FIELDS_FND.put(3, RatioFieldDescription.bviperformance3_2y);
        FTREFF_FIELDS_FND.put(4, RatioFieldDescription.bviperformance4_3y);
        FTREFF_FIELDS_FND.put(5, RatioFieldDescription.bviperformance5_4y);
        FTREFF_FIELDS_FND.put(6, RatioFieldDescription.dateoflast);
        FTREFF_FIELDS_FND.put(7, RatioFieldDescription.reference1w);
        FTREFF_FIELDS_FND.put(8, RatioFieldDescription.reference1m);
        FTREFF_FIELDS_FND.put(9, RatioFieldDescription.reference3m);
        FTREFF_FIELDS_FND.put(10, RatioFieldDescription.reference6m);
        FTREFF_FIELDS_FND.put(11, RatioFieldDescription.reference1y);
        FTREFF_FIELDS_FND.put(12, RatioFieldDescription.reference3y);
        FTREFF_FIELDS_FND.put(13, RatioFieldDescription.reference5y);
        FTREFF_FIELDS_FND.put(14, RatioFieldDescription.reference10y);
        FTREFF_FIELDS_FND.put(15, RatioFieldDescription.referenceAlltime);
        FTREFF_FIELDS_FND.put(16, RatioFieldDescription.volatilityCurrentYear);
        FTREFF_FIELDS_FND.put(17, RatioFieldDescription.volatilityAlltime);
        FTREFF_FIELDS_FND.put(18, RatioFieldDescription.betaCurrentYear);
        FTREFF_FIELDS_FND.put(19, RatioFieldDescription.betaAlltime);
        FTREFF_FIELDS_FND.put(20, RatioFieldDescription.beta1y);
        FTREFF_FIELDS_FND.put(21, RatioFieldDescription.beta3y);
        FTREFF_FIELDS_FND.put(22, RatioFieldDescription.beta5y);
        FTREFF_FIELDS_FND.put(23, RatioFieldDescription.beta10y);
        FTREFF_FIELDS_FND.put(24, RatioFieldDescription.sharperatioCurrentYear);
        FTREFF_FIELDS_FND.put(25, RatioFieldDescription.sharperatioAlltime);
        FTREFF_FIELDS_FND.put(26, RatioFieldDescription.alphaCurrentYear);
        FTREFF_FIELDS_FND.put(27, RatioFieldDescription.alphaAlltime);
        FTREFF_FIELDS_FND.put(28, RatioFieldDescription.alpha1y);
        FTREFF_FIELDS_FND.put(29, RatioFieldDescription.alpha1y);
        FTREFF_FIELDS_FND.put(30, RatioFieldDescription.alpha3y);
        FTREFF_FIELDS_FND.put(31, RatioFieldDescription.alpha5y);
        FTREFF_FIELDS_FND.put(32, RatioFieldDescription.alpha10y);
        FTREFF_FIELDS_FND.put(33, RatioFieldDescription.treynorCurrentYear);
        FTREFF_FIELDS_FND.put(34, RatioFieldDescription.treynorAlltime);
        FTREFF_FIELDS_FND.put(35, RatioFieldDescription.treynor1y);
        FTREFF_FIELDS_FND.put(36, RatioFieldDescription.treynor3y);
        FTREFF_FIELDS_FND.put(37, RatioFieldDescription.treynor5y);
        FTREFF_FIELDS_FND.put(38, RatioFieldDescription.treynor10y);
        FTREFF_FIELDS_FND.put(39, RatioFieldDescription.maximumLoss6m);
        FTREFF_FIELDS_FND.put(40, RatioFieldDescription.maximumLoss1y);
        FTREFF_FIELDS_FND.put(41, RatioFieldDescription.maximumLoss3y);
        FTREFF_FIELDS_FND.put(42, RatioFieldDescription.maximumLoss5y);
        FTREFF_FIELDS_FND.put(43, RatioFieldDescription.maximumLoss10y);
        FTREFF_FIELDS_FND.put(44, RatioFieldDescription.maximumLossAlltime);
        FTREFF_FIELDS_FND.put(45, RatioFieldDescription.sharperatio6m);
        FTREFF_FIELDS_FND.put(46, RatioFieldDescription.sharperatio10y);
        FTREFF_FIELDS_FND.put(47, RatioFieldDescription.volatility10y);

    }

    private final static Set<Integer> AVERAGE_FUND_FIELDS = new HashSet<>(Arrays.asList(
            VwdFieldDescription.ADF_Performance_Average_YTD.id(),
            VwdFieldDescription.ADF_Performance_Average_1M.id(),
            VwdFieldDescription.ADF_Performance_Average_3M.id(),
            VwdFieldDescription.ADF_Performance_Average_6M.id(),
            VwdFieldDescription.ADF_Performance_Average_1J.id(),
            VwdFieldDescription.ADF_Performance_Average_3J.id(),
            VwdFieldDescription.ADF_Performance_Average_5J.id(),
            VwdFieldDescription.ADF_Performance_Average_10J.id(),
            VwdFieldDescription.ADF_hist_Performance_Average.id(),
            VwdFieldDescription.ADF_Vola_Average_3J.id())
    );

    private final static Set<String> BLACK_MARKETS = new HashSet<>(Arrays.asList(
            "ETROTC",
            "JCF",
            "JCFON",
            "OBUS",
            "STGOTC",
            "VIEOTC",
            "WEIGHT"
    ));

    private static final DecimalFormat FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    private final static Map<Integer, Map<String, String>> MAPPINGS
            = new HashMap<>();

    static {
        final Map<String, String> bviFundtype = new HashMap<>();
        bviFundtype.put("A", "Aktienfonds");
        bviFundtype.put("C", "Lebenszyklusfonds");
        bviFundtype.put("D", "Dachfonds");
        bviFundtype.put("G", "Geldmarktfonds");
        bviFundtype.put("I", "Immobilienfonds");
        bviFundtype.put("M", "Mischfonds");
        bviFundtype.put("R", "Rentenfonds");
        bviFundtype.put("S", "Sonstige");
        bviFundtype.put("W", "Wertgesicherter Fonds");
        bviFundtype.put("X", "Alternativer Anlagefonds");
        bviFundtype.put("Y", "Hybridfonds");
        bviFundtype.put("Z", "Zielvorgabefonds");
        MAPPINGS.put(RatioFieldDescription.bviKategorieGrob.id(), bviFundtype);

        FORMAT.applyLocalizedPattern("0.#####");
    }


    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss");

    private static final DateTimeFormatter DTF_DATE = DateTimeFormat.forPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DTF_TIME = DateTimeFormat.forPattern("HH:mm:ss");

    private static final int INVESTMENT_FOCUS_LOCALE_INDEX
            = getLocaleIndex(RatioFieldDescription.msInvestmentFocus, singletonList(Locale.GERMAN));

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File configDir;

    private File dataDir;

    private File outputDir;

    private File metadataDir;

    private InstrumentTypeEnum[] types;

    private final Map<InstrumentTypeEnum, Map<Integer, RatioFieldDescription.Field>> fieldsByType
            = new HashMap<>();

    private int badQuality = 0;

    private Pattern vwdCodePattern;

    public void setConfigDir(File configDir) {
        this.configDir = configDir;
    }

    public void setDataDir(File dataDir) {
        this.dataDir = dataDir;
    }

    public void setMetadataDir(File metadataDir) {
        this.metadataDir = metadataDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public void setTypes(String... types) {
        this.types = new InstrumentTypeEnum[types.length];
        for (int i = 0; i < types.length; i++) {
            this.types[i] = InstrumentTypeEnum.valueOf(types[i]);
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (!EnumFlyweightFactory.WITH_LAZY_ENUMS) {
            throw new IllegalStateException("VM option -DwithLazyEnums=true is required but not set");
        }
        for (final InstrumentTypeEnum type : this.types) {
            final Map<Integer, RatioFieldDescription.Field> map = createMap(type);
            this.fieldsByType.put(type, map);

            this.logger.info("<afterPropertiesSet> " + type.name() + ": " + map);

            exportMetadata(type);
            export(type);
        }
        exportFiguresForWebTechnologySystems();
    }

    private Map<Integer, RatioFieldDescription.Field> createMap(InstrumentTypeEnum type)
            throws IOException {
        return Files.lines(getFieldsFile(type).toPath())
                .map(line -> line.split("\\s+"))
                .filter(tokens -> {
                    if (tokens.length != 3) {
                        this.logger.warn("<createMap> line did not match (blabla vwdFieldId ratioFieldName): " + Joiner.on('\t').join(tokens));
                        return false;
                    }
                    return true;
                })
                .collect(
                        Collectors.toMap(
                                tokens -> Integer.parseInt(tokens[1]),
                                tokens -> RatioFieldDescription.getFieldByName(tokens[2]),
                                (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                                TreeMap::new
                        )
                );
    }

    private File getFieldsFile(InstrumentTypeEnum type) {
        return new File(this.configDir, "fields-" + type.name().toLowerCase() + ".prop");
    }

    private File getOutFile(InstrumentTypeEnum type) {
        return new File(this.outputDir, "mm-mdps-ratios-"
                + type.name().toLowerCase() + "_" + DTF.print(new DateTime()) + ".txt");
    }

    private File getMetadataFile(InstrumentTypeEnum type) {
        return new File(this.metadataDir, "mm-mdps-ratios-"
                + type.name().toLowerCase() + "-metadata.txt");
    }

    /**
     * specialized export only for vwd AG Schweinfurt, Team Web Technology Systems
     * @throws Exception in case of problems generating the output file
     */
    private void exportFiguresForWebTechnologySystems() throws Exception {
        exportFigures(InstrumentTypeEnum.STK, FTREFF_FIELDS_STK);
        exportFigures(InstrumentTypeEnum.FND, FTREFF_FIELDS_FND);
    }

    private void exportFigures(InstrumentTypeEnum type,
            Map<Integer, RatioFieldDescription.Field> fields) throws Exception {
        this.logger.info("<exportFigures> start");

        String filename = "mm-ratios-for-web-technology-systems-" + type.name()
                + "_" + DTF_DATE.print(new DateTime()) + ".txt";

        File outFile = new File(this.outputDir, filename);
        try (PrintWriter pw = new PrintWriter(outFile)) {
            Set<String> exportedKeys = new HashSet<>();
            pw.println(
                    fields.entrySet()
                            .stream()
                            .map((e) -> e.getKey() + ":" + e.getValue())
                            .collect(Collectors.joining(";", "# ", ""))
            );

            restore(type, rd ->
                    Stream.of(rd.getQuoteRatios())
                            .filter(qr -> isToBeExported(qr, type))
                            .map(qr -> formatQuoteRatios(qr, fields, Collections.emptyMap(), ""))
                            .filter(s -> isToBeExported(s, exportedKeys))
                            .forEach(pw::println));
            this.logger.info("<exportFigures> wrote " + outFile.getAbsolutePath() + " with "
                + exportedKeys.size() + " lines");
        } catch (Throwable t) {
            this.logger.error("<exportFigures> failed for " + outFile.getAbsolutePath(), t);
            if (outFile.exists() && !outFile.delete()) {
                this.logger.error("<exportFigures> failed to delete " + outFile.getAbsolutePath());
            }
        }
    }

    private boolean isToBeExported(String s, Set<String> exportedKeys) {
        if (!isWithValidFigures(s)) {
            return false;
        }
        final String key = s.substring(0, s.indexOf(';'));
        if (exportedKeys.contains(key)) {
            this.logger.warn("<isToBeExported> duplicate key in " + s);
            return false;
        }
        exportedKeys.add(key);
        return true;
    }

    private boolean isWithValidFigures(String s) {
        if (s == null) {
            return false;
        }
        final int lastFieldIdx = s.lastIndexOf(";") + 1;
        return lastFieldIdx > 0 && !s.startsWith("858:", lastFieldIdx);
    }

    private void exportMetadata(InstrumentTypeEnum type) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(getMetadataFile(type)))) {
            final Map<Integer, RatioFieldDescription.Field> fields = this.fieldsByType.get(type);

            pw.println("#MDPS field id;MDPS field name;MDPS type;XFeed type");

            fields.keySet()
                    .stream()
                    .map(VwdFieldDescription::getField)
                    .filter(Objects::nonNull)
                    .forEach(field ->
                            pw.write(field.id() + ";" +
                                    field.name() + ";" +
                                    field.mdpsType().name() + ";" +
                                    field.type().name() + "\n"));
        }
    }

    private void export(InstrumentTypeEnum type) throws Exception {
        final Map<Integer, Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>>>
                averages = getAverages(type);

        final Map<Integer, RatioFieldDescription.Field> fields = this.fieldsByType.get(type);

        File outFile = getOutFile(type);
        try (PrintWriter pw = new PrintWriter(outFile)) {
            restore(type,
                    rd -> Stream.of(rd.getQuoteRatios())
                            .filter(qr -> isToBeExported(qr, type))
                            .peek(this::checkQuality)
                            .map(qr -> formatQuoteRatios(qr, fields, averages, ".M;"))
                            .filter(Objects::nonNull)
                            .forEach(pw::println)
            );
            this.logger.info("<export> wrote " + outFile.getAbsolutePath());
        } catch (Throwable t) {
            this.logger.error("<export> failed for " + outFile.getAbsolutePath(), t);
            if (outFile.exists() && !outFile.delete()) {
                this.logger.error("<export> failed to delete " + outFile.getAbsolutePath());
            }
        } finally {
            this.logger.info("<export> number of quality problems: " + this.badQuality);
        }
    }

    private void checkQuality(QuoteRatios qr) {
        final String vwdcode = qr.getString(RatioFieldDescription.vwdCode.id());

//        final long lastPrice = qr.getLong(RatioFieldDescription.lastPrice.id()) == null ? 0 : qr.getLong(RatioFieldDescription.lastPrice.id());
        final int lastDate = qr.getInt(RatioFieldDescription.lastDate.id()) == null ? 0 : qr.getInt(RatioFieldDescription.lastDate.id());

        final int closePreviousYearDate = qr.getInt(RatioFieldDescription.closePreviousYearDate.id()) == null ? 0 : qr.getInt(RatioFieldDescription.closePreviousYearDate.id());
        final int closePreviousMonthDate = qr.getInt(RatioFieldDescription.closePreviousMonthDate.id()) == null ? 0 : qr.getInt(RatioFieldDescription.closePreviousMonthDate.id());

        final int thisYear = toYear(DateUtil.dateToYyyyMmDd()); // e.g. 20110106 -> 20110000
        final int thisMonth = toMonth(DateUtil.dateToYyyyMmDd()); // e.g. 20110106 -> 20110100

        if (lastDate != 0) {
            if ((closePreviousMonthDate != 0) && !(closePreviousMonthDate == lastDate) && (closePreviousMonthDate < lastMonth(thisMonth))) {
                this.logger.warn("<checkQuality> " + vwdcode + " has closePreviousMonthDate=" + DateFormatter.formatYyyymmdd(closePreviousMonthDate) + " and lastDate=" + DateFormatter.formatYyyymmdd(lastDate));
                this.badQuality++;
            }

            if ((closePreviousYearDate != 0) && !(closePreviousYearDate == lastDate) && (closePreviousYearDate < lastYear(thisYear))) {
                this.logger.warn("<checkQuality> " + vwdcode + " has closePreviousYearDate=" + DateFormatter.formatYyyymmdd(closePreviousYearDate) + " and lastDate=" + DateFormatter.formatYyyymmdd(lastDate));
                this.badQuality++;
            }
        }
    }

    private int lastYear(int thisYear) {
        return thisYear - 10000;
    }

    private int lastMonth(int thisMonth) {
        if ((thisMonth % 10000) == 100) {
            return thisMonth - 8900; // month before Jan x is Dec (x-1)
        }
        else {
            return thisMonth - 100;
        }
    }

    private int toMonth(int i) {
        return i - (i % 100);
    }

    private int toYear(int i) {
        return i - (i % 10000);
    }

    private boolean isToBeExported(QuoteRatios qr, InstrumentTypeEnum type) {
        // skip blacklisted markets
        final String vwdcode = qr.getString(RatioFieldDescription.vwdCode.id());
        if (!StringUtils.hasText(vwdcode) || BLACK_MARKETS.contains(getMarket(vwdcode))) {
            return false;
        }

        // only export certificates of type ETC and ETN
        if (type.equals(InstrumentTypeEnum.CER)) {
            final String typeKey = qr.getString(RatioFieldDescription.typeKey.id());
            if (!("ETC".equals(typeKey) || "ETN".equals(typeKey))) {
                return false;
            }
        }

        return true;
    }

    private Map<Integer, Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>>> getAverages(
            InstrumentTypeEnum type) throws Exception {
        if (type != InstrumentTypeEnum.FND) {
            return Collections.emptyMap();
        }

        final TimeTaker tt = new TimeTaker();
        final RatioSearchRequest mmaRequest = new RatioSearchRequest(ProfileFactory.valueOf(true), null);
        mmaRequest.setType(InstrumentTypeEnum.FND);
        mmaRequest.setDataRecordStrategyClass(DefaultDataRecordStrategy.class);
        mmaRequest.setVisitorClass(MinMaxAvgVisitor.class);
        final Map<String, String> mmaParameters = new HashMap<>();
        mmaRequest.setParameters(mmaParameters);
        mmaParameters.put("vwdMarket", "+FONDS");
        mmaParameters.put(MinMaxAvgVisitor.KEY_GROUP_BY, "msInvestmentFocus");
        mmaParameters.put(MinMaxAvgVisitor.KEY_SOURCE, "bviperformancecurrentyear,bviperformance1m,bviperformance3m,bviperformance6m," +
                "bviperformance1y,bviperformance3y,bviperformance5y,bviperformance10y,bviperformanceAlltime,volatility3y");

        final SearchParameterParser spp = new SearchParameterParser(mmaRequest, null);
        final MinMaxAvgVisitor visitor = (MinMaxAvgVisitor) spp.createVisitor();

        restore(type, data -> {
            if (data.select(spp)) {
                visitor.visit(data);
            }
        });

        this.logger.info("<getAverages> took " + tt);
        return visitor.getResponse().getResult();
    }


    private String formatQuoteRatios(QuoteRatios qr,
            Map<Integer, RatioFieldDescription.Field> fields,
            Map<Integer, Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>>> averages,
            String prefix) {

        final String vwdcode = qr.getString(RatioFieldDescription.vwdCode.id());
        if (this.vwdCodePattern != null) {
            if (!this.vwdCodePattern.matcher(vwdcode).find()) {
                return null;
            }
        }
        final StringBuilder sb = new StringBuilder(100);
        if (this.vwdCodePattern != null) {
            sb.append(qr.getInstrumentRatios().getId()).append(".iid, ").append(qr.getId()).append(".qid => ");
        }

        // send mixed content (new content and deletions)
        sb.append(prefix);
        sb.append(vwdcode);

        final String market = getMarket(vwdcode);

        final long timestamp = qr.getTimestamp();
        final int d = (int) (timestamp / 100000L);
        final int t = (int) (timestamp % 100000L);
        try {
            //System.out.println(d + " " + t);
            // TODO Future-BUG: d < 20500101 is a bug after 01.01.2050! isn't this far any more!
            if (d > 0 && d < 20500101) {
                final DateTime referenceDate = DateUtil.toDateTime(d, t);
                append(sb, VwdFieldDescription.ADF_Berechnungsdatum.id(), DTF_DATE.print(referenceDate));
                append(sb, VwdFieldDescription.ADF_Berechnungszeit.id(), DTF_TIME.print(referenceDate));
            }
            else {
                // no ratio data available yet
                return null;
            }

            for (final Map.Entry<Integer, RatioFieldDescription.Field> entry : fields.entrySet()) {
                final RatioFieldDescription.Field value = entry.getValue();
                if (AVERAGE_FUND_FIELDS.contains(entry.getKey())) {
                    if ("FONDS".equals(market)) {
                        addAverages(sb, qr, entry.getKey(), value, averages);
                    }
                }
                else if (entry.getKey() == VwdFieldDescription.ADF_Bez_Benchmark.id() && isDutchFund(qr, market)) {
                    append(sb, vwdcode, qr, entry, RatioFieldDescription.vwdbenlBenchmarkName);
                }
                else {
                    append(sb, vwdcode, qr, entry, value);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            sb.append(", ").append(timestamp).append(": ").append(e.getMessage());
            this.logger.warn("<formatQuoteRatios> failed for " + sb.toString());
            return null;
        }
    }

    private boolean isDutchFund(QuoteRatios qr, String market) {
        // either market is FONDNL or market is NL for funds (means is dutch ETF)
        return "FONDNL".equals(market) || (qr instanceof QuoteRatiosFND && "NL".equals(market));
    }

    private void append(StringBuilder sb, String vwdcode, QuoteRatios qr,
            Map.Entry<Integer, RatioFieldDescription.Field> entry,
            RatioFieldDescription.Field value) {
        final String str = formatValue(qr, vwdcode, value);

        if (str != null) {
            append(sb, entry.getKey(), str);
        }
        else {
            switch (entry.getKey()) {
                case 299: // ADF_Dividende_hochgerechnet
                case 1266: // ADF_Div_Rendite_EOD
                case 769: // ADF_Bez_Benchmark
                    // always return at least 0 to overwrite old values in MDPS because of missing delete records
                    // see T-19896
                    // append(sb, entry.getKey(), "0");
                    // see R-63427
                    delete(sb, entry.getKey());
                    break;
            }
        }
    }

    private void append(StringBuilder sb, int fieldId, String value) {
        sb.append(";").append(fieldId).append(":").append(value);
    }

    private void delete(StringBuilder sb, int fieldId) {
        sb.append(";~").append(fieldId);
    }

    private String formatValue(QuoteRatios qr, String vwdcode, RatioFieldDescription.Field value) {
        switch (value.type()) {
            case DECIMAL:
                return formatDecimal(qr, value);
            case NUMBER:
                return formatNumber(qr, vwdcode, value);
            case DATE:
                return formatDate(qr, value);
            case STRING:
                return formatString(qr, value);
            case ENUMSET:
                final BitSet esVal = value.isInstrumentField()
                        ? qr.getInstrumentRatios().getBitSet(value.id())
                        : qr.getBitSet(value.id());
                return RatioEnumSetFactory.fromBits(value.id(), esVal);
            default:
                this.logger.warn("<export> unknown type: " + value.type());
                throw new RuntimeException("unknown type: " + value.type());
        }
    }

    private String formatString(QuoteRatios qr, RatioFieldDescription.Field value) {
        final String fieldStr = value.isQuoteRatioField()
                ? qr.getString(value.id())
                : qr.getInstrumentRatios().getString(value.id());
        return translate(value, fieldStr);
    }

    private String formatDate(QuoteRatios qr, RatioFieldDescription.Field value) {
        final Integer date = value.isQuoteRatioField()
                ? qr.getInt(value.id())
                : qr.getInstrumentRatios().getInt(value.id());
        if (date == null || date == Integer.MIN_VALUE) {
            return null;
        }
        return DTF_DATE.print(DateUtil.yyyymmddToDateTime(date));
    }

    private String formatNumber(QuoteRatios qr, String vwdcode, RatioFieldDescription.Field value) {
        if (value.id() >= VwdFieldDescription.ADF_Durchschnittsumsatz_1M.id()
                && value.id() <= VwdFieldDescription.ADF_Durchschnittsumsatz_10J.id()
                && InstrumentUtil.isVwdFundVwdcode(vwdcode)) {
            return null;
        }

        final Long number = value.isQuoteRatioField()
                ? qr.getLong(value.id())
                : qr.getInstrumentRatios().getLong(value.id());
        if (number == null || number == Long.MIN_VALUE) {
            return null;
        }
        return Long.toString(number);
    }

    private String formatDecimal(QuoteRatios qr, final RatioFieldDescription.Field value) {
        final Long l = value.isQuoteRatioField()
                ? qr.getLong(value.id())
                : qr.getInstrumentRatios().getLong(value.id());
        if (l == null || l == Long.MIN_VALUE) {
            return null;
        }
        final BigDecimal bd = new BigDecimal(l);
        return FORMAT.format(bd.movePointLeft(value.isPercent() ? 3 : 5));
    }

    private void restore(InstrumentTypeEnum type, Consumer<RatioData> c) throws Exception {
        final FileRatioDataStore store = new FileRatioDataStore();
        store.setBaseDir(this.dataDir);
        store.restore(type, c);
    }

    private void addAverages(StringBuilder sb, QuoteRatios qr,
            int vwdFieldid, RatioFieldDescription.Field value,
            Map<Integer, Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>>> averages) {

        final String msInvestmentFocus
                = qr.getInstrumentRatios().getString(RatioFieldDescription.msInvestmentFocus.id(), INVESTMENT_FOCUS_LOCALE_INDEX);
        if (msInvestmentFocus == null) {
            return;
        }

        final Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>> byInvestmentFocus
                = averages.get(value.id());
        if (byInvestmentFocus == null) {
            return;
        }

        final Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg> bySecondaryField
                = byInvestmentFocus.get(msInvestmentFocus);
        if (bySecondaryField == null) {
            return;
        }
        final MinMaxAvgRatioSearchResponse.MinMaxAvg avg = bySecondaryField.get("default");
        append(sb, vwdFieldid, format(avg.getAvg(), value.isPercent()));
    }

    private String format(BigDecimal bd, boolean percent) {
        return FORMAT.format(bd.movePointRight(percent ? 2 : 0));
    }

    private String translate(RatioFieldDescription.Field field, String str) {
        final Map<String, String> map = MAPPINGS.get(field.id());
        if (map == null) {
            return str;
        }

        final String result = map.get(str);
        return result == null ? str : result;
    }

    private String getMarket(String vwdcode) {
        final int marketStart = vwdcode.indexOf(".");
        final int marketEnd = vwdcode.indexOf(".", marketStart + 1);

        return vwdcode.substring(marketStart + 1, marketEnd < 0 ? vwdcode.length() : marketEnd);
    }

    public static void main(String[] args) throws Exception {
        final MdpsExporter exporter = new MdpsExporter();

        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-o".equals(args[n])) {
                exporter.setOutputDir(new File(args[++n]));
            }
            else if ("-t".equals(args[n])) {
                exporter.setTypes(args[++n].split(","));
            }
            else if ("-p".equals(args[n])) {
                exporter.vwdCodePattern = Pattern.compile(args[++n], Pattern.CASE_INSENSITIVE);
            }
            else {
                System.err.println("Usage: MdpsExport [options]");
                System.err.println("options");
                System.err.println("-o <outDir>");
                System.err.println("-t <comma-separated-instrument-types>");
                System.err.println("-p <pattern>");
                System.err.println("   only produce output for vwdcodes in which pattern is found");
                System.err.println("   for debugging, also adds iid/qid to output");
                return;
            }
            n++;
        }

        exporter.setConfigDir(new File(getIstarSrcDir(), "config/src/main/resources/istar-ratios-mdpsexport/conf/"));
        File dir = getProductionDir("var/data/");
        exporter.setDataDir(new File(dir, "ratios"));
        exporter.setMetadataDir(new File(dir, "ratios"));
        if (exporter.outputDir == null) {
            exporter.setOutputDir(new File(dir, "out"));
        }
        if (exporter.types == null) {
            exporter.setTypes("FND");
        }

        exporter.afterPropertiesSet();
    }
}
