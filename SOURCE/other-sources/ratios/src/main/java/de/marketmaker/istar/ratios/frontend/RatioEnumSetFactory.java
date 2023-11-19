package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.common.util.concurrent.InMemoryCache;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.RatioFieldDescription.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zzhao
 */
public final class RatioEnumSetFactory {

    static final int SIZE_IN_LONGS;
    static final int SIZE_IN_BYTES;
    static final int SIZE_IN_BITS;

    public static final String SEPARATOR = ",";

    private static final Logger log = LoggerFactory.getLogger(RatioEnumSetFactory.class);

    private static final Map<Integer, Map<String, Integer>> MAP_POS;

    private static final Map<Integer, Map<Integer, String>> MAP_NAM;

    private static final Map<Integer, InMemoryCache<BitSet, String>> CACHE_BITS;

    private static final Map<Integer, InMemoryCache<String, BitSet>> CACHE_NAME;

    static {
        // NOTE: Add newly introduced country codes here in the relevant field.
        // The size of the BitSet will be calculated dynamically as a multiple of Long size.
        // For example:
        // - 50 country codes => 64 bits (1 long)
        // - 65 country codes => 128 bits (2 longs)
        // - 127 country codes => 128 bits (2 longs)
        // - 129 country codes => 192 bits (3 longs)
        final String[] msMarketAdmissin = new String[] {
            "AT", "CH", "DE", "IT" // 4
        };
        final String[] ssatMarketAdmission = new String[] {
            "AT", "BE", "CL", "CZ", "DE", "DK", "ES", "FI", "FR", "GB", // 10
            "GR", "HK", "HU", "IE", "IT", "LI", "LU", "NL", "NO", "PL",
            "PT", "SE", "SG", "SI", "SK" // 5
        };
        final String[] marketAdmission = new String[] {
            "AT", "BE", "CH", "CL", "CY", "CZ", "DE", "DK", "EE", "ES", // 10
            "FI", "FO", "FR", "GB", "GG", "GI", "GL", "GR", "HK", "HU",
            "IE", "IS", "IT", "JE", "LI", "LT", "LU", "LV", "MT", "NL",
            "NO", "PL", "PT", "SE", "SG", "SI", "SK", "AU", "JP", "MC",
            "IM", "RO", "BH", "AE", "BG", "GE", "IR", "KR", "KY", "LB",
            "MO", "PE", "TW", "BN", "CN", "DO", "KP", "MA", "MK", "MQ",
            "SA", "ZA", "CA", "SM", "RU", "BM", "HR", "IL", "MX", "SV",
            "AD", "AN", "BD", "BR", "BS", "BV", "BW", "CG", "CI", "CO",
            "CW", "EG", "EU", "GH", "ID", "IN", "JO", "KE", "KW", "MF",
            "MG", "MR", "MU", "MY", "NE", "NG", "NI", "NZ", "OM", "PH",
            "PK", "QE", "RE", "SL", "SZ", "TH", "TR", "TT", "UA", "US",
            "VG", "VN", "QA" // 3
        };
        final String[] vwdMarketAdmission = new String[] {
            "AT", "AU", "BE", "CH", "CL", "CY", "CZ", "DE", "DK", "EE", // 10
            "ES", "FI", "FO", "FR", "GB", "GG", "GI", "GL", "GR", "HK",
            "HU", "IE", "IM", "IS", "IT", "JE", "JP", "LI", "LT", "LU",
            "LV", "MC", "MT", "NL", "NO", "PL", "PT", "RO", "SE", "SG",
            "SI", "SK", "BH", "AE", "BG", "GE", "IR", "KR", "KY", "LB",
            "MO", "PE", "TW", "BN", "CN", "DO", "KP", "MA", "MK", "MQ",
            "SA", "ZA", "CA", "SM", "RU", "BM", "HR", "IL", "MX", "SV",
            "AD", "AN", "BD", "BR", "BS", "BV", "BW", "CG", "CI", "CO",
            "CW", "EG", "EU", "GH", "ID", "IN", "JO", "KE", "KW", "MF",
            "MG", "MR", "MU", "MY", "NE", "NG", "NI", "NZ", "OM", "PH",
            "PK", "QE", "RE", "SL", "SZ", "TH", "TR", "TT", "UA", "US",
            "VG", "VN", "QA" // 3
        };

        // finding max. size we need
        final Integer maxBits = Collections.max(Arrays.asList(
            msMarketAdmissin.length, ssatMarketAdmission.length, marketAdmission.length, vwdMarketAdmission.length
        ));
        SIZE_IN_LONGS = (int) Math.ceil(maxBits.doubleValue() / Long.SIZE);
        SIZE_IN_BYTES = SIZE_IN_LONGS * Long.SIZE / Byte.SIZE;
        SIZE_IN_BITS = SIZE_IN_LONGS * Long.SIZE;

        // write access during initialization, afterwards only read access, thread safe
        MAP_POS = new HashMap<>();
        MAP_NAM = new HashMap<>();

        map(RatioFieldDescription.msMarketAdmission.id(), msMarketAdmissin);
        map(RatioFieldDescription.ssatMarketAdmission.id(), ssatMarketAdmission);
        map(RatioFieldDescription.marketAdmission.id(), marketAdmission);
        map(RatioFieldDescription.vwdMarketAdmission.id(), vwdMarketAdmission);

        CACHE_BITS = new HashMap<>();
        createCacheBits(RatioFieldDescription.msMarketAdmission);
        createCacheBits(RatioFieldDescription.ssatMarketAdmission);
        createCacheBits(RatioFieldDescription.marketAdmission);
        createCacheBits(RatioFieldDescription.vwdMarketAdmission);

        CACHE_NAME = new HashMap<>();
        createCacheString(RatioFieldDescription.msMarketAdmission);
        createCacheString(RatioFieldDescription.ssatMarketAdmission);
        createCacheString(RatioFieldDescription.marketAdmission);
        createCacheString(RatioFieldDescription.vwdMarketAdmission);
    }

