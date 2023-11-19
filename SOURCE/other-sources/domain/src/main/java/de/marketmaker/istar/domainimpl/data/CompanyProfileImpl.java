/*
 * CompanyProfileImpl.java
 *
 * Created on 09.08.2006 08:19:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.CompanyProfile;
import de.marketmaker.istar.domain.data.LocalizedString;

import static de.marketmaker.istar.domain.data.CompanyProfile.BoardMember.Job.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompanyProfileImpl implements CompanyProfile, Serializable {
    protected static final long serialVersionUID = 1L;

    public static class Builder {
        private long instrumentid;

        private String name;

        private String street;

        private String postalcode;

        private String city;

        private String country;

        private String telephone;

        private String fax;

        private String url;

        private final LocalizedString.Builder portrait = new LocalizedString.Builder();

        private String email;

        private BigDecimal annualReportFactor;

        private String annualReportCurrency;

        private String annualReportType;

        private List<Shareholder> shareholders = new ArrayList<>();

        private List<BoardMember> boardMembers = new ArrayList<>();

        public CompanyProfileImpl build() {
            if (!isValid()) {
                throw new IllegalStateException("instrumentid not set");
            }
            return new CompanyProfileImpl(this);
        }

        public boolean isValid() {
            return this.instrumentid != -1;
        }

        public long getInstrumentid() {
            return instrumentid;
        }

        public void setInstrumentid(long instrumentid) {
            this.instrumentid = instrumentid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public void setPostalcode(String postalcode) {
            this.postalcode = postalcode;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public void setTelephone(String telephone) {
            this.telephone = telephone;
        }

        public void setFax(String fax) {
            this.fax = fax;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setPortrait(String portrait, Language... languages) {
            this.portrait.add(portrait, languages);
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setAnnualReportFactor(BigDecimal annualReportFactor) {
            this.annualReportFactor = annualReportFactor;
        }

        public void setAnnualReportCurrency(String annualReportCurrency) {
            this.annualReportCurrency = annualReportCurrency;
        }

        public void setAnnualReportType(String annualReportType) {
            this.annualReportType = annualReportType;
        }

        public void setShareholders(List<Shareholder> shareholders) {
            this.shareholders = shareholders;
        }

        public void setBoardMembers(List<BoardMember> boardMembers) {
            this.boardMembers = boardMembers;
        }

        public void add(Shareholder shareholder) {
            this.shareholders.add(shareholder);
        }

        public void add(BoardMember boardMember) {
            this.boardMembers.add(boardMember);
        }
    }

    private final long instrumentid;

    private final String name;

    private final String street;

    private final String postalcode;

    private final String city;

    private final String country;

    private final String telephone;

    private final String fax;

    private final String url;

    private final LocalizedString portrait;

    private final String email;

    private final BigDecimal annualReportFactor;

    private final String annualReportCurrency;

    private final String annualReportType;

    private final List<Shareholder> shareholders;

    private final List<BoardMember> boardMembers;

    public CompanyProfileImpl(Builder b) {
        this.instrumentid = b.instrumentid;
        this.name = b.name;
        this.street = b.street;
        this.postalcode = b.postalcode;
        this.city = b.city;
        this.country = b.country;
        this.telephone = b.telephone;
        this.fax = b.fax;
        this.url = b.url;
        this.email = b.email;
        this.portrait = b.portrait.build();
        this.annualReportFactor = b.annualReportFactor;
        this.annualReportCurrency = b.annualReportCurrency;
        this.annualReportType = b.annualReportType;
        this.shareholders = b.shareholders;
        this.boardMembers = b.boardMembers;
        this.boardMembers.sort(CompanyProfileImpl.BOARD_MEMBER_COMPARATOR);
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public String getName() {
        return name;
    }

    public String getStreet() {
        return street;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getFax() {
        return fax;
    }

    public String getUrl() {
        return url;
    }

    public String getEmail() {
        return email;
    }

    public LocalizedString getPortrait() {
        return portrait;
    }

    public BigDecimal getAnnualReportFactor() {
        return annualReportFactor;
    }

    public String getAnnualReportCurrency() {
        return annualReportCurrency;
    }

    public String getAnnualReportType() {
        return annualReportType;
    }

    public List<Shareholder> getShareholders() {
        return this.shareholders;
    }

    public List<BoardMember> getBoardMembers() {
        return this.boardMembers;
    }

    @Override
    public List<BoardMember> getManager() {
        return this.boardMembers.stream().filter(v -> MANAGEMENT.equals(v.getJob())).collect(Collectors.toList());
    }

    @Override
    public List<BoardMember> getSupervisor() {
        return this.boardMembers.stream().filter(v -> SUPERVISORY.equals(v.getJob())).collect(Collectors.toList());
    }

    public String toString() {
        final String defaultPortrait = portrait.getDefault();
        return "CompanyProfileImpl[instrumentid=" + instrumentid
                + ", name=" + name
                + ", street=" + street
                + ", postalcode=" + postalcode
                + ", city=" + city
                + ", country=" + country
                + ", telephone=" + telephone
                + ", fax=" + fax
                + ", url=" + url
                + ", email=" + email
                + ", annualReportFactor=" + annualReportFactor
                + ", annualReportCurrency=" + annualReportCurrency
                + ", annualReportType=" + annualReportType
                + ", portrait=" + (Objects.nonNull(defaultPortrait) ? defaultPortrait.substring(0, Math.min(defaultPortrait.length(), 25)) + " ..." : defaultPortrait)
                + ", shareholders=" + getShareholders()
                + ", boardMembers=" + getBoardMembers()
                + "]";
    }

    public static class ShareholderImpl implements Shareholder, Serializable {
        protected static final long serialVersionUID = 1L;

        private final String name;

        private final BigDecimal share;

        public ShareholderImpl(String name, BigDecimal share) {
            this.name = name;
            this.share = share;
        }

        public String getName() {
            return name;
        }

        public BigDecimal getShare() {
            return share;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ShareholderImpl that = (ShareholderImpl) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(share, that.share);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, share);
        }
    }

    public static class BoardMemeberImpl implements BoardMember, Serializable {
        protected static final long serialVersionUID = 1L;

        private final String name;

        private final String title;

        private final Job job;

        public BoardMemeberImpl(String name, String title) {
            this.name = name;
            this.title = title;
            this.job = UNSPECIFIED;
        }

        public BoardMemeberImpl(String name, String title, Job job) {
            this.name = name;
            this.title = title;
            this.job = job;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public Job getJob() {
            return job;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BoardMemeberImpl that = (BoardMemeberImpl) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(title, that.title) &&
                    job == that.job;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, title, job);
        }
    }
}
