package de.marketmaker.istar.merger.provider.funddata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.NullMasterDataFund;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.MasterDataFundImpl;
import de.marketmaker.istar.domainimpl.data.MasterDataFundProfiler;
import de.marketmaker.istar.ratios.backend.MarketAdmissionUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundDataProviderImpl implements FundDataProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private FundDataProvider ssatProvider;

    private FundDataProvider fidaProvider;

    private FundDataProvider fwwProvider;

    private FundDataProvider morningstarProvider;

    private FundDataProvider vwditProvider;

    private FundDataProvider unionProvider;

    private FundDataProvider vwdProvider;

    private final Map<String, MasterDataFundProfiler> profilers = new HashMap<>();

    public FundDataProviderImpl() {
        this.profilers.put("Vwd:120080", MasterDataFundProfiler.ALLOW_ALL); // Risiko-Service für pm
        final MasterDataFundProfiler spsProfiler = new MasterDataFundProfiler(Collections.singleton("dr"),
                "strategy", "currency", "fundtype", "investmentFocus",
                "benchmarkName", "benchmarkQid", "issuerName", "issuerAddress", "issuerStreet",
                "issuerPostalcode", "issuerCity", "issuerEmail", "issuerUrl", "issuerPhone",
                "issuerFax", "sector", "issueDate", "reportDate", "lastDistribution",
                "distributionCurrency", "lastDistributionDate", "fundVolume", "fundManager",
                "distributionStrategy", "distributionCount",
                "issueSurcharge", "issueSurchargeType", "issueSurchargeNet", "issueSurchargeGross",
                "managementFee", "accountFee", "performanceFee", "ter", "allinFee", "country",
                "minimumInvestment", "marketAdmission", "fwwRiskclass"
        );
        this.profilers.put("Vwd:120137", spsProfiler);
        this.profilers.put("Vwd:120138", spsProfiler);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setFidaProvider(FundDataProvider fidaProvider) {
        this.fidaProvider = fidaProvider;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setSsatProvider(FundDataProvider ssatProvider) {
        this.ssatProvider = ssatProvider;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setFwwProvider(FundDataProvider fwwProvider) {
        this.fwwProvider = fwwProvider;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setMorningstarProvider(FundDataProvider morningstarProvider) {
        this.morningstarProvider = morningstarProvider;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setVwdProvider(FundDataProvider vwdProvider) {
        this.vwdProvider = vwdProvider;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setUnionProvider(FundDataProvider unionProvider) {
        this.unionProvider = unionProvider;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setVwditProvider(FundDataProvider vwditProvider) {
        this.vwditProvider = vwditProvider;
    }

    private FundDataProvider getProvider(FundDataRequest request) {
        final Profile profile = request.getProfile();
        final String providerPreference = request.getProviderPreference();

        if (providerPreference != null) {
            final FundDataProvider provider = getProvider(profile, providerPreference);
            if (provider != null) {
                return provider;
            }
        }

        if (profile.isAllowed(Selector.VWD_FUND_DATA) || profile.isAllowed(Selector.VWD_FUND_MAPBIT_BREAKDOWNS)) {
            return this.vwdProvider;
        }
        if (MarketAdmissionUtil.allowByMarketAdmissionMorningstar(profile)) {
            return this.morningstarProvider;
        }
        if (profile.isAllowed(Selector.FIDA_FUND_DATA) || profile.isAllowed(Selector.FIDA_FUND_DATA_I)) {
            return this.fidaProvider;
        }
        if (profile.isAllowed(Selector.FUNDDATA_FWW)) {
            this.logger.warn("<getProviderR> FUNDDATA_FWW should not be used any more");
            return this.fwwProvider;
        }
        if (profile.isAllowed(Selector.FUNDDATA_VWD_IT)) {
            this.logger.warn("<getProviderR> FUNDDATA_VWD_IT should not be used any more");
            return this.vwditProvider;
        }
        if (profile.isAllowed(Selector.SSAT_FUND_DATA)) {
            this.logger.warn("<getProviderR> SSAT_FUND_DATA should not be used any more");
            return this.ssatProvider;
        }

        return null;
    }

    private FundDataProvider getProvider(Profile profile, String providerPreference) {
        if ("VWD".equals(providerPreference) && (profile.isAllowed(Selector.VWD_FUND_DATA) || profile.isAllowed(Selector.VWD_FUND_MAPBIT_BREAKDOWNS))) {
            return this.vwdProvider;
        }
        if ("MORNINGSTAR".equals(providerPreference) && MarketAdmissionUtil.allowByMarketAdmissionMorningstar(profile)) {
            return this.morningstarProvider;
        }
        if ("FIDA".equals(providerPreference) && (profile.isAllowed(Selector.FIDA_FUND_DATA) || profile.isAllowed(Selector.FIDA_FUND_DATA_I))) {
            return this.fidaProvider;
        }
        if ("FWW".equals(providerPreference) && profile.isAllowed(Selector.FUNDDATA_FWW)) {
            this.logger.warn("<getProviderP> FUNDDATA_FWW should not be used any more");
            return this.fwwProvider;
        }
        if ("VWDIT".equals(providerPreference) && profile.isAllowed(Selector.FUNDDATA_VWD_IT)) {
            this.logger.warn("<getProviderP> FUNDDATA_VWD_IT should not be used any more");
            return this.vwditProvider;
        }
        if ("SSAT".equals(providerPreference) && profile.isAllowed(Selector.SSAT_FUND_DATA)) {
            this.logger.warn("<getProviderP> SSAT_FUND_DATA should not be used any more");
            return this.ssatProvider;
        }
        // XXX do not forget sync restricted set of provider preference in FndStammdaten

        return null;
    }

    private boolean isValid(FundDataResponse r) {
        return r != null && r.isValid();
    }

    public FundDataResponse getFundData(FundDataRequest request) {
        try {
            return doGetFundData(request);
        } catch (Exception e) {
            this.logger.error("<getFundData> failed", e);
            final FundDataResponse failed = new FundDataResponse();
            failed.setInvalid();
            return failed;
        }
    }

    private FundDataResponse doGetFundData(FundDataRequest request) {
        final FundDataResponse response = getResponse(request);
        final FundDataResponse unionResponse = getUnionResponse(request);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<getFundData>1 " + response);
            this.logger.debug("<getFundData>2 " + unionResponse);
        }

        if (!isValid(response) && !isValid(unionResponse)) {
            return FundDataResponse.createEmptyResponse(request.getInstrumentids().size());
        }

        if (!isValid(response)) {
            return unionResponse;
        }

        if (!isValid(unionResponse)) {
            return response;
        }

        final int numInstruments = request.getInstrumentids().size();
        mergeUnionPortfolioDate(numInstruments, response, unionResponse);
        mergeUnionData(numInstruments, response, unionResponse);

        return response;
    }

    private void mergeUnionPortfolioDate(int numInstruments, FundDataResponse response, FundDataResponse unionResponse) {
        if (unionResponse.getMasterDataFunds() == null || response.getMasterDataFunds().size() != unionResponse.getMasterDataFunds().size()) {
            return;
        }

        final List<MasterDataFund> masterDataFunds = new ArrayList<>(numInstruments);
        for (int i = 0; i < numInstruments; i++) {
            MasterDataFund mdf = response.getMasterDataFunds().get(i);
            MasterDataFund unionMdf = unionResponse.getMasterDataFunds().get(i);
            masterDataFunds.add(addUnionPortfolioDate(mdf, unionMdf));
        }
        response.setMasterDataFunds(masterDataFunds);
    }

    private MasterDataFund addUnionPortfolioDate(MasterDataFund mdf, MasterDataFund unionMdf) {
        if (unionMdf instanceof NullMasterDataFund) {
            return mdf;
        }
        else if (mdf instanceof NullMasterDataFund) {
            return unionMdf;
        }
        else if (mdf instanceof MasterDataFundImpl) {
            MasterDataFundImpl.Builder b = new MasterDataFundImpl.Builder((MasterDataFundImpl) mdf);
            b.setPortfolioDate(unionMdf.getPortfolioDate());
            return b.build();
        }

        this.logger.warn("<addUnionPortfolioDate> cannot update " + mdf.getClass().getName());
        return mdf;
    }

    private void mergeUnionData(int numInstruments, FundDataResponse response, FundDataResponse unionResponse) {
        if (unionResponse.getInstrumentAllocationses() == null || response.getInstrumentAllocationses().size() != unionResponse.getInstrumentAllocationses().size()) {
            return;
        }

        final List<InstrumentAllocations> instrumentAllocationses = new ArrayList<>();

        for (int i = 0; i < numInstruments; i++) {
            final InstrumentAllocations standard = response.getInstrumentAllocationses().get(i);
            final InstrumentAllocations vwd = unionResponse.getInstrumentAllocationses().get(i);

            instrumentAllocationses.add(vwd.isEmpty() ? standard : vwd);
        }
        response.setInstrumentAllocationses(instrumentAllocationses);

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<mergeUnionData> " + response);
        }
    }

    private FundDataResponse getResponse(FundDataRequest request) {
        final FundDataProvider provider = getProvider(request);
        if (provider == null) {
            return null;
        }
        final Profile profile = request.getProfile();
        FundDataResponse result = provider.getFundData(request);

        if (provider == this.morningstarProvider) {
            result = MarketAdmissionProfiler.profileFundDataResponse(profile, result);
        }

        if (provider == this.fidaProvider) {
            result = FidaProfiler.profileFundDataResponse(profile, result);
        }

        if (provider == this.fwwProvider) {
            applyProfileToFwwMasterdata(profile, result);
        }

        if (provider == this.vwdProvider) {
            applyProfileToVwdMasterdata(profile, request, result);
        }
        return result;
    }

    private void applyProfileToFwwMasterdata(Profile profile, FundDataResponse r) {
        if (!isValid(r) || r.getMasterDataFunds() == null || r.getMasterDataFunds().isEmpty()) {
            return;
        }

        final String pname = profile.getName();
        final MasterDataFundProfiler profiler = this.profilers.get(pname);

        if (profiler == null) {
            r.setMasterDataFunds(new ArrayList<>(Collections.nCopies(r.getMasterDataFunds().size(),
                    NullMasterDataFund.INSTANCE)));
            return;
        }

        r.setMasterDataFunds(r.getMasterDataFunds().stream().map(profiler::applyTo).collect(Collectors.toList()));
    }

    /**
     * Check response against profile for data w/o permission to access master data or allocations.
     */
    private void applyProfileToVwdMasterdata(Profile profile, FundDataRequest request,
            FundDataResponse response) {
        if (!isValid(response)) {
            return;
        }
        // data to be deleted can only exist if requested, so check against request
        // if allocations are requested but not allowed -> delete them
        if ((request.isWithAllocations() || request.isWithConsolidatedAllocations()) && !profile.isAllowed(Selector.VWD_FUND_MAPBIT_BREAKDOWNS)) {
            response.setInstrumentAllocationses(new ArrayList<>(Collections.nCopies(request.getInstrumentids().size(), InstrumentAllocations.NULL)));
        }
        // if master data is requested but not allowed -> delete it
        if (request.isWithMasterData() && !profile.isAllowed(Selector.VWD_FUND_DATA)) {
            response.setMasterDataFunds(new ArrayList<>(Collections.nCopies(request.getInstrumentids().size(), NullMasterDataFund.INSTANCE)));
        }
        // if diamond ratings are not allowed -> delete them
        else if (request.isWithMasterData() && !profile.isAllowed(Selector.DIAMOND_RATING)) {
            removeDiamondRating(response);
        }
    }

    private void applyProfileToVwdBenlMasterdata(Profile profile, FundDataRequest request, FundDataResponse response) {
        if (!isValid(response)) {
            return;
        }
        if (request.isWithMasterData() && !profile.isAllowed(Selector.DIAMOND_RATING)) {
            removeDiamondRating(response);
        }
    }


    private void removeDiamondRating(FundDataResponse response) {
        final List<MasterDataFund> masterDataOld = response.getMasterDataFunds();
        final List<MasterDataFund> masterDataNew = new ArrayList<>(masterDataOld.size());
        for (MasterDataFund masterDataFund : response.getMasterDataFunds()) {
            if (masterDataFund instanceof NullMasterDataFund) {
                // has no DiamondRating
                masterDataNew.add(masterDataFund);
            } else {
                MasterDataFundImpl.Builder b = new MasterDataFundImpl.Builder((MasterDataFundImpl) masterDataFund);
                b.setDiamondRating(null);
                b.setDiamondRatingDate(null);
                masterDataNew.add(b.build());
            }
        }
        response.setMasterDataFunds(masterDataNew);
    }

    private FundDataResponse getUnionResponse(FundDataRequest request) {
        if (!request.getProfile().isAllowed(Selector.VWD_BREAKDOWN)) {
            return null;
        }

        return this.unionProvider.getFundData(request);
    }
}
