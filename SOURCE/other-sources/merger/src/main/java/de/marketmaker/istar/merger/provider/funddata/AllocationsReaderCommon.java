/*
 * AllocationsReader.java
 *
 * Created on 10.01.2011 15:38:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domainimpl.data.InstrumentAllocationImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AllocationsReaderCommon extends
        IstarMdpExportReader<Map<Long, List<InstrumentAllocation>>> {

    private final Map<Long, List<InstrumentAllocation>> values =
            new HashMap<>();

    private final Set<String> unknownTypes = new HashSet<>();

    private String instrumentIdTag = "SECURITY";

    private final String categoryTag;

    private final String typeTag;

    private String shareTag = "SHARE_";

    private String sourceTag = "SOURCE";

    private MasterDataFund.Source source;

    public AllocationsReaderCommon(boolean limited, String categoryTag, String typeTag,
            MasterDataFund.Source source) {
        super(limited, categoryTag, typeTag);
        this.categoryTag = categoryTag;
        this.typeTag = typeTag;
        this.source = source;
    }

    public AllocationsReaderCommon(boolean limited, MasterDataFund.Source source) {
        this(limited, "NAME", "TYPE", source);
    }

    public AllocationsReaderCommon withInstrumentIdTag(String instrumentIdTag) {
        this.instrumentIdTag = instrumentIdTag;
        return this;
    }

    public AllocationsReaderCommon withShareTag(String shareTag) {
        this.shareTag = shareTag;
        return this;
    }

    protected InstrumentAllocation.Type getType(String str) {
        try {
            return InstrumentAllocation.Type.valueOf(str);
        } catch (IllegalArgumentException e) {
            this.unknownTypes.add(str);
            return null;
        }
    }

    @Override
    protected void handleRow() {
        Long instrumentid = getLong(this.instrumentIdTag, "IID");
        if (instrumentid == null) {
            return;
        }
        InstrumentAllocation.Type type = getType(get(this.typeTag));
        if (null == type) {
            return;
        }

        String category = get(this.categoryTag);
        BigDecimal share = getPercent(this.shareTag);
        if (share == null || share.signum() == 0) {
            return;
        }

        final InstrumentAllocationImpl ia = new InstrumentAllocationImpl(type, category, share);


        String sourceField = get(sourceTag);
        try {
            if (StringUtils.hasText(sourceField)) {
                MasterDataFund.Source sourceFromFile = MasterDataFund.Source.valueOf(sourceField.trim());
                ia.withSource(sourceFromFile);
            } else {
                ia.withSource(this.source);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Found unknown allocation source " + sourceField.trim() + " in allocation file");
        }

        if (!this.values.containsKey(instrumentid)) {
            this.values.put(instrumentid, new ArrayList<>());
        }
        this.values.get(instrumentid).add(ia);
    }

    @Override
    protected Map<Long, List<InstrumentAllocation>> getResult() {
        if (!this.unknownTypes.isEmpty()) {
            this.logger.warn("<getResult> ignored allocations with type(s) " + this.unknownTypes);
        }
        return this.values;
    }
}
