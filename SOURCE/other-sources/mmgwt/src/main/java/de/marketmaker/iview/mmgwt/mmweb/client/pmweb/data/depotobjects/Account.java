/*
 * Account.java
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

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.*;

/**
 * @author Michael Lösch
 */
public class Account implements ContextItem, HasUserDefinedFields, DepotObject {
    public static class AccountsTalker extends AbstractMmTalker<DatabaseIdQuery, List<Account>, Account> {
        public AccountsTalker() {
            this("Konto"); // $NON-NLS$
        }

        protected AccountsTalker(String formula) {
            super(formula);
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        @Override
        public List<Account> createResultObject(MMTalkResponse response) {
            return this.wrapper.createResultObjectList(response);
        }

        @Override
        public MmTalkWrapper<Account> createWrapper(Formula formula) {
            return Account.createWrapper(formula);
        }
    }

    private String id;
    private String zone;
    private String name;
    private String investorId;
    private String investorName;
    private String portfolioId;
    private String portfolioName;
    private String type;
    private String bank;
    private String bankCode;
    private String accountNumber;
    private String balance;
    private String currency;
    private String creationDate;
    private String deactivationDate;
    private String iban;
    private VerificationStatus dataStatusAccount;
    private String dataStatusAccountDate;
    private VerificationStatus dataStatusSpecialInvestments;
    private String dataStatusSpecialInvestmentsDate;
    private VerificationStatus dataStatusTotal;
    private String dataStatusTotalDate;
    private String comment;
    private Boolean excludeFromTaxAnalysis;
    private String interestRate;
    private List<HistoryItem> interestRateHistory;
    private String interestRateMethod;
    private String interestDay;
    private Boolean overnightDepositAccount;
    private Address bankAddress;
    private UserDefinedFields userDefinedFields;
    private AlertsResponse alerts;

    public static MmTalkWrapper<Account> createWrapper(String formula) {
        return createWrapper(Formula.create(formula));
    }

