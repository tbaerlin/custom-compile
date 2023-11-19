/*
 * PriceWithSupplement.java
 *
 * Created on 06.08.2008 13:38:34
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.data;

/**
 * @author Ulrich Maurer
 */
public class PriceWithSupplement {
    public static final PriceWithSupplement NULL = new PriceWithSupplement(null, null);
    
    private String price;
    private String supplement;

    public PriceWithSupplement(String price, String supplement) {
        this.price = price;
        this.supplement = supplement;
    }

    public String getPrice() {
        return price;
    }

    public String getSupplement() {
        return supplement;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setSupplement(String supplement) {
        this.supplement = supplement;
    }

    @Override
    public String toString() {
        return this.price + (this.supplement != null ? this.supplement : ""); // $NON-NLS-0$
    }
}
