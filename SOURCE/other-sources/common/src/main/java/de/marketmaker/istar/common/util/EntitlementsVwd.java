/*
 * EntitlementsVwd.java
 *
 * Created on 07.02.2005 16:53:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EntitlementsVwd implements Serializable {
    public final static int MAX_ENTITLEMENT_VALUE = 1560;

    public final static int MAX_NEW_ENTITLEMENT_VALUE = 1 << 16;
    protected static final long serialVersionUID = 1L;


    public static String asString(final BitSet bs) {
        final StringBuilder sb = new StringBuilder(bs.cardinality() * 3);
        sb.append("[");
        for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
            if (sb.length() > 1) {
                sb.append(",");
            }
            sb.append(toEntitlement(i));

            final int j = bs.nextClearBit(i + 1);
            if (j - i > 2) {
                sb.append("-").append(toEntitlement(j - 1));
                i = j;
            }
        }
        return sb.append("]").toString();
    }

    public static String normalize(String s) {
        return toEntitlement(toValue(s));
    }

    public static String toNumericEntitlement(int value) {
        if (value < 1 || value > MAX_NEW_ENTITLEMENT_VALUE) {
            onInvalid(Integer.toString(value));
        }
        return Integer.toString(value);
    }

    public static String toEntitlement(int value) {
        if (value > MAX_ENTITLEMENT_VALUE && value <= MAX_NEW_ENTITLEMENT_VALUE) {
            return Integer.toString(value);
        }
        if (value < 1 || value > MAX_ENTITLEMENT_VALUE) {
            onInvalid(Integer.toString(value));
        }
        final int mod = (value % 26);
        final int n = (mod == 0) ? (value / 26) : (value / 26 + 1);
        final char c = (mod == 0) ? 'Z' : (char) ('A' + mod - 1);
        return new StringBuilder(3).append(n).append(c).toString();
    }

    public static int toValue(String s) {
        if (s.length() == 2 || s.length() == 3) {
            final char c = Character.toUpperCase(s.charAt(s.length() - 1));
            if (c >= 'A' && c <= 'Z') {
                final int d0 = s.charAt(s.length() - 2) - '0';
                final int d1 = s.length() == 2 ? 0 : (s.charAt(0) - '0');

                if (d0 < 0 || d0 > 9 || d1 < 0 || d1 > 6) {
                    onInvalid(s);
                }

                int result = ((d1 * 10 + d0) - 1) * 26 + (c - 'A' + 1);
                if (result > MAX_ENTITLEMENT_VALUE) {
                    onInvalid(s);
                }
                return result;
            }
        }

        try {
            final int result = Integer.parseInt(s);
            if (result < 1 || result > MAX_NEW_ENTITLEMENT_VALUE) {
                onInvalid(s + "' =>" + result + " not in [1.." + MAX_NEW_ENTITLEMENT_VALUE + "]");
            }
            return result;
        } catch (NumberFormatException e) {
            onInvalid(s);
        }
        return 0; // cannot happen
    }

    private static void onInvalid(String s) {
        throw new IllegalArgumentException("Invalid: '" + s + "'");
    }

    private static BitSet copy(BitSet bs) {
        final BitSet result = new BitSet(bs.size());
        result.or(bs);
        return result;
    }

    private static BitSet getEODEntitlements(BitSet bs) {
        if (bs.size() <= 1041) {
            return new BitSet(0);
        }
        final BitSet result = new BitSet(521);
        for (int i = bs.nextSetBit(1041); i >= 0 && i <= 1560; i = bs.nextSetBit(i + 1)) {
            result.set(i - 1040);
        }
        result.andNot(getRealtimeEntitlements(bs)); // clear all that would be realtime as well
        result.andNot(getNeartimeEntitlements(bs)); // ... neartime ...
        return result;
    }

    private static BitSet getNeartimeEntitlements(BitSet bs) {
        if (bs.size() <= 521) {
            return new BitSet(0);
        }
        final BitSet result = new BitSet(521);
        for (int i = bs.nextSetBit(521); i >= 0 && i <= 1040; i = bs.nextSetBit(i + 1)) {
            result.set(i - 520);
        }
        result.andNot(getRealtimeEntitlements(bs)); // clear all that would be realtime as well
        return result;
    }

    private static BitSet getRealtimeEntitlements(BitSet bs) {
        final BitSet result = new BitSet(520);
        result.or(bs);
        if (result.size() > 521) {
            result.clear(521, 1561);
        }
        return result;
    }

    private static BitSet join(BitSet[] values) {
        final BitSet result = new BitSet(1040);
        result.or(values[0]);
        for (int i = values[1].nextSetBit(0); i != -1; i = values[1].nextSetBit(i + 1)) {
            result.set(i + 520);
        }
        return result;
    }

    private final UnmodifiableBitSet entitlements;

    private BitSet[] values = new BitSet[3]; // 0=rt, 1=dl, 2=eod

    public EntitlementsVwd(BitSet bs) {
        this.values = new BitSet[]{
                getRealtimeEntitlements(bs),
                getNeartimeEntitlements(bs),
                getEODEntitlements(bs)
        };
        this.entitlements = new UnmodifiableBitSet(bs);
    }

    public EntitlementsVwd(BitSet[] values) {
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] = i < values.length ? copy(values[i]) : new BitSet(0);
        }
        this.entitlements = new UnmodifiableBitSet(join(values));
    }

    public BitSet getEODEntitlements() {
        return copy(this.values[2]);
    }

    /**
     * @return unified entitlements
     * @deprecated
     */
    public BitSet getEntitlements() {
        return this.entitlements;
    }

    public BitSet getNeartimeEntitlements() {
        return copy(this.values[1]);
    }

    public BitSet getRealtimeEntitlements() {
        return copy(this.values[0]);
    }

    public String toString() {
        return new StringBuilder(4096)
                .append("EntitlementsVwd[RT=").append(asString(this.values[0]))
                .append(", NT=").append(asString(this.values[1]))
                .append(", EOD=").append(asString(this.values[2]))
                .append("]").toString();
    }

    protected Object readResolve() {
        if (this.values == null || this.values[0] == null) {
            this.values = new BitSet[]{
                    getRealtimeEntitlements(this.entitlements),
                    getNeartimeEntitlements(this.entitlements),
                    new BitSet(0)
            };
        }
        return this;
    }

    public static String toNumericSelector(String s) {
        for (int i = s.length(); i-- > 0; ) {
            if (!Character.isDigit(s.charAt(i))) {
                return toNumericEntitlement(toValue(s));
            }
        }
        return s;
    }

    public static void main(String[] args) {
        // FTSE Italia Index Values
        System.out.println(EntitlementsVwd.toNumericSelector("18V"));
    }
}
