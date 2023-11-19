package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.domain.special.DzBankRecord;

import java.util.List;

/**
 * Created on 19.10.12 10:07
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface DzBankRecordProvider {

    List<DzBankRecord> getDzBankRecords(List<Long> iids);

    DzBankRecordSearchResponse searchDzBankRecords(IstarQueryListRequest req);

    DzBankRecordMetaDataResponse createDzBankRecordsMetadata();
}
