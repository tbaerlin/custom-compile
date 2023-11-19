/*
 * OpraSymbolProviderImpl.java
 *
 * Created on 26.09.2008 07:36:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.opra;

import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.ratios.frontend.EnumFlyweightFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.opra.OpraWriter;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.MatrixMetadataRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchMetaResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import static de.marketmaker.istar.ratios.RatioFieldDescription.*;
import static de.marketmaker.istar.ratios.frontend.EnumFlyweightFactory.intern;

/**
 * Opra symbol file is parsed and indexed.
 *
 * Symbol elements (sample: A.XCBO.10000.8C):
 *
 * <ol>
 *     <li>underlyingWkn</li>
 *     <li>vwdMarket</li>
 *     <li>strike</li>
 *     <li>version (optional, if absent 0 is assumed)</li>
 *     <li>maturity</li>
 * </ol>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class OpraSymbolProviderImpl implements InitializingBean, OpraSearchEngine {

    public static final Map<RatioDataRecord.Field, RatioFieldDescription.Field> FIELDS = new HashMap<RatioDataRecord.Field, Field>() {
        {
            put(RatioDataRecord.Field.underlyingSymbol, RatioFieldDescription.underlyingWkn);
            put(RatioDataRecord.Field.market, RatioFieldDescription.vwdMarket);
            put(RatioDataRecord.Field.symbol, RatioFieldDescription.wkn);
            put(RatioDataRecord.Field.expirationDate, RatioFieldDescription.expires);
            put(RatioDataRecord.Field.version, RatioFieldDescription.currencyStrike);
            put(RatioDataRecord.Field.strike, RatioFieldDescription.strikePrice);
            put(RatioDataRecord.Field.optionType, RatioFieldDescription.osType);
            put(RatioDataRecord.Field.vwdCode, RatioFieldDescription.vwdCode);
            put(RatioDataRecord.Field.isFlex, RatioFieldDescription.isFlex);
            put(RatioDataRecord.Field.vwdsymbol, RatioFieldDescription.vwdCode);
            put(RatioDataRecord.Field.optionCategory, RatioFieldDescription.optionCategory);
            put(RatioDataRecord.Field.exerciseType, RatioFieldDescription.exerciseType);
            put(RatioDataRecord.Field.contractSize, RatioFieldDescription.contractSize);
        }
    };

    /**
     * Since non standard equity options (weeklies) at the US Equity Option Exchanges
     * will no longer be filtered, the option category has to be set for differentiation.
     *
     * <p>A weekly can be differentiated by symbol from default options.</p>
     *
     * <p>Weeklies will carry an additional two digit number representing the expiration day as a suffix.
     * An underscore delimiter is set between the options root code and the day.</p>
     */
    protected static final Pattern WEEKLIES = Pattern.compile("^[A-Z]+_[0-9]{2}\\.[\\S]*$");

    private static final long DEFAULT_CONTACT_SIZE = PriceCoder.encode("100");

    private static final String WEEKLY_OPTION_CATEGORY = intern(RatioFieldDescription.optionCategory.id(), "weekly");

    private static final String DEFAULT_OPTION_CATEGORY = intern(RatioFieldDescription.optionCategory.id(), "default");

    private static final Field[] META_FIELDS = new Field[]{
            underlyingWkn,
            wkn,
            osType,
            vwdMarket,
            optionCategory,
    };

    // see de.marketmaker.istar.merger.web.easytrade.block.OpraFinder.FIELDS:
    private static final String VERSION = intern(RatioFieldDescription.currencyStrike.id(), "0");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private File file;

    private int line;

    private AtomicReference<List<OpraItem>> itemsRef = new AtomicReference<>(null);

    private AtomicReference<Map<Integer, Map<String, Integer>>> metaDataRef = new AtomicReference<>(null);

    /**
     * supports parallel search
     */
    private ForkJoinPool pool;

    private static final Pattern DOT_PATTERN = Pattern.compile(Pattern.quote("."));

    public ForkJoinPool getPool() {
        return pool;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setPool(ForkJoinPool pool) {
        this.pool = pool;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.pool == null) {
            this.pool = new ForkJoinPool();
            this.logger.info("<initialize> #created pool, #search threads = " + this.pool.getParallelism());
        }
        final FileResource resource = new FileResource(this.file);
        resource.addPropertyChangeListener(evt -> readItems());
        this.activeMonitor.addResource(resource);

        readItems();
        if (this.itemsRef.get() == null) {
            throw new IllegalStateException("no data");
        }
    }

    private void readItems() {
        final TimeTaker tt = new TimeTaker();
        try {
            final List<OpraItem> items = doReadItems();
            final Map<Integer, Map<String, Integer>> data = getMetaData(items);

            this.metaDataRef.set(data);
            this.itemsRef.set(items);

            this.logger.info("<readItems> read repository, #" + items.size() + ", took " + tt);
        } catch (Exception e) {
            this.logger.error("<readItems> failed in line " + this.line, e);
        }
    }

    private InputStream createInputStream(File inputFile) throws IOException {
        if (!inputFile.exists()) {
            String message = "no file " + inputFile;
            this.logger.error("<readItems> {}", message);
            throw new IllegalArgumentException(message);
        }

        InputStream fileStream = new FileInputStream(inputFile);
        return inputFile.getName().endsWith(".gz") ? new GZIPInputStream(fileStream) : fileStream;
    }

    private List<OpraItem> doReadItems() throws Exception {
        TimeTaker tt = new TimeTaker();

        this.line = 0;
        try (Scanner scanner = new Scanner(createInputStream(this.file), StandardCharsets.UTF_8.name())) {
            final List<OpraItem> result = new ArrayList<>(1 << 22);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (0 == this.line++ && line.startsWith(OpraWriter.HEADER)) {
                    continue;
                }
                if (this.line % 1_000_000 == 0) {
                    this.logger.info("<doReadItems> reading line " + this.line);
                }
                try {
                    OpraItem item = createItem(line);
                    if (item != null) {
                        result.add(item);
                    }
                } catch (Exception e) {
                    this.logger.warn("<doReadItems> failed for line '" + line + "', " + this.line, e);
                    return result;
                }
            }
            this.logger.info("<doReadItems> " + result.size() + ", took " + tt);
            return result;
        }
    }

    private static boolean isValidMaturity(String maturity) {
        if (maturity.length() != 2) {
            return false;
        }
        char y = maturity.charAt(0);
        if (y < '0' || y > '9') {
            return false;
        }
        char m = maturity.charAt(1);
        return m >= 'A' || m <= 'X';
    }

    private OpraItem createItem(String line) {
        final String[] tokens = line.split(";");
        final String[] vwdcode = DOT_PATTERN.split(tokens[0]);
        if (vwdcode.length < 4 || !isValidMaturity(vwdcode[vwdcode.length - 1])) {
            return null;
        }

        final String maturity = vwdcode[vwdcode.length - 1].intern();

        int expirationDate = getDate(tokens[1]);

        final String vwdCodePrefixPart = vwdcode[0];
        final String underlyingSymbol = intern(RatioFieldDescription.underlyingWkn.id(), vwdCodePrefixPart);
        final String market = intern(RatioFieldDescription.vwdMarket.id(), vwdcode[1]);

        final String version = (vwdcode.length == 4)
                ? VERSION : intern(RatioFieldDescription.currencyStrike.id(), vwdcode[3]);

        final int strike = Integer.parseInt(vwdcode[2]);

        final OpraItem result = new OpraItem(underlyingSymbol, market, expirationDate,
                version, strike, maturity);

        if (tokens.length > 2 && !tokens[2].isEmpty() && !tokens[3].isEmpty()) {
            result.setSettlement(getPrice(tokens[2]), getDate(tokens[3]));
        }

        if (tokens.length > 4 && !tokens[4].isEmpty()) {
            result.setContractSize(getPrice(tokens[4]));
        } else {
            result.setContractSize(DEFAULT_CONTACT_SIZE);
        }

        setOptionCategory(result, tokens[0]);

        return result;
    }

    /**
     * Initializes {@link RatioFieldDescription#optionCategory} enum in {@link EnumFlyweightFactory#MAPS}
     * since it is not part of the input file.
     *
     * @see #WEEKLIES
     */
    private void setOptionCategory(OpraItem item, String vwdCode) {
        if (WEEKLIES.matcher(vwdCode).matches()) {
            item.setOptionCategory(WEEKLY_OPTION_CATEGORY);
        } else {
            item.setOptionCategory(DEFAULT_OPTION_CATEGORY);
        }
    }

    private Map<Integer, Map<String, Integer>> getMetaData(List<OpraItem> items) {
        final Map<Integer, Map<String, Integer>> result = new HashMap<>();
        for (RatioFieldDescription.Field f : META_FIELDS) {
            result.put(f.id(), getMetaData(items, f));
        }
        return result;
    }

    private Map<String, Integer> getMetaData(List<OpraItem> items,
            RatioFieldDescription.Field f) {
        final Object2IntMap<String> map = new Object2IntOpenHashMap<>();
        map.defaultReturnValue(0);
        for (OpraItem item : items) {
            add(map, item.getString(f.id()));
        }
        return new TreeMap<>(map);
    }

    private void add(Object2IntMap<String> map, String value) {
        map.put(value, 1 + map.getInt(value));
    }

    private long getPrice(String token) {
        return PriceCoder.encode(token);
    }

    private int getDate(String token) {
        if (StringUtils.isEmpty(token) || "0".equals(token)) {
            return 0;
        }
        return Integer.parseInt(token);
    }

    @Override
    public OpraRatioSearchResponse getOpraItems(RatioSearchRequest request) {
        try {
            return doGetOpraItems(request);
        } catch (Exception e) {
            this.logger.error("<getOpraItems> failed", e);
            final OpraRatioSearchResponse response = new OpraRatioSearchResponse();
            response.setInvalid();
            return response;
        }
    }

    @Override
    public RatioSearchMetaResponse getOpraMetaData() {
        try {
            return doGetOpraMetaData();
        } catch (Exception e) {
            this.logger.error("<getOpraItems> failed", e);
            final RatioSearchMetaResponse response = new RatioSearchMetaResponse();
            response.setInvalid();
            return response;
        }
    }

    private OpraRatioSearchResponse doGetOpraItems(RatioSearchRequest request) throws Exception {
        List<OpraItem> items = this.itemsRef.get();
        if (items == null) {
            this.logger.warn("<doGetOpraItems> no items!");
            OpraRatioSearchResponse result = new OpraRatioSearchResponse();
            result.setInvalid();
            return result;
        }
        return new OpraSearchMethod(this.pool, items, request).invoke();
    }

    private RatioSearchMetaResponse doGetOpraMetaData() {
        final RatioSearchMetaResponse result = new RatioSearchMetaResponse();
        Map<Integer, Map<String, Integer>> data = this.metaDataRef.get();
        if (data == null) {
            this.logger.warn("<doGetOpraMetaData> no data!");
            result.setInvalid();
        }
        else {
            result.setEnumFields(data);
        }
        return result;
    }

    @Override
    public MatrixMetadataRatioSearchResponse getOpraMatrix(RatioSearchRequest request) {
        try {
            return doGetOpraMatrix(request);
        } catch (Exception e) {
            this.logger.error("<getOpraMatrix> failed", e);
            final MatrixMetadataRatioSearchResponse response = new MatrixMetadataRatioSearchResponse();
            response.setInvalid();
            return response;
        }
    }

    private MatrixMetadataRatioSearchResponse doGetOpraMatrix(RatioSearchRequest request) throws Exception {
        List<OpraItem> items = this.itemsRef.get();
        if (items == null) {
            this.logger.warn("<doGetOpraMatrix> no items!");
            MatrixMetadataRatioSearchResponse result = new MatrixMetadataRatioSearchResponse();
            result.setInvalid();
            return result;
        }
        return new OpraMatrixMetadataMethod(this.pool, items, request).invoke();
    }
}
