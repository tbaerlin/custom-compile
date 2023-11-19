package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

/**
 * Parsed response
 * @author mcoenen
 */
public class BasicDocument implements Serializable, Comparable<BasicDocument> {

    static final long serialVersionUID = 2L;

    public static final String SORT_BY_TITLE = "title";

    public static final String SORT_BY_PUBLICATION_DATE = "publicationDate";

    public static final String SORT_BY_CATEGORY = "category";

    public static final String SORT_BY_RATING = "rating";

    public static final String SORT_BY_TARGET_PRICE = "targetPrice";

    public static final int BUY_VALUE = 3;

    public static final int HOLD_VALUE = 2;

    public static final int SELL_VALUE = 1;

    public static final int NO_VALUE = 0;

    static final Map<Integer, String> RATING_NAMES =
            ImmutableMap.<Integer, String>builder()
                    .put(NO_VALUE, "NOT RATED")
                    .put(BUY_VALUE, "BUY")
                    .put(HOLD_VALUE, "HOLD")
                    .put(SELL_VALUE, "SELL")
                    .build();

    private static final Comparator<BasicDocument> BY_TITLE = Comparator.comparing(o -> o.title);

    private static final Comparator<BasicDocument> BY_DATE = (o1, o2) -> {
        int cmp = o1.publicationDate.compareTo(o2.publicationDate);
        return cmp != 0 ? cmp : BY_TITLE.compare(o1, o2);
    };

    private static final Comparator<BasicDocument> BY_CATEGORY = (o1, o2) -> {
        int cmp = o1.category.compareTo(o2.category);
        return cmp != 0 ? cmp : -BY_DATE.compare(o1, o2);
    };

    private static final Comparator<BasicDocument> BY_RATING = (o1, o2) -> {
        int cmp = Integer.compare(o1.rating, o2.rating);
        return cmp != 0 ? cmp : -BY_DATE.compare(o1, o2);
    };

    /* TODO: right now document without target prices will be at the top, should we change it? */
    private static final Comparator<BasicDocument> BY_TARGET_PRICE =
            Comparator.comparing((BasicDocument doc) -> doc.getDecimalTargetPrice().orElse(BigDecimal.valueOf(NO_VALUE)))
                    .thenComparing(BY_DATE.reversed());

    static final Map<String, Comparator<BasicDocument>> COMPARATORS =
            ImmutableMap.<String, Comparator<BasicDocument>>builder()
                    .put(SORT_BY_TITLE, BY_TITLE)
                    .put(SORT_BY_PUBLICATION_DATE, BY_DATE)
                    .put(SORT_BY_CATEGORY, BY_CATEGORY)
                    .put(SORT_BY_RATING, BY_RATING)
                    .put(SORT_BY_TARGET_PRICE, BY_TARGET_PRICE)
                    .put("", BY_DATE.reversed())
                    .build();

    private String objectId;

    private String title;

    private String filename;

    private String language;

    private String documentType;

    private String category;

    private String sector;

    private OffsetDateTime publicationDate;

    private transient Set<String> companyObjectIds = Collections.emptySet();

    private Set<CompanyInfo> companyInfos = Collections.emptySet();

    private int rating = BasicDocument.NO_VALUE;

    private int previousRating = BasicDocument.NO_VALUE;

    private BigDecimal decimalTargetPrice;

    private BigDecimal previousDecimalTargetPrice;

    @Override
    public int compareTo(BasicDocument o) {
        return BY_DATE.reversed().compare(this, o); // by desc. date
    }

    public String getObjectId() {
        return objectId;
    }

    void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    void setTitle(String title) {
        this.title = title;
    }

    public int getRating() {
        return this.rating;
    }

    void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getLanguage() {
        return language;
    }

    void setLanguage(String language) {
        this.language = language;
    }

    public String getDocumentType() {
        return documentType;
    }

    void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public OffsetDateTime getPublicationDate() {
        return publicationDate;
    }

    void setPublicationDate(String publicationDate) {
        this.publicationDate = OffsetDateTime.parse(publicationDate);
    }

    void setPublicationDate(OffsetDateTime publicationDate) {
        this.publicationDate = publicationDate;
    }

    Set<String> getCompanyObjectIds() {
        return companyObjectIds;
    }

    void setCompanyObjectIds(Set<String> companyObjectIds) {
        this.companyObjectIds = companyObjectIds;
    }

    public String getFilename() {
        return filename;
    }

    void setFilename(String filename) {
        this.filename = filename;
    }

    public Set<CompanyInfo> getCompanyInfos() {
        return companyInfos;
    }

    void setCompanyInfos(Set<CompanyInfo> companyInfos) {
        this.companyInfos = companyInfos;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    @Override
    public String toString() {
        return "BasicDocument{" +
                "objectId='" + objectId + '\'' +
                ", title='" + title + '\'' +
                ", filename='" + filename + '\'' +
                ", language='" + language + '\'' +
                ", documentType='" + documentType + '\'' +
                ", publicationDate=" + publicationDate +
                ", companyObjectIds=" + companyObjectIds +
                ", companyInfos=" + companyInfos +
                ", rating='" + rating + '\'' +
                ", previousRating='" + previousRating + '\'' +
                ", targetPrice=" + decimalTargetPrice +
                ", previousTargetPrice=" + previousDecimalTargetPrice +
                ", sector=" + sector +
                '}';
    }

    public int getPreviousRating() {
        return this.previousRating;
    }

    void setPreviousRating(Integer previousRating) {
        this.previousRating = previousRating;
    }

    public String getCategory() {
        return category;
    }

    void setCategory(String category) {
        this.category = category;
    }

    public Optional<BigDecimal> getPreviousDecimalTargetPrice() {
        return Optional.ofNullable(previousDecimalTargetPrice);
    }

    public void setPreviousDecimalTargetPrice(BigDecimal decimalPreviousTargetPrice) {
        this.previousDecimalTargetPrice = decimalPreviousTargetPrice;
    }

    public Optional<BigDecimal> getDecimalTargetPrice() {
        return Optional.ofNullable(decimalTargetPrice);
    }

    public void setDecimalTargetPrice(BigDecimal decimalTargetPrice) {
        this.decimalTargetPrice = decimalTargetPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicDocument that = (BasicDocument) o;

        return objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return objectId.hashCode();
    }
}