    private static void map(int fieldId, String... mss) {
        final HashMap<String, Integer> posMap = new HashMap<>();
        final HashMap<Integer, String> namMap = new HashMap<>();
        for (int i = 0; i < mss.length; i++) {
            posMap.put(mss[i], i);
            namMap.put(i, mss[i]);
        }

        MAP_POS.put(fieldId, posMap);
        MAP_NAM.put(fieldId, namMap);
    }

    private static void createCacheString(final Field field) {
        RatioEnumSetFactory.CACHE_NAME
            .put(field.id(), new InMemoryCache<>(key -> toBitsIntern(field.id(), key)));
    }

    private static void createCacheBits(final Field field) {
        RatioEnumSetFactory.CACHE_BITS
            .put(field.id(), new InMemoryCache<>(key -> fromBitsIntern(field.id(), key)));
    }

    private RatioEnumSetFactory() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static BitSet toBits(int fieldId, String str) {
        return CACHE_NAME.get(fieldId).get(str);
    }

    private static BitSet toBitsIntern(int fieldId, String str) {
        final Map<String, Integer> posMap = MAP_POS.get(fieldId);
        // using an utilized BitSet, supporting up to RatioEnumSet.SIZE_IN_BITS
        final BitSet bitSet = RatioEnumSet.zero();
        if (str != null) {
            final String[] split = str.split(SEPARATOR);
            for (String en : split) {
                if (en == null || en.trim().isEmpty()) {
                    continue;
                }
                final Integer idx = posMap.get(en.trim());
                if (idx == null) {
                    final Field field = RatioFieldDescription.getFieldById(fieldId);
                    log.error("<toBits> out of range enum {}, ratio field {}", en.trim(),
                        (field != null) ? field.name() : fieldId);
                }
                else {
                    bitSet.set(idx);
                }
            }
        }

        return RatioEnumSet.unmodifiableBitSet(bitSet);
    }

    private static List<String> toEnumList(int fieldId, BitSet bits) {
        final Map<Integer, String> namMap = MAP_NAM.get(fieldId);
        if (namMap == null) {
            return Collections.emptyList();
        }
        final ArrayList<String> result = new ArrayList<>(bits.cardinality());
        // iterate over set bits
        bits.stream().forEach(idx -> {
            if (!namMap.containsKey(idx)) {
                final Field field = RatioFieldDescription.getFieldById(fieldId);
                log.error("<append> index {} out of range, ratio field {}", idx, (field != null) ? field.name() : fieldId);
            }
            else {
                result.add(namMap.get(idx));
            }
        });

        return result;
    }

    public static String fromPosition(int fieldId, int idx) {
        return MAP_NAM.containsKey(fieldId) ? MAP_NAM.get(fieldId).get(idx) : null;
    }

    public static String fromBits(int fieldId, BitSet bits) {
        return CACHE_BITS.get(fieldId).get(bits);
    }

    private static String fromBitsIntern(int fieldId, BitSet bits) {
        final List<String> list = toEnumList(fieldId, bits);
        return list.isEmpty() ? null : String.join(SEPARATOR, list);
    }
}
