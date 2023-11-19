/*
 * NewsLanguageUtil.java
 *
 * Created on 07.11.2008 08:56:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.news.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsLanguageUtil {
    private static final String EN = "en";

    private static final String DE = "de";

    private static final String IT = "it";

    private static final String FR = "fr";

    private static final String NL = "nl";

    private static final Map<String, String> LANGUAGE_BY_AGENCY = new HashMap<>();

    static {
        // GERMAN
        LANGUAGE_BY_AGENCY.put("FTD", DE);
        LANGUAGE_BY_AGENCY.put("VWDZSS", DE);
        LANGUAGE_BY_AGENCY.put("HUGINNEWS", DE);
        LANGUAGE_BY_AGENCY.put("PR_SCHRODER", DE);
        LANGUAGE_BY_AGENCY.put("PR_BANTLEON", DE);
        LANGUAGE_BY_AGENCY.put("GENO", DE);
        LANGUAGE_BY_AGENCY.put("BUBA", DE);

        // ENGLISH
        LANGUAGE_BY_AGENCY.put("JAN_PETERS", EN);
        LANGUAGE_BY_AGENCY.put("ECB", EN);

        // ITALIAN
        LANGUAGE_BY_AGENCY.put("MF-DJ", IT);
    }

    private NewsLanguageUtil() {
    }

    /**
     * Returns iso-639-1 language code (2 characters, lower case) for the news.
     * @return iso-639-1 code or null for unknown language
     */
    public static String getLanguage(NewsRecordImpl nr) {
        final String ndbSprache = nr.getString(VwdFieldDescription.NDB_Sprache);
        // the content of ndbSprache is a mess, it is inconsistent for different agencies and
        // changed over time, so let's try to figure out the appropriate code:
        final String agency = nr.getAgency();
        if ("DJN".equals(agency)) {
            return getDJNLanguage(nr);
        }

        if (ndbSprache == null) {
            return LANGUAGE_BY_AGENCY.get(agency);
        }

        final String lang = ndbSprache.toLowerCase();
        if ("german".equals(lang)) {
            return DE;
        }
        if ("english".equals(lang)) {
            return EN;
        }
        return lang.length() == 2 ? lang : null;
    }

    private static String getDJNLanguage(NewsRecordImpl nr) {
        final Set<String> categories = nr.getAttributes().get(NewsAttributeEnum.CATEGORY);
        if (categories == null) {
            return null; // we don't know
        }
        if (!categories.contains("N/NENG")) { // !non-english => english
            return EN;
        }
        if (categories.contains("N/GERM")) {
            return DE;
        }
        if (categories.contains("N/ITAL")) {
            return IT;
        }
        if (categories.contains("N/FRE")) {
            return FR;
        }
        if (categories.contains("N/DUTC")) {
            return NL;
        }
        return null;
    }
}
