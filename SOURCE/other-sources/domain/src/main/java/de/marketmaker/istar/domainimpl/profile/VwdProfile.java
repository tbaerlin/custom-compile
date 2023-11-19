/*
 * VwdProfile.java
 *
 * Created on 26.06.2008 09:55:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A Profile as retrieved from vwd-ent:1968
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VwdProfile implements Profile, Serializable {
    public enum State {
        ACTIVE, INACTIVE, UNKNOWN
    }

    /**
     * Data for a single entitlement entity
     */
    static class SelectorItem implements Comparable<SelectorItem> {
        private final int id;

        private final PriceQuality pq;

        private final boolean push;

        public SelectorItem(int id, PriceQuality pq, boolean push) {
            this.id = id;
            this.pq = pq;
            this.push = push;
        }

        public int compareTo(SelectorItem o) {
            return this.id - o.id;
        }

        public int getId() {
            return id;
        }

        public PriceQuality getPq() {
            return pq;
        }

        public boolean isPush() {
            return push;
        }
    }

    /**
     * Data for a single entitled {@link de.marketmaker.istar.domain.profile.Profile.Aspect}.
     * Selectors and their attributes are incoded using a single int value to store them as
     * efficiently as possible.
     */
    private static class AspectData implements Serializable {
        private static final int ID_MASK = 0xFFFF;

        private static final int FLAGS_MASK = 0xE0000000;

        private static final int MODE_PUSH = 0x80000000;

        private static final int QUALITY_EOD = 0x20000000;

        private static final int QUALITY_MASK = 0x60000000;

        private static final int QUALITY_NT = 0x40000000;

        private static final int QUALITY_RT = 0x60000000;

        private static final long serialVersionUID = 1L;

        private final int[] attributedSelectors;

        private final boolean withPush;

        private AspectData(List<SelectorItem> items) {
            if (items == null) {
                this.attributedSelectors = new int[0];
                this.withPush = false;
                return;
            }
            this.attributedSelectors = new int[items.size()];
            boolean tmpWithPush = false;
            for (int i = 0; i < this.attributedSelectors.length; i++) {
                final SelectorItem item = items.get(i);
                this.attributedSelectors[i] = item.getId();
                if (item.isPush()) {
                    this.attributedSelectors[i] |= MODE_PUSH;
                    tmpWithPush = true;
                }
                this.attributedSelectors[i] |= getMaskFor(item.getPq());
            }
            this.withPush = tmpWithPush;
        }

        public boolean isWithPush() {
            return this.withPush;
        }

        public String toString() {
            final StringBuilder sb = new StringBuilder(500);
            sb.append("[");
            for (int i = 0; i < this.attributedSelectors.length; ) {
                int j = i + 1;
                while (j < this.attributedSelectors.length && isSuccessor(i, j)) {
                    j++;
                }
                if (sb.length() > 1) {
                    sb.append(", ");
                }
                sb.append(this.attributedSelectors[i] & ID_MASK);
                if (j > (i + 1)) {
                    sb.append("-").append(this.attributedSelectors[j - 1] & ID_MASK);
                }
                sb.append(getPriceQualitySuffix(this.attributedSelectors[i]));
                i = j;
            }
            return sb.append("]").toString();
        }

        private boolean isSuccessor(int i, int j) {
            final int si = this.attributedSelectors[i];
            final int sj = this.attributedSelectors[j];
            return (sj - si) == (j - i) && ((si & FLAGS_MASK) == (sj & FLAGS_MASK));
        }

        private int getMaskFor(PriceQuality pq) {
            if (pq == null) {
                return 0;
            }
            switch (pq) {
                case REALTIME:
                    return QUALITY_RT;
                case DELAYED:
                    return QUALITY_NT;
                case END_OF_DAY:
                    return QUALITY_EOD;
                default:
                    return 0;
            }
        }

        private PriceQuality getPriceQuality(int n) {
            return getPriceQuality(n, false);
        }

        private PriceQuality getPriceQuality(int n, boolean pushRequired) {
            final int s = getAttributedSelector(n);
            if (pushRequired && ((s & MODE_PUSH) == 0)) {
                return PriceQuality.NONE;
            }
            switch (s & QUALITY_MASK) {
                case QUALITY_EOD:
                    return PriceQuality.END_OF_DAY;
                case QUALITY_NT:
                    return PriceQuality.DELAYED;
                case QUALITY_RT:
                    return PriceQuality.REALTIME;
                default:
                    return PriceQuality.NONE;
            }
        }

        private String getPriceQualitySuffix(int selector) {
            final String p = ((selector & MODE_PUSH) == MODE_PUSH) ? "+" : "";
            switch (selector & QUALITY_MASK) {
                case QUALITY_EOD:
                    return "EOD" + p;
                case QUALITY_NT:
                    return "NT" + p;
                case QUALITY_RT:
                    return "RT" + p;
                default:
                    return "";
            }
        }

        private int getAttributedSelector(int n) {
            int low = 0;
            int high = this.attributedSelectors.length - 1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                int midVal = this.attributedSelectors[mid] & ID_MASK;

                if (midVal < n) {
                    low = mid + 1;
                }
                else if (midVal > n) {
                    high = mid - 1;
                }
                else {
                    return this.attributedSelectors[mid]; // found
                }
            }
            return 0;  // not found.
        }

        private boolean hasSelector(int n) {
            return getAttributedSelector(n) != 0;
        }

