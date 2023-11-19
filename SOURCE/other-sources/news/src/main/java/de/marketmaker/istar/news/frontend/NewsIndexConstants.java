/*
 * NewsIndexConstants.java
 *
 * Created on 15.03.2007 12:25:15
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.frontend;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public final class NewsIndexConstants {

    public static final String FIELD_ID = "id";

    public static final String FIELD_SHORTID = "shortid";

    public static final String FIELD_TIMESTAMP = "timestamp";

    public static final String FIELD_AGENCY = "agency";

    public static final String FIELD_AGENCY_PROVIDED_ID = "agencyProvidedId";

    public static final String FIELD_RATING_ID = "ratingId";

    public static final String FIELD_PREVIOUS_ID = "previousId";

    public static final String FIELD_IID = "iid";

    public static final String FIELD_SYMBOL = "symbol";

    public static final String FIELD_HEADLINE = "headline";

    public static final String FIELD_TEASER = "teaser";

    public static final String FIELD_TEXT = "text";

    public static final String FIELD_LANGUAGE = "language";

    public static final String FIELD_SUPPLIER = "supplier";

    public static final String FIELD_PRIORITY = "priority";

    public static final String FIELD_TOPIC = "topic";

    public static final String FIELD_SEQ_NO = "seqno";

    public static final String FIELD_AD = "ad";

    public static final String FIELD_MIMETYPE = "mimetype";

    /** contains a set of  {@see NewsAttributeEnum.PROVIDER_CODE_ATTRIBUTES}  */
    public static final String FIELD_PROVIDERCODE = "providercode";

    /** used for field name resolving, we rewrite some fieldnames in the queries */
    private static final Map<String, String> FIELD_NAME_MAPPINGS = new HashMap<>();

    /** the lucene field names to be used for each element of {@link NewsAttributeEnum} */
    public static final Map<NewsAttributeEnum, String> ATTRIBUTE_2_FIELDNAME;

    static {
        // map enum values to their lowercase names
        final Map<NewsAttributeEnum, String> m = new EnumMap<>(NewsAttributeEnum.class);
        for (NewsAttributeEnum anEnum : NewsAttributeEnum.values()) {
            m.put(anEnum, anEnum.name().toLowerCase());
        }
        ATTRIBUTE_2_FIELDNAME = Collections.unmodifiableMap(m);
        NewsIndexConstants.ATTRIBUTE_2_FIELDNAME.forEach((f, name) -> addMapping(f.getField(), name));
    }

    private static void addMapping(VwdFieldDescription.Field field, String name) {
        final String key = field.name().toLowerCase();
        FIELD_NAME_MAPPINGS.put(key, name);
        FIELD_NAME_MAPPINGS.put(key.substring(4), name); // w/o "NDB_"-prefix
    }

    public static final String FIELD_SELECTOR = NewsIndexConstants.ATTRIBUTE_2_FIELDNAME.get(NewsAttributeEnum.SELECTOR);

    public static String resolveFieldname(String fieldname) {
        final String normalized = fieldname.toLowerCase();
        return FIELD_NAME_MAPPINGS.getOrDefault(normalized, normalized);
    }
}
