/*
 * AnnualReportDataImpl.java
 *
 * Created on 17.03.2010 15:56:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.domain.data.AnnualReportAssets;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class AnnualReportAssetsImpl implements AnnualReportAssets, Serializable {
    protected static final long serialVersionUID = 1L;

    public static class Builder {
        private BigDecimal intangibleAssets;

        private BigDecimal tangibleAssets;

        private BigDecimal financialAssets;

        private BigDecimal fixedAssets;

        private BigDecimal inventories;

        private BigDecimal traceAccountsReceivable;

        private BigDecimal otherAccountsReceivable;

        private BigDecimal cashAndEquivalents;

        private BigDecimal otherCurrentAssets;

        private BigDecimal cash;

        private BigDecimal governmentDebtSecurities;

        private BigDecimal dueFromBanks;

        private BigDecimal dueFromCustomers;

        private BigDecimal debenturesAndOtherFixedIncomes;

        private BigDecimal numberOfSharesInThousand;

        private BigDecimal equityAndNonFixedIncome;

        private BigDecimal otherFinancialInvestments;

        private BigDecimal totalBanking;

        private BigDecimal investmentsInsuranceBusiness;

        private BigDecimal investmentsNotAtOwnRisk;

        private BigDecimal Receivables;

        private BigDecimal totalInsurance;

        private BigDecimal tangibleAndIntangibleAssets;

        private BigDecimal otherAssets;

        private BigDecimal prepaymentAndAccruedIncome;

        private BigDecimal totalAssets;

        private boolean isProvisional;

        public AnnualReportAssetsImpl build() {
            return new AnnualReportAssetsImpl(this);
        }

        public void setIntangibleAssets(BigDecimal intangibleAssets) {
            this.intangibleAssets = intangibleAssets;
        }

        public void setTangibleAssets(BigDecimal tangibleAssets) {
            this.tangibleAssets = tangibleAssets;
        }

        public void setFinancialAssets(BigDecimal financialAssets) {
            this.financialAssets = financialAssets;
        }

        public void setFixedAssets(BigDecimal fixedAssets) {
            this.fixedAssets = fixedAssets;
        }

        public void setInventories(BigDecimal inventories) {
            this.inventories = inventories;
        }

        public void setTraceAccountsReceivable(BigDecimal traceAccountsReceivable) {
            this.traceAccountsReceivable = traceAccountsReceivable;
        }

        public void setOtherAccountsReceivable(BigDecimal otherAccountsReceivable) {
            this.otherAccountsReceivable = otherAccountsReceivable;
        }

        public void setCashAndEquivalents(BigDecimal cashAndEquivalents) {
            this.cashAndEquivalents = cashAndEquivalents;
        }

        public void setOtherCurrentAssets(BigDecimal otherCurrentAssets) {
            this.otherCurrentAssets = otherCurrentAssets;
        }

        public void setCash(BigDecimal cash) {
            this.cash = cash;
        }

        public void setGovernmentDebtSecurities(BigDecimal governmentDebtSecurities) {
            this.governmentDebtSecurities = governmentDebtSecurities;
        }

        public void setDueFromBanks(BigDecimal dueFromBanks) {
            this.dueFromBanks = dueFromBanks;
        }

        public void setDueFromCustomers(BigDecimal dueFromCustomers) {
            this.dueFromCustomers = dueFromCustomers;
        }

        public void setDebenturesAndOtherFixedIncomes(BigDecimal debenturesAndOtherFixedIncomes) {
            this.debenturesAndOtherFixedIncomes = debenturesAndOtherFixedIncomes;
        }

        public void setNumberOfSharesInThousand(BigDecimal numberOfSharesInThousand) {
            this.numberOfSharesInThousand = numberOfSharesInThousand;
        }

        public void setEquityAndNonFixedIncome(BigDecimal equityAndNonFixedIncome) {
            this.equityAndNonFixedIncome = equityAndNonFixedIncome;
        }

        public void setOtherFinancialInvestments(BigDecimal otherFinancialInvestments) {
            this.otherFinancialInvestments = otherFinancialInvestments;
        }

        public void setTotalBanking(BigDecimal totalBanking) {
            this.totalBanking = totalBanking;
        }

        public void setInvestmentsInsuranceBusiness(BigDecimal investmentsInsuranceBusiness) {
            this.investmentsInsuranceBusiness = investmentsInsuranceBusiness;
        }

        public void setInvestmentsNotAtOwnRisk(BigDecimal investmentsNotAtOwnRisk) {
            this.investmentsNotAtOwnRisk = investmentsNotAtOwnRisk;
        }

        public void setReceivables(BigDecimal receivables) {
            Receivables = receivables;
        }

        public void setTotalInsurance(BigDecimal totalInsurance) {
            this.totalInsurance = totalInsurance;
        }

        public void setTangibleAndIntangibleAssets(BigDecimal tangibleAndIntangibleAssets) {
            this.tangibleAndIntangibleAssets = tangibleAndIntangibleAssets;
        }

        public void setOtherAssets(BigDecimal otherAssets) {
            this.otherAssets = otherAssets;
        }

        public void setPrepaymentAndAccruedIncome(BigDecimal prepaymentAndAccruedIncome) {
            this.prepaymentAndAccruedIncome = prepaymentAndAccruedIncome;
        }

        public void setTotalAssets(BigDecimal totalAssets) {
            this.totalAssets = totalAssets;
        }

        public void setIsProvisional(boolean provisional) {
            isProvisional = provisional;
        }
    }

    private final BigDecimal intangibleAssets;

    private final BigDecimal tangibleAssets;

    private final BigDecimal financialAssets;

    private final BigDecimal fixedAssets;

    private final BigDecimal inventories;

    private final BigDecimal traceAccountsReceivable;

    private final BigDecimal otherAccountsReceivable;

    private final BigDecimal cashAndEquivalents;

    private final BigDecimal otherCurrentAssets;

    private final BigDecimal cash;

    private final BigDecimal governmentDebtSecurities;

    private final BigDecimal dueFromBanks;

    private final BigDecimal dueFromCustomers;

    private final BigDecimal debenturesAndOtherFixedIncomes;

    private final BigDecimal numberOfSharesInThousand;

    private final BigDecimal equityAndNonFixedIncome;

    private final BigDecimal otherFinancialInvestments;

    private final BigDecimal totalBanking;

    private final BigDecimal investmentsInsuranceBusiness;

    private final BigDecimal investmentsNotAtOwnRisk;

    private final BigDecimal Receivables;

    private final BigDecimal totalInsurance;

    private final BigDecimal tangibleAndIntangibleAssets;

    private final BigDecimal otherAssets;

    private final BigDecimal prepaymentAndAccruedIncome;

    private final BigDecimal totalAssets;

    private final boolean isProvisional;

    public AnnualReportAssetsImpl(Builder b) {
        this.intangibleAssets = b.intangibleAssets;
        this.tangibleAssets = b.tangibleAssets;
        this.financialAssets = b.financialAssets;
        this.fixedAssets = b.fixedAssets;
        this.inventories = b.inventories;
        this.traceAccountsReceivable = b.traceAccountsReceivable;
        this.otherAccountsReceivable = b.otherAccountsReceivable;
        this.cashAndEquivalents = b.cashAndEquivalents;
        this.otherCurrentAssets = b.otherCurrentAssets;
        this.cash = b.cash;
        this.governmentDebtSecurities = b.governmentDebtSecurities;
        this.dueFromBanks = b.dueFromBanks;
        this.dueFromCustomers = b.dueFromCustomers;
        this.debenturesAndOtherFixedIncomes = b.debenturesAndOtherFixedIncomes;
        this.numberOfSharesInThousand = b.numberOfSharesInThousand;
        this.equityAndNonFixedIncome = b.equityAndNonFixedIncome;
        this.otherFinancialInvestments = b.otherFinancialInvestments;
        this.totalBanking = b.totalBanking;
        this.investmentsInsuranceBusiness = b.investmentsInsuranceBusiness;
        this.investmentsNotAtOwnRisk = b.investmentsNotAtOwnRisk;
        this.Receivables = b.Receivables;
        this.totalInsurance = b.totalInsurance;
        this.tangibleAndIntangibleAssets = b.tangibleAndIntangibleAssets;
        this.otherAssets = b.otherAssets;
        this.prepaymentAndAccruedIncome = b.prepaymentAndAccruedIncome;
        this.totalAssets = b.totalAssets;
        this.isProvisional = b.isProvisional;
    }

    public BigDecimal getIntangibleAssets() {
        return intangibleAssets;
    }

    public BigDecimal getTangibleAssets() {
        return tangibleAssets;
    }

    public BigDecimal getFinancialAssets() {
        return financialAssets;
    }

    public BigDecimal getFixedAssets() {
        return fixedAssets;
    }

    public BigDecimal getInventories() {
        return inventories;
    }

    public BigDecimal getTraceAccountsReceivable() {
        return traceAccountsReceivable;
    }

    public BigDecimal getOtherAccountsReceivable() {
        return otherAccountsReceivable;
    }

    public BigDecimal getCashAndEquivalents() {
        return cashAndEquivalents;
    }

    public BigDecimal getOtherCurrentAssets() {
        return otherCurrentAssets;
    }

    public BigDecimal getCash() {
        return cash;
    }

    public BigDecimal getGovernmentDebtSecurities() {
        return governmentDebtSecurities;
    }

    public BigDecimal getDueFromBanks() {
        return dueFromBanks;
    }

    public BigDecimal getDueFromCustomers() {
        return dueFromCustomers;
    }

    public BigDecimal getDebenturesAndOtherFixedIncomes() {
        return debenturesAndOtherFixedIncomes;
    }

    public BigDecimal getNumberOfSharesInThousand() {
        return numberOfSharesInThousand;
    }

    public BigDecimal getEquityAndNonFixedIncome() {
        return equityAndNonFixedIncome;
    }

    public BigDecimal getOtherFinancialInvestments() {
        return otherFinancialInvestments;
    }

    public BigDecimal getTotalBanking() {
        return totalBanking;
    }

    public BigDecimal getInvestmentsInsuranceBusiness() {
        return investmentsInsuranceBusiness;
    }

    public BigDecimal getInvestmentsNotAtOwnRisk() {
        return investmentsNotAtOwnRisk;
    }

    public BigDecimal getReceivables() {
        return Receivables;
    }

    public BigDecimal getTotalInsurance() {
        return totalInsurance;
    }

    public BigDecimal getTangibleAndIntangibleAssets() {
        return tangibleAndIntangibleAssets;
    }

    public BigDecimal getOtherAssets() {
        return otherAssets;
    }

    public BigDecimal getPrepaymentAndAccruedIncome() {
        return prepaymentAndAccruedIncome;
    }

    public BigDecimal getTotalAssets() {
        return totalAssets;
    }

    public boolean isProvisional() {
        return isProvisional;
    }

    @Override
    public String toString() {
        return "AnnualReportAssetsImpl{" +
                "intangibleAssets=" + intangibleAssets +
                ", tangibleAssets=" + tangibleAssets +
                ", financialAssets=" + financialAssets +
                ", fixedAssets=" + fixedAssets +
                ", inventories=" + inventories +
                ", traceAccountsReceivable=" + traceAccountsReceivable +
                ", otherAccountsReceivable=" + otherAccountsReceivable +
                ", cashAndEquivalents=" + cashAndEquivalents +
                ", otherCurrentAssets=" + otherCurrentAssets +
                ", cash=" + cash +
                ", governmentDebtSecurities=" + governmentDebtSecurities +
                ", dueFromBanks=" + dueFromBanks +
                ", dueFromCustomers=" + dueFromCustomers +
                ", debenturesAndOtherFixedIncomes=" + debenturesAndOtherFixedIncomes +
                ", numberOfSharesInThousand=" + numberOfSharesInThousand +
                ", equityAndNonFixedIncome=" + equityAndNonFixedIncome +
                ", otherFinancialInvestments=" + otherFinancialInvestments +
                ", totalBanking=" + totalBanking +
                ", investmentsInsuranceBusiness=" + investmentsInsuranceBusiness +
                ", investmentsNotAtOwnRisk=" + investmentsNotAtOwnRisk +
                ", Receivables=" + Receivables +
                ", totalInsurance=" + totalInsurance +
                ", tangibleAndIntangibleAssets=" + tangibleAndIntangibleAssets +
                ", otherAssets=" + otherAssets +
                ", prepaymentAndAccruedIncome=" + prepaymentAndAccruedIncome +
                ", totalAssets=" + totalAssets +
                ", isProvisional=" + isProvisional +
                '}';
    }
}