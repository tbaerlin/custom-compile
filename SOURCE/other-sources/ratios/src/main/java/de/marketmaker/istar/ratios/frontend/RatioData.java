/*
 * RatioData.java
 *
 * Created on 26.10.2005 10:01:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Future;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.MetalCodesLME;
import de.marketmaker.istar.domain.instrument.Option;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.util.NameUtil;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.RatioInstrumentFilter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatioData {
    private static final Logger logger = LoggerFactory.getLogger(RatioData.class);

    private static final Comparator<QuoteRatios> COMPARATOR_NEWEST_FIRST
            = (o1, o2) -> Long.signum(o1.getTimestamp() - o2.getTimestamp());

    private static final Comparator<QuoteRatios> COMPARATOR_GERMAN_FOCUS
            = new Comparator<QuoteRatios>() {
        @Override
        public int compare(QuoteRatios o1, QuoteRatios o2) {
            final int marketPrio1 = getPriority(o1.getSymbolVwdfeedMarket());
            final int marketPrio2 = getPriority(o2.getSymbolVwdfeedMarket());

            if (marketPrio1 < marketPrio2) {
                return 1;
            }
            if (marketPrio2 < marketPrio1) {
                return -1;
            }

            return COMPARATOR_NEWEST_FIRST.compare(o1, o2);
        }

        private int getPriority(String market) {
            if ("STG".equals(market)) {
                return 500;
            }
            if ("EUWAX".equals(market)) {
                return 100;
            }
            if ("FFMST".equals(market)) {
                return 50;
            }
            if ("ETR".equals(market)) {
                return 20;
            }
            if ("FFM".equals(market)) {
                return 10;
            }
            return 0;
        }
    };

    private static final Predicate<Quote> LME_QUOTE = quote -> quote != null
            && InstrumentUtil.isLMEMarket(quote.getSymbolVwdfeedMarket())
            && isLMEVwdsymbol(quote.getSymbolVwdsymbol());

    private static final int GARBAGE_FLAG = 0x40000000;

    private static final int INDEX_MASK = GARBAGE_FLAG - 1;

    private static final Set<String> LME_METAL_CODES = Collections.unmodifiableSet(Stream.of(MetalCodesLME.values()).map(Enum::name).collect(Collectors.toSet()));

    private InstrumentRatios instrumentRatios;

    private QuoteRatios[] quoteRatios;

    /**
     * In the context of the current search operation on RatioDatas, this field points to the
     * element in quoteRatios that was selected by a {@link DataRecordStrategy}. Avoids the
     * creation of a "result object" for each matched search result.
     */
    private QuoteRatios searchResult;

    /**
     * index in the list of RatioData elements in a TypeData object. Maintained by TypeData,
     * supports fast parallel search. Also used to store the GARBAGE_FLAG.
     */
    private int index;

    public RatioData() {
    }

    int getIndex() {
        return index & INDEX_MASK;
    }

    void setIndex(int index) {
        this.index = index | (isGarbage() ? GARBAGE_FLAG : 0);
    }

    public QuoteRatios getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(QuoteRatios searchResult) {
        this.searchResult = searchResult;
    }

    public static RatioData create(InstrumentTypeEnum type, long instrumentid) {
        return new RatioData(InstrumentRatios.create(type, instrumentid), new QuoteRatios[0]);
    }

    public RatioData(InstrumentRatios instrumentRatios, QuoteRatios[] quoteRatios) {
        this.instrumentRatios = instrumentRatios;
        this.quoteRatios = quoteRatios;
    }

    public void setInstrumentRatios(InstrumentRatios instrumentRatios) {
        this.instrumentRatios = instrumentRatios;
    }

    public void setQuoteRatios(QuoteRatios[] quoteRatios) {
        this.quoteRatios = ArraysUtil.copyOf(quoteRatios);
    }

    // #####################
    // TEST ACCESSORS
    // #####################

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "method is only test accessor")
    public QuoteRatios[] getQuoteRatios() {
        return quoteRatios;
    }

    public InstrumentRatios getInstrumentRatios() {
        return instrumentRatios;
    }

    public QuoteRatios getQuoteRatios(long qid) {
        final int i = getQuoteDataIndex(qid);
        return i < 0 ? null : this.quoteRatios[i];
    }
    // #####################

    /**
     * Updates the contents of the object with data from the given instrument.
     */
    public int[] update(Instrument instrument, RatioDataRepository.Underlying underlying) {
        markAsUpdated();
        updateInstrumentRatios(instrument, underlying);

        final List<Quote> quotes = RatioInstrumentFilter.getValidQuotes(instrument);
        final QuoteRatios[] drs = new QuoteRatios[quotes.size()];
        final int[] result = new int[quotes.size()];
        int n = 0;

        for (int i = 0; i < quotes.size(); i++) {
            final Quote quote = quotes.get(i);
            final int index = getQuoteDataIndex(quote.getId());

            if (index == -1) {
                result[n++] = (int) quote.getId();
                drs[i] = this.instrumentRatios.createQuoteRatios(quote.getId());
            }
            else {
                drs[i] = this.quoteRatios[index];
                this.quoteRatios[index] = null;
            }

            drs[i].setEntitlement(RatioEntitlementQuote.getFor(drs[i].getEntitlementQuote(), quote));
            updateQuoteData(quote, drs[i]);
        }

        if (this.quoteRatios.length != drs.length) {
            this.quoteRatios = drs;
        }
        else {
            // reuse quoteRatios to avoid garbage
            System.arraycopy(drs, 0, this.quoteRatios, 0, drs.length);
        }
        Arrays.sort(this.quoteRatios, COMPARATOR_GERMAN_FOCUS);

        return (n == 0) ? null : ((n < result.length) ? Arrays.copyOf(result, n) : result);
    }

    private void updateInstrumentRatios(Instrument instrument, RatioDataRepository.Underlying underlying) {
        setInstrumentField(RatioFieldDescription.name, instrument.getName());
        setInstrumentField(RatioFieldDescription.wkn, instrument.getSymbol(KeysystemEnum.WKN));
        setInstrumentField(RatioFieldDescription.isin, instrument.getSymbol(KeysystemEnum.ISIN));
        setInstrumentField(RatioFieldDescription.lei, instrument.getLei());

        if (underlying != null && instrument instanceof Derivative) {
            final long underlyingIid = ((Derivative) instrument).getUnderlyingId();

            this.instrumentRatios.set(RatioFieldDescription.underlyingIid.id(), underlyingIid);
            setInstrumentField(RatioFieldDescription.underlyingWkn, underlying.wkn);
            setInstrumentField(RatioFieldDescription.underlyingIsin, underlying.isin);
            setInstrumentField(RatioFieldDescription.underlyingEurexTicker, underlying.eurexTicker);
            setInstrumentField(RatioFieldDescription.underlyingName, underlying.name);

            if (instrument instanceof Future) {
                this.instrumentRatios.set(RatioFieldDescription.underlyingProductIid.id(),
                        ((Future) instrument).getUnderlyingProductId());
            }
            if (instrument instanceof Option) {
                this.instrumentRatios.set(RatioFieldDescription.underlyingProductIid.id(), underlyingIid);
            }
        }
        this.instrumentRatios.set(RatioFieldDescription.dzPib.id(),
                instrument.getQuotes().get(0).getContentFlags().isPibDz());

        if (instrument.getInstrumentType() == InstrumentTypeEnum.MER) {
            updateMerInstrumentRatios(instrument);
        }
    }

    /**
     * protected for the sake of testing
     */
    protected void updateMerInstrumentRatios(Instrument instrument) {
        final List<Quote> quotes = instrument.getQuotes();
        if (quotes != null && !quotes.isEmpty()) {
            quotes.stream().filter(LME_QUOTE).findAny().ifPresent(quote -> {
                final String metalCode = toLMEMetalCode(quote.getSymbolVwdsymbol());
                setInstrumentField(RatioFieldDescription.lmeMetalCode, metalCode);

                if (!quotes.stream().filter(LME_QUOTE).allMatch(quote1 ->
                        metalCode.equals(toLMEMetalCode(quote1.getSymbolVwdsymbol())))) {
                    final String instrumentName = instrument.getName();
                    logger.warn("<updateMerInstrumentRatios> Not all quotes of the LME " +
                            "instrument with ID " + instrument.getId() +
                            (instrumentName != null ? " and name '" + instrumentName + "'" : "") +
                            " share the same LME metal code '" + metalCode + "'. " +
                            "The metal code of the quote with vwd code '" +
                            quote.getSymbolVwdcode() + "' was used to infer the metal code of" +
                            " the instrument. This may be wrong.");
                }
            });
        }

    }

    private static boolean isLMEVwdsymbol(String vwdsymbol) {
        if (vwdsymbol == null || vwdsymbol.length() != 3) {
            return false;
        }
        switch (vwdsymbol.charAt(2)) {
            case 'D':
            case 'E':
            case 'S':
            case 'Y':
                return LME_METAL_CODES.contains(vwdsymbol.substring(0, 2));
            default:
                return false;
        }
    }

    private static String toLMEMetalCode(String vwdsymbol) {
        return vwdsymbol.substring(0, 2);
    }

    private void updateQuoteData(Quote quote, QuoteRatios qr) {
        qr.set(RatioFieldDescription.qid.id(), quote.getId());

        setField(qr, RatioFieldDescription.marketmanagerName, NameUtil.getMarketmanagerName(quote));
        setField(qr, RatioFieldDescription.vwdCode, quote.getSymbol(KeysystemEnum.VWDCODE));
        setField(qr, RatioFieldDescription.vwdsymbol, quote.getSymbol(KeysystemEnum.VWDSYMBOL));
        setField(qr, RatioFieldDescription.bisKey, quote.getSymbol(KeysystemEnum.BIS_KEY));
        setField(qr, RatioFieldDescription.vwdMarket, intern(quote.getSymbolVwdfeedMarket()));
        setField(qr, RatioFieldDescription.mmwkn, quote.getSymbol(KeysystemEnum.MMWKN));
        setField(qr, RatioFieldDescription.currency, quote.getCurrency().getSymbol(KeysystemEnum.ISO));
    }

    private String intern(String s) {
        return s != null ? s.intern() : null;
    }

    private void setInstrumentField(RatioFieldDescription.Field f, String s) {
        setField(this.instrumentRatios, f, s);
    }

    private void setField(RatioUpdatable u, RatioFieldDescription.Field f, String s) {
        if (StringUtils.hasText(s)) {
            final String str = f.isEnum() ? EnumFlyweightFactory.intern(f.id(), s) : s;
            u.set(f.id(), 0, str);
        }
        else {
            u.set(f.id(), 0, null);
        }
    }

    private int getQuoteDataIndex(long id) {
        for (int i = 0; i < this.quoteRatios.length; i++) {
            if (this.quoteRatios[i] != null && this.quoteRatios[i].getId() == id) {
                return i;
            }
        }
        return -1;
    }

    boolean removeUpdatedQids(Int2IntMap updatedQidToIid) {
        QuoteRatios[] tmp = null;
        int n = 0;
        for (int i = 0; i < this.quoteRatios.length; i++) {
            QuoteRatios qr = this.quoteRatios[i];
            int newIid = updatedQidToIid.get((int) qr.getId());
            if (newIid > 0 && newIid != (int) this.instrumentRatios.getId()) {
                if (tmp == null) {
                    tmp = Arrays.copyOf(this.quoteRatios, this.quoteRatios.length);
                    n = i;
                }
                logger.info("<removeUpdatedQids> removed " + qr.getId() + ".qid from "
                        + this.instrumentRatios.getId() + ".iid as it belongs to " + newIid + ".iid now");
            }
            else if (tmp != null) {
                tmp[n++] = qr;
            }
        }
        if (tmp != null) {
            if (n == 0) {
                return true;
            }
            this.quoteRatios = Arrays.copyOf(tmp, n);
        }
        return false;
    }

    public void update(ByteBuffer data) {
        update(this.instrumentRatios, data);
    }

    public void update(long quoteid, ByteBuffer data) {
        final int idx = getQuoteDataIndex(quoteid);
        if (idx == -1) {
            return;
        }

        final QuoteRatios qr = this.quoteRatios[idx];
        update(qr, data);
        Arrays.sort(this.quoteRatios, COMPARATOR_GERMAN_FOCUS);
    }

    @SuppressWarnings("unchecked")
    private void update(PropertySupported data, ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            final int fieldid = buffer.getShort();

            try {
                // ignore underlying fields for STK
                final boolean ignore = (data instanceof QuoteRatiosSTK && fieldid >= 46 && fieldid <= 59);

                final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(fieldid);
                if (field == null) {
                    logger.warn("<update> failed, no field available for fieldid " + fieldid);
                    continue;
                }

                if (field == RatioFieldDescription.mostRecentUpdateTimestamp) {
                    data.setTimestamp(buffer.getLong());
                    continue;
                }

                switch (field.type()) {
                    case BOOLEAN:
                        if (!ignore) {
                            data.propertySupport(fieldid).set(data, buffer.get() > 0);
                        }
                        break;
                    case DATE:
                    case TIME:
                        final int intvalue = buffer.getInt();
                        if (!ignore) {
                            if (intvalue == Integer.MIN_VALUE || intvalue == Integer.MAX_VALUE) {
                                data.propertySupport(fieldid).set(data, Integer.MIN_VALUE);
                            }
                            else {
                                data.propertySupport(fieldid).set(data, intvalue);
                            }
                        }
                        break;
                    case ENUMSET:
                        final BitSet esVal = RatioEnumSet.read(buffer);
                        if (!ignore) {
                            data.propertySupport(fieldid).set(data, esVal);
                        }
                        break;
                    case DECIMAL:
                    case NUMBER:
                    case TIMESTAMP:
                        final long longvalue = buffer.getLong();
                        if (!ignore) {
                            if (longvalue == Long.MIN_VALUE || longvalue == Long.MAX_VALUE) {
                                data.propertySupport(fieldid).set(data, Long.MIN_VALUE);
                            }
                            else {
                                data.propertySupport(fieldid).set(data, longvalue);
                            }
                        }
                        break;
                    case STRING:
                        final int localeIndex = field.isLocalized() ? buffer.get() : -1;

                        final int length = Math.max(0, buffer.getShort());
                        if (length == 0) {
                            if (!ignore) {
                                data.propertySupport(fieldid).set(data, localeIndex, null);
                            }
                        }
                        else {
                            final String str = ByteString.readWithLengthFrom(buffer, length).toString();
                            if (!ignore) {
                                final String resultStr = field.isEnum()
                                        ? EnumFlyweightFactory.intern(fieldid, str) : str;

                                data.propertySupport(fieldid).set(data, localeIndex, resultStr);
                            }
                        }
                        break;
                }
            } catch (Exception ex) {
                logger.warn("<update> failed for fieldid " + fieldid + " on data: " + data, ex);
                throw new RuntimeException(ex);
            }
        }
    }

    RatioDataResult createResult() {
        return new RatioDataResult(this.searchResult.getId(), getAllowedRecords());
    }

    public boolean select(SearchParameterParser spp) {
        this.searchResult = null;

        if (spp.isInstrumentRatioOnly() && !spp.getSelector().select(this.instrumentRatios)) {
            return false;
        }

        if (markAllowedRecords(spp) == 0) {
            return false;
        }

        if (!spp.isInstrumentRatioOnly()) {
            if (markSelectedRecords(spp) == 0) {
                return false;
            }
        }

        this.searchResult = spp.getDataRecordStrategy().select(this.quoteRatios);
        return searchResult != null;
    }

    private int markSelectedRecords(SearchParameterParser spp) {
        int result = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < this.quoteRatios.length; i++) {
            final QuoteRatios qr = this.quoteRatios[i];
            if (qr.isAllowed() && markSelected(spp, qr)) {
                result++;
            }
        }
        return result;
    }

    private boolean markSelected(SearchParameterParser spp, QuoteRatios qr) {
        boolean selected = spp.getSelector().select(qr);
        qr.setSelected(selected);
        return selected;
    }

    private int markAllowedRecords(SearchParameterParser spp) {
        final Profile profile = spp.getProfile();

        if (profile == ProfileFactory.valueOf(true)) {
            for (QuoteRatios quoteRatio : quoteRatios) {
                quoteRatio.setAllowed(true);
            }
            return this.quoteRatios.length;
        }

        final int searchId = spp.getSearchId();
        int n = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < this.quoteRatios.length; i++) {
            if (markAllowed(profile, searchId, this.quoteRatios[i])) {
                n++;
            }
        }
        return n;
    }

    private boolean markAllowed(Profile profile, int searchId, QuoteRatios qr) {
        final RatioEntitlementQuote eq = qr.getEntitlementQuote();
        final boolean allowed;
        if (eq.allowed.getStamp() == searchId) {
            allowed = eq.allowed.getReference();
        }
        else {
            allowed = profile.getPriceQuality(eq) != PriceQuality.NONE;
            eq.allowed.set(allowed, searchId);
        }
        qr.setAllowed(allowed);
        return allowed;
    }

    private QuoteRatios[] getAllowedRecords() {
        final QuoteRatios[] result = new QuoteRatios[this.quoteRatios.length];
        int n = 0;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < quoteRatios.length; i++) {
            QuoteRatios dr = quoteRatios[i];
            if (dr.isAllowed()) {
                result[n++] = dr;
            }
        }

        if (n == this.quoteRatios.length) {
            return result;
        }
        if (n == 0) {
            return null;
        }
        return Arrays.copyOf(result, n);
    }

    private void markAsUpdated() {
        this.index &= INDEX_MASK; // remove GARBAGE_FLAG
    }

    void markAsGarbage() {
        this.index |= GARBAGE_FLAG;
    }

    boolean isGarbage() {
        return (this.index & GARBAGE_FLAG) != 0;
    }
}
