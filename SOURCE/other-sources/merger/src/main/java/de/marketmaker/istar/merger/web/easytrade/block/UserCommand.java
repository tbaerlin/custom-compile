/*
 * UserCommand.java
 *
 * Created on 24.10.2006 16:49:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.common.validator.NotNull;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface UserCommand {
    /**
     * HACK: allows the {@link de.marketmaker.istar.merger.web.easytrade.block.UserHandler} to set
     * the userid based on session or credential information
     * @param userid to be set
     */
    void setUserid(String userid);

    @NotNull
    String getUserid();

    @NotNull
    Long getCompanyid();

    /**
     * HACK: allows the {@link de.marketmaker.istar.merger.web.easytrade.block.UserHandler} to set
     * the companyid based on molecule request parameters.
     * @param companyid to be set
     */
    void setCompanyid(Long companyid);
}
