/*
 * MorningStarSelector.java
 *
 * Created on 24.08.2010 15:01:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.funddata;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.NullMasterDataFund;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;

import static de.marketmaker.istar.domain.data.DownloadableItem.Type;

/**
 * @author zzhao
 */
public final class FidaProfiler {

    private FidaProfiler() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    public static FundDataResponse profileFundDataResponse(Profile profile, FundDataResponse resp) {
        final FundDataResponse ret = new FundDataResponse();
        ret.setBenchmarksList(resp.getBenchmarksList());
        ret.setFeriPerformanceses(resp.getFeriPerformanceses());
        ret.setFeriRatings(resp.getFeriRatings());
        ret.setMorningstarRatings(resp.getMorningstarRatings());
        ret.setInstrumentAllocationses(profileInstrumentAllocations(profile,
                resp.getInstrumentAllocationses()));
        ret.setMasterDataFunds(profileMasterDataFunds(profile, resp.getMasterDataFunds()));
        return ret;
    }

    private static List<MasterDataFund> profileMasterDataFunds(
            Profile profile, List<MasterDataFund> masterDataFunds) {
        if (CollectionUtils.isEmpty(masterDataFunds)) {
            return masterDataFunds;
        }
        return masterDataFunds.stream()
                .map((md) -> profileMasterData(profile, md))
                .collect(Collectors.toList());
    }

    private static MasterDataFund profileMasterData(Profile profile, MasterDataFund md) {
        if (!allowByPermissionTypeFundData(profile, md.getPermissionType())) {
            return NullMasterDataFund.INSTANCE;
        }
        final boolean ratingAllowed = FidaProfiler.allowByPermissionType(profile, md.getPermissionType(),
                Selector.FIDA_FUND_RATING, Selector.FIDA_FUND_RATING_I);
        if (ratingAllowed || !(md instanceof MasterDataFundImpl)) {
            return md;
        }
        return ((MasterDataFundImpl) md).withoutFidaRating();
    }

    private static List<InstrumentAllocations> profileInstrumentAllocations(
            Profile profile, List<InstrumentAllocations> allocations) {
        if (allocations == null) {
            return null;
        }
        if (allocations.isEmpty()) {
            return Collections.emptyList();
        }
        return allocations.stream()
                .map((ias) -> profileInstrumentAllocations(profile, ias))
                .collect(Collectors.toList());
    }

    private static InstrumentAllocations profileInstrumentAllocations(Profile profile,
            InstrumentAllocations ias) {
        if (!allowByPermissionTypeFundData(profile, ias.getPermissionType())) {
            return InstrumentAllocations.NULL;
        }
        return ias;
    }

    private static boolean allowByPermissionTypeFundData(Profile profile, String permissionType) {
        return allowByPermissionType(profile, permissionType, Selector.FIDA_FUND_DATA,
                Selector.FIDA_FUND_DATA_I);
    }

    public static boolean allowByPermissionType(Profile profile, String permissionType,
            Selector rSelector, Selector iSelector) {
        return (profile.isAllowed(rSelector) && "R".equals(permissionType))
                || (profile.isAllowed(iSelector) && "I".equals(permissionType));
    }

    public static boolean profileDownloadableItem(Profile profile, DownloadableItem item) {
        return Type.KIID != item.getType() || allowByPermissionType(profile, item.getPermissionType(),
                Selector.FIDA_FUND_REPORTS_KIID, Selector.FIDA_FUND_REPORTS_KIID_I);
    }
}
