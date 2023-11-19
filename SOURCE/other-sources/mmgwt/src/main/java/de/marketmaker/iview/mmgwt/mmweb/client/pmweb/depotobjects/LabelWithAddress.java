/*
 * LabelWithAddress.java
 *
 * Created on 14.03.13 09:51
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.PopupPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Address;

/**
 * @author Markus Dick
 */
public class LabelWithAddress extends AbstractLabelWithPopup<Address> {
    public LabelWithAddress(String label, Address address) {
        super(label, address);
    }

    @Override
    protected void updatePopupPanel() {
        final Address a = getPopupData();
        final PopupPanel p = getPopupPanel();
        p.clear();

        final FlexTable t = new FlexTable();
        p.add(t);

        int nextRow = add(t, 0, a.getAddress());
        nextRow = add(t, nextRow, a.getPostCode() + " " + a.getCity());
        nextRow = add(t, nextRow, a.getCountry());
        nextRow = add(t, nextRow, I18n.I.telephoneNumberPrivateAbbrShort(), a.getPhonePrivate());
        nextRow = add(t, nextRow, I18n.I.telephoneNumberMobileAbbrShort(), a.getPhoneMobile());
        nextRow = add(t, nextRow, I18n.I.telephoneNumberBusinessAbbrShort(), a.getPhoneBusiness());
        nextRow = add(t, nextRow, I18n.I.telefaxNumberAbbrShort(), a.getFax());
        nextRow = add(t, nextRow, I18n.I.email(), a.getEmail());
        add(t, nextRow, I18n.I.taxNumberAbbr(), a.getTaxNumber());
    }
}
