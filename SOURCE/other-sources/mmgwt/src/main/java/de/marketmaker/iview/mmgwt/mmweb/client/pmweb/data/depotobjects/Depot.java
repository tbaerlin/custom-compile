/*
 * Depot.java
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

import java.util.List;

/**
 * @author Michael Lösch
 */
public class Depot implements ContextItem, HasUserDefinedFields, DepotObject {
    public static class DepotsTalker extends AbstractMmTalker<DatabaseIdQuery, List<Depot>, Depot> {

        public DepotsTalker() {
            this("Depot"); // $NON-NLS$
        }

        protected DepotsTalker(String formula) {
            super(formula);
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        public MmTalkWrapper<Depot> createWrapper(Formula formula) {
            final MmTalkWrapper<Depot> cols = MmTalkWrapper.create(formula, Depot.class);
            cols.appendColumnMapper(new MmTalkColumnMapper<Depot>("Id") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.id = MmTalkHelper.asMMNumber(item).getValue();
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Zone") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.zone = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Depotnummer") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.depotNumber = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Typ") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.type = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Name") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.name = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Inhaber.Id") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.investorId = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Inhaber.Name") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.investorName = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Portfolio.Id") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.portfolioId = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Portfolio.Name") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.portfolioName = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Identifier") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.identifier = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Bemerkung") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.comment = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Bank.Name") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.bankName = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("Währung.Kürzel") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.currency = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("AngelegtAm") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.creationDate = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("DeactivatedOn") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.deactivationDate = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("((DeactivatedOn <> \"n/a\" and DeactivatedOn <= heute).As[\"Boolean\"])") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.deactivated = MmTalkHelper.asBoolean(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("VerificationStatus") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.dataStatus = MmTalkHelper.asEnum(MMTypRefType.TRT_VERIFY_STATUS, VerificationStatus.values(), item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("VerificationDate") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.dataStatusDate = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("VerificationStatusTotal") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.dataStatusTotal = MmTalkHelper.asEnum(MMTypRefType.TRT_VERIFY_STATUS, VerificationStatus.values(), item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("VerificationDateTotal") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.dataStatusTotalDate = MmTalkHelper.asString(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("AusschlussSteuer") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.excludeFromTaxAnalysis = MmTalkHelper.asBoolean(item);
                }
            }).appendColumnMapper(new MmTalkColumnMapper<Depot>("PerformanceStartDate") { // $NON-NLS$
                @Override
                public void setValue(Depot d, MM item) {
                    d.performanceCalculationFromDate = MmTalkHelper.asString(item);
                }
            }).appendNodeMapper(new MmTalkNodeMapper<Depot, Account>(Account.createWrapper("Konto")) { // $NON-NLS$
                @Override
                public void setValue(Depot depot, MmTalkWrapper<Account> wrapper, MMTable table) {
                    depot.standardSettlementAccount = wrapper.createResultObject(table);
                }
            }).appendNodeMapper(new MmTalkNodeMapper<Depot, Bank>(Bank.createWrapper("Bank")) { // $NON-NLS$
                @Override
                public void setValue(Depot depot, MmTalkWrapper<Bank> wrapper, MMTable table) {
                    depot.bank = wrapper.createResultObject(table);
                }
            }).appendNodeMapper(new MmTalkNodeMapper<Depot, CommissionScale>(CommissionScale.createWrapper("OrderProvisionsSchema")) { // $NON-NLS$
                @Override
                public void setValue(Depot depot, MmTalkWrapper<CommissionScale> wrapper, MMTable table) {
                    depot.commissionScale = wrapper.createResultObject(table);
                }
            });
            return cols;
        }

        @Override
        public List<Depot> createResultObject(MMTalkResponse response) {
            return this.wrapper.createResultObjectList(response);
        }
    }

    public static class OnlyActiveDepotsTalker extends DepotsTalker {
        public OnlyActiveDepotsTalker() {
            super("Depot.DeleteIf[#[]((DeactivatedOn <> \"n/a\" and DeactivatedOn <= heute).As[\"Boolean\"])] "); // $NON-NLS$
        }
    }

    private String id;
    private String zone;
    private String name;
    private String investorId;
    private String investorName;
    private String portfolioId;
    private String portfolioName;
    private String depotNumber;
    private String type;
    private String identifier;
    private String comment;
    private String currency;
    private String creationDate;
    private String bankName;
    private String deactivationDate;
    private Boolean deactivated;
    private VerificationStatus dataStatus;
    private VerificationStatus dataStatusTotal;
    private String dataStatusDate;
    private String dataStatusTotalDate;
    private String performanceCalculationFromDate;
    private Boolean excludeFromTaxAnalysis;
    private Account standardSettlementAccount;
    private Bank bank;
    private CommissionScale commissionScale;
    private UserDefinedFields userDefinedFields;
    private AlertsResponse alerts;

    public Depot() {
    }

    public String getId() {
        return this.id;
    }

    public String getZone() {
        return zone;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String getComment() {
        return this.comment;
    }

    public String getPerformanceCalculationFromDate() {
        return performanceCalculationFromDate;
    }

    public String getDepotNumber() {
        return this.depotNumber;
    }

    public String getCurrency() {
        return this.currency;
    }

    public String getCreationDate() {
        return this.creationDate;
    }

    public String getBankName() {
        return this.bankName;
    }

    public String getDeactivationDate() {
        return deactivationDate;
    }

    public boolean isDeactivated() {
        return this.deactivated != null && this.deactivated;
    }

    public VerificationStatus getDataStatus() {
        return this.dataStatus;
    }

    public Account getStandardSettlementAccount() {
        return standardSettlementAccount;
    }

    public Bank getBank() {
        return bank;
    }

    public Boolean isExcludeFromTaxAnalysis() {
        return excludeFromTaxAnalysis;
    }

    public CommissionScale getCommissionScale() {
        return commissionScale;
    }

    public String getInvestorName() {
        return investorName;
    }

    public String getDataStatusDate() {
        return dataStatusDate;
    }

    public VerificationStatus getDataStatusTotal() {
        return this.dataStatusTotal;
    }

    public String getDataStatusTotalDate() {
        return this.dataStatusTotalDate;
    }

    public String getInvestorId() {
        return investorId;
    }

    public String getPortfolioId() {
        return portfolioId;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public UserDefinedFields getUserDefinedFields() {
        return userDefinedFields;
    }

    public void setUserDefinedFields(UserDefinedFields userDefinedFields) {
        this.userDefinedFields = userDefinedFields;
    }

    public Depot withAlerts(AlertsResponse alerts) {
        this.alerts = alerts;
        return this;
    }

    public AlertsResponse getAlertResponse() {
        return alerts;
    }

    @Override
    public ShellMMType getShellMMType() {
        return ShellMMType.ST_DEPOT;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Depot depot = (Depot) o;

        if (alerts != null ? !alerts.equals(depot.alerts) : depot.alerts != null) return false;
        if (bank != null ? !bank.equals(depot.bank) : depot.bank != null) return false;
        if (bankName != null ? !bankName.equals(depot.bankName) : depot.bankName != null) return false;
        if (comment != null ? !comment.equals(depot.comment) : depot.comment != null) return false;
        if (commissionScale != null ? !commissionScale.equals(depot.commissionScale) : depot.commissionScale != null)
            return false;
        if (creationDate != null ? !creationDate.equals(depot.creationDate) : depot.creationDate != null) return false;
        if (currency != null ? !currency.equals(depot.currency) : depot.currency != null) return false;
        if (dataStatus != depot.dataStatus) return false;
        if (dataStatusDate != null ? !dataStatusDate.equals(depot.dataStatusDate) : depot.dataStatusDate != null)
            return false;
        if (dataStatusTotal != depot.dataStatusTotal) return false;
        if (dataStatusTotalDate != null ? !dataStatusTotalDate.equals(depot.dataStatusTotalDate) : depot.dataStatusTotalDate != null)
            return false;
        if (deactivated != null ? !deactivated.equals(depot.deactivated) : depot.deactivated != null) return false;
        if (deactivationDate != null ? !deactivationDate.equals(depot.deactivationDate) : depot.deactivationDate != null)
            return false;
        if (depotNumber != null ? !depotNumber.equals(depot.depotNumber) : depot.depotNumber != null) return false;
        if (excludeFromTaxAnalysis != null ? !excludeFromTaxAnalysis.equals(depot.excludeFromTaxAnalysis) : depot.excludeFromTaxAnalysis != null)
            return false;
        if (id != null ? !id.equals(depot.id) : depot.id != null) return false;
        if (identifier != null ? !identifier.equals(depot.identifier) : depot.identifier != null) return false;
        if (investorId != null ? !investorId.equals(depot.investorId) : depot.investorId != null) return false;
        if (investorName != null ? !investorName.equals(depot.investorName) : depot.investorName != null) return false;
        if (name != null ? !name.equals(depot.name) : depot.name != null) return false;
        if (performanceCalculationFromDate != null ? !performanceCalculationFromDate.equals(depot.performanceCalculationFromDate) : depot.performanceCalculationFromDate != null)
            return false;
        if (portfolioId != null ? !portfolioId.equals(depot.portfolioId) : depot.portfolioId != null) return false;
        if (portfolioName != null ? !portfolioName.equals(depot.portfolioName) : depot.portfolioName != null)
            return false;
        if (standardSettlementAccount != null ? !standardSettlementAccount.equals(depot.standardSettlementAccount) : depot.standardSettlementAccount != null)
            return false;
        if (type != null ? !type.equals(depot.type) : depot.type != null) return false;
        if (userDefinedFields != null ? !userDefinedFields.equals(depot.userDefinedFields) : depot.userDefinedFields != null)
            return false;
        if (zone != null ? !zone.equals(depot.zone) : depot.zone != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (investorId != null ? investorId.hashCode() : 0);
        result = 31 * result + (investorName != null ? investorName.hashCode() : 0);
        result = 31 * result + (portfolioId != null ? portfolioId.hashCode() : 0);
        result = 31 * result + (portfolioName != null ? portfolioName.hashCode() : 0);
        result = 31 * result + (depotNumber != null ? depotNumber.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (bankName != null ? bankName.hashCode() : 0);
        result = 31 * result + (deactivationDate != null ? deactivationDate.hashCode() : 0);
        result = 31 * result + (deactivated != null ? deactivated.hashCode() : 0);
        result = 31 * result + (dataStatus != null ? dataStatus.hashCode() : 0);
        result = 31 * result + (dataStatusTotal != null ? dataStatusTotal.hashCode() : 0);
        result = 31 * result + (dataStatusDate != null ? dataStatusDate.hashCode() : 0);
        result = 31 * result + (dataStatusTotalDate != null ? dataStatusTotalDate.hashCode() : 0);
        result = 31 * result + (performanceCalculationFromDate != null ? performanceCalculationFromDate.hashCode() : 0);
        result = 31 * result + (excludeFromTaxAnalysis != null ? excludeFromTaxAnalysis.hashCode() : 0);
        result = 31 * result + (standardSettlementAccount != null ? standardSettlementAccount.hashCode() : 0);
        result = 31 * result + (bank != null ? bank.hashCode() : 0);
        result = 31 * result + (commissionScale != null ? commissionScale.hashCode() : 0);
        result = 31 * result + (userDefinedFields != null ? userDefinedFields.hashCode() : 0);
        result = 31 * result + (alerts != null ? alerts.hashCode() : 0);
        return result;
    }
}