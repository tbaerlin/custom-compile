/*
 * BewHistoricPriceRequest.java
 *
 * Created on 27.10.2010 15:18:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author oflege
 */
public class BewHistoricPriceRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final LocalDate date;

    private List<String> vwdcodes = new ArrayList<>();

    public BewHistoricPriceRequest(LocalDate date) {
        this.date = date;
    }

    public void addVwdcode(String s) {
        this.vwdcodes.add(s);
    }

    public List<String> getVwdcodes() {
        return this.vwdcodes;
    }

    public LocalDate getDate() {
        return date;
    }

    protected void appendToString(StringBuilder sb) {
        sb.append(this.date).append(", ").append(this.vwdcodes);
    }
}
