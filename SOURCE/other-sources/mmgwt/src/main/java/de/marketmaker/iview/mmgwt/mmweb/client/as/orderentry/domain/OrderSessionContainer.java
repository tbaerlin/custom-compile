package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.domain;

import de.marketmaker.iview.pmxml.AllocateOrderSessionContainerDataResponse;

/**
 * Created on 30.10.12 10:20
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class OrderSessionContainer {

    private final String handle;

    public OrderSessionContainer(AllocateOrderSessionContainerDataResponse response) {
        this.handle = response.getHandle();
    }

    public String getHandle() {
        return this.handle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderSessionContainer that = (OrderSessionContainer) o;

        return !(this.handle != null ? !this.handle.equals(that.handle) : that.handle != null);
    }

    @Override
    public int hashCode() {
        return this.handle != null ? this.handle.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "OrderSessionContainer { handle='" + this.handle + "' }"; //$NON-NLS$
    }
}
