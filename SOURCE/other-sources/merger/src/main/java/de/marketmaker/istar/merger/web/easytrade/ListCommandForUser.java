/*
 * ListCommandForUser.java
 *
 * Created on 29.01.2007 13:32:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.web.easytrade.block.UserCommand;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ListCommandForUser extends ListCommandWithOptionalPaging implements UserCommand {
    private String userid;
    private Long companyid;

    @NotNull
    public Long getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Long companyid) {
        this.companyid = companyid;
    }

    @NotNull
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }
}
