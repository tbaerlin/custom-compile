/*
 * InstrumentAllocations.java
 *
 * Created on 29.08.2006 14:16:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.jcip.annotations.Immutable;

/**
 * Maintains InstrumentAllocation objects for some fund.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class InstrumentAllocations implements Serializable {
    protected static final long serialVersionUID = 1L;

    public static final InstrumentAllocations NULL = new InstrumentAllocations();

    private static final Comparator<InstrumentAllocation> COMPARE_BY_SHARE_DESC = new Comparator<InstrumentAllocation>() {
        public int compare(InstrumentAllocation o1, InstrumentAllocation o2) {
            return -(o1.getShare().compareTo(o2.getShare()));
        }
    };

    private final String marketAdmission;

    private final String permissionType;

    private final List<InstrumentAllocation> allocations;

    public static InstrumentAllocations create(String msMarketAdmission,
            List<? extends InstrumentAllocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            return NULL;
        }
        return new InstrumentAllocations(msMarketAdmission, allocations, null);
    }

    public static InstrumentAllocations create(List<? extends InstrumentAllocation> allocations,
            String permissionType) {
        if (allocations == null || allocations.isEmpty()) {
            return NULL;
        }
        return new InstrumentAllocations(null, allocations, permissionType);
    }

    public static InstrumentAllocations create(List<? extends InstrumentAllocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            return NULL;
        }
        return new InstrumentAllocations(null, allocations, null);
    }

    private InstrumentAllocations() {
        this(null, Collections.<InstrumentAllocation>emptyList(), null);
    }

    private InstrumentAllocations(String marketAdmission,
            List<? extends InstrumentAllocation> allocations, String permissionType) {
        this.marketAdmission = marketAdmission;
        this.permissionType = permissionType;
        this.allocations = new ArrayList<>(allocations.size());
        this.allocations.addAll(allocations);
    }

    /**
     * @param type desired allocation type
     * @return InstrumentAllocations of a given type ordered by descending share
     */
    public List<InstrumentAllocation> getAllocations(InstrumentAllocation.Type type) {
        final List<InstrumentAllocation> result = new ArrayList<>();
        for (InstrumentAllocation allocation : allocations) {
            if (allocation.getType() == type && allocation.getShare() != null) {
                result.add(allocation);
            }
        }
        result.sort(COMPARE_BY_SHARE_DESC);
        return result;
    }

    // used in velocity
    public List<InstrumentAllocation> getAllocations(String sType) {
        final InstrumentAllocation.Type type = InstrumentAllocation.Type.valueOf(sType);
        return getAllocations(type);
    }

    public boolean isEmpty() {
        return this.allocations.isEmpty();
    }

    private Object readResolve() {
        return this.allocations.isEmpty() ? NULL : this;
    }

    public String getMarketAdmission() {
        return marketAdmission;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public MasterDataFund.Source getSource() {
        return allocations.isEmpty() ? null : allocations.get(0).getSource();
    }

    @Override
    public String toString() {
        return "InstrumentAllocations{" +
                "marketAdmission='" + marketAdmission + '\'' +
                ", allocations=" + allocations +
                '}';
    }
}
