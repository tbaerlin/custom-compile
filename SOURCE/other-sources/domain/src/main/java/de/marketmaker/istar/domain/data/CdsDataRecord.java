/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * CDS Data provided by the MDP
 */
public interface CdsDataRecord {

    String getProductCategory();

    String getProduktcharakteristika();

    String getWpNameKurz();
}
