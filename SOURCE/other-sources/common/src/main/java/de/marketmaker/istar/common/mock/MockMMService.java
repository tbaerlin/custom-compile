/*
 * MockMMService.java
 *
 * Created on 08.09.2006 11:57:30
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.mock;

import org.joda.time.YearMonthDay;

import de.marketmaker.istar.common.mm.MMKeyType;
import de.marketmaker.istar.common.mm.MMService;
import de.marketmaker.istar.common.mm.MMServiceResponse;
import de.marketmaker.istar.common.mm.MMTalkException;
import de.marketmaker.istar.common.mm.MMTalkTableRequest;
import de.marketmaker.istar.common.mm.MMTimeseriesRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI", justification = "Class is just a mock object.")
public class MockMMService implements MMService {
    private Object[] getTimeseriesResult;
    private Object[] getMMTalkTableResult;

    public int[] getHandles(String handleStr, MMKeyType keytype) throws MMTalkException {
        throw new UnsupportedOperationException();
    }

    public Object[] getMMTalkTable(int[] handles, String contextHandle, String[] formulas,
                                   String preFormula) throws MMTalkException {
        throw new UnsupportedOperationException();
    }

    public Object[] getMMTalkTable(String[] keys, MMKeyType keytype, String contextHandle,
                                   String[] formulas, String preFormula) throws MMTalkException {
        return this.getMMTalkTableResult;
    }

    public MMServiceResponse getTimeseries(MMTimeseriesRequest request) throws MMTalkException {
        return new MMServiceResponse(this.getMMTalkTableResult);
    }

    public Object[] getTimeseries(String[] keys, MMKeyType keytype, String[] formulas,
                                  YearMonthDay from, YearMonthDay to) throws MMTalkException {
        return this.getTimeseriesResult;
    }

    public MMServiceResponse getMMTalkTable(MMTalkTableRequest request) throws MMTalkException {
        return new MMServiceResponse(this.getTimeseriesResult);
    }

    public void setGetMMTalkTableResult(Object[] getMMTalkTableResult) {
        this.getMMTalkTableResult = getMMTalkTableResult;
    }

    public void setGetTimeseriesResult(Object[] result) {
        this.getTimeseriesResult = result;
    }

//    public Object[] getMMTalkTable(List<CurrentPrices> prices, String[] keys, MMKeyType keytype, String contextHandle, String[] formulas, String preFormula) throws MMTalkException {
//        return this.getMMTalkTableResult;
//    }
}
