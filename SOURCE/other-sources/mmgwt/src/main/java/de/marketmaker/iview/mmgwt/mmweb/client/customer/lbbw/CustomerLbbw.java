package de.marketmaker.iview.mmgwt.mmweb.client.customer.lbbw;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;

/**
 * Created on 30.05.12 14:31
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class CustomerLbbw extends Customer {

    @Override
    public boolean isPreferredFinderIssuer(String issuer) {
        return issuer.equals("LBBW"); // $NON-NLS$
    }

    @Override
    public String getCustomPageTypeString() {
        return I18n.I.overview();
    }
}