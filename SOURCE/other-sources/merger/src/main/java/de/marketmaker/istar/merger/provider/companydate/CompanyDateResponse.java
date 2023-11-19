/*
 * CompanyDateResponse.java
 *
 * Created on 19.07.2008 15:18:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.companydate;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.data.CompanyDate;

import java.util.List;
import java.util.Collections;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CompanyDateResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private List<CompanyDate> dates = Collections.emptyList();

    private int totalCount;

    public static CompanyDateResponse getInvalid() {
        final CompanyDateResponse result = new CompanyDateResponse();
        result.setInvalid();
        return result;
    }

    public List<CompanyDate> getDates() {
        return dates;
    }

    public void setDates(List<CompanyDate> dates) {
        this.dates = dates;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(", dates=").append(dates);
    }
}
