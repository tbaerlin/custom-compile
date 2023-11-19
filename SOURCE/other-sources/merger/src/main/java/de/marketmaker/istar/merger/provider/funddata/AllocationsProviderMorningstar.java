/*
 * AllocationsProviderMorningstar.java
 *
 * Created on 01.03.12 12:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.InvalidProtocolBufferException;

import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domainimpl.data.InstrumentAllocationImpl;
import de.marketmaker.istar.merger.provider.protobuf.BreakdownProtos;
import de.marketmaker.istar.merger.provider.protobuf.ProtobufDataReader;
import de.marketmaker.istar.ratios.backend.MarketAdmissionUtil;

/**
 * @author oflege
 */
public class AllocationsProviderMorningstar extends ProtobufDataReader {
    public AllocationsProviderMorningstar() {
        super(BreakdownProtos.Breakdown.getDescriptor());
    }

    InstrumentAllocations getAllocations(long id) {
        BreakdownProtos.Breakdown.Builder builder = BreakdownProtos.Breakdown.newBuilder();
        try {
            if (build(id, builder) && builder.isInitialized()) {
                return asInstrumentAllocations(builder.build());
            }
        } catch (InvalidProtocolBufferException e) {
            this.logger.warn("<getAllocations> failed for " + id, e);
        }
        return null;
    }

    private InstrumentAllocations asInstrumentAllocations(BreakdownProtos.Breakdown breakdown) {
        final List<InstrumentAllocationImpl> allocations = new ArrayList<>();

        for (int i = 0; i < breakdown.getItemsCount(); i++) {
            final BreakdownProtos.BreakdownItem item = breakdown.getItems(i);
            final InstrumentAllocationImpl ia = getAllocation(allocations, item);
            setAttributes(item, ia);
        }

        for (InstrumentAllocationImpl allocation : allocations) {
            allocation.finish();
        }
        return InstrumentAllocations.create(getMarketAdmission(breakdown), allocations);
    }

    private InstrumentAllocationImpl getAllocation(List<InstrumentAllocationImpl> allocations,
            BreakdownProtos.BreakdownItem item) {
        InstrumentAllocation.Type type = InstrumentAllocation.Type.valueOf(item.getType());
        LocalizedString name = toLocalizedString(item.getNameList());
        InstrumentAllocationImpl ia = getExisting(allocations, type, name.getDe());
        if (ia == null) {
            ia = new InstrumentAllocationImpl(type, name.getDe());
            allocations.add(ia);
        }

        ia.withSource(FundDataProviderMorningstar.SOURCE);
        return ia;
    }

    private void setAttributes(BreakdownProtos.BreakdownItem item, InstrumentAllocationImpl ia) {
        final String meta = item.hasMeta() ? item.getMeta() : null;
        final BigDecimal share = item.hasShare() ? toBigDecimal(item.getShare()) : null;

        if ("L".equals(meta)) {
            ia.withLongPosition(share);
        }
        else if ("S".equals(meta)) {
            ia.withShortPosition(share);
        }
        else {
            ia.withSharePosition(share, getShareType(meta));
        }
    }

    private InstrumentAllocation.ShareType getShareType(String meta) {
        if ("K".equals(meta)) {
            return InstrumentAllocation.ShareType.CONSOLIDATED;
        }
        if ("N".equals(meta)) {
            return InstrumentAllocation.ShareType.STANDARDIZED;
        }
        return null;
    }

    private String getMarketAdmission(BreakdownProtos.Breakdown breakdown) {
        final String s = breakdown.hasMsmarketAdmission() ? breakdown.getMsmarketAdmission() : null;
        return (s != null) ? MarketAdmissionUtil.iso3166Alpha3To2(s) : null;
    }

    private InstrumentAllocationImpl getExisting(List<InstrumentAllocationImpl> allocations,
            InstrumentAllocation.Type type, String name) {
        for (final InstrumentAllocationImpl ia : allocations) {
            if (ia.getType() == type && ia.getCategory().equals(name)) {
                return ia;
            }
        }
        return null;
    }

}
