/*
 * Bank.java
 *
 * Created on 14.03.13 14:56
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.*;

/**
 * @author Markus Dick
 */
public class Bank {
    /* caches if this object is empty or not ! do not include in equals */
    private Boolean empty = null;

    private String name;
    private String bankCode;
    private String bic;
    private String subsidiaryNo;
    private String brokerageModuleName;
    private String brokerageModuleId;
    private Address address;

    public boolean isEmpty() {
        if(this.empty != null) {
            return this.empty;
        }
        if(StringUtil.hasText(this.name) ||
                StringUtil.hasText(this.bankCode) ||
                StringUtil.hasText(this.bic) ||
                StringUtil.hasText(this.subsidiaryNo) ||
                StringUtil.hasText(this.brokerageModuleName) ||
                StringUtil.hasText(this.brokerageModuleId) ||
                this.address != null && !address.isEmpty()) {

            return this.empty = Boolean.FALSE;
        }

        return this.empty = Boolean.TRUE;
    }

    public String getName() {
        return name;
    }

    public String getBrokerageModuleId() {
        return brokerageModuleId;
    }

    public Address getAddress() {
        return address;
    }

    public static MmTalkWrapper<Bank> createWrapper(String formula) {
        final MmTalkWrapper<Bank> cols = MmTalkWrapper.create(formula, Bank.class);

        cols.appendColumnMapper(new MmTalkColumnMapper<Bank>("Name") { // $NON-NLS$
            @Override
            public void setValue(Bank b, MM item) {
                b.name = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Bank>("BLZ") { // $NON-NLS$
            @Override
            public void setValue(Bank b, MM item) {
                b.bankCode = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Bank>("BIC") { // $NON-NLS$
            @Override
            public void setValue(Bank b, MM item) {
                b.bic = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Bank>("Filialnummer") { // $NON-NLS$
            @Override
            public void setValue(Bank b, MM item) {
                b.subsidiaryNo = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Bank>("BrokerageModule") { // $NON-NLS$
            @Override
            public void setValue(Bank b, MM item) {
                b.brokerageModuleName = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Bank>("BROKERAGEMODULEID") { // $NON-NLS$
            @Override
            public void setValue(Bank b, MM item) {
                b.brokerageModuleId = asString(item);
            }
        }).appendNodeMapper(new MmTalkNodeMapper<Bank, Address>(Address.createWrapper("Adresse")) { // $NON-NLS$
            @Override
            public void setValue(Bank b, MmTalkWrapper<Address> wrapper, MMTable table) {
                b.address = wrapper.createResultObject(table);
            }
        });
        return cols;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bank)) return false;

        final Bank bank = (Bank) o;

        if (address != null ? !address.equals(bank.address) : bank.address != null) return false;
        if (bankCode != null ? !bankCode.equals(bank.bankCode) : bank.bankCode != null) return false;
        if (bic != null ? !bic.equals(bank.bic) : bank.bic != null) return false;
        if (brokerageModuleId != null ? !brokerageModuleId.equals(bank.brokerageModuleId) : bank.brokerageModuleId != null)
            return false;
        if (brokerageModuleName != null ? !brokerageModuleName.equals(bank.brokerageModuleName) : bank.brokerageModuleName != null)
            return false;
        if (name != null ? !name.equals(bank.name) : bank.name != null) return false;
        if (subsidiaryNo != null ? !subsidiaryNo.equals(bank.subsidiaryNo) : bank.subsidiaryNo != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (bankCode != null ? bankCode.hashCode() : 0);
        result = 31 * result + (bic != null ? bic.hashCode() : 0);
        result = 31 * result + (subsidiaryNo != null ? subsidiaryNo.hashCode() : 0);
        result = 31 * result + (brokerageModuleName != null ? brokerageModuleName.hashCode() : 0);
        result = 31 * result + (brokerageModuleId != null ? brokerageModuleId.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }
}
