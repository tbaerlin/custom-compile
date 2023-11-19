package de.marketmaker.istar.merger.provider.funddata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.NullMasterDataFund;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundDataResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private List<MasterDataFund> masterDataFunds;

    private List<InstrumentAllocations> instrumentAllocationses;

    private List<Integer> morningstarRatings;

    private List<String> feriRatings;

    private List<List<InstrumentAllocation>> benchmarksList;

    private List<FeriPerformances> feriPerformanceses;

    public static FundDataResponse createEmptyResponse(int numInstruments) {
        final FundDataResponse response = new FundDataResponse();
        response.masterDataFunds = new ArrayList<>(Collections.nCopies(numInstruments, NullMasterDataFund.INSTANCE));
        response.instrumentAllocationses = Collections.nCopies(numInstruments, InstrumentAllocations.create(null));
        response.morningstarRatings = Collections.nCopies(numInstruments, null);
        response.feriRatings = Collections.nCopies(numInstruments, null);
        response.benchmarksList = Collections.nCopies(numInstruments, Collections.<InstrumentAllocation>emptyList());
        response.feriPerformanceses = Collections.nCopies(numInstruments, new FeriPerformances());
        return response;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", masterData=").append(this.masterDataFunds);
        sb.append(", allocations=").append(this.instrumentAllocationses);
        sb.append(", msRatings=").append(this.morningstarRatings);
        sb.append(", feriRatings=").append(this.feriRatings);
        sb.append(", feriPerformances=").append(this.feriPerformanceses);
        sb.append(", benchmarks=").append(this.benchmarksList);
    }

    public List<MasterDataFund> getMasterDataFunds() {
        return masterDataFunds;
    }

    public void setMasterDataFunds(List<MasterDataFund> masterDataFunds) {
        this.masterDataFunds = masterDataFunds;
    }

    public List<InstrumentAllocations> getInstrumentAllocationses() {
        return instrumentAllocationses;
    }

    public void setInstrumentAllocationses(List<InstrumentAllocations> instrumentAllocationses) {
        this.instrumentAllocationses = instrumentAllocationses;
    }

    public List<Integer> getMorningstarRatings() {
        return morningstarRatings;
    }

    public void setMorningstarRatings(List<Integer> morningstarRatings) {
        this.morningstarRatings = morningstarRatings;
    }

    public List<String> getFeriRatings() {
        return feriRatings;
    }

    public void setFeriRatings(List<String> feriRatings) {
        this.feriRatings = feriRatings;
    }

    public List<List<InstrumentAllocation>> getBenchmarksList() {
        return benchmarksList;
    }

    public void setBenchmarksList(List<List<InstrumentAllocation>> benchmarksList) {
        this.benchmarksList = benchmarksList;
    }

    public List<FeriPerformances> getFeriPerformanceses() {
        return feriPerformanceses;
    }

    public void setFeriPerformanceses(List<FeriPerformances> feriPerformanceses) {
        this.feriPerformanceses = feriPerformanceses;
    }
}
