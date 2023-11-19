/*
 * DownloadableItem.java
 *
 * Created on 20.09.2006 14:39:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.DateTime;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface DownloadableItem {

    Source getSource();

     enum Source {
        FUNDINFO,
        SOFTWARESYSTEMSAT,
        FWW,
        STOCK_SELECTION,
        DZBANK,
        FIDA,
        VWD
    }

    Integer getYear();

    String getDescription();

    String getUrl();

    DateTime getDate();

    String getCountry();

    String getLanguage();

    InstrumentTypeEnum getInstrumentType();

    String getMarketAdmission();

    String getPermissionType();

    Type getType();

    enum Type {
        Monthly("report.monthly"),
        SemiAnnual("report.semi.annual"),
        Annual("report.annual"),
        Accounts("report.accounts"),
        Prospectus("report.prospectus"),
        ProspectusSimplified("report.prospectus.simplified"),
        ProspectusUnfinished("report.prospectus.unfinished"),
        FactSheet("report.fact.sheet"),
        TermSheet("report.term.sheet"),
        SpectSheet("report.spect.sheet"),
        Addendum("report.addendum"),
        KIID("report.kiid"),
        PIB("report.pib"),
        Unknown("report.unknown"),
        PIF("report.pif"),
        BIB("report.bib");

        private final String key;

        Type(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
