/*
 * LimitCommand.java
 *
 * Created on 01.08.2006 14:37:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.RestrictedSet;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LimitCommand {
    private Long userid;
    private Long companyid;

    private Long limitlistid;

    private Long positionid;

    private String limitName;

    private String limitNachricht;

    private String limitOperator;

    private String limitWert;

    private String limitUebermittlung;

    @NotNull
    public Long getCompanyid() {
        return this.companyid;
    }

    public void setCompanyid(Long companyid) {
        this.companyid = companyid;
    }

    @NotNull
    public Long getLimitlistid() {
        return this.limitlistid;
    }

    public void setLimitlistid(Long limitlistid) {
        this.limitlistid = limitlistid;
    }

    @NotNull
    public String getLimitNachricht() {
        return this.limitNachricht;
    }

    public void setLimitNachricht(String limitNachricht) {
        this.limitNachricht = limitNachricht;
    }

    @NotNull
    public String getLimitName() {
        return this.limitName;
    }

    public void setLimitName(String limitName) {
        this.limitName = limitName;
    }

    @NotNull
    @RestrictedSet("LT,LEQ,GT,GEQ")
    public String getLimitOperator() {
        return this.limitOperator;
    }

    public void setLimitOperator(String limitOperator) {
        this.limitOperator = limitOperator;
    }

    @NotNull
    public String getLimitUebermittlung() {
        return this.limitUebermittlung;
    }

    public void setLimitUebermittlung(String limitUebermittlung) {
        this.limitUebermittlung = limitUebermittlung;
    }

    @NotNull
    public String getLimitWert() {
        return this.limitWert;
    }

    public void setLimitWert(String limitWert) {
        this.limitWert = limitWert;
    }

    @NotNull
    public Long getPositionid() {
        return this.positionid;
    }

    public void setPositionid(Long positionid) {
        this.positionid = positionid;
    }

    @NotNull
    public Long getUserid() {
        return this.userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

}
