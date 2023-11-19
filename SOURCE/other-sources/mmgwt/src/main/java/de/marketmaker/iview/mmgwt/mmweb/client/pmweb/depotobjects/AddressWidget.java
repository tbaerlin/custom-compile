/*
 * AddressWidget.java
 *
 * Created on 21.03.13 11:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Address;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractUserObjectView.*;

/**
 * @author Markus Dick
 */
public class AddressWidget extends Composite implements TakesValue<Address>{
    private static final String SECOND_ENTRY = " 2"; //$NON-NLS$

    private final Panel layout;
    private Address address;

    public AddressWidget() {
        this.address = null;
        this.layout = new FlowPanel();
        initWidget(this.layout);
    }

    @Override
    public void setValue(Address value) {
        this.address = value;
        update();
    }

    @Override
    public Address getValue() {
        return this.address;
    }

    private void update() {
        final Address a = this.address;
        this.layout.clear();

        if(this.address == null) return;

        addAddressPanel(a);
        addAdditionalAddressPanel(a);
        addContactPanel(a);
        addOthers(a);
    }

    private void addAddressPanel(Address a) {
        HasWidgets hw = this.layout;

        addSubHeading(hw, I18n.I.address());

        addField(hw, I18n.I.salutation(), a.getSalutation());
        addField(hw, I18n.I.titleName(), a.getTitle());
        addField(hw, I18n.I.firstName(), a.getFirstname());
        addField(hw, I18n.I.middleName(), a.getMiddlename());
        addField(hw, I18n.I.lastName(), a.getLastname());
        addField(hw, I18n.I.dateOfBirth(), PmRenderers.DATE_STRING.render(a.getDateOfBirth()));
        addField(hw, I18n.I.organization(), a.getOrganization());
        addField(hw, I18n.I.address(), a.getAddress());
        addField(hw, I18n.I.addressSupplement(), a.getAddressSupplement());
        addField(hw, I18n.I.postCode(), a.getPostCode());
        addField(hw, I18n.I.city(), a.getCity());
        addField(hw, I18n.I.country(), a.getCountry());
    }

    private void addAdditionalAddressPanel(Address a) {
        if(!(StringUtil.hasText(a.getAddress2()) &&
                StringUtil.hasText(a.getPostCode2()) &&
                StringUtil.hasText(a.getCity2()) &&
                StringUtil.hasText(a.getCountry2()))) {
            return;
        }

        final HasWidgets hw = this.layout;

        addSubHeading(hw, I18n.I.additionalAddress());

        addField(hw, I18n.I.address(), a.getAddress2());
        addField(hw, I18n.I.addressSupplement(), a.getAddressSupplement2());
        addField(hw, I18n.I.postCode(), a.getPostCode2());
        addField(hw, I18n.I.city(), a.getCity2());
        addField(hw, I18n.I.country(), a.getCountry2());
    }

    private void addContactPanel(Address a) {
        final HasWidgets hw = this.layout;

        addSubHeading(hw, I18n.I.contact());

        addField(hw, I18n.I.telephoneNumberPrivateAbbr(), a.getPhonePrivate());
        if(StringUtil.hasText(a.getPhonePrivate2())) {
            addField(hw, I18n.I.telephoneNumberPrivateAbbr() + SECOND_ENTRY, a.getPhonePrivate2());
        }

        addField(hw, I18n.I.telephoneNumberBusinessAbbr(), a.getPhoneBusiness());
        if(StringUtil.hasText(a.getPhoneBusiness2())) {
            addField(hw, I18n.I.telephoneNumberBusinessAbbr() + SECOND_ENTRY, a.getPhoneBusiness2());
        }

        addField(hw, I18n.I.telephoneNumberMobileAbbr(), a.getPhoneMobile());
        if(StringUtil.hasText(a.getPhoneMobile2())) {
            addField(hw, I18n.I.telephoneNumberMobileAbbr() + SECOND_ENTRY, a.getPhoneMobile2());
        }

        addField(hw, I18n.I.telefaxNumberAbbr(), a.getFax());
        if(StringUtil.hasText(a.getFax2())) {
            addField(hw, I18n.I.telefaxNumberAbbr() + SECOND_ENTRY, a.getFax2());
        }

        addField(hw, I18n.I.email(), createMailLink(a.getEmail()));
        if(StringUtil.hasText(a.getEmail2())) {
            addField(hw, I18n.I.email() + SECOND_ENTRY, createMailLink(a.getEmail2()));
        }
    }

    private Widget createMailLink(String eMailAddress) {
        if(StringUtil.hasText(eMailAddress)) {
            final Anchor a = new Anchor(eMailAddress, "mailto:" + eMailAddress); //$NON-NLS$
            a.setTitle(eMailAddress);
            return a;
        }
        return new HTML(""); //$NON-NLS$
    }

    private void addOthers(Address a) {
        final HasWidgets hw = this.layout;

        addSubHeading(hw, I18n.I.others());

        addField(hw, I18n.I.taxNumberAbbr(), a.getTaxNumber());
        addMultilineField(hw, I18n.I.comment(), a.getComment());
    }
}
