/*
 * IpoData.java
 *
 * Created on 16.07.2006 16:11:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;

import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Sector;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface WGZCertificateData {

    Integer getCell();

    String getName();

    List<String> getColumnList();

    String getSortColumn();

    List<Long> getInstrumentids();

    String getListid();
}