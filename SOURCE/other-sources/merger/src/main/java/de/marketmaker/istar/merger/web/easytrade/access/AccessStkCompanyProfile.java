package de.marketmaker.istar.merger.web.easytrade.access;

import de.marketmaker.istar.domain.data.CompanyProfile;

/**
 * @author zzhao
 */
public class AccessStkCompanyProfile extends AccessSymbolAndPredicate {

  public AccessStkCompanyProfile() {
    super("profile", obj -> {
      final CompanyProfile companyProfile = (CompanyProfile) obj;
      return companyProfile != CompanyProfile.NULL_INSTANCE && companyProfile.getInstrumentid() > 0;
    });
  }
}
