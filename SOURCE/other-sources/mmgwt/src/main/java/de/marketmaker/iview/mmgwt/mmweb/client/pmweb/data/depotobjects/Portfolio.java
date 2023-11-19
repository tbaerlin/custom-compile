/*
 * Portfolio.java
 *
 * Created on 18.12.12 08:08
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.AbstractMmTalker;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.AlertsResponse;
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.VerificationStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class Portfolio implements ContextItem, HasUserDefinedFields, DepotObject {
    public static final String EFFECTIVE_SINCE_PARAM = "effectiveSince"; //$NON-NLS$

    public static class PortfoliosTalker extends AbstractMmTalker<DatabaseIdQuery, List<Portfolio>, Portfolio> {
        public PortfoliosTalker() {
            super("Portfolio"); // $NON-NLS$
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        public void setEffectiveSince(String pmDate) {
            new EffectivePortfolioVersionMethod(pmDate, this.wrapper).invoke();
        }

        public MmTalkWrapper<Portfolio> createWrapper(Formula formula) {
            final MmTalkWrapper<Portfolio> cols = MmTalkWrapper.create(formula, Portfolio.class);

            cols.appendColumnMapper(new MmTalkColumnMapper<Portfolio>("Id") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.id = MmTalkHelper.asMMNumber(item).getValue();
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("Zone") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.zone = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("Name") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.name = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("PortfolioNumber") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.portfolioNumber = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("Typ") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.type = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("Inhaber.Id") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.investorId = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("Inhaber.Name") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.investorName = MmTalkHelper.asString(item);
                }
            }).appendNodeMapper(new MmTalkNodeMapper<Portfolio, Address>(Address.createWrapper("Inhaber.Adresse")) { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MmTalkWrapper<Address> wrapper, MMTable table) {
                    p.investorAddress = wrapper.createResultObject(table);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("CreatedOn") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.creationDate = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("PerformanceStartDate") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.performanceStartDate = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("InvestmentterminationDate") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.liquidateInvestmentDate = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("VerificationStatus") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.dataStatus = MmTalkHelper.asEnum(MMTypRefType.TRT_VERIFY_STATUS, VerificationStatus.values(), item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("VerificationDate") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.dataStatusDate = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("VerificationStatusTotal") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.dataStatusTotal = MmTalkHelper.asEnum(MMTypRefType.TRT_VERIFY_STATUS, VerificationStatus.values(), item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("VerificationDateTotal") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.dataStatusTotalDate = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("Letzte_ScheduledReportingBenachrichtigung") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.scheduledReportingLastNotification = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Portfolio>("Letzte_VerlustBenachrichtigung") { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MM item) {
                    p.lossThresholdLastNotification = MmTalkHelper.asString(item);
                }
            }).appendNodeMapper(new MmTalkNodeMapper<Portfolio, PortfolioVersion>(PortfolioVersion.createWrapper(
                    Formula.create("PortfolioVersion[$" + EFFECTIVE_SINCE_PARAM + "]") // $NON-NLS$
                            .withMmTalkParam(EFFECTIVE_SINCE_PARAM, MmTalkHelper.nowAsDIDateTime())
            )) {
                @Override
                public void setValue(Portfolio p, MmTalkWrapper<PortfolioVersion> pvw, MMTable table) {
                    p.portfolioVersion = pvw.createResultObject(table);
                }
            }).appendNodeMapper(new MmTalkNodeMapper<Portfolio, PortfolioVersionListItem>(PortfolioVersionListItem.createWrapper("PortfolioVersionList")) { // $NON-NLS$
                @Override
                public void setValue(Portfolio p, MmTalkWrapper<PortfolioVersionListItem> wrapper, MMTable table) {
                    p.portfolioVersionList = wrapper.createResultObjectList(table);
                }
            });
            return cols;
        }

        @Override
        public List<Portfolio> createResultObject(MMTalkResponse response) {
            return this.wrapper.createResultObjectList(response);
        }
    }

    private String id;
    private String zone;
    private String name;
    private String portfolioNumber;
    private String type;
    private String investorId;
    private String investorName;
    private Address investorAddress;
    private String creationDate;
    private String performanceStartDate;
    private String liquidateInvestmentDate;
    private PortfolioVersion portfolioVersion;
    private List<PortfolioVersionListItem> portfolioVersionList;
    private String lossThresholdLastNotification;
    private String scheduledReportingLastNotification;
    private String dataStatusDate;
    private VerificationStatus dataStatus;
    private String dataStatusTotalDate;
    private VerificationStatus dataStatusTotal;
    private UserDefinedFields userDefinedFields;
    private AlertsResponse alerts;

    private List<Account> accounts = new ArrayList<>();
    private List<Depot> depots = new ArrayList<>();

    public String getId() {
        return id;
    }

    @Override
    public String getZone() {
        return zone;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getPortfolioNumber() {
        return portfolioNumber;
    }

    public String getType() {
        return type;
    }

    public String getInvestorName() {
        return investorName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getPerformanceStartDate() {
        return performanceStartDate;
    }

    public String getLiquidateInvestmentDate() {
        return liquidateInvestmentDate;
    }

    public PortfolioVersion getPortfolioVersion() {
        return portfolioVersion;
    }

    public String getLossThresholdLastNotification() {
        return lossThresholdLastNotification;
    }

    public String getScheduledReportingLastNotification() {
        return scheduledReportingLastNotification;
    }

    public String getDataStatusDate() {
        return dataStatusDate;
    }

    public VerificationStatus getDataStatus() {
        return dataStatus;
    }

    public String getDataStatusTotalDate() {
        return dataStatusTotalDate;
    }

    public VerificationStatus getDataStatusTotal() {
        return dataStatusTotal;
    }

    public List<PortfolioVersionListItem> getPortfolioVersionList() {
        return portfolioVersionList;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Depot> getDepots() {
        return depots;
    }

    public String getInvestorId() {
        return investorId;
    }

    @Override
    public UserDefinedFields getUserDefinedFields() {
        return userDefinedFields;
    }

    @Override
    public void setUserDefinedFields(UserDefinedFields userDefinedFields) {
        this.userDefinedFields = userDefinedFields;
    }

    public Portfolio withAlerts(AlertsResponse alerts) {
        this.alerts = alerts;
        return this;
    }

    public AlertsResponse getAlertResponse() {
        return alerts;
    }

    @Override
    public ShellMMType getShellMMType() {
        return ShellMMType.ST_PORTFOLIO;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Portfolio portfolio = (Portfolio) o;

        if (accounts != null ? !accounts.equals(portfolio.accounts) : portfolio.accounts != null) return false;
        if (alerts != null ? !alerts.equals(portfolio.alerts) : portfolio.alerts != null) return false;
        if (creationDate != null ? !creationDate.equals(portfolio.creationDate) : portfolio.creationDate != null)
            return false;
        if (dataStatus != portfolio.dataStatus) return false;
        if (dataStatusDate != null ? !dataStatusDate.equals(portfolio.dataStatusDate) : portfolio.dataStatusDate != null)
            return false;
        if (dataStatusTotal != portfolio.dataStatusTotal) return false;
        if (dataStatusTotalDate != null ? !dataStatusTotalDate.equals(portfolio.dataStatusTotalDate) : portfolio.dataStatusTotalDate != null)
            return false;
        if (depots != null ? !depots.equals(portfolio.depots) : portfolio.depots != null) return false;
        if (id != null ? !id.equals(portfolio.id) : portfolio.id != null) return false;
        if (investorAddress != null ? !investorAddress.equals(portfolio.investorAddress) : portfolio.investorAddress != null)
            return false;
        if (investorId != null ? !investorId.equals(portfolio.investorId) : portfolio.investorId != null) return false;
        if (investorName != null ? !investorName.equals(portfolio.investorName) : portfolio.investorName != null)
            return false;
        if (liquidateInvestmentDate != null ? !liquidateInvestmentDate.equals(portfolio.liquidateInvestmentDate) : portfolio.liquidateInvestmentDate != null)
            return false;
        if (lossThresholdLastNotification != null ? !lossThresholdLastNotification.equals(portfolio.lossThresholdLastNotification) : portfolio.lossThresholdLastNotification != null)
            return false;
        if (name != null ? !name.equals(portfolio.name) : portfolio.name != null) return false;
        if (performanceStartDate != null ? !performanceStartDate.equals(portfolio.performanceStartDate) : portfolio.performanceStartDate != null)
            return false;
        if (portfolioNumber != null ? !portfolioNumber.equals(portfolio.portfolioNumber) : portfolio.portfolioNumber != null)
            return false;
        if (portfolioVersion != null ? !portfolioVersion.equals(portfolio.portfolioVersion) : portfolio.portfolioVersion != null)
            return false;
        if (portfolioVersionList != null ? !portfolioVersionList.equals(portfolio.portfolioVersionList) : portfolio.portfolioVersionList != null)
            return false;
        if (scheduledReportingLastNotification != null ? !scheduledReportingLastNotification.equals(portfolio.scheduledReportingLastNotification) : portfolio.scheduledReportingLastNotification != null)
            return false;
        if (type != null ? !type.equals(portfolio.type) : portfolio.type != null) return false;
        if (userDefinedFields != null ? !userDefinedFields.equals(portfolio.userDefinedFields) : portfolio.userDefinedFields != null)
            return false;
        if (zone != null ? !zone.equals(portfolio.zone) : portfolio.zone != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (portfolioNumber != null ? portfolioNumber.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (investorId != null ? investorId.hashCode() : 0);
        result = 31 * result + (investorName != null ? investorName.hashCode() : 0);
        result = 31 * result + (investorAddress != null ? investorAddress.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (performanceStartDate != null ? performanceStartDate.hashCode() : 0);
        result = 31 * result + (liquidateInvestmentDate != null ? liquidateInvestmentDate.hashCode() : 0);
        result = 31 * result + (portfolioVersion != null ? portfolioVersion.hashCode() : 0);
        result = 31 * result + (portfolioVersionList != null ? portfolioVersionList.hashCode() : 0);
        result = 31 * result + (lossThresholdLastNotification != null ? lossThresholdLastNotification.hashCode() : 0);
        result = 31 * result + (scheduledReportingLastNotification != null ? scheduledReportingLastNotification.hashCode() : 0);
        result = 31 * result + (dataStatusDate != null ? dataStatusDate.hashCode() : 0);
        result = 31 * result + (dataStatus != null ? dataStatus.hashCode() : 0);
        result = 31 * result + (dataStatusTotalDate != null ? dataStatusTotalDate.hashCode() : 0);
        result = 31 * result + (dataStatusTotal != null ? dataStatusTotal.hashCode() : 0);
        result = 31 * result + (userDefinedFields != null ? userDefinedFields.hashCode() : 0);
        result = 31 * result + (alerts != null ? alerts.hashCode() : 0);
        result = 31 * result + (accounts != null ? accounts.hashCode() : 0);
        result = 31 * result + (depots != null ? depots.hashCode() : 0);
        return result;
    }
}