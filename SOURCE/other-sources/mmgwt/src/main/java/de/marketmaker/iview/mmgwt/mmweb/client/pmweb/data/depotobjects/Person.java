/*
 * Person.java
 *
 * Created on 2014-03-12 11:32
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
import de.marketmaker.iview.pmxml.DatabaseIdQuery;
import de.marketmaker.iview.pmxml.Familienstand;
import de.marketmaker.iview.pmxml.Geschlecht;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMTable;
import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.MMTypRefType;

import java.util.List;

/**
 * @author Markus Dick
 */
public class Person implements ContextItem, HasUserDefinedFields, HasZone {
    public static class PersonTalker extends AbstractMmTalker<DatabaseIdQuery, Person, Person> {
        public PersonTalker() {
            super("Person"); // $NON-NLS$
        }

        @Override
        protected DatabaseIdQuery createQuery() {
            return new DatabaseIdQuery();
        }

        public MmTalkWrapper<Person> createWrapper(Formula formula) {
            return Person.withLinkedProspects(Person.withLinkedInvestors(Person.createWrapper(formula)));
        }

        @Override
        public Person createResultObject(MMTalkResponse response) {
            final List<Person> resultObject = this.wrapper.createResultObjectList(response);
            if (!resultObject.isEmpty()) {
                return resultObject.get(0);
            }
            return null;
        }
    }

    public static MmTalkWrapper<Person> withLinkedInvestors(MmTalkWrapper<Person> cols) {
        return cols.appendNodeMapper(new MmTalkNodeMapper<Person, OwnerPersonLink>(
                OwnerPersonLink.withInvestor(OwnerPersonLink.createWrapper(Formula.create("OwnerLinks.deleteifnot[#[](Owner.is[\"Inhaber\"])]"))) //$NON-NLS$
        ) {
            @Override
            public void setValue(Person o, MmTalkWrapper<OwnerPersonLink> wrapper, MMTable table) {
                o.linkedInvestors = wrapper.createResultObjectList(table);
            }
        });
    }

    public static MmTalkWrapper<Person> withLinkedProspects(MmTalkWrapper<Person> cols) {
        return cols.appendNodeMapper(new MmTalkNodeMapper<Person, OwnerPersonLink>(
                OwnerPersonLink.withProspect(OwnerPersonLink.createWrapper(Formula.create("OwnerLinks.deleteifnot[#[](Owner.is[\"Interessent\"])]"))) //$NON-NLS$
        ) {
            @Override
            public void setValue(Person o, MmTalkWrapper<OwnerPersonLink> wrapper, MMTable table) {
                o.linkedProspects = wrapper.createResultObjectList(table);
            }
        });
    }

