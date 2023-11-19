/*
 * AbstractOwner.java
 *
 * Created on 05.06.2014 07:41
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.AlertsResponse;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.VerificationStatus;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.*;


/**
 * @author Markus Dick
 */
public abstract class AbstractOwner implements ContextItem, HasUserDefinedFields, DepotObject {
    public static <T extends AbstractOwner> MmTalkWrapper<T> withLinkedPersons(MmTalkWrapper<T> cols) {
        return cols.appendNodeMapper(new MmTalkNodeMapper<T, OwnerPersonLink>(
                OwnerPersonLink.withPerson(OwnerPersonLink.createWrapper(Formula.create("PersonLinks"))) //$NON-NLS$
        ) {
            @Override
            public void setValue(T o, MmTalkWrapper<OwnerPersonLink> wrapper, MMTable table) {
                o.linkedPersons = wrapper.createResultObjectList(table);
            }
        });
    }

    protected static <T extends AbstractOwner> MmTalkWrapper<T> appendMappers(MmTalkWrapper<T> cols, final String taxRateFunctionName) {
        cols.appendColumnMapper(new MmTalkColumnMapper<T>("Id") { // $NON-NLS$
            @Override
            public void setValue(T o, MM item) {
                o.id = asMMNumber(item).getValue();
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Zone") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.zone = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Typ") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.type = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Name") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.name = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Number") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.number = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Kundenklassifizierung") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.customerCategory = asString(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<T, Address>(Address.createWrapper("Adresse")) { // $NON-NLS$
            @Override
            public void setValue(T o, MmTalkWrapper<Address> wrapper, MMTable table) {
                o.address = wrapper.createResultObject(table);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Währung.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.analysisCurrency = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Steuerland") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.taxDomicile = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Tax_Currency.Kürzel") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.currencyOfTax = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Verheiratet") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.married = asBoolean(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("FreibetragKapitalerträge") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.flatRateSavingsAllowance = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Verlustvortrag_Spek") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.lossCarryforwardSpeculativeGains = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Verlustvortrag_Aktien") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.lossCarryforwardEquities = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Verlustvortrag_Kapital") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.lossCarryforwardCapitalAssets = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>(taxRateFunctionName) {
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.taxRate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("KiStSatz") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.churchTaxRate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("KiStAbzugBank") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.churchTaxDeductionByBank = asBoolean(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("KiStSatz_Ehepartner") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.churchTaxRateOfSpouse = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("KiStAbzugBank_Ehepartner") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.churchTaxDeductionByBankOfSpouse = asBoolean(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Eigentumsanteil_Ehepartner") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.ownershipShareOfSpouse = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("Bemerkung") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.comment = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("AngelegtAm") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.creationDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("DeactivatedOn") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.deactivationDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("VerificationStatus") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.dataStatus = asEnum(MMTypRefType.TRT_VERIFY_STATUS, VerificationStatus.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("VerificationDate") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner o, MM item) {
                o.dataStatusDate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("VerificationStatusTotal") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner d, MM item) {
                d.dataStatusTotal = asEnum(MMTypRefType.TRT_VERIFY_STATUS, VerificationStatus.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<T>("VerificationDateTotal") { // $NON-NLS$
            @Override
            public void setValue(AbstractOwner d, MM item) {
                d.dataStatusTotalDate = asString(item);
            }
        });

        return cols;
    }

    protected String id;
    private String zone;
    private String type;
    private String customerCategory;
    private String name;
    private String number;
    private String taxDomicile;
    protected Address address;
    private String analysisCurrency;
    private String currencyOfTax;
    private Boolean married;
    private String flatRateSavingsAllowance;
    private String lossCarryforwardSpeculativeGains;
    private String lossCarryforwardEquities;
    private String lossCarryforwardCapitalAssets;
    private String taxRate;
    private String churchTaxRate;
    private Boolean churchTaxDeductionByBank;
    private String churchTaxRateOfSpouse;
    private Boolean churchTaxDeductionByBankOfSpouse;
    private String ownershipShareOfSpouse;
    private String comment;
    private String creationDate;
    private String deactivationDate;
    private VerificationStatus dataStatus;
    private String dataStatusDate;
    private VerificationStatus dataStatusTotal;
    private String dataStatusTotalDate;
    protected List<OwnerPersonLink> linkedPersons;
    private AlertsResponse alerts;

    private UserDefinedFields userDefinedFields;

    protected AbstractOwner() {
    }

    public String getId() {
        return id;
    }

    public String getZone() {
        return zone;
    }

    public String getType() {
        return type;
    }

    public String getCustomerCategory() {
        return customerCategory;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public String getTaxDomicile() {
        return taxDomicile;
    }

    public Address getAddress() {
        return address;
    }

    public String getAnalysisCurrency() {
        return analysisCurrency;
    }

    public String getCurrencyOfTax() {
        return currencyOfTax;
    }

    public Boolean isMarried() {
        return married;
    }

    public String getFlatRateSavingsAllowance() {
        return flatRateSavingsAllowance;
    }

    public String getLossCarryforwardSpeculativeGains() {
        return lossCarryforwardSpeculativeGains;
    }

    public String getLossCarryforwardEquities() {
        return lossCarryforwardEquities;
    }

    public String getLossCarryforwardCapitalAssets() {
        return lossCarryforwardCapitalAssets;
    }

    public String getTaxRate() {
        return taxRate;
    }

    public String getChurchTaxRate() {
        return churchTaxRate;
    }

    public Boolean isChurchTaxDeductionByBank() {
        return churchTaxDeductionByBank;
    }

    public String getChurchTaxRateOfSpouse() {
        return churchTaxRateOfSpouse;
    }

    public Boolean isChurchTaxDeductionByBankOfSpouse() {
        return churchTaxDeductionByBankOfSpouse;
    }

    public String getOwnershipShareOfSpouse() {
        return ownershipShareOfSpouse;
    }

    public String getComment() {
        return comment;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getDeactivationDate() {
        return deactivationDate;
    }

    public VerificationStatus getDataStatus() {
        return dataStatus;
    }

    public VerificationStatus getDataStatusTotal() {
        return this.dataStatusTotal;
    }

    public String getDataStatusTotalDate() {
        return dataStatusTotalDate;
    }

    public String getDataStatusDate() {
        return dataStatusDate;
    }

    public UserDefinedFields getUserDefinedFields() {
        return userDefinedFields;
    }

    public void setUserDefinedFields(UserDefinedFields userDefinedFields) {
        this.userDefinedFields = userDefinedFields;
    }

    public List<OwnerPersonLink> linkedPersons() {
        return linkedPersons;
    }

    public AbstractOwner withAlerts(AlertsResponse alerts) {
        this.alerts = alerts;
        return this;
    }

    public AlertsResponse getAlertResponse() {
        return alerts;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractOwner that = (AbstractOwner) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (alerts != null ? !alerts.equals(that.alerts) : that.alerts != null) return false;
        if (analysisCurrency != null ? !analysisCurrency.equals(that.analysisCurrency) : that.analysisCurrency != null)
            return false;
        if (churchTaxDeductionByBank != null ? !churchTaxDeductionByBank.equals(that.churchTaxDeductionByBank) : that.churchTaxDeductionByBank != null)
            return false;
        if (churchTaxDeductionByBankOfSpouse != null ? !churchTaxDeductionByBankOfSpouse.equals(that.churchTaxDeductionByBankOfSpouse) : that.churchTaxDeductionByBankOfSpouse != null)
            return false;
        if (churchTaxRate != null ? !churchTaxRate.equals(that.churchTaxRate) : that.churchTaxRate != null)
            return false;
        if (churchTaxRateOfSpouse != null ? !churchTaxRateOfSpouse.equals(that.churchTaxRateOfSpouse) : that.churchTaxRateOfSpouse != null)
            return false;
        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (creationDate != null ? !creationDate.equals(that.creationDate) : that.creationDate != null) return false;
        if (currencyOfTax != null ? !currencyOfTax.equals(that.currencyOfTax) : that.currencyOfTax != null)
            return false;
        if (customerCategory != null ? !customerCategory.equals(that.customerCategory) : that.customerCategory != null)
            return false;
        if (dataStatus != that.dataStatus) return false;
        if (dataStatusDate != null ? !dataStatusDate.equals(that.dataStatusDate) : that.dataStatusDate != null)
            return false;
        if (dataStatusTotal != that.dataStatusTotal) return false;
        if (dataStatusTotalDate != null ? !dataStatusTotalDate.equals(that.dataStatusTotalDate) : that.dataStatusTotalDate != null)
            return false;
        if (deactivationDate != null ? !deactivationDate.equals(that.deactivationDate) : that.deactivationDate != null)
            return false;
        if (flatRateSavingsAllowance != null ? !flatRateSavingsAllowance.equals(that.flatRateSavingsAllowance) : that.flatRateSavingsAllowance != null)
            return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (linkedPersons != null ? !linkedPersons.equals(that.linkedPersons) : that.linkedPersons != null)
            return false;
        if (lossCarryforwardCapitalAssets != null ? !lossCarryforwardCapitalAssets.equals(that.lossCarryforwardCapitalAssets) : that.lossCarryforwardCapitalAssets != null)
            return false;
        if (lossCarryforwardEquities != null ? !lossCarryforwardEquities.equals(that.lossCarryforwardEquities) : that.lossCarryforwardEquities != null)
            return false;
        if (lossCarryforwardSpeculativeGains != null ? !lossCarryforwardSpeculativeGains.equals(that.lossCarryforwardSpeculativeGains) : that.lossCarryforwardSpeculativeGains != null)
            return false;
        if (married != null ? !married.equals(that.married) : that.married != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        if (ownershipShareOfSpouse != null ? !ownershipShareOfSpouse.equals(that.ownershipShareOfSpouse) : that.ownershipShareOfSpouse != null)
            return false;
        if (taxDomicile != null ? !taxDomicile.equals(that.taxDomicile) : that.taxDomicile != null) return false;
        if (taxRate != null ? !taxRate.equals(that.taxRate) : that.taxRate != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (userDefinedFields != null ? !userDefinedFields.equals(that.userDefinedFields) : that.userDefinedFields != null)
            return false;
        //noinspection RedundantIfStatement
        if (zone != null ? !zone.equals(that.zone) : that.zone != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (customerCategory != null ? customerCategory.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (taxDomicile != null ? taxDomicile.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (analysisCurrency != null ? analysisCurrency.hashCode() : 0);
        result = 31 * result + (currencyOfTax != null ? currencyOfTax.hashCode() : 0);
        result = 31 * result + (married != null ? married.hashCode() : 0);
        result = 31 * result + (flatRateSavingsAllowance != null ? flatRateSavingsAllowance.hashCode() : 0);
        result = 31 * result + (lossCarryforwardSpeculativeGains != null ? lossCarryforwardSpeculativeGains.hashCode() : 0);
        result = 31 * result + (lossCarryforwardEquities != null ? lossCarryforwardEquities.hashCode() : 0);
        result = 31 * result + (lossCarryforwardCapitalAssets != null ? lossCarryforwardCapitalAssets.hashCode() : 0);
        result = 31 * result + (taxRate != null ? taxRate.hashCode() : 0);
        result = 31 * result + (churchTaxRate != null ? churchTaxRate.hashCode() : 0);
        result = 31 * result + (churchTaxDeductionByBank != null ? churchTaxDeductionByBank.hashCode() : 0);
        result = 31 * result + (churchTaxRateOfSpouse != null ? churchTaxRateOfSpouse.hashCode() : 0);
        result = 31 * result + (churchTaxDeductionByBankOfSpouse != null ? churchTaxDeductionByBankOfSpouse.hashCode() : 0);
        result = 31 * result + (ownershipShareOfSpouse != null ? ownershipShareOfSpouse.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (creationDate != null ? creationDate.hashCode() : 0);
        result = 31 * result + (deactivationDate != null ? deactivationDate.hashCode() : 0);
        result = 31 * result + (dataStatus != null ? dataStatus.hashCode() : 0);
        result = 31 * result + (dataStatusDate != null ? dataStatusDate.hashCode() : 0);
        result = 31 * result + (dataStatusTotal != null ? dataStatusTotal.hashCode() : 0);
        result = 31 * result + (dataStatusTotalDate != null ? dataStatusTotalDate.hashCode() : 0);
        result = 31 * result + (linkedPersons != null ? linkedPersons.hashCode() : 0);
        result = 31 * result + (alerts != null ? alerts.hashCode() : 0);
        result = 31 * result + (userDefinedFields != null ? userDefinedFields.hashCode() : 0);
        return result;
    }
}