/*
        private boolean isPush(int n) {
            return (getAttributedSelector(n) & MODE_PUSH) != 0;
        }
*/

        private BitSet toBitSet() {
            return toBitSet(0);
        }

        private BitSet toBitSet(PriceQuality pq) {
            if (pq == PriceQuality.NONE) {
                return new BitSet();
            }
            final int pqMask = getMaskFor(pq);
            return toBitSet(pqMask);
        }

        private BitSet toBitSet(int pqMask) {
            final BitSet result = new BitSet();
            for (int selector : this.attributedSelectors) {
                if (pqMask == 0 || (selector & QUALITY_MASK) == pqMask) { // use pqMask == 0 to return all selectors for all qualities
                    result.set(selector & ID_MASK);
                }
            }
            return result;
        }
    }

    private static final class NullAspectData extends AspectData {
        private static final long serialVersionUID = 1L;

        private NullAspectData() {
            super(null);
        }

        protected Object readResolve() {
            return NULL_ASPECT_DATA;
        }
    }

    private static final NullAspectData NULL_ASPECT_DATA = new NullAspectData();

    private static final long serialVersionUID = 1L;

    private DateTime created;

    private final AspectData[] data = new AspectData[Aspect.values().length];

    private DateTime exported;

    private State state;

    private String terminalName;

    private String produktId;

    private String konzernId;

    private DateTime updated;

    private String vwdId;

    private String appId;

    public VwdProfile() {
        Arrays.fill(this.data, NULL_ASPECT_DATA);
    }

    public DateTime getCreated() {
        return created;
    }

    public DateTime getExported() {
        return exported;
    }

    public PriceQuality getPriceQuality(String entitlement, KeysystemEnum ks) {
        if (ks != KeysystemEnum.VWDFEED) {
            return PriceQuality.NONE;
        }
        final int n = toEntitlementId(entitlement);
        return getData(Aspect.PRICE).getPriceQuality(n);
    }

    @Override
    public PriceQuality getPushPriceQuality(String entitlement, KeysystemEnum ks) {
        if (ks != KeysystemEnum.VWDFEED) {
            return PriceQuality.NONE;
        }
        final int ent = toEntitlementId(entitlement);
        return getData(Aspect.PRICE).getPriceQuality(ent, true);
    }

    public BitSet getSelectorIds(Aspect aspect) {
        return getData(aspect).toBitSet();
    }

    public BitSet getSelectorIds(Aspect aspect, PriceQuality pq) {
        return getData(aspect).toBitSet(pq);
    }

    public State getState() {
        return state;
    }

    public String getTerminalName() {
        return terminalName;
    }

    public DateTime getUpdated() {
        return updated;
    }

    public String getVwdId() {
        return vwdId;
    }

    public String getAppId() {
        return appId;
    }

    public String getProduktId() {
        return produktId;
    }

    public String getKonzernId() {
        return konzernId;
    }

    public boolean isWithPush() {
        return getData(Aspect.PRICE).isWithPush();
    }

    public boolean isAllowed(Aspect aspect, String entitlement) {
        return getData(aspect).hasSelector(toEntitlementId(entitlement));
    }

    public boolean isAllowed(Selector selector) {
        for (Aspect aspect : selector.getAspects()) {
            if (getData(aspect).hasSelector(selector.getId())) {
                return true;
            }
        }
        return false;
    }

    public BitSet toEntitlements(Aspect aspect, PriceQuality pq) {
        return getData(aspect).toBitSet(pq);
    }

    public Profile toAspectSpecificProfile(Aspect aspect) {
        final VwdProfile result = new VwdProfile();
        result.vwdId = this.vwdId;
        result.appId = this.appId;
        result.state = this.state;
        result.created = this.created;
        result.exported = this.exported;
        result.updated = this.updated;
        result.terminalName = aspect.name();
        result.data[aspect.ordinal()] = getData(aspect);
        return result;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public void setExported(DateTime exported) {
        this.exported = exported;
    }

    void setState(State state) {
        this.state = state;
    }

    public void setTerminalName(String terminalName) {
        this.terminalName = terminalName;
    }

    public void setUpdated(DateTime updated) {
        this.updated = updated;
    }

    public void setVwdId(String vwdId) {
        this.vwdId = vwdId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setKonzernId(String konzernId) {
        this.konzernId = konzernId;
    }

    public void setProduktId(String produktId) {
        this.produktId = produktId;
    }

    public String toString() {
        return new StringBuilder(500)
                .append("VwdProfile[").append(this.vwdId)
                .append(", appId=").append(this.appId)
                .append(", state=").append(this.state)
                .append(", name=").append(this.terminalName)
                .append(", created=").append(this.created)
                .append(", updated=").append(this.updated)
                .append(", exported=").append(this.exported)
                .append(", PRICE=").append(getData(Aspect.PRICE))
                .append(", PAGE=").append(getData(Aspect.PAGE))
                .append(", NEWS=").append(getData(Aspect.NEWS))
                .append(", PRODUCT=").append(getData(Aspect.PRODUCT))
                .append(", FUNCTION=").append(getData(Aspect.FUNCTION))
                .append("]").toString();
    }

    void setSelectors(List<SelectorItem> selectors, Aspect aspect) {
        this.data[aspect.ordinal()] = new AspectData(selectors);
    }

    private int toEntitlementId(String entitlement) {
        return EntitlementsVwd.toValue(entitlement);
    }

    public String getName() {
        return "Vwd:" + this.vwdId;
    }

    public Collection<String> getPermission(PermissionType type) {
        return Collections.emptyList();
    }

    public PriceQuality getPriceQuality(Quote quote) {
        return getPriceQuality(quote, 0, false);
    }

    public PriceQuality getPushPriceQuality(Quote quote, String e) {
        return getPriceQuality(quote, (e != null) ? toEntitlementId(e) : 0, true);
    }

    private PriceQuality getPriceQuality(Quote quote, int requiredEnt, boolean pushRequired) {
        final Entitlement entitlement = quote.getEntitlement();
        final String[] entitlements = entitlement.getEntitlements(KeysystemEnum.VWDFEED);
        PriceQuality result = null;

        // calc lowest PQ not equal to NONE
        for (String s : entitlements) {
            final int i = toEntitlementId(s);
            if (requiredEnt != 0 && i != requiredEnt) {
                continue;
            }

            final PriceQuality quality = getData(Aspect.PRICE).getPriceQuality(i, pushRequired);
            if (quality != PriceQuality.NONE) {
                if (result == null) {
                    result = quality;
                }
                else {
                    result = PriceQuality.min(result, quality);
                }
            }
        }
        return result == null ? PriceQuality.NONE : result;
    }

    private AspectData getData(final Aspect aspect) {
        return this.data[aspect.ordinal()];
    }

}
