/*
 * UpdateLoginCommand.java
 *
 * Created on 22.09.2010 11:29:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

/**
 * @author oflege
 */
public class UpdateLoginCommand {
    private Long companyid;

    private String oldLogin;

    private String newLogin;

    public Long getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Long companyid) {
        this.companyid = companyid;
    }

    public String getOldLogin() {
        return oldLogin;
    }

    public void setOldLogin(String oldLogin) {
        this.oldLogin = oldLogin;
    }

    public String getNewLogin() {
        return newLogin;
    }

    public void setNewLogin(String newLogin) {
        this.newLogin = newLogin;
    }

    public String toString() {
        return "UpdateLoginComman[" + this.companyid + "; '" + this.oldLogin + "' => '" + this.newLogin + "']";
    }
}
