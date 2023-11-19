/*
 * VendorkeyVwd.java
 *
 * Created on 08.08.2002 09:39:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.util.regex.Pattern;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.Vendorkey;
import de.marketmaker.istar.feed.mdps.MdpsKeyConverter;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;

import static de.marketmaker.istar.feed.mdps.MdpsTypeMappings.*;

/**
 * A vwd-specific vendorkey.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class VendorkeyVwd implements Vendorkey {
    /**
     * Matches a vwd key and captures the following groups:<ol>
     * <li>vwd key type prefix, ending with dot (optional) (e.g., '<b>6.</b>')</li>
     * <li>vwd symbol (e.g., '<b>846900</b>')</li>
     * <li>vwd market, starting with dot (e.g., '<b>.ETR</b>')</li>
     * <li>vwdcode suffix, starting with dot (optional) (e.g., '<b>.110.3U_1</b>')</li>
     * <li>mdps key suffix, starting with comma (optional) (e.g., '<b>,I</b>')</li>
     * </ol>
     * May match a key with both vwd type prefix and mdps type suffix, which cannot occur
     * in real life.
     */
    public static final Pattern KEY_PATTERN =
            Pattern.compile(""
                    // optional key type / sectyp prefix chicago style ('1.'-'12.') but also to '19.' --> group1
                    + "(1?\\d\\.)?"
                    // any character besides a dot (e.g. 710000) but also '§%&§!' or umlauts  --> group2
                    + "([^\\.]+)"
                    // the market part (.ETR or .BXE4 or .A_A) but also HAHAHAHAHAHA, note the
                    // noncapturing optional group for a single digit at the end of the market   --> group3
                    + "(\\.[A-Z_]+(?:[0-9])?)"
                    // optional part of the suffix, anything besides comma, starting with a dot  --> group4
                    + "(\\.[^,]+)?"
                    // sectype mdps prefix max 2 chars usually (',F' or ',E')             --> group5
                    + "(,[A-Z]{1,2})?");

    /**
     * Dummy vendorkey that will be returned in the case that {@link #getInstance}
     * is called with an illegal argument. Objects interested in testing for errors
     * can compare the returned object with this one.
     */
    public static final VendorkeyVwd ERROR = getInstance(new ByteString("0.ERROR.VWD"));

    private static final byte[] NINETYNINE = new byte[]{'9', '9', '.'};

    private static final byte[] ZERO = new byte[]{'0' , '.'};

    /**
     * the key without the secType
     */
    private final ByteString vwdcode;

    /**
     * the security type
     * encodes mdps type (upper byte) and xfeed type (lower byte)
     */
    private final short typeMapping;

    /**
     * market name; will be needed in most cases, so compute once and keep in this field
     */
    private final ByteString marketName;

    public static VendorkeyVwd getInstance(ByteString vwdcode, int typeMapping) {
        if (typeMapping < 256) {
            // incomplete mapping, add mdps type mapping as well
            return new VendorkeyVwd(vwdcode, MdpsTypeMappings.getMappingForVwdType(typeMapping));
        }
        return new VendorkeyVwd(vwdcode, typeMapping);
    }

    /**
     * Returns a new vendorkey
     * @param vkey textual vendorkey respresentation with xfeed type
     * @return new vendorkey or {@link #ERROR} if vkey is invalid.
     */
    public static VendorkeyVwd getInstance(ByteString vkey) {
        if (!isValidLength(vkey) || !isWithXfeedType(vkey)) {
            return ERROR;
        }
        return getInstanceUnchecked(vkey);
    }

    private static int getVendorkeyMapping(ByteString vkey) {
        return MdpsTypeMappings.getMappingForVwdKey(vkey);
    }

    private static int getVendorkeyMapping(String vkey) {
        if (vkey.charAt(1) == '.') {
            return getMappingForVwdType(0, vkey.charAt(0));
        }
        else if (vkey.charAt(2) == '.') {
            return getMappingForVwdType(vkey.charAt(0), vkey.charAt(1));
        }
        else {
            return UNKNOWN;
        }
    }

    /**
     * Returns a new vendorkey
     * @param vkey textual vendorkey respresentation
     * @return new vendorkey or {@link #ERROR} if vkey is invalid.
     */
    public static VendorkeyVwd getInstance(String vkey) {
        if (vkey == null || vkey.length() < 3) {
            return ERROR;
        }
        final int c1 = vkey.lastIndexOf(',');
        if (c1 != -1) {
            return getMdpsInstance(vkey, c1);
        }
        final int p1 = vkey.indexOf('.');
        if (p1 == -1) {
            return ERROR;
        }
        final int mapping = getVendorkeyMapping(vkey);
        if (mapping == 0) {
            return ERROR;
        }
        return new VendorkeyVwd(new ByteString(vkey.substring(p1 + 1)), mapping);
    }

    private static VendorkeyVwd getMdpsInstance(String vkey, int c1) {
        if (c1 + 1 == vkey.length() || c1 + 3 < vkey.length()) {
            return ERROR;
        }
        final int mapping = MdpsKeyConverter.getMapping(new ByteString(vkey));
        if (MdpsKeyConverter.isUnknownXfeedType(mapping)) {
            return ERROR;
        }
        return new VendorkeyVwd(new ByteString(vkey.substring(0, c1)), mapping);
    }

    /**
     * Returns a new vendorkey
     * @param vkey textual vendorkey respresentation
     * @return new vendorkey that might cause exceptions if a non valid key was used.
     */
    public static VendorkeyVwd getInstanceUnchecked(ByteString vkey) {
        final int n = vkey.indexOf('.');
        return new VendorkeyVwd(vkey.substring(n + 1), getVendorkeyMapping(vkey));
    }

    /**
     * Returns true iff vkey is a valid vendorkey representation;
     * invoking {@link #getInstance(de.marketmaker.istar.common.util.ByteString)} with a key
     * for which this method returned true will never return {@link #ERROR}.
     * @param vkey to be checked
     * @return true iff valid.
     */
    public static boolean isKeyWithTypePrefix(ByteString vkey) {
        // TODO: refactor callers, should invoke getInstance(String) and eval result.
        return isValidLength(vkey) && isWithXfeedType(vkey);
    }

    private static boolean isValidLength(ByteString vkey) {
        return vkey != null && vkey.length() >= 3 && vkey.length() <= MAX_VENDORKEY_LEN;
    }

    public static boolean isWithXfeedType(ByteString vkey) {
        if (!vkey.isDigit(0)) {
            return false;
        }

        if (vkey.byteAt(1) != '.') {
            if (!vkey.isDigit(1) || vkey.byteAt(2) != (byte) '.') {
                return false;
            }
        }
        return vkey.lastIndexOf('.') > 2;
    }

    /**
     * Private constructor.
     * @param vwdcode string representation for this object
     * @param typeMapping as obtained by {@link de.marketmaker.istar.feed.mdps.MdpsKeyConverter#getMapping(de.marketmaker.istar.common.util.ByteString)}
     */
    private VendorkeyVwd(ByteString vwdcode, int typeMapping) {
        this.vwdcode = vwdcode;
        this.typeMapping = (short) typeMapping;
        this.marketName = doGetMarketName();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VendorkeyVwd)) {
            return false;
        }

        final VendorkeyVwd other = (VendorkeyVwd) o;
        return this.vwdcode.equals(other.vwdcode);
    }

    public int hashCode() {
        return this.vwdcode.hashCode();
    }

    public String toString() {
        return this.vwdcode.toString();
    }

    public int compareTo(final Vendorkey v) {
        if (this == v) {
            return 0;
        }
        return this.vwdcode.compareTo(v.toVwdcode());
    }

    public ByteString toByteString() {
        return toByteString(WITH_OLD_TYPE_MAPPING);
    }

    public ByteString toByteString(boolean withOldMapping) {
        final int xfeedType = withOldMapping ? getOldType() : getType();
        final byte[] tmp;
        if (xfeedType < 10) {
            tmp = new byte[2];
            tmp[0] = (byte) (xfeedType + '0');
            tmp[1] = (byte) '.';
        }
        else if (xfeedType < 20) {
            tmp = new byte[3];
            tmp[0] = (byte) '1';
            tmp[1] = (byte) (xfeedType - 10 + '0');
            tmp[2] = (byte) '.';
        }
        else if (xfeedType == 99) {
            tmp = NINETYNINE;
        }
        else {
            tmp = ZERO;
        }
        return this.vwdcode.prepend(tmp);
    }

    private int getOldType() {
        final int type = getType();
        switch (type) {
            case 17:
                return 8;
            case 18:
                return 1;
            default:
                return type;
        }
    }

    public ByteString toMdpsKey() {
        final int mdpsType = getMdpsType();
        if (mdpsType == 0) {
            return ByteString.NULL;
        }
        final ByteString suffix = getMdpsKeyTypeSuffixById(mdpsType);
        return this.vwdcode.append(suffix);
    }

    @Override
    public int getMdpsType() {
        return (this.typeMapping >> 8) & 0xFF;
    }

    public final ByteString toVwdcode() {
        return this.vwdcode;
    }

    public int getType() {
        return this.typeMapping & 0xFF;
    }

    public ByteString getMarketName() {
        return this.marketName;
    }

    private ByteString doGetMarketName() {
        final int start = this.vwdcode.indexOf('.', 1) + 1;
        if (start == 0) {
            return ByteString.EMPTY;
        }

        final int end = this.vwdcode.indexOf('.', start + 1);
        return this.vwdcode.substring(start, (end == -1) ? this.vwdcode.length() : end);
    }

    public ByteString getSymbol() {
        return this.vwdcode.substring(0, this.vwdcode.indexOf('.'));
    }

    public ByteString getMaturity() {
        final int first = this.vwdcode.indexOf('.');
        final int second = this.vwdcode.indexOf('.', first + 1);
        if (second < 0) {
            return null;
        }
        final int third = this.vwdcode.indexOf('.', second + 1);
        if (third < 0) {
            return null;
        }

        return this.vwdcode.substring(third + 1);
    }

    public ByteString getStrike() {
        final int first = this.vwdcode.indexOf('.');
        final int second = this.vwdcode.indexOf('.', first + 1);
        if (second < 0) {
            return null;
        }
        final int third = this.vwdcode.indexOf('.', second + 1);

        return this.vwdcode.substring(second + 1, third > 0 ? third : this.vwdcode.length() - 1);
    }
}
