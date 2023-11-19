/*
 * CompanyProfile.java
 *
 * Created on 16.07.2006 16:37:36
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.util.Comparator;
import java.util.List;
import java.math.BigDecimal;

import de.marketmaker.istar.domainimpl.data.CompanyProfileImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface CompanyProfile {
    CompanyProfile NULL_INSTANCE = new CompanyProfileImpl.Builder().build();

    Comparator<BoardMember> BOARD_MEMBER_COMPARATOR = Comparator.comparing(BoardMember::getJob)
            .thenComparing(BoardMember::getTitle, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparing(BoardMember::getName);

    long getInstrumentid();

    String getName();

    String getStreet();

    String getPostalcode();

    String getCity();

    String getCountry();

    String getTelephone();

    String getFax();

    String getEmail();

    String getUrl();

    LocalizedString getPortrait();

    BigDecimal getAnnualReportFactor();

    String getAnnualReportCurrency();

    List<Shareholder> getShareholders();

    List<BoardMember> getBoardMembers();

    List<BoardMember> getManager();

    List<BoardMember> getSupervisor();

    interface Shareholder {
        String getName();

        BigDecimal getShare();
    }

    interface BoardMember {
        enum Job {
            UNSPECIFIED,
            MANAGEMENT,
            SUPERVISORY
        }

        String getName();

        String getTitle();

        Job getJob();
    }
}
