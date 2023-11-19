/*
 * Address.java
 *
 * Created on 26.02.13 15:36
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkColumnMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.MM;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.*;

/**
 * @author Michael Lösch
 */
public class Address {
    private Boolean empty = null;

    private String salutation;
    private String title;
    private String firstname;
    private String middlename;
    private String lastname;
    private String organization;
    private String dateOfBirth;
    private String address;
    private String addressSupplement;
    private String postCode;
    private String city;
    private String country;
    private String phonePrivate;
    private String phoneBusiness;
    private String phoneMobile;
    private String fax;
    private String email;
    private String address2;
    private String addressSupplement2;
    private String city2;
    private String postCode2;
    private String country2;
    private String phonePrivate2;
    private String phoneBusiness2;
    private String phoneMobile2;
    private String fax2;
    private String email2;
    private String comment;
    private String taxNumber;

    public static MmTalkWrapper<Address> createWrapper(String formula) {
        final MmTalkWrapper<Address> cols = MmTalkWrapper.create(formula, Address.class);
        cols.appendColumnMapper(new MmTalkColumnMapper<Address>("Vorname") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.firstname = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("ZweiterVorname") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.middlename = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Nachname") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.lastname = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Titel") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.title = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Anrede") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.salutation = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Geburtsdatum") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.dateOfBirth = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Organisation") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.organization = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Anschrift") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.address = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("AnschriftZusatz") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.addressSupplement = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Ort") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.city = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("PLZ") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.postCode = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Land") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.country = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("TelPrivat") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.phonePrivate = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("TelBeruf") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.phoneBusiness = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("TelMobil") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.phoneMobile = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Fax") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.fax = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("EMail") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.email = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Anschrift2") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.address2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("AnschriftZusatz2") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.addressSupplement2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Ort2") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.city2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("PLZ2") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.postCode2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Land2") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.country2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("TelPrivat2") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.phonePrivate2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("TelBeruf2") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.phoneBusiness2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("TelMobil2") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.phoneMobile2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Fax2") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.fax2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("EMail2") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.email2 = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Bemerkung") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.comment = asString(item);
            }
        }).appendColumnMapper(new MmTalkColumnMapper<Address>("Steuernummer") { // $NON-NLS$
            @Override
            public void setValue(Address a, MM item) {
                a.taxNumber = asString(item);
            }
        });
        return cols;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getCity2() {
        return city2;
    }

    public String getCity() {
        return city;
    }

    public String getCountry2() {
        return country2;
    }

    public String getCountry() {
        return country;
    }

    public String getEmail2() {
        return email2;
    }

    public String getEmail() {
        return email;
    }

    public String getFax2() {
        return fax2;
    }

    public String getFax() {
        return fax;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public String getOrganization() {
        return organization;
    }

    public String getPhoneBusiness2() {
        return phoneBusiness2;
    }

    public String getPhoneBusiness() {
        return phoneBusiness;
    }

    public String getPhoneMobile2() {
        return phoneMobile2;
    }

    public String getPhoneMobile() {
        return phoneMobile;
    }

    public String getPhonePrivate2() {
        return phonePrivate2;
    }

    public String getPhonePrivate() {
        return phonePrivate;
    }

    public String getComment() {
        return comment;
    }

    public String getSalutation() {
        return salutation;
    }

    public String getAddress2() {
        return address2;
    }

    public String getAddress() {
        return address;
    }

    public String getAddressSupplement() {
        return addressSupplement;
    }

    public String getAddressSupplement2() {
        return addressSupplement2;
    }

    public String getTaxNumber() {
        return taxNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getPostCode2() {
        return postCode2;
    }

    public boolean isEmpty() {
        if(this.empty != null) {
            return this.empty;
        }
        if(StringUtil.hasText(this.salutation) ||
                StringUtil.hasText(this.title) ||
                StringUtil.hasText(this.firstname) ||
                StringUtil.hasText(this.middlename) ||
                StringUtil.hasText(this.lastname) ||
                StringUtil.hasText(this.organization) ||
                StringUtil.hasText(this.dateOfBirth) ||
                StringUtil.hasText(this.address) ||
                StringUtil.hasText(this.addressSupplement)||
                StringUtil.hasText(this.postCode) ||
                StringUtil.hasText(this.city) ||
                StringUtil.hasText(this.country) ||
                StringUtil.hasText(this.phonePrivate) ||
                StringUtil.hasText(this.phoneBusiness) ||
                StringUtil.hasText(this.phoneMobile) ||
                StringUtil.hasText(this.fax)||
                StringUtil.hasText(this.email)||
                StringUtil.hasText(this.address2)||
                StringUtil.hasText(this.addressSupplement2)||
                StringUtil.hasText(this.city2)||
                StringUtil.hasText(this.postCode2)||
                StringUtil.hasText(this.country2) ||
                StringUtil.hasText(this.phonePrivate2) ||
                StringUtil.hasText(this.phoneBusiness2)||
                StringUtil.hasText(this.phoneMobile2)||
                StringUtil.hasText(this.fax2) ||
                StringUtil.hasText(this.email2) ||
                StringUtil.hasText(this.comment)||
                StringUtil.hasText(this.taxNumber)) {
            return this.empty = Boolean.FALSE;
        }

        return this.empty = Boolean.TRUE;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;

        final Address address1 = (Address) o;

        if (address != null ? !address.equals(address1.address) : address1.address != null) return false;
        if (address2 != null ? !address2.equals(address1.address2) : address1.address2 != null) return false;
        if (addressSupplement != null ? !addressSupplement.equals(address1.addressSupplement) : address1.addressSupplement != null)
            return false;
        if (addressSupplement2 != null ? !addressSupplement2.equals(address1.addressSupplement2) : address1.addressSupplement2 != null)
            return false;
        if (city != null ? !city.equals(address1.city) : address1.city != null) return false;
        if (city2 != null ? !city2.equals(address1.city2) : address1.city2 != null) return false;
        if (comment != null ? !comment.equals(address1.comment) : address1.comment != null) return false;
        if (country != null ? !country.equals(address1.country) : address1.country != null) return false;
        if (country2 != null ? !country2.equals(address1.country2) : address1.country2 != null) return false;
        if (dateOfBirth != null ? !dateOfBirth.equals(address1.dateOfBirth) : address1.dateOfBirth != null)
            return false;
        if (email != null ? !email.equals(address1.email) : address1.email != null) return false;
        if (email2 != null ? !email2.equals(address1.email2) : address1.email2 != null) return false;
        if (empty != null ? !empty.equals(address1.empty) : address1.empty != null) return false;
        if (fax != null ? !fax.equals(address1.fax) : address1.fax != null) return false;
        if (fax2 != null ? !fax2.equals(address1.fax2) : address1.fax2 != null) return false;
        if (firstname != null ? !firstname.equals(address1.firstname) : address1.firstname != null) return false;
        if (lastname != null ? !lastname.equals(address1.lastname) : address1.lastname != null) return false;
        if (middlename != null ? !middlename.equals(address1.middlename) : address1.middlename != null) return false;
        if (organization != null ? !organization.equals(address1.organization) : address1.organization != null)
            return false;
        if (phoneBusiness != null ? !phoneBusiness.equals(address1.phoneBusiness) : address1.phoneBusiness != null)
            return false;
        if (phoneBusiness2 != null ? !phoneBusiness2.equals(address1.phoneBusiness2) : address1.phoneBusiness2 != null)
            return false;
        if (phoneMobile != null ? !phoneMobile.equals(address1.phoneMobile) : address1.phoneMobile != null)
            return false;
        if (phoneMobile2 != null ? !phoneMobile2.equals(address1.phoneMobile2) : address1.phoneMobile2 != null)
            return false;
        if (phonePrivate != null ? !phonePrivate.equals(address1.phonePrivate) : address1.phonePrivate != null)
            return false;
        if (phonePrivate2 != null ? !phonePrivate2.equals(address1.phonePrivate2) : address1.phonePrivate2 != null)
            return false;
        if (postCode != null ? !postCode.equals(address1.postCode) : address1.postCode != null) return false;
        if (postCode2 != null ? !postCode2.equals(address1.postCode2) : address1.postCode2 != null) return false;
        if (salutation != null ? !salutation.equals(address1.salutation) : address1.salutation != null) return false;
        if (taxNumber != null ? !taxNumber.equals(address1.taxNumber) : address1.taxNumber != null) return false;
        if (title != null ? !title.equals(address1.title) : address1.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = empty != null ? empty.hashCode() : 0;
        result = 31 * result + (salutation != null ? salutation.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (firstname != null ? firstname.hashCode() : 0);
        result = 31 * result + (middlename != null ? middlename.hashCode() : 0);
        result = 31 * result + (lastname != null ? lastname.hashCode() : 0);
        result = 31 * result + (organization != null ? organization.hashCode() : 0);
        result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (addressSupplement != null ? addressSupplement.hashCode() : 0);
        result = 31 * result + (postCode != null ? postCode.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (phonePrivate != null ? phonePrivate.hashCode() : 0);
        result = 31 * result + (phoneBusiness != null ? phoneBusiness.hashCode() : 0);
        result = 31 * result + (phoneMobile != null ? phoneMobile.hashCode() : 0);
        result = 31 * result + (fax != null ? fax.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (address2 != null ? address2.hashCode() : 0);
        result = 31 * result + (addressSupplement2 != null ? addressSupplement2.hashCode() : 0);
        result = 31 * result + (city2 != null ? city2.hashCode() : 0);
        result = 31 * result + (postCode2 != null ? postCode2.hashCode() : 0);
        result = 31 * result + (country2 != null ? country2.hashCode() : 0);
        result = 31 * result + (phonePrivate2 != null ? phonePrivate2.hashCode() : 0);
        result = 31 * result + (phoneBusiness2 != null ? phoneBusiness2.hashCode() : 0);
        result = 31 * result + (phoneMobile2 != null ? phoneMobile2.hashCode() : 0);
        result = 31 * result + (fax2 != null ? fax2.hashCode() : 0);
        result = 31 * result + (email2 != null ? email2.hashCode() : 0);
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        result = 31 * result + (taxNumber != null ? taxNumber.hashCode() : 0);
        return result;
    }
}