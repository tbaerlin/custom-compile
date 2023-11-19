/*
 * CustomerVwd.java
 *
 * Created on 2/11/16
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.vwd;

import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;

/**
 * @author Stefan Willenbrock
 */
public class CustomerVwd extends Customer {
    @Override
    public boolean isVwd() {
        return true;
    }
}