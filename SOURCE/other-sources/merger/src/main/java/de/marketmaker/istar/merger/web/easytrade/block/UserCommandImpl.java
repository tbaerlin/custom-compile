/*
 * UserCommand.java
 *
 * Created on 24.10.2006 16:42:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;

/**
 * Base class for commands that retrieve user related data.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserCommandImpl implements UserCommand {
    private Long companyid;
    private String userid;

    /**
     * @return user's id
     */
    @NotNull
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    /**
     * @return id of the user's company (assigned by vwd)
     */
    @NotNull
    public Long getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Long companyid) {
        this.companyid = companyid;
    }
}
