/*
 * EntitlementDp2.java
 *
 * Created on 31.05.2005 13:40:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.Entitlement;
import de.marketmaker.istar.domain.KeysystemEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EntitlementDp2 implements Entitlement, Serializable {
    static final long serialVersionUID = -9L;

    private final static String[] EMPTY_ENTITLEMENTS = new String[0];

    private final Map<KeysystemEnum, String[]> entitlements = new HashMap<>();

    public static class EntitlementItem {
        private KeysystemEnum key;
        private String value;

        public EntitlementItem() {
        }

        public EntitlementItem(KeysystemEnum key, String value) {
            this.key = key;
            this.value = value;
        }

        @NotNull
        public KeysystemEnum getKey() {
            return key;
        }

        public void setKey(KeysystemEnum key) {
            this.key = key;
        }

        @NotNull
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public EntitlementDp2() {
    }


    public Iterable<Map.Entry<KeysystemEnum, String[]>> getEntitlements() {
        if (this.entitlements.isEmpty()) {
            return Collections.<KeysystemEnum, String[]>emptyMap().entrySet();
        }
        return new EnumMap<>(this.entitlements).entrySet();
    }

    public String[] getEntitlements(KeysystemEnum id) {
        final String[] result = this.entitlements.get(id);
        return result != null ? result : EMPTY_ENTITLEMENTS;
    }

    public void setEntitlements(KeysystemEnum id, String[] ents) {
        this.entitlements.put(id, ents);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(100);
        sb.append("EntitlementDp2[");
        for (Map.Entry<KeysystemEnum, String[]> e : getEntitlements()) {
            sb.append(" ");
            sb.append(e.getKey().name()).append("=>").append(Arrays.toString(getEntitlements(e.getKey())));
        }
        return sb.append("]").toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final EntitlementDp2 that = (EntitlementDp2) o;

        for (KeysystemEnum k : entitlements.keySet()) {
            final String[] thisValues = this.entitlements.get(k);
            final String[] thatValues = that.entitlements.get(k);
            if (!Arrays.equals(thisValues, thatValues)) {
                return false;
            }
        }

        return true;
    }
}
