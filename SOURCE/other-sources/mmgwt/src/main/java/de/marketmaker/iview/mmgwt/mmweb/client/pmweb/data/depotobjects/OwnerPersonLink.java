/*
 * OwnerPersonLink.java
 *
 * Created on 12.03.14 13:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.Formula;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkNodeMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTypRefType;
import de.marketmaker.iview.pmxml.OwnerPersonLinkType;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asEnum;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asString;

/**
 * @author Markus Dick
 */
public class OwnerPersonLink implements ContextItem {
    private String comment;
    private OwnerPersonLinkType type;
    private Person person;
    private AbstractOwner owner;
    private String validFrom;
    private String validTo;
    private String contractNumber;

    public static MmTalkWrapper<OwnerPersonLink> createWrapper(Formula formula) {
        return createWrapper(MmTalkWrapper.create(formula, OwnerPersonLink.class));
    }

    /**
     * Be aware of endless loops if you query withInvestor starting from an investor!
     */
    public static MmTalkWrapper<OwnerPersonLink> withInvestor(MmTalkWrapper<OwnerPersonLink> cols) {
        return cols.appendNodeMapper(new MmTalkNodeMapper<OwnerPersonLink, Investor>(Investor.createWrapper(
                Formula.create("Owner.as[\"Inhaber\"]"))) { // $NON-NLS$
            @Override
            public void setValue(OwnerPersonLink pl, MmTalkWrapper<Investor> wrapper, MMTable table) {
                pl.owner = wrapper.createResultObject(table);
            }
        });
    }

    /**
     * Be aware of endless loops if you query withProspect starting from a prospect!
     */
    public static MmTalkWrapper<OwnerPersonLink> withProspect(MmTalkWrapper<OwnerPersonLink> cols) {
        return cols.appendNodeMapper(new MmTalkNodeMapper<OwnerPersonLink, Prospect>(Prospect.createWrapper(
                Formula.create("Owner.as[\"Interessent\"]"))) { // $NON-NLS$
            @Override
            public void setValue(OwnerPersonLink pl, MmTalkWrapper<Prospect> wrapper, MMTable table) {
                pl.owner = wrapper.createResultObject(table);
            }
        });
    }

    /**
     * Be aware of endless loops if you query withPerson starting from a person!
     */
    public static MmTalkWrapper<OwnerPersonLink> withPerson(MmTalkWrapper<OwnerPersonLink> cols) {
        return cols.appendNodeMapper(new MmTalkNodeMapper<OwnerPersonLink, Person>(Person.createWrapper(
                Formula.create("Person"))) { // $NON-NLS$
            @Override
            public void setValue(OwnerPersonLink pl, MmTalkWrapper<Person> wrapper, MMTable table) {
                pl.person = wrapper.createResultObject(table);
            }
        });
    }

    protected static MmTalkWrapper<OwnerPersonLink> createWrapper(MmTalkWrapper<OwnerPersonLink> cols) {
        cols.appendColumnMapper(new MmTalkColumnMapper<OwnerPersonLink>("Bemerkung") { // $NON-NLS$
            @Override
            public void setValue(OwnerPersonLink pl, MM item) {
                pl.comment = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<OwnerPersonLink>("PersonLinkType") { // $NON-NLS$
            @Override
            public void setValue(OwnerPersonLink pl, MM item) {
                pl.type = asEnum(MMTypRefType.TRT_OWNER_PERSON_LINK_TYPE, OwnerPersonLinkType.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<OwnerPersonLink>("ValidFrom") { // $NON-NLS$
            @Override
            public void setValue(OwnerPersonLink pl, MM item) {
                pl.validFrom = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<OwnerPersonLink>("ValidTo") { // $NON-NLS$
            @Override
            public void setValue(OwnerPersonLink pl, MM item) {
                pl.validTo = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<OwnerPersonLink>("ContractNumber") { // $NON-NLS$
            @Override
            public void setValue(OwnerPersonLink pl, MM item) {
                pl.contractNumber = asString(item);
            }
        });

        return cols;
    }

    public String getComment() {
        return comment;
    }

    public OwnerPersonLinkType getType() {
        return type;
    }

    public Person getPerson() {
        return person;
    }

    @Override
    public String getName() {
        return this.person.getName();
    }

    public AbstractOwner getOwner() {
        return owner;
    }

    public Investor getInvestor() {
        return (Investor) this.owner;
    }

    public Prospect getProspect() {
        return (Prospect) this.owner;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public String getValidTo() {
        return validTo;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OwnerPersonLink)) return false;

        final OwnerPersonLink that = (OwnerPersonLink) o;

        if (comment != null ? !comment.equals(that.comment) : that.comment != null) return false;
        if (contractNumber != null ? !contractNumber.equals(that.contractNumber) : that.contractNumber != null)
            return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        if (person != null ? !person.equals(that.person) : that.person != null) return false;
        if (type != that.type) return false;
        if (validFrom != null ? !validFrom.equals(that.validFrom) : that.validFrom != null) return false;
        if (validTo != null ? !validTo.equals(that.validTo) : that.validTo != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = comment != null ? comment.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (person != null ? person.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (validFrom != null ? validFrom.hashCode() : 0);
        result = 31 * result + (validTo != null ? validTo.hashCode() : 0);
        result = 31 * result + (contractNumber != null ? contractNumber.hashCode() : 0);
        return result;
    }
}