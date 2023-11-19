package de.marketmaker.istar.merger.provider.stockdata;

import de.marketmaker.istar.domain.data.AnnualReportData;
import de.marketmaker.istar.domain.data.CompanyProfile;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;

import java.util.List;
import java.util.Objects;

public class CompanyDataProviderImpl implements CompanyDataProvider {

    private CompanyDataProvider companyDataProviderVwdBenl;

    private CompanyDataProvider companyDataProviderAfu;

    public void setCompanyDataProviderVwdBenl(CompanyDataProvider companyDataProviderVwdBenl) {
        this.companyDataProviderVwdBenl = companyDataProviderVwdBenl;
    }

    public void setCompanyDataProviderAfu(CompanyDataProvider companyDataProviderAfu) {
        this.companyDataProviderAfu = companyDataProviderAfu;
    }

    @Override
    public List<AnnualReportData> getAnnualReportData(CompanyDataRequest request) {
        final CompanyDataProvider provider = getProvider(request.getProfile());
        return Objects.isNull(provider) ? null : provider.getAnnualReportData(request);
    }

    @Override
    public CompanyProfile getCompanyProfile(CompanyDataRequest request) {
        final CompanyDataProvider provider = getProvider(request.getProfile());

        return Objects.isNull(provider) ? null : provider.getCompanyProfile(request);
    }

    private CompanyDataProvider getProvider(Profile profile) {
        if (profile.isAllowed(Selector.AFU_COMPANY_DATA)) {
            return this.companyDataProviderAfu;
        }
        return this.companyDataProviderVwdBenl;
    }
}
