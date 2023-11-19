package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;

/**
 * @author mcoenen
 */
class CompanyInfo implements Serializable {

    static final long serialVersionUID = 1L;

    static final List<String> ENABLED_DETAILS =
            ImmutableList.<String>builder()
                    .add("companyGuidance")

                    // IMPORTANT: If the following will be enabled above company guidance can no
                    // longer be easily extracted from SOAP response.
                    // LbbwSOAPClientImpl#getCompanyInfo(Node) needs to be adjusted then.
//                    .add("companyProfile")

                    .add("instrument")
                    // The next three flags ONLY work if above instrument is also enabled
                    .add("isin")
//                    .add("rating")
//                    .add("targetPrice")

//                    .add("latestUpdate")
//                    .add("investmentCase")
//                    .add("latestComment")
//                    .add("prosCons")
//                    .add("currentQuote")
                    .build();

    private String objectId;

    private String isin;

    private String country;

    String name;

    private transient String companyGuidance;

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Optional<String> getCountry() {
        return Optional.ofNullable(country);
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public Optional<String> getCompanyGuidance() {
        return Optional.ofNullable(companyGuidance);
    }

    public void setCompanyGuidance(String companyGuidance) {
        this.companyGuidance = companyGuidance;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CompanyInfo{" +
                "objectId='" + objectId + '\'' +
                ", isin='" + isin + '\'' +
                ", name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", companyGuidance='" + companyGuidance + '\'' +
                '}';
    }
}
