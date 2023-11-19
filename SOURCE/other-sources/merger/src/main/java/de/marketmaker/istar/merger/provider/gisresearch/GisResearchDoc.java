/*
 * GisResearchDoc.java
 *
 * Created on 17.04.14 14:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.DateTime;

/**
 * @author oflege
 */
public class GisResearchDoc implements Serializable, Comparable<GisResearchDoc> {
    static final long serialVersionUID = 2L;

    private static final Comparator<GisResearchDoc> BY_NAME = (o1, o2) -> o1.name.compareTo(o2.name);

    static final Comparator<GisResearchDoc> BY_DATE = (o1, o2) -> {
        int cmp = Long.compare(o1.publicationDate, o2.publicationDate);
        return cmp != 0 ? cmp : BY_NAME.compare(o1, o2);
    };

    static final Comparator<GisResearchDoc> BY_RECOMMENDATION = new Comparator<GisResearchDoc>() {
        @Override
        public int compare(GisResearchDoc o1, GisResearchDoc o2) {
            final int cmp = Integer.compare(getRecommendationIndex(o1), getRecommendationIndex(o2));
            return (cmp != 0) ? cmp : -BY_DATE.compare(o1, o2);
        }

        private int getRecommendationIndex(GisResearchDoc d) {
            if (d.getRecommendation() == null) {
                return -1;
            }
            switch (d.getRecommendation()) {
                case "1":  // kaufen
                    return 4;
                case "5": // halten
                    return 3;
                case "4": // verkaufen
                    return 2;
                case "0": // nicht bewertet
                    return 1;
                default:
                    return 0;
            }
        }
    };

    static final Comparator<GisResearchDoc> BY_SECTOR = (o1, o2) -> {
        final int cmp = doCompare(o1.getSector(), o2.getSector());
        return (cmp != 0) ? cmp : -BY_DATE.compare(o1, o2);
    };

    static final Comparator<GisResearchDoc> BY_TITLE = (o1, o2) -> {
        final int cmp = doCompare(o1.getTitle(), o2.getTitle());
        return (cmp != 0) ? cmp : -BY_DATE.compare(o1, o2);
    };

    static final Comparator<GisResearchDoc> BY_ASSET_CLASS = (o1, o2) -> {
        DocumentType type1 = o1.getDocumentType();
        DocumentType type2 = o2.getDocumentType();

        if (type1 == null && type2 == null) {
            return -BY_DATE.compare(o1, o2);
        }
        else if (type1 == null) {
            return 1;
        }
        else if (type2 == null) {
            return -1;
        }
        else {
            final int cmp = doCompare(type1.getAssetClass(), type2.getAssetClass());
            return (cmp != 0) ? cmp : -BY_DATE.compare(o1, o2);
        }
    };

    private static <T extends Comparable<T>> int doCompare(T d1, T d2) {
        if (d1 == null) {
            return (d2 == null) ? 0 : -1;
        }
        if (d2 == null) {
            return 1;
        }
        return d1.compareTo(d2);
    }

    private static final Map<String, String> RECOMMENDATIONS = new HashMap<>();

    static {
        RECOMMENDATIONS.put("0", "Nicht bewertet");
        RECOMMENDATIONS.put("1", "Kaufen");
        RECOMMENDATIONS.put("4", "Verkaufen");
        RECOMMENDATIONS.put("5", "Halten");
    }


    private String name;

    private String researchId;

    private long start;

    private long end;

    private List<String> isins;

    private DocumentType documentType;

    private List<GisResearchIssuer> issuers;

    private List<String> countries;

    private List<String> indexes;

    private String sector;

    private String recommendation;

    private String riskGroup;

    private String title;

    private long publicationDate;

    GisResearchDoc() {
        // for testing
    }

    GisResearchDoc(ControlFile cf, String sector) {
        this.name = cf.name;
        this.researchId = cf.researchId;
        this.start = cf.start.getMillis();
        this.end = cf.end.getMillis();
        this.publicationDate = cf.publicationDate.getMillis();
        this.isins = cf.isins;
        this.documentType = cf.documentType;
        this.issuers = cf.issuers;
        this.sector = sector;
        this.countries = cf.countries;
        this.indexes = cf.indexes;
        this.recommendation = cf.recommendation;
        this.riskGroup = cf.riskGroup;
        this.title = cf.title;
    }

    @Override
    public int compareTo(GisResearchDoc o) {
        return -BY_DATE.compare(this, o); // by desc. date
    }

    public String getName() {
        return name;
    }

    public String getResearchId() {
        return researchId;
    }

    public DateTime getStart() {
        return new DateTime(start);
    }

    public DateTime getEnd() {
        return new DateTime(this.end);
    }

    public List<String> getIsins() {
        return isins;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public List<GisResearchIssuer> getIssuers() {
        return issuers;
    }

    public List<String> getCountries() {
        return countries;
    }

    public List<String> getCountryNames(Locale locale) {
        if (this.countries.isEmpty()) {
            return this.countries;
        }
        return this.countries.stream()
                .map(country -> new Locale("", country).getDisplayCountry(locale))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<String> getIndexes() {
        return indexes;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public String getRecommendationText() {
        if (this.recommendation == null) {
            return null;
        }
        final String result = RECOMMENDATIONS.get(this.recommendation);
        return (result != null) ? result : ("Unbekannt(" + this.recommendation + ")");
    }

    public String getRiskGroup() {
        return riskGroup;
    }

    public String getTitle() {
        return title;
    }

    public DateTime getPublicationDate() {
        return new DateTime(publicationDate);
    }

    public String getSector() {
        return sector;
    }

    /*
     * SETTERS FOR TESTING ONLY
     */

    void setName(String name) {
        this.name = name;
    }

    void setResearchId(String researchId) {
        this.researchId = researchId;
    }

    void setStart(long start) {
        this.start = start;
    }

    void setEnd(long end) {
        this.end = end;
    }

    void setIsins(List<String> isins) {
        this.isins = isins;
    }

    void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    void setIssuers(List<GisResearchIssuer> issuers) {
        this.issuers = issuers;
    }

    void setCountries(List<String> countries) {
        this.countries = countries;
    }

    void setIndexes(List<String> indexes) {
        this.indexes = indexes;
    }

    void setSector(String sector) {
        this.sector = sector;
    }

    void setRecommendation(String recommendation) {
        this.recommendation = recommendation;
    }

    void setRiskGroup(String riskGroup) {
        this.riskGroup = riskGroup;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setPublicationDate(long publicationDate) {
        this.publicationDate = publicationDate;
    }

    @Override
    public String toString() {
        return "GisResearchDoc{" +
                "researchId='" + researchId + '\'' +
                ", name='" + name + '\'' +
                ", start=" + new DateTime(start) +
                ", end=" + new DateTime(end) +
                ", isins=" + isins +
                ", documentType=" + documentType +
                ", issuers=" + issuers +
                ", countries=" + countries +
                ", indexes=" + indexes +
                ", sector=" + sector +
                ", recommendation='" + recommendation + '\'' +
                ", riskGroup='" + riskGroup + '\'' +
                ", title='" + title + '\'' +
                ", publicationDate=" + publicationDate +
                '}';
    }
}
