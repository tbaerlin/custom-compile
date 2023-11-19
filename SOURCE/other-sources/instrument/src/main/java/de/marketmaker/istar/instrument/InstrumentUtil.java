/*
 * InstrumentUtil.java
 *
 * Created on 13.03.2010 16:38:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.ContentFlagsDp2;

import static de.marketmaker.istar.instrument.export.ScoachInstrumentAdaptor.*;


/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InstrumentUtil {
    private static final Set<String> FUND_MARKETS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("FONDS", "FONDNL", "FONDSC", "FONDIT", "FONITI", "SWXFO", "OPC", "CSZ", "SARA", "CRELAN")));

    private static final Set<String> OPRA_MARKETS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("XCBO", "OPRA")));

    private InstrumentUtil() {
    }

    public static boolean isOpraInstrument(Instrument instrument) {
        if (instrument.getInstrumentType() != InstrumentTypeEnum.OPT) {
            return false;
        }
        for (Quote q : instrument.getQuotes()) {
            if (isOPRAMarket(q.getSymbolVwdfeedMarket())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVwdFundFeedsymbol(String vkey) {
        if (vkey == null) {
            return false;
        }
        return vkey.startsWith("9.") && FUND_MARKETS.contains(getMarketName(vkey));
    }

    public static boolean isVwdFund(Quote quote) {
        if (quote == null) {
            return false;
        }
        return isVwdFundFeedsymbol(quote.getSymbolVwdfeed());
    }

    public static Quote getQuoteByBisKey(Instrument instrument, String biskey) {
        for (final Quote quote : instrument.getQuotes()) {
            if (biskey.equals(quote.getSymbolBisKey())) {
                return quote;
            }
        }
        if (biskey.startsWith(BIS_KEY_PREFIX_FFM)) { // ISTAR-395
            return getQuoteByBisKey(instrument, replaceBisKeyMarketPrefix(biskey, BIS_KEY_PREFIX_FFMST));
        }
        return null;
    }


    public static boolean isMSCIFeedsymbol(String vkey) {
        final String market = getMarketName(vkey);
        return "MSCI".equals(market) || "MSCIEM".equals(market);
    }

    public static boolean isLMEMarket(String market) {
        return "LME".equals(market) || "LMES".equals(market);
    }

    public static boolean isEZBMarket(String market) {
        return "EZB".equals(market);
    }

    public static boolean isOPRAMarket(String market) {
        return OPRA_MARKETS.contains(market);
    }

    public static boolean isVwdFundVwdcode(String vwdcode) {
        return vwdcode != null && FUND_MARKETS.contains(getMarketName(vwdcode, 0));
    }

    public static String getMarketName(String vendorkey) {
        return getMarketName(vendorkey, 3);
    }

    private static String getMarketName(String symbol, int initialOffset) {
        if (symbol == null) {
            return null;
        }
        final int start = symbol.indexOf('.', initialOffset) + 1;
        if (start == 0) {
            return null;
        }

        final int end = symbol.indexOf('.', start + 1);
        return symbol.substring(start, end > 0 ? end : symbol.length());
    }

    public static final long[] ZERO_LEN_LA = new long[0];

    /**
     * encode the given long array to a base 64 string,i.e.:
     * <p>
     * <pre>
     *         long val=0x00AB00CD00EF0000 => byte[] bytes={0x00,0xAB,0x00,0xCD,0x00,0xEF}
     *         and this byte array is transformed to a base 64 string.
     *     </pre>
     * </p>
     * @param la
     * @return
     */
    public static String toBase64String(long[] la) {
        if (null == la) {
            return null;
        }
        if (la.length == 0) {
            return "";
        }
        final byte[] bytes = new byte[la.length << 3];
        for (int i = 0; i < la.length; i++) {
            for (int j = 0; j < 8; j++) {
                bytes[(i << 3) + j] = ((byte) (0xFF & (la[i] >> (j << 3))));
            }
        }

        int idx = bytes.length - 1;
        while (idx > 0 && bytes[idx] == 0) {
            idx--;
        }

        return new Base64(0, null).encodeToString(Arrays.copyOf(bytes, idx + 1));
//        return new BASE64Encoder().encode(Arrays.copyOf(bytes, idx + 1));
    }

    /**
     * decodes the given base 64 string into a long array,
     * i.e. the inverse operation of {@link #toBase64String(long[])}.
     * @param b64
     * @return
     */
    public static long[] toLongArray(String b64) {
        if (!StringUtils.hasText(b64)) {
            return ZERO_LEN_LA;
        }
        final byte[] bytes = new Base64(0, null).decode(b64);
//        final byte[] bytes = new BASE64Decoder().decodeBuffer(b64);
        final int len = ((bytes.length & 7) == 0) ? (bytes.length >> 3) : (bytes.length >> 3) + 1;
        final long[] ret = new long[len];
        for (int i = 0; i < bytes.length; i++) {
            ret[i >> 3] |= (((long) (0x00FF & bytes[i])) << ((i & 7) << 3));
        }

        return ret;
    }

    public static long[] binaryString2LongArray(String bs) {
        if (!StringUtils.hasText(bs)) {
            return ZERO_LEN_LA;
        }

        final int len = ((bs.length() & 63) == 0) ? (bs.length() >> 6) : (bs.length() >> 6) + 1;
        final long[] ret = new long[len];
        for (int i = 0; i < bs.length(); i++) {
            if ('0' != bs.charAt(i)) {
                ret[i >> 6] |= (1L << (i & 63));
            }
        }

        return ret;
    }

    public static BitSet toBitSet(String bs) {
        if (!StringUtils.hasText(bs)) {
            return null;
        }
        final BitSet ret = new BitSet(bs.length());
        for (int i = 0; i < bs.length(); i++) {
            if ('0' != bs.charAt(i)) {
                ret.set(i);
            }
        }

        return ret;
    }

    public static long[] bitSet2LongArray(BitSet bs) {
        if (null == bs || bs.length() == 0) {
            return ZERO_LEN_LA;
        }

        final int len = ((bs.length() & 63) == 0) ? (bs.length() >> 6) : (bs.length() >> 6) + 1;
        final long[] ret = new long[len];
        for (int i = 0; i < bs.length(); i++) {
            if (bs.get(i)) {
                ret[i >> 6] |= (1L << (i & 63));
            }
        }

        return ret;
    }

    public static BitSet toBitSet(long[] la) {
        if (null == la || la.length == 0) {
            return null;
        }
        final BitSet ret = new BitSet(la.length << 6);
        for (int i = 0; i < la.length; i++) {
            for (int j = 0; j < 64; j++) {
                if ((la[i] & (1L << j)) != 0) {
                    ret.set((i << 6) + j);
                }
            }
        }

        return ret;
    }

    public static void main(String[] args) {
        ContentFlagsDp2 cf;

        cf = new ContentFlagsDp2(InstrumentUtil.toLongArray("CQAECw=="));
        System.out.println(cf);

        cf = new ContentFlagsDp2(InstrumentUtil.toLongArray("AQAECw=="));
        System.out.println(cf);

    }
}
