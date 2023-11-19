/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.domain.data.CdsDataRecord;

import java.util.List;
import java.util.Map;

public interface CdsDataProvider {

    CdsDataRecord getCdsDataRecord(String symbol);
    Map<String, CdsDataRecord> getCdsDataRecordBulk(List<String> symbol);
}