    public static MmTalkWrapper<Person> createWrapper(Formula formula) {
        final MmTalkWrapper<Person> cols = MmTalkWrapper.create(formula, Person.class);
        cols.appendColumnMapper(new MmTalkColumnMapper<Person>("Id") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.id = MmTalkHelper.asMMNumber(item).getValue();
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Typ") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.type = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Name") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.name = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Zone") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.zone = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Familienstand") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.maritalStatus = MmTalkHelper.asEnum(MMTypRefType.TRT_FAMILIENSTAND, Familienstand.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Geschlecht") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.gender = MmTalkHelper.asEnum(MMTypRefType.TRT_GESCHLECHT, Geschlecht.values(), item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Staatsangehörigkeit.Name") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.nationalStatus = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Geburtsdatum") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.dateOfBirth = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Titel") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.title = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Nachname") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.lastName = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Vorname") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.firstName = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("ZweiterVorname") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.middleName = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Namenszusatz") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.lastNamePrefix = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Namensnachsatz") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.lastNameSuffix = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Grad") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.doctorates = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Geburtsort") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.placeOfBirth = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Personennummer") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.serialNumber = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Ausweisnummer") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.identityNumber = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("AUSGESTELLT_VON") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.identityCardIssuedBy = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("GESCHÄFTSUNFÄHIGKEIT") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.incapacityToContract = MmTalkHelper.asBoolean(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("BERUF") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.occupation = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("TIN") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.taxIdentificationNumber = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Bemerkung") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.comment = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Ausweisart") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.identityCardType = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Anschrift") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.address = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("AnschriftZusatz") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.addressSupplement = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("PLZ") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.postCode = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Ort") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.city = MmTalkHelper.asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Person>("Land.Name") { // $NON-NLS$
            @Override
            public void setValue(Person o, MM item) {
                o.country = MmTalkHelper.asString(item);
            }
        });
        return cols;
    }

    private String id;
    private String type;
    private String name;
    private String zone;
    private String comment;
    private Familienstand maritalStatus;
    private Geschlecht gender;
    private String nationalStatus;
    private String dateOfBirth;
    private String title;
    private String firstName;
    private String middleName;
    private String lastName;
    private String lastNamePrefix;
    private String lastNameSuffix;
    private String doctorates;
    private String placeOfBirth;
    private String serialNumber;
    private String identityNumber;
    private String identityCardIssuedBy;
    private String identityCardType;
    private Boolean incapacityToContract;
    private String occupation;
    private String taxIdentificationNumber;
    private UserDefinedFields userDefinedFields;
    private List<OwnerPersonLink> linkedInvestors;
    private List<OwnerPersonLink> linkedProspects;

    private String address;
    private String addressSupplement;
    private String postCode;
    private String city;
    private String country;

    private Person() {
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getZone() {
        return zone;
    }

    public String getComment() {
        return comment;
    }

    public Familienstand getMaritalStatus() {
        return maritalStatus;
    }

    public Geschlecht getGender() {
        return gender;
    }

    public String getNationalStatus() {
        return nationalStatus;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getLastNamePrefix() {
        return lastNamePrefix;
    }

    public String getLastNameSuffix() {
        return lastNameSuffix;
    }

    public String getDoctorates() {
        return doctorates;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getIdentityNumber() {
        return identityNumber;
    }

    public String getIdentityCardIssuedBy() {
        return identityCardIssuedBy;
    }

    public String getIdentityCardType() {
        return identityCardType;
    }

    public Boolean getIncapacityToContract() {
        return incapacityToContract;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getTaxIdentificationNumber() {
        return taxIdentificationNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getAddressSupplement() {
        return addressSupplement;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public List<OwnerPersonLink> getLinkedInvestors() {
        return linkedInvestors;
    }

    public List<OwnerPersonLink> getLinkedProspects() {
        return linkedProspects;
    }

    @Override
    public UserDefinedFields getUserDefinedFields() {
        return userDefinedFields;
    }

    @Override
    public void setUserDefinedFields(UserDefinedFields userDefinedFields) {
        this.userDefinedFields = userDefinedFields;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        final Person person = (Person) o;

        if (address != null ? !address.equals(person.address) : person.address != null) return false;
        if (addressSupplement != null ? !addressSupplement.equals(person.addressSupplement) : person.addressSupplement != null)
            return false;
        if (city != null ? !city.equals(person.city) : person.city != null) return false;
        if (comment != null ? !comment.equals(person.comment) : person.comment != null) return false;
        if (country != null ? !country.equals(person.country) : person.country != null) return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(person.dateOfBirth) : person.dateOfBirth != null) return false;
        if (doctorates != null ? !doctorates.equals(person.doctorates) : person.doctorates != null) return false;
        if (firstName != null ? !firstName.equals(person.firstName) : person.firstName != null) return false;
        if (gender != person.gender) return false;
        if (!id.equals(person.id)) return false;
        if (identityCardIssuedBy != null ? !identityCardIssuedBy.equals(person.identityCardIssuedBy) : person.identityCardIssuedBy != null)
            return false;
        if (identityCardType != null ? !identityCardType.equals(person.identityCardType) : person.identityCardType != null)
            return false;
        if (identityNumber != null ? !identityNumber.equals(person.identityNumber) : person.identityNumber != null)
            return false;
        if (incapacityToContract != null ? !incapacityToContract.equals(person.incapacityToContract) : person.incapacityToContract != null)
            return false;
        if (lastName != null ? !lastName.equals(person.lastName) : person.lastName != null) return false;
        if (lastNamePrefix != null ? !lastNamePrefix.equals(person.lastNamePrefix) : person.lastNamePrefix != null)
            return false;
        if (lastNameSuffix != null ? !lastNameSuffix.equals(person.lastNameSuffix) : person.lastNameSuffix != null)
            return false;
        if (linkedInvestors != null ? !linkedInvestors.equals(person.linkedInvestors) : person.linkedInvestors != null)
            return false;
        if (linkedProspects != null ? !linkedProspects.equals(person.linkedProspects) : person.linkedProspects != null)
            return false;
        if (maritalStatus != person.maritalStatus) return false;
        if (middleName != null ? !middleName.equals(person.middleName) : person.middleName != null) return false;
        if (name != null ? !name.equals(person.name) : person.name != null) return false;
        if (nationalStatus != null ? !nationalStatus.equals(person.nationalStatus) : person.nationalStatus != null)
            return false;
        if (occupation != null ? !occupation.equals(person.occupation) : person.occupation != null) return false;
        if (placeOfBirth != null ? !placeOfBirth.equals(person.placeOfBirth) : person.placeOfBirth != null)
            return false;
        if (postCode != null ? !postCode.equals(person.postCode) : person.postCode != null) return false;
        if (serialNumber != null ? !serialNumber.equals(person.serialNumber) : person.serialNumber != null)
            return false;
        if (taxIdentificationNumber != null ? !taxIdentificationNumber.equals(person.taxIdentificationNumber) : person.taxIdentificationNumber != null)
            return false;
        if (title != null ? !title.equals(person.title) : person.title != null) return false;
        if (type != null ? !type.equals(person.type) : person.type != null) return false;
        if (userDefinedFields != null ? !userDefinedFields.equals(person.userDefinedFields) : person.userDefinedFields != null)
            return false;
        if (zone != null ? !zone.equals(person.zone) : person.zone != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (maritalStatus != null ? maritalStatus.hashCode() : 0);
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (nationalStatus != null ? nationalStatus.hashCode() : 0);
        result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (middleName != null ? middleName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (lastNamePrefix != null ? lastNamePrefix.hashCode() : 0);
        result = 31 * result + (lastNameSuffix != null ? lastNameSuffix.hashCode() : 0);
        result = 31 * result + (doctorates != null ? doctorates.hashCode() : 0);
        result = 31 * result + (placeOfBirth != null ? placeOfBirth.hashCode() : 0);
        result = 31 * result + (serialNumber != null ? serialNumber.hashCode() : 0);
        result = 31 * result + (identityNumber != null ? identityNumber.hashCode() : 0);
        result = 31 * result + (identityCardIssuedBy != null ? identityCardIssuedBy.hashCode() : 0);
        result = 31 * result + (identityCardType != null ? identityCardType.hashCode() : 0);
        result = 31 * result + (incapacityToContract != null ? incapacityToContract.hashCode() : 0);
        result = 31 * result + (occupation != null ? occupation.hashCode() : 0);
        result = 31 * result + (taxIdentificationNumber != null ? taxIdentificationNumber.hashCode() : 0);
        result = 31 * result + (userDefinedFields != null ? userDefinedFields.hashCode() : 0);
        result = 31 * result + (linkedInvestors != null ? linkedInvestors.hashCode() : 0);
        result = 31 * result + (linkedProspects != null ? linkedProspects.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (addressSupplement != null ? addressSupplement.hashCode() : 0);
        result = 31 * result + (postCode != null ? postCode.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        return result;
    }
}