/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.CdsDataRecord;
import org.joda.time.DateTime;

import java.math.BigDecimal;

public class CdsDataRecordImpl implements CdsDataRecord {
    private String productCategory;
    private String produktcharakteristika;
    private String wpNameKurz;

    @Override
    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    @Override
    public String getProduktcharakteristika() {
        return produktcharakteristika;
    }

    public void setProduktcharakteristika(String produktcharakteristika) {
        this.produktcharakteristika = produktcharakteristika;
    }

    @Override
    public String getWpNameKurz() {
        return wpNameKurz;
    }

    public void setWpNameKurz(String wpNameKurz) {
        this.wpNameKurz = wpNameKurz;
    }


    @Override
    public String toString() {
        return "CdsDataRecordImpl{" +
            "productCategory='" + productCategory + '\'' +
            ", produktcharakteristika='" + produktcharakteristika + '\'' +
            ", wpNameKurz='" + wpNameKurz + '\'' +
            '}';
    }
}
