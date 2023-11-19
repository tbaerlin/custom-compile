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

import de.marketmaker.istar.domain.data.AnnualReportLiabilities;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class AnnualReportLiabilitiesImpl implements AnnualReportLiabilities, Serializable {
    protected static final long serialVersionUID = 1L;

    public static class Builder {
        private BigDecimal shareholdersEquity;

        private BigDecimal preferentEquity;

        private BigDecimal minorityInterests;

        private BigDecimal groupEquity;

        private BigDecimal subordinatedLoans;

        private BigDecimal generalRiskAllowance;

        private BigDecimal liableCapital;

        private BigDecimal liabilitiesDueToBanks;

        private BigDecimal customerDepositsSavings;

        private BigDecimal customerDepositsOther;

        private BigDecimal debtSecurities;

        private BigDecimal totalBanking;

        private BigDecimal technicalProvisions;

        private BigDecimal technicalProvisionsWithInvestmentsNotForOwnRisk;

        private BigDecimal totalTechnicalProvisions;

        private BigDecimal depositsReceivedFromReinsurers;

        private BigDecimal totalInsurance;

        private BigDecimal provisions;

        private BigDecimal longTermDebts;

        private BigDecimal accountsPayable;

        private BigDecimal bank;

        private BigDecimal otherShortTermLiabilities;

        private BigDecimal shortTermLiabilities;

        private BigDecimal otherLiabilities;

        private BigDecimal accrualsAndDeferredIncome;

        private BigDecimal totalLiabilities;

        private boolean isProvisional;

        public AnnualReportLiabilitiesImpl build() {
            return new AnnualReportLiabilitiesImpl(this);
        }

        public void setShareholdersEquity(BigDecimal shareholdersEquity) {
            this.shareholdersEquity = shareholdersEquity;
        }

        public void setPreferentEquity(BigDecimal preferentEquity) {
            this.preferentEquity = preferentEquity;
        }

        public void setMinorityInterests(BigDecimal minorityInterests) {
            this.minorityInterests = minorityInterests;
        }

        public void setGroupEquity(BigDecimal groupEquity) {
            this.groupEquity = groupEquity;
        }

        public void setSubordinatedLoans(BigDecimal subordinatedLoans) {
            this.subordinatedLoans = subordinatedLoans;
        }

        public void setGeneralRiskAllowance(BigDecimal generalRiskAllowance) {
            this.generalRiskAllowance = generalRiskAllowance;
        }

        public void setLiableCapital(BigDecimal liableCapital) {
            this.liableCapital = liableCapital;
        }

        public void setLiabilitiesDueToBanks(BigDecimal liabilitiesDueToBanks) {
            this.liabilitiesDueToBanks = liabilitiesDueToBanks;
        }

        public void setCustomerDepositsSavings(BigDecimal customerDepositsSavings) {
            this.customerDepositsSavings = customerDepositsSavings;
        }

        public void setCustomerDepositsOther(BigDecimal customerDepositsOther) {
            this.customerDepositsOther = customerDepositsOther;
        }

        public void setDebtSecurities(BigDecimal debtSecurities) {
            this.debtSecurities = debtSecurities;
        }

        public void setTotalBanking(BigDecimal totalBanking) {
            this.totalBanking = totalBanking;
        }

        public void setTechnicalProvisions(BigDecimal technicalProvisions) {
            this.technicalProvisions = technicalProvisions;
        }

        public void setTechnicalProvisionsWithInvestmentsNotForOwnRisk(
                BigDecimal technicalProvisionsWithInvestmentsNotForOwnRisk) {
            this.technicalProvisionsWithInvestmentsNotForOwnRisk = technicalProvisionsWithInvestmentsNotForOwnRisk;
        }

        public void setTotalTechnicalProvisions(BigDecimal totalTechnicalProvisions) {
            this.totalTechnicalProvisions = totalTechnicalProvisions;
        }

        public void setDepositsReceivedFromReinsurers(BigDecimal depositsReceivedFromReinsurers) {
            this.depositsReceivedFromReinsurers = depositsReceivedFromReinsurers;
        }

        public void setTotalInsurance(BigDecimal totalInsurance) {
            this.totalInsurance = totalInsurance;
        }

        public void setProvisions(BigDecimal provisions) {
            this.provisions = provisions;
        }

        public void setLongTermDebts(BigDecimal longTermDebts) {
            this.longTermDebts = longTermDebts;
        }

        public void setAccountsPayable(BigDecimal accountsPayable) {
            this.accountsPayable = accountsPayable;
        }

        public void setBank(BigDecimal bank) {
            this.bank = bank;
        }

        public void setOtherShortTermLiabilities(BigDecimal otherShortTermLiabilities) {
            this.otherShortTermLiabilities = otherShortTermLiabilities;
        }

        public void setShortTermLiabilities(BigDecimal shortTermLiabilities) {
            this.shortTermLiabilities = shortTermLiabilities;
        }

        public void setOtherLiabilities(BigDecimal otherLiabilities) {
            this.otherLiabilities = otherLiabilities;
        }

        public void setAccrualsAndDeferredIncome(BigDecimal accrualsAndDeferredIncome) {
            this.accrualsAndDeferredIncome = accrualsAndDeferredIncome;
        }

        public void setTotalLiabilities(BigDecimal totalLiabilities) {
            this.totalLiabilities = totalLiabilities;
        }

        public void setIsProvisional(boolean provisional) {
            isProvisional = provisional;
        }
    }

    private final BigDecimal shareholdersEquity;

    private final BigDecimal preferentEquity;

    private final BigDecimal minorityInterests;

    private final BigDecimal groupEquity;

    private final BigDecimal subordinatedLoans;

    private final BigDecimal generalRiskAllowance;

    private final BigDecimal liableCapital;

    private final BigDecimal liabilitiesDueToBanks;

    private final BigDecimal customerDepositsSavings;

    private final BigDecimal customerDepositsOther;

    private final BigDecimal debtSecurities;

    private final BigDecimal totalBanking;

    private final BigDecimal technicalProvisions;

    private final BigDecimal technicalProvisionsWithInvestmentsNotForOwnRisk;

    private final BigDecimal totalTechnicalProvisions;

    private final BigDecimal depositsReceivedFromReinsurers;

    private final BigDecimal totalInsurance;

    private final BigDecimal provisions;

    private final BigDecimal longTermDebts;

    private final BigDecimal accountsPayable;

    private final BigDecimal bank;

    private final BigDecimal otherShortTermLiabilities;

    private final BigDecimal shortTermLiabilities;

    private final BigDecimal otherLiabilities;

    private final BigDecimal accrualsAndDeferredIncome;

    private final BigDecimal totalLiabilities;

    private final boolean isProvisional;

    public AnnualReportLiabilitiesImpl(Builder b) {
        this.shareholdersEquity = b.shareholdersEquity;
        this.preferentEquity = b.preferentEquity;
        this.minorityInterests = b.minorityInterests;
        this.groupEquity = b.groupEquity;
        this.subordinatedLoans = b.subordinatedLoans;
        this.generalRiskAllowance = b.generalRiskAllowance;
        this.liableCapital = b.liableCapital;
        this.liabilitiesDueToBanks = b.liabilitiesDueToBanks;
        this.customerDepositsSavings = b.customerDepositsSavings;
        this.customerDepositsOther = b.customerDepositsOther;
        this.debtSecurities = b.debtSecurities;
        this.totalBanking = b.totalBanking;
        this.technicalProvisions = b.technicalProvisions;
        this.technicalProvisionsWithInvestmentsNotForOwnRisk = b.technicalProvisionsWithInvestmentsNotForOwnRisk;
        this.totalTechnicalProvisions = b.totalTechnicalProvisions;
        this.depositsReceivedFromReinsurers = b.depositsReceivedFromReinsurers;
        this.totalInsurance = b.totalInsurance;
        this.provisions = b.provisions;
        this.longTermDebts = b.longTermDebts;
        this.accountsPayable = b.accountsPayable;
        this.bank = b.bank;
        this.otherShortTermLiabilities = b.otherShortTermLiabilities;
        this.shortTermLiabilities = b.shortTermLiabilities;
        this.otherLiabilities = b.otherLiabilities;
        this.accrualsAndDeferredIncome = b.accrualsAndDeferredIncome;
        this.totalLiabilities = b.totalLiabilities;
        this.isProvisional = b.isProvisional;
    }

    public BigDecimal getShareholdersEquity() {
        return shareholdersEquity;
    }

    public BigDecimal getPreferentEquity() {
        return preferentEquity;
    }

    public BigDecimal getMinorityInterests() {
        return minorityInterests;
    }

    public BigDecimal getGroupEquity() {
        return groupEquity;
    }

    public BigDecimal getSubordinatedLoans() {
        return subordinatedLoans;
    }

    public BigDecimal getGeneralRiskAllowance() {
        return generalRiskAllowance;
    }

    public BigDecimal getLiableCapital() {
        return liableCapital;
    }

    public BigDecimal getLiabilitiesDueToBanks() {
        return liabilitiesDueToBanks;
    }

    public BigDecimal getCustomerDepositsSavings() {
        return customerDepositsSavings;
    }

    public BigDecimal getCustomerDepositsOther() {
        return customerDepositsOther;
    }

    public BigDecimal getDebtSecurities() {
        return debtSecurities;
    }

    public BigDecimal getTotalBanking() {
        return totalBanking;
    }

    public BigDecimal getTechnicalProvisions() {
        return technicalProvisions;
    }

    public BigDecimal getTechnicalProvisionsWithInvestmentsNotForOwnRisk() {
        return technicalProvisionsWithInvestmentsNotForOwnRisk;
    }

    public BigDecimal getTotalTechnicalProvisions() {
        return totalTechnicalProvisions;
    }

    public BigDecimal getDepositsReceivedFromReinsurers() {
        return depositsReceivedFromReinsurers;
    }

    public BigDecimal getTotalInsurance() {
        return totalInsurance;
    }

    public BigDecimal getProvisions() {
        return provisions;
    }

    public BigDecimal getLongTermDebts() {
        return longTermDebts;
    }

    public BigDecimal getAccountsPayable() {
        return accountsPayable;
    }

    public BigDecimal getBank() {
        return bank;
    }

    public BigDecimal getOtherShortTermLiabilities() {
        return otherShortTermLiabilities;
    }

    public BigDecimal getShortTermLiabilities() {
        return shortTermLiabilities;
    }

    public BigDecimal getOtherLiabilities() {
        return otherLiabilities;
    }

    public BigDecimal getAccrualsAndDeferredIncome() {
        return accrualsAndDeferredIncome;
    }

    public BigDecimal getTotalLiabilities() {
        return totalLiabilities;
    }

    public boolean isProvisional() {
        return isProvisional;
    }

    @Override
    public String toString() {
        return "AnnualReportLiabilitiesImpl{" +
                "shareholdersEquity=" + shareholdersEquity +
                ", preferentEquity=" + preferentEquity +
                ", minorityInterests=" + minorityInterests +
                ", groupEquity=" + groupEquity +
                ", subordinatedLoans=" + subordinatedLoans +
                ", generalRiskAllowance=" + generalRiskAllowance +
                ", liableCapital=" + liableCapital +
                ", liabilitiesDueToBanks=" + liabilitiesDueToBanks +
                ", customerDepositsSavings=" + customerDepositsSavings +
                ", customerDepositsOther=" + customerDepositsOther +
                ", debtSecurities=" + debtSecurities +
                ", totalBanking=" + totalBanking +
                ", technicalProvisions=" + technicalProvisions +
                ", technicalProvisionsWithInvestmentsNotForOwnRisk=" + technicalProvisionsWithInvestmentsNotForOwnRisk +
                ", totalTechnicalProvisions=" + totalTechnicalProvisions +
                ", depositsReceivedFromReinsurers=" + depositsReceivedFromReinsurers +
                ", totalInsurance=" + totalInsurance +
                ", provisions=" + provisions +
                ", longTermDebts=" + longTermDebts +
                ", accountsPayable=" + accountsPayable +
                ", bank=" + bank +
                ", otherShortTermLiabilities=" + otherShortTermLiabilities +
                ", shortTermLiabilities=" + shortTermLiabilities +
                ", otherLiabilities=" + otherLiabilities +
                ", accrualsAndDeferredIncome=" + accrualsAndDeferredIncome +
                ", totalLiabilities=" + totalLiabilities +
                ", isProvisional=" + isProvisional +
                '}';
    }
}