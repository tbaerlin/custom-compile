/*
 * CustomerBankhausLampe.java
 *
 * Created on 04.12.2014 16:10
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.advisorysolution;

/**
 * @author mdick
 */
public class CustomerBankhausLampe extends CustomerAS {
    @Override
    public String getOrderEntryDepotChoiceFilterName() {
        return "BHL"; // $NON-NLS$
    }

    @Override
    public String getOrderEntryAccountChoiceFilterName() {
        return "BHL"; // $NON-NLS$
    }
}
