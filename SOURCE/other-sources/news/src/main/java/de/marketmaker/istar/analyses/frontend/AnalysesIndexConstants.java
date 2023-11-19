/*
 * AnalysesIndexConstants.java
 *
 * Created on 23.03.12 12:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.frontend;

/**
 * contains the field ids that are used in the lucene search index for analyses
 *
 * @author oflege
 */
public interface AnalysesIndexConstants {

    String FIELD_ID = "id";

    String FIELD_IID = "iid";

    String FIELD_SOURCE = "source";

    String FIELD_RECOMMENDATION = "recommendation";

    String FIELD_AGENCY_ID = "agency_id";

    String FIELD_DATE = "date";

    String FIELD_SYMBOL = "symbol";

    String FIELD_ANALYST = "analyst";

    String FIELD_BRANCH = "branch";

    String FIELD_COUNTRY = "country";

    String FIELD_CATEGORY = "category";

    String FIELD_SUBCATEGORY = "subcategory";

    // fulltext search

    String FIELD_HEADLINE = "headline";

    String FIELD_TEXT = "text";

    // specific provider

    String FIELD_RACCFOND = "raccfond";

    String FIELD_RACCTECN = "racctecn";

    String FIELD_TARGET = "target";

    String FIELD_TIMEFRAME = "timeframe";

}
