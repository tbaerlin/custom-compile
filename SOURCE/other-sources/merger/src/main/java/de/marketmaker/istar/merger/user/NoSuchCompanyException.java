/*
 * NoSuchCompanyException.java
 *
 * Created on 02.08.2006 16:20:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NoSuchCompanyException extends MergerException {
    public NoSuchCompanyException(String message, long companyid) {
        super(message, companyid);
    }

    public String getCode() {
        return "user.companyid.invalid";
    }
}
