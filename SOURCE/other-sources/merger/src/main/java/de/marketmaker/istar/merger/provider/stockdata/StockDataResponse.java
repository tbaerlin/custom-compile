package de.marketmaker.istar.merger.provider.stockdata;

import java.util.List;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.AnnualReportData;
import de.marketmaker.istar.domain.data.CompanyProfile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockDataResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private List<CompanyProfile> companyProfiles;
    private List<List<AnnualReportData>> annualReportsDatas;

    public List<CompanyProfile> getCompanyProfiles() {
        return companyProfiles;
    }

    public void setCompanyProfiles(List<CompanyProfile> companyProfiles) {
        this.companyProfiles = companyProfiles;
    }

    public List<List<AnnualReportData>> getAnnualReportsDatas() {
        return annualReportsDatas;
    }

    public void setAnnualReportsDatas(List<List<AnnualReportData>> annualReportsDatas) {
        this.annualReportsDatas = annualReportsDatas;
    }
}