    public static MmTalkWrapper<Account> createWrapper(Formula formula) {
        final MmTalkWrapper<Account> cols = MmTalkWrapper.create(formula, Account.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<Account>("Id") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.id = asMMNumber(item).getValue();
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Zone") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.zone = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Typ") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.type = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Name") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.name = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Inhaber.Id") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.investorId = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Inhaber.Name") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.investorName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Portfolio.Id") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.portfolioId = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Portfolio.Name") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.portfolioName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Konto_Bank") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.bank = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Konto_BLZ") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.bankCode = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Kontonummer") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.accountNumber = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Saldo") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.balance = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Währung.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.currency = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("AngelegtAm") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.creationDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("IBAN") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.iban = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("VerificationStatus") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.dataStatusAccount = asEnum(MMTypRefType.TRT_VERIFY_STATUS, VerificationStatus.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("VerificationDate") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.dataStatusAccountDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("VerificationStatus_SpecialAssets") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.dataStatusSpecialInvestments = asEnum(MMTypRefType.TRT_VERIFY_STATUS, VerificationStatus.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("VerificationDate_SpecialAssets") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.dataStatusSpecialInvestmentsDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("VerificationStatusTotal") { // $NON-NLS$
            @Override
            public void setValue(Account d, MM item) {
                d.dataStatusTotal = asEnum(MMTypRefType.TRT_VERIFY_STATUS, VerificationStatus.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("VerificationDateTotal") { // $NON-NLS$
            @Override
            public void setValue(Account d, MM item) {
                d.dataStatusTotalDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Bemerkung") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.comment = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("AusschlussSteuer") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.excludeFromTaxAnalysis = asBoolean(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("KONTO_ZINSINTERVALL") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.interestDay = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Zins") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.interestRate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("Zinsmethode") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.interestRateMethod = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("DeactivatedOn") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.deactivationDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Account>("IstTagesgeld") { // $NON-NLS$
            @Override
            public void setValue(Account a, MM item) {
                a.overnightDepositAccount = asBoolean(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<Account, Address>(Address.createWrapper("BankAdresse")) { // $NON-NLS$
            @Override
            public void setValue(Account account, MmTalkWrapper<Address> wrapper, MMTable table) {
                account.bankAddress = wrapper.createResultObject(table);
            }
        }).appendNodeMapper(createInterestRateHistoryMapper());

        return cols;
    }

    private static MmTalkNodeMapper<Account, HistoryItem> createInterestRateHistoryMapper() {
        final String preFormula =
                "$h := object.ToList.Nth[0].ZINSHISTORIE;" + // $NON-NLS$
                //Map über die Datumswerte
                "map(#[](" +  // $NON-NLS$
                    "makeCollection" +  // $NON-NLS$
                    //füge für jedes Datum den jeweils hinterlegten Wert ein
                    ".add[\"Wert\"; $h.HistoryItem_byDate[object]]" +  // $NON-NLS$
                ");"+
                "$h.HistoryTransitionDates)";  // $NON-NLS$

        return new MmTalkNodeMapper<Account, HistoryItem>(HistoryItem.createWrapper(preFormula)) {
            @Override
            public void setValue(Account account, MmTalkWrapper<HistoryItem> wrapper, MMTable table) {
                account.interestRateHistory = wrapper.createResultObjectList(table);
            }
        };
    }

    private Account() {
    }

    public String getId() {
        return this.id;
    }

    public String getZone() {
        return this.zone;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public String getBank() {
        return this.bank;
    }

    public String getBankCode() {
        return this.bankCode;
    }

    public String getBalance() {
        return this.balance;
    }

    public Address getBankAddress() {
        return bankAddress;
    }

    public String getDeactivationDate() {
        return deactivationDate;
    }

    public String getCurrency() {
        return this.currency;

    }

    public String getCreationDate() {
        return this.creationDate;
    }

    public String getIban() {
        return this.iban;
    }

    public VerificationStatus getDataStatusAccount() {
        return this.dataStatusAccount;
    }

    public VerificationStatus getDataStatusSpecialInvestments() {
        return this.dataStatusSpecialInvestments;
    }

    public VerificationStatus getDataStatusTotal() {
        return dataStatusTotal;
    }

    public String getDataStatusTotalDate() {
        return dataStatusTotalDate;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public String getComment() {
        return this.comment;
    }

    public Boolean isExcludeFromTaxAnalysis() {
        return this.excludeFromTaxAnalysis;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public String getInterestRateMethod() {
        return interestRateMethod;
    }

    public String getInterestDay() {
        return interestDay;
    }

    public Boolean isOvernightDepositAccount() {
        return overnightDepositAccount;
    }

    public String getInvestorName() {
        return investorName;
    }

    public String getDataStatusAccountDate() {
        return dataStatusAccountDate;
    }

    public String getDataStatusSpecialInvestmentsDate() {
        return dataStatusSpecialInvestmentsDate;
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

    public List<HistoryItem> getInterestRateHistory() {
        return interestRateHistory;
    }

    public Account withAlerts(AlertsResponse alerts) {
        this.alerts = alerts;
        return this;
    }

    @Override
    public AlertsResponse getAlertResponse() {
        return alerts;
    }

    @Override
    public ShellMMType getShellMMType() {
        return ShellMMType.ST_KONTO;
    }


    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Account account = (Account) o;

        if (accountNumber != null ? !accountNumber.equals(account.accountNumber) : account.accountNumber != null)
            return false;
        if (alerts != null ? !alerts.equals(account.alerts) : account.alerts != null) return false;
        if (balance != null ? !balance.equals(account.balance) : account.balance != null) return false;
        if (bank != null ? !bank.equals(account.bank) : account.bank != null) return false;
        if (bankAddress != null ? !bankAddress.equals(account.bankAddress) : account.bankAddress != null) return false;
        if (bankCode != null ? !bankCode.equals(account.bankCode) : account.bankCode != null) return false;
        if (comment != null ? !comment.equals(account.comment) : account.comment != null) return false;
        if (creationDate != null ? !creationDate.equals(account.creationDate) : account.creationDate != null)
            return false;
        if (currency != null ? !currency.equals(account.currency) : account.currency != null) return false;
        if (dataStatusAccount != account.dataStatusAccount) return false;
        if (dataStatusAccountDate != null ? !dataStatusAccountDate.equals(account.dataStatusAccountDate) : account.dataStatusAccountDate != null)
            return false;
        if (dataStatusSpecialInvestments != account.dataStatusSpecialInvestments) return false;
        if (dataStatusSpecialInvestmentsDate != null ? !dataStatusSpecialInvestmentsDate.equals(account.dataStatusSpecialInvestmentsDate) : account.dataStatusSpecialInvestmentsDate != null)
            return false;
        if (dataStatusTotal != account.dataStatusTotal) return false;
        if (dataStatusTotalDate != null ? !dataStatusTotalDate.equals(account.dataStatusTotalDate) : account.dataStatusTotalDate != null)
            return false;
        if (deactivationDate != null ? !deactivationDate.equals(account.deactivationDate) : account.deactivationDate != null)
            return false;
        if (excludeFromTaxAnalysis != null ? !excludeFromTaxAnalysis.equals(account.excludeFromTaxAnalysis) : account.excludeFromTaxAnalysis != null)
            return false;
        if (iban != null ? !iban.equals(account.iban) : account.iban != null) return false;
        if (id != null ? !id.equals(account.id) : account.id != null) return false;
        if (interestDay != null ? !interestDay.equals(account.interestDay) : account.interestDay != null) return false;
        if (interestRate != null ? !interestRate.equals(account.interestRate) : account.interestRate != null)
            return false;
        if (interestRateHistory != null ? !interestRateHistory.equals(account.interestRateHistory) : account.interestRateHistory != null)
            return false;
        if (interestRateMethod != null ? !interestRateMethod.equals(account.interestRateMethod) : account.interestRateMethod != null)
            return false;
        if (investorId != null ? !investorId.equals(account.investorId) : account.investorId != null) return false;
        if (investorName != null ? !investorName.equals(account.investorName) : account.investorName != null)
            return false;
        if (name != null ? !name.equals(account.name) : account.name != null) return false;
        if (overnightDepositAccount != null ? !overnightDepositAccount.equals(account.overnightDepositAccount) : account.overnightDepositAccount != null)
            return false;
        if (portfolioId != null ? !portfolioId.equals(account.portfolioId) : account.portfolioId != null) return false;
        if (portfolioName != null ? !portfolioName.equals(account.portfolioName) : account.portfolioName != null)
            return false;
        if (type != null ? !type.equals(account.type) : account.type != null) return false;
        if (userDefinedFields != null ? !userDefinedFields.equals(account.userDefinedFields) : account.userDefinedFields != null)
            return false;
        if (zone != null ? !zone.equals(account.zone) : account.zone != null) return false;

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
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (bank != null ? bank.hashCode() : 0);
        result = 31 * result + (bankCode != null ? bankCode.hashCode() : 0);
        result = 31 * result + (accountNumber != null ? accountNumber.hashCode() : 0);
        result = 31 * result + (balance != null ? balance.hashCode() : 0);
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (deactivationDate != null ? deactivationDate.hashCode() : 0);
        result = 31 * result + (iban != null ? iban.hashCode() : 0);
        result = 31 * result + (dataStatusAccount != null ? dataStatusAccount.hashCode() : 0);
        result = 31 * result + (dataStatusAccountDate != null ? dataStatusAccountDate.hashCode() : 0);
        result = 31 * result + (dataStatusSpecialInvestments != null ? dataStatusSpecialInvestments.hashCode() : 0);
        result = 31 * result + (dataStatusSpecialInvestmentsDate != null ? dataStatusSpecialInvestmentsDate.hashCode() : 0);
        result = 31 * result + (dataStatusTotal != null ? dataStatusTotal.hashCode() : 0);
        result = 31 * result + (dataStatusTotalDate != null ? dataStatusTotalDate.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (excludeFromTaxAnalysis != null ? excludeFromTaxAnalysis.hashCode() : 0);
        result = 31 * result + (interestRate != null ? interestRate.hashCode() : 0);
        result = 31 * result + (interestRateHistory != null ? interestRateHistory.hashCode() : 0);
        result = 31 * result + (interestRateMethod != null ? interestRateMethod.hashCode() : 0);
        result = 31 * result + (interestDay != null ? interestDay.hashCode() : 0);
        result = 31 * result + (overnightDepositAccount != null ? overnightDepositAccount.hashCode() : 0);
        result = 31 * result + (bankAddress != null ? bankAddress.hashCode() : 0);
        result = 31 * result + (userDefinedFields != null ? userDefinedFields.hashCode() : 0);
        result = 31 * result + (alerts != null ? alerts.hashCode() : 0);
        return result;
    }
}