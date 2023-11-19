package de.marketmaker.istar.analyses.analyzer;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * data model for a security/instrument
 */
public class Security {

    // primary id,
    private final long iid;

    // might be needed in user queries
    private final String symbol;

    // 1-n relationships
    private final Set<Analysis> analyses = new TreeSet<>(Analysis.ID_COMPARATOR);
    // n-n relationships, there is a set of securities in the index class
    private final Set<Index> indices = new TreeSet<>(Index.QID_COMPARATOR);

    // provider resolved data / on bootstrap derived from home-market
    private Long qid;
    private String vwdCode;
    private InstrumentTypeEnum type;
    private String currency;

    // retrieved from ratios
    private String industry;
    private String sector;
    private String country;
    private String name;
    private String industryGroup;
    private String subIndustry;

    public static final Comparator<Security> IID_COMPARATOR
            = (left, right) -> ComparisonChain.start().compare(left.iid, right.iid).result();

    Security(long iid, String symbol) {
        assert symbol != null : "symbol for security must not be null";
        this.iid = iid;
        this.symbol = symbol;
    }

    public long getIid() {
        return iid;
    }

    public String getSymbol() {
        return symbol;
    }

    public void put(Analysis analysis) {
        analyses.add(analysis);
    }

    public Collection<Analysis> getAnalyses() {
        return analyses;
    }

    public void put(Index index) {
        indices.add(index);
    }

    public Set<Index> getIndices() {
        return indices;
    }



    public void setQid(long qid) {
        this.qid = qid;
    }

    public Long getQid() {
        return qid;
    }

    public void setVwdCode(String vwdCode) {
        this.vwdCode = vwdCode;
    }

    public String getVwdCode() {
        return vwdCode;
    }

    public void setType(InstrumentTypeEnum type) {
        this.type = type;
    }

    public InstrumentTypeEnum getType() {
        return type;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getSector() {
        return sector;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIndustryGroup(String industryGroup) {
        this.industryGroup = industryGroup;
    }

    public String getIndustryGroup() {
        return industryGroup;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getIndustry() {
        return industry;
    }

    public void setSubIndustry(String subIndustry) {
        this.subIndustry = subIndustry;
    }

    public String getSubIndustry() {
        return subIndustry;
    }


    @Override
    public String toString() {
        return "Security [" + iid  + "/" + "'" + symbol + "'"
                + " is in " + analyses.size() + " analyses"
                + " and in " + indices.size() + " indices"
                + "]";
    }

}
