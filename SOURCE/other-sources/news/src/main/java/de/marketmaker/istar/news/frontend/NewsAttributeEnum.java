/*
 * NewsAttributeEnum.java
 *
 * Created on 06.03.2007 17:25:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.frontend;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Enum for fields that contain multiple comma separated values
 * 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum NewsAttributeEnum {

    // lowercase names are used as lucene fieldnames
    // @see NewsIndexConstants
    IID(VwdFieldDescription.MMF_Iid_List),
    ISIN(VwdFieldDescription.NDB_ISINList),
    WKN(VwdFieldDescription.NDB_Wpknlist),
    COUNTRY(VwdFieldDescription.NDB_Country),
    SECTOR(VwdFieldDescription.NDB_Branch),
    CATEGORY(VwdFieldDescription.NDB_Rubrik),
    SELECTOR(VwdFieldDescription.NDB_Selectors),
    IPTC_CODE(VwdFieldDescription.NDB_IPTC_Code),
    TELEKURS(VwdFieldDescription.NDB_Telekurs_Nummer),
    MARKET_SECTOR(VwdFieldDescription.NDB_Market_Sector),
    GENRE(VwdFieldDescription.NDB_Genre),
    WIRE(VwdFieldDescription.NDB_Wire),
    FINANCIAL(VwdFieldDescription.NDB_Financial_News),
    CROSS_MKT(VwdFieldDescription.NDB_General_Cross_Mkt),
    RTRS_CODE(VwdFieldDescription.NDB_Reuters_Codes),
    FIXED_PAGE_CODE(VwdFieldDescription.NDB_Fixed_Page_Code),
    GOVERNMENT(VwdFieldDescription.NDB_Government),
    ;

    private final VwdFieldDescription.Field field;

    // attributes that need to be searchable with fieldname "providercode"
    public static final Set<NewsAttributeEnum> PROVIDER_CODE_ATTRIBUTES = Collections.unmodifiableSet(
            EnumSet.of(
                    COUNTRY,
                    SECTOR,
                    CATEGORY,
                    MARKET_SECTOR,
                    FIXED_PAGE_CODE,
                    GOVERNMENT));

    public static NewsAttributeEnum byField(VwdFieldDescription.Field f) {
        return Arrays.stream(NewsAttributeEnum.values())
                .filter(e -> e.field == f)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(f.name()));
    }

    NewsAttributeEnum(VwdFieldDescription.Field field) {
        this(field, false);
    }

    NewsAttributeEnum(VwdFieldDescription.Field field, boolean providerCode) {
        this.field = field;
    }

    public VwdFieldDescription.Field getField() {
        return this.field;
    }

}
