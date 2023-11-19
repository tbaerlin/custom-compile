/*
 * InstrumentSerializer.java
 *
 * Created on 19.06.12 17:51
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.protobuf;

import java.util.Set;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentNameStrategies;
import de.marketmaker.istar.instrument.export.SuggestionRankings;

/**
 * Performs protobuf serialization of
 * {@link de.marketmaker.istar.domainimpl.instrument.InstrumentDp2} objects as suggestions.
 * @author oflege
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SuggestionSerializer {

    private final SuggestionRankings rankings;

    public SuggestionSerializer(SuggestionRankings rankings) {
        this.rankings = rankings;
    }

    public byte[] serialize(Instrument i, Set<String> entitlements) {
        InstrumentProtos.Suggestion.Builder ib = InstrumentProtos.Suggestion.newBuilder();
        ib.setId(i.getId());
        ib.setTypeOrd(i.getInstrumentType().ordinal());

        for (int j = 0, n = this.rankings.getStrategyNames().length; j < n; j++) {
            ib.addRanks(this.rankings.getIntOrder(i, j));
        }

        for (String ent : entitlements) {
            ib.addEntitlements(EntitlementsVwd.toValue(ent));
        }

        ib.setName(i.getName());

        String wmWpNameKurz = getWmWpNameKurz(i);
        if (wmWpNameKurz != null) {
            ib.setWmWpNameKurz(wmWpNameKurz);
        }

        String pmNameCost = i.getSymbol(KeysystemEnum.PM_INSTRUMENT_NAME);
        if (pmNameCost != null) {
            ib.setPmNameCost(pmNameCost);
        }

        String pmNameFree = i.getSymbol(KeysystemEnum.PM_INSTRUMENT_NAME_FREE);
        if (pmNameFree != null) {
            ib.setPmNameFree(pmNameFree);
        }

        String isin = i.getSymbolIsin();
        if (isin != null) {
            ib.setIsin(isin);
        }
        String wkn = i.getSymbolWkn();
        if (wkn != null) {
            ib.setWkn(wkn);
        }

        return ib.build().toByteArray();
    }

    private String getWmWpNameKurz(Instrument i) {
        return InstrumentNameStrategies.WM_WP_NAME_KURZ.getName(i);
    }
}
