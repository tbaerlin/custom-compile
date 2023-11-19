/*
 * PortfolioProfileBase.java
 *
 * Created on 22.03.13 16:37
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.ReportingFrequenz;

import java.util.List;

/**
 * @author Markus Dick
 */
public class PortfolioProfileBase {
    private String id;
    private Advisor advisor;
    private String allocationComments;
    private String assetAllocationName;
    private String analysisCurrency;
    private String generalComment;
    private List<MMTalkStringListEntryNode> restrictions;
    private String investmentAgentName;
    private Boolean financialPortfolioManagement;
    private ReportingFrequenz reportingFrequency;
    private String riskLimit;
    private Boolean scheduledReportingActive;
    private String scheduledReportingProfileName;
    private String lossThreshold;
    private String benchmarkSecurityName;
    private String benchmark2SecurityName;

    protected static <T extends PortfolioProfileBase> MmTalkWrapper<T> appendMappers(MmTalkWrapper<T> cols) {
        cols.appendColumnMapper(new MmTalkColumnMapper<T>("Id") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).id = MmTalkHelper.asMMNumber(item).getValue();
            }
        }).appendNodeMapper(new MmTalkNodeMapper<T, Advisor>(Advisor.createWrapper("Advisor")) { // $NON-NLS$
            @Override
            public void setValue(T t, MmTalkWrapper<Advisor> wrapper, MMTable table) {
                ((PortfolioProfileBase)t).advisor = wrapper.createResultObject(table);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("AllocationNotes") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).allocationComments = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("AssetAllocation.Name") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).assetAllocationName = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("AuswertungsWährung.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).analysisCurrency = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Bemerkung") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).generalComment = MmTalkHelper.asString(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<T, MMTalkStringListEntryNode>(
                MMTalkStringListEntryNode.createWrapper("BestandsRestriktionen")) { // $NON-NLS$
            @Override
            public void setValue(T t, MmTalkWrapper<MMTalkStringListEntryNode> wrapper, MMTable table) {
                ((PortfolioProfileBase)t).restrictions = wrapper.createResultObjectList(table);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("InvestmentAgentName") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).investmentAgentName = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("ReportingFrequenz") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).reportingFrequency = MmTalkHelper.asEnum(MMTypRefType.TRT_REPORTING_FREQUENZ, ReportingFrequenz.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("RiskLimit") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).riskLimit = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Scheduledreporting_Aktiv") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).scheduledReportingActive = MmTalkHelper.asBoolean(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Scheduledreporting_ProfileName") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).scheduledReportingProfileName = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Verlustgrenze") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).lossThreshold = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Benchmark.Name") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).benchmarkSecurityName = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Benchmark2.Name") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).benchmark2SecurityName = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Finanzportfolioverwaltung") { // $NON-NLS$
            @Override
            public void setValue(T t, MM item) {
                ((PortfolioProfileBase)t).financialPortfolioManagement = MmTalkHelper.asBoolean(item);
            }
        });
        return cols;
    }

    public String getId() {
        return id;
    }

    public Advisor getAdvisor() {
        return advisor;
    }

    public String getAllocationComments() {
        return allocationComments;
    }

    public String getAssetAllocationName() {
        return assetAllocationName;
    }

    public String getAnalysisCurrency() {
        return analysisCurrency;
    }

    public String getGeneralComment() {
        return generalComment;
    }

    public List<MMTalkStringListEntryNode> getRestrictions() {
        return restrictions;
    }

    public String getInvestmentAgentName() {
        return investmentAgentName;
    }

    public Boolean isFinancialPortfolioManagement() {
        return financialPortfolioManagement;
    }

    public ReportingFrequenz getReportingFrequency() {
        return reportingFrequency;
    }

    public String getRiskLimit() {
        return riskLimit;
    }

    public Boolean isScheduledReportingActive() {
        return scheduledReportingActive;
    }

    public String getScheduledReportingProfileName() {
        return scheduledReportingProfileName;
    }

    public String getLossThreshold() {
        return lossThreshold;
    }

    public String getBenchmarkSecurityName() {
        return benchmarkSecurityName;
    }

    public String getBenchmark2SecurityName() {
        return benchmark2SecurityName;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PortfolioProfileBase that = (PortfolioProfileBase) o;

        if (advisor != null ? !advisor.equals(that.advisor) : that.advisor != null) {
            return false;
        }
        if (allocationComments != null ? !allocationComments.equals(that.allocationComments) : that.allocationComments != null) {
            return false;
        }
        if (analysisCurrency != null ? !analysisCurrency.equals(that.analysisCurrency) : that.analysisCurrency != null) {
            return false;
        }
        if (assetAllocationName != null ? !assetAllocationName.equals(that.assetAllocationName) : that.assetAllocationName != null) {
            return false;
        }
        if (benchmark2SecurityName != null ? !benchmark2SecurityName.equals(that.benchmark2SecurityName) : that.benchmark2SecurityName != null) {
            return false;
        }
        if (benchmarkSecurityName != null ? !benchmarkSecurityName.equals(that.benchmarkSecurityName) : that.benchmarkSecurityName != null) {
            return false;
        }
        if (financialPortfolioManagement != null ? !financialPortfolioManagement.equals(that.financialPortfolioManagement) : that.financialPortfolioManagement != null) {
            return false;
        }
        if (generalComment != null ? !generalComment.equals(that.generalComment) : that.generalComment != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (investmentAgentName != null ? !investmentAgentName.equals(that.investmentAgentName) : that.investmentAgentName != null) {
            return false;
        }
        if (lossThreshold != null ? !lossThreshold.equals(that.lossThreshold) : that.lossThreshold != null) {
            return false;
        }
        if (reportingFrequency != that.reportingFrequency) {
            return false;
        }
        if (restrictions != null ? !restrictions.equals(that.restrictions) : that.restrictions != null) {
            return false;
        }
        if (riskLimit != null ? !riskLimit.equals(that.riskLimit) : that.riskLimit != null) {
            return false;
        }
        if (scheduledReportingActive != null ? !scheduledReportingActive.equals(that.scheduledReportingActive) : that.scheduledReportingActive != null) {
            return false;
        }
        if (scheduledReportingProfileName != null ? !scheduledReportingProfileName.equals(that.scheduledReportingProfileName) : that.scheduledReportingProfileName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (advisor != null ? advisor.hashCode() : 0);
        result = 31 * result + (allocationComments != null ? allocationComments.hashCode() : 0);
        result = 31 * result + (assetAllocationName != null ? assetAllocationName.hashCode() : 0);
        result = 31 * result + (analysisCurrency != null ? analysisCurrency.hashCode() : 0);
        result = 31 * result + (generalComment != null ? generalComment.hashCode() : 0);
        result = 31 * result + (restrictions != null ? restrictions.hashCode() : 0);
        result = 31 * result + (investmentAgentName != null ? investmentAgentName.hashCode() : 0);
        result = 31 * result + (financialPortfolioManagement != null ? financialPortfolioManagement.hashCode() : 0);
        result = 31 * result + (reportingFrequency != null ? reportingFrequency.hashCode() : 0);
        result = 31 * result + (riskLimit != null ? riskLimit.hashCode() : 0);
        result = 31 * result + (scheduledReportingActive != null ? scheduledReportingActive.hashCode() : 0);
        result = 31 * result + (scheduledReportingProfileName != null ? scheduledReportingProfileName.hashCode() : 0);
        result = 31 * result + (lossThreshold != null ? lossThreshold.hashCode() : 0);
        result = 31 * result + (benchmarkSecurityName != null ? benchmarkSecurityName.hashCode() : 0);
        result = 31 * result + (benchmark2SecurityName != null ? benchmark2SecurityName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PortfolioProfileBase{" +  // $NON-NLS$
                "advisor=" + advisor +   // $NON-NLS$
                ", allocationComments='" + allocationComments + '\'' +   // $NON-NLS$
                ", assetAllocationName='" + assetAllocationName + '\'' + // $NON-NLS$
                ", analysisCurrency='" + analysisCurrency + '\'' +  // $NON-NLS$
                ", generalComment='" + generalComment + '\'' +   // $NON-NLS$
                ", restrictions=" + restrictions +  // $NON-NLS$
                ", investmentAgentName='" + investmentAgentName + '\'' + // $NON-NLS$
                ", financialPortfolioManagement=" + financialPortfolioManagement +   // $NON-NLS$
                ", reportingFrequency='" + reportingFrequency + '\'' + // $NON-NLS$
                ", riskLimit='" + riskLimit + '\'' + // $NON-NLS$
                ", scheduledReportingActive=" + scheduledReportingActive + // $NON-NLS$
                ", scheduledReportingProfileName='" + scheduledReportingProfileName + '\'' + // $NON-NLS$
                ", lossThreshold='" + lossThreshold + '\'' +  // $NON-NLS$
                ", benchmarkSecurityName='" + benchmarkSecurityName + '\'' +   // $NON-NLS$
                ", benchmark2SecurityName='" + benchmark2SecurityName + '\'' + // $NON-NLS$
                '}';
    }
}
