/*
 * DerivativeIpoData.java
 *
 * Created on 20.11.2008 09:55:36
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.LocalDate;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface DerivativeIpoData {
    String getWkn();

    String getName();

    LocalDate getSubscriptionStart();

    LocalDate getSubscriptionEnd();

    LocalDate getValutaDate();

    LocalDate getExpirationDate();

    DownloadableItem getPibReport();

    List<DownloadableItem> getReports();

    int getSort();

    boolean isDzPib();
}
