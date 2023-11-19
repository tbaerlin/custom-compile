/*
 * CustomerAS.java
 *
 * Created on 04.12.2014 16:10
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.advisorysolution;

import java.util.function.Supplier;

import com.google.gwt.user.client.ui.Image;

import de.marketmaker.iview.mmgwt.mmweb.client.as.AsMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;

/**
 * A basic Customer for advisory solution customers.
 * Advisory solution installations do not use any zone specific guidefs.
 * However, this specific customer enables us to provide some basic web-zone specific customizations.
 *
 * @author mdick
 */
public class CustomerAS extends Customer {
    @Override
    public boolean isCustomerAS() {
        return true;
    }

    @Override
    public CustomerAS asCustomerAS() {
        return this;
    }

    public String getOrderEntryDepotChoiceFilterName() {
        return null;
    }

    public String getOrderEntryAccountChoiceFilterName() {
        return null;
    }

    public boolean isOrderEntryWithDmXmlSymbolSearch() {
        return false;
    }

    public boolean isXlsExportForTableLayouts() {
        return true;
    }

    /**
     * Returns an {@linkplain UnsupportedOperationException} to indicate that tis method is of no
     * use for Infront Advisory Solution. This method is not called by the AsMainController, because
     * logo resolution is for all advisory customers the same and handled by
     * {@linkplain de.marketmaker.iview.mmgwt.mmweb.client.as.AsRightLogoSupplier}.
     * This supplier is directly injected into the toolbar by {@linkplain AsMainController#createView()}
     * without using this method.
     *
     * @return nothing
     * @throws UnsupportedOperationException
     */
    @Override
    public Supplier<Image> getRightLogoSupplier() {
        throw new UnsupportedOperationException("Logo suppliers are not supported by Infront Advisory Solution customers"); // $NON-NLS$
    }
}
