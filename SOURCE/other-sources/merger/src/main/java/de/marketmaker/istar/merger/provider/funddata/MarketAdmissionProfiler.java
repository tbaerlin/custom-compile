/*
 * MorningStarSelector.java
 *
 * Created on 24.08.2010 15:01:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.NullMasterDataFund;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.ratios.backend.MarketAdmissionUtil;

/**
 * @author zzhao
 */
public final class MarketAdmissionProfiler {

    private MarketAdmissionProfiler() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static FundDataResponse profileFundDataResponse(Profile profile, FundDataResponse resp) {
        final FundDataResponse ret = new FundDataResponse();
        ret.setBenchmarksList(resp.getBenchmarksList());
        ret.setFeriPerformanceses(resp.getFeriPerformanceses());
        ret.setFeriRatings(resp.getFeriRatings());

        ret.setInstrumentAllocationses(profileInstrumentAllocations(profile,
                resp.getInstrumentAllocationses()));

        ret.setMasterDataFunds(profileMasterDataFunds(profile, resp.getMasterDataFunds()));

        ret.setMorningstarRatings(resp.getMorningstarRatings());

        return ret;
    }

    private static List<MasterDataFund> profileMasterDataFunds(
            Profile profile, List<MasterDataFund> masterDataFunds) {
        if (CollectionUtils.isEmpty(masterDataFunds)) {
            return masterDataFunds;
        }

        final List<MasterDataFund> ret = new ArrayList<>(masterDataFunds.size());
        for (final MasterDataFund md : masterDataFunds) {
            if (null != md && !MarketAdmissionUtil.allowByMarketAdmissionMorningstar(profile, md.getMarketAdmission())) {
                ret.add(NullMasterDataFund.INSTANCE);
            }
            else {
                ret.add(md);
            }
        }

        return ret;
    }

    private static List<InstrumentAllocations> profileInstrumentAllocations(
            Profile profile, List<InstrumentAllocations> allocations) {
        if (null == allocations) {
            return null;
        }
        if (allocations.isEmpty()) {
            return Collections.emptyList();
        }

        final List<InstrumentAllocations> ret =
                new ArrayList<>(allocations.size());

        for (InstrumentAllocations ias : allocations) {
            if (MarketAdmissionUtil.allowByMarketAdmissionMorningstar(profile, ias.getMarketAdmission())) {
                ret.add(ias);
            }
            else {
                ret.add(InstrumentAllocations.NULL);
            }
        }

        return ret;
    }

    private static InstrumentTypeEnum getInstrumentType(List<DownloadableItem> reports) {
        for (DownloadableItem di : reports) {
            if (null != di.getInstrumentType()) {
                return di.getInstrumentType();
            }
        }

        return null;
    }

    public static List<DownloadableItem> profileReports(Profile profile,
            List<DownloadableItem> reports) {
        if (CollectionUtils.isEmpty(reports)) {
            return reports;
        }

        final InstrumentTypeEnum instrumentType = getInstrumentType(reports);

        if (instrumentType != InstrumentTypeEnum.FND) {
            return reports;
        }

        final ArrayList<DownloadableItem> result = new ArrayList<>(reports.size());
        result.addAll(reports
                .stream()
                .filter(item -> MarketAdmissionUtil.allowByMarketAdmissionStockSelection(profile, item.getMarketAdmission()))
                .collect(Collectors.toList()));

        return result;
    }
}
