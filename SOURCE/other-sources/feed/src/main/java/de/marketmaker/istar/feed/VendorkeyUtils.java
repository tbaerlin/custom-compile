/*
 * VendorkeyUtils.java
 *
 * Created on 14.03.2007 09:19:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VendorkeyUtils {
    private static final Pattern START_OF_KEY_PATTERN
            = Pattern.compile("^([1-9][0-9]?\\.)?[^.]+\\.([^.]+)");

    private static final Set<ByteString> OTC_MARKETS = new HashSet<>();

    static {
        for (String market : new String[]{
                "ABN", "BGB", "BHF", "BLB", "CENTRO", "CITIF", "COBAF", "CSFB", "DBKF", "DEKA", "DREBA",
                "DZF", "ERSTE", "GMS", "HVB", "LASW", "LUS", "SALOF", "SCGP", "TUBD", "UBS", "WESTLB", "VONT"
        }) {
            OTC_MARKETS.add(new ByteString(market));
        }
    }

    private static final byte[] OTC_SUFFIX = new ByteString(".1.1A").getBytes();

    public static boolean isOTCKey(Vendorkey vkey) {
        return vkey.getType() == 8
                && OTC_MARKETS.contains(vkey.getMarketName());
    }

    /**
     * Converts a new key (8.wkn.market) into an old one (2.wkn.market.1.1A)
     * @param vkey new otc key
     * @return old otc key
     */
    public static ByteString getOldOTCKey(Vendorkey vkey) {
        return vkey.toVwdcode().prepend(new ByteString("2.")).append(OTC_SUFFIX);
    }

    public static String getMarketName(String vendorkey) {
        final Matcher m = START_OF_KEY_PATTERN.matcher(vendorkey);
        return (m.find()) ? m.group(2) : null;
    }

    public static boolean isWithType(String vendorkey) {
        final Matcher m = START_OF_KEY_PATTERN.matcher(vendorkey);
        return (m.find()) && m.group(1) != null;
    }

    public static Boolean getOptionType(VendorkeyVwd vkey) {
        final ByteString maturity = vkey.getMaturity();
        if (maturity == null || maturity.length() != 2) {
            return null;
        }
        return maturity.toString().charAt(1) <= ('A' + 12);
    }

    /**
     * Returns the maturity date for an option as yyyymmdd. the vendorkey is supposed to look like
     * <em>code.market.strike.maturity</tm>, if no maturity is present, 0 is returned, if
     * maturity does not match [0-9][A-Y](_[0-9]+)?, -1 is returned.
     * @param vkey
     * @return
     */
    public static int getOptionMaturity(VendorkeyVwd vkey) {
        return getOptionMaturity(vkey.getMaturity());
    }

    private static int getOptionMaturity(ByteString maturity) {
        if (maturity == null) {
            return 0;
        }
        if (maturity.length() < 2) {
            return -1;
        }
        if (maturity.length() > 2 && !isWithGenerationSuffix(maturity)) {
            return -1;
        }
        final int yearPart = maturity.byteAt(0) - '0';
        if (yearPart < 0 || yearPart > 9) {
            return -1;
        }
        final int m = maturity.byteAt(1) - 'A' + 1;
        if (m < 1 || m > 24) {
            return -1;
        }

        final LocalDate ld = new LocalDate();
        final int currentYear = ld.getYear();

        final int yearInDecade = currentYear % 10;

        int matYear = currentYear - yearInDecade + yearPart;
        if (yearPart - yearInDecade >= 6) {
            matYear -= 10;
        }
        else if (yearInDecade - yearPart > 4) {
            matYear += 10;
        }

        return matYear * 10000 + (m <= 12 ? m : m - 12) * 100 + 1;
    }

    private static boolean isWithGenerationSuffix(ByteString maturity) {
        if (maturity.length() < 4 || maturity.byteAt(2) != '_') {
            return false;
        }
        for (int i = 3; i < maturity.length(); i++) {
            byte b = maturity.byteAt(i);
            if (b < '0' || b > '9') {
                return false;
            }
        }
        return true;
    }
}
