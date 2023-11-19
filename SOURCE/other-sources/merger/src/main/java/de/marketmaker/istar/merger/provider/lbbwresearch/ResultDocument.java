package de.marketmaker.istar.merger.provider.lbbwresearch;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

/**
 * DTO from Provider to dm[xml] block to remove non-used fields and decouple
 * from original indexer object.
 */
public class ResultDocument implements Serializable {

    static final long serialVersionUID = 3L;

    private final String objectId;

    private final String filename;

    private final String title;

    private final OffsetDateTime publicationDate;

    private final String documentType;

    private final String category;

    private final int rating;

    private final BigDecimal targetPrice;

    private final String language;

    private final Set<CompanyInfo> companyInfos;

    private final String sector;

    ResultDocument(BasicDocument basicDocument) {
        this.objectId = basicDocument.getObjectId();
        this.filename = basicDocument.getFilename();
        this.title = basicDocument.getTitle().orElse(null);
        this.publicationDate = basicDocument.getPublicationDate();
        this.documentType = basicDocument.getDocumentType();
        this.category = basicDocument.getCategory();
        this.rating = basicDocument.getRating();
        this.targetPrice = basicDocument.getDecimalTargetPrice().orElse(null);
        this.language = basicDocument.getLanguage();
        this.sector = basicDocument.getSector();
        this.companyInfos = ImmutableSet.copyOf(
                basicDocument.getCompanyInfos().stream()
                        .map(CompanyInfo::new)
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResultDocument that = (ResultDocument) o;

        return objectId.equals(that.objectId);
    }

    @Override
    public int hashCode() {
        return objectId.hashCode();
    }

    public String getObjectId() {
        return objectId;
    }

    public String getFilename() {
        return filename;
    }

    public String getTitle() {
        return title;
    }

    public OffsetDateTime getPublicationDate() {
        return publicationDate;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getCategory() {
        return category;
    }

    public int getRating() {
        return rating;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public String getLanguage() {
        return language;
    }

    public String getSector() {
        return sector;
    }

    public Set<CompanyInfo> getCompanyInfos() {
        return companyInfos;
    }

    @Override
    public String toString() {
        return "ResultDocument{" +
                "objectId='" + objectId + '\'' +
                ", filename='" + filename + '\'' +
                ", title='" + title + '\'' +
                ", publicationDate=" + publicationDate +
                ", documentType='" + documentType + '\'' +
                ", category='" + category + '\'' +
                ", rating=" + rating +
                ", targetPrice=" + targetPrice +
                ", language='" + language + '\'' +
                ", sector='" + sector + '\'' +
                ", companyInfos=" + companyInfos +
                '}';
    }

    public static class CompanyInfo implements Serializable {

        static final long serialVersionUID = 1L;

        private final String isin;

        private final String country;

        CompanyInfo(de.marketmaker.istar.merger.provider.lbbwresearch.CompanyInfo orig) {
            this.isin = orig.getIsin();
            this.country = orig.getCountry().orElse(null);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CompanyInfo that = (CompanyInfo) o;

            return isin.equals(that.isin);
        }

        @Override
        public int hashCode() {
            return isin.hashCode();
        }

        public String getIsin() {
            return isin;
        }

        public String getCountry() {
            return country;
        }

        @Override
        public String toString() {
            return "CompanyInfo{" +
                    "isin='" + isin + '\'' +
                    ", country='" + country + '\'' +
                    '}';
        }
    }
}
