package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.math.BigDecimal;

/**
 * Constants for index keys.
 * @author mcoenen
 */
public final class LbbwResearchIndexConstants {

    static final String FIELD_ID = "id";

    public static final String FIELD_SELECTOR = "selector";

    public static final String FIELD_PUBLICATION_DATE = "publicationDate";

    public static final String FIELD_DOCUMENT_TYPE = "documentType";

    public static final String FIELD_CATEGORY = "category";

    static final String FIELD_FILENAME = "fileName";

    public static final String FIELD_LANGUAGE = "language";

    public static final String FIELD_RATING = "rating";

    static final String FIELD_PREVIOUS_RATING = "previousRating";

    public static final String FIELD_TARGET_PRICE = "targetPrice";

    static final String FIELD_PREVIOUS_TARGET_PRICE = "previousTargetPrice";

    public static final String FIELD_TEXT = "text";

    public static final String FIELD_TITLE = "title";

    public static final String FIELD_ISIN = "isin";

    public static final String FIELD_NAME = "name";

    public static final String FIELD_SECTOR = "sector";

    public static final String FIELD_COUNTRY = "country";

    static final String FIELD_COMPANY_GUIDANCE = "companyGuidance";

    public static final BigDecimal TARGET_PRICE_FACTOR = BigDecimal.valueOf(10000L);
}
