/*
 * NoSuchUserException.java
 *
 * Created on 03.08.2006 15:20:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NoSuchUserException extends MergerException {
    public NoSuchUserException(String message, String login, Long companyid) {
        super(message, login, companyid);
    }

    public NoSuchUserException(String message, long userid) {
        super(message, userid);
    }

    public String getCode() {
        return "user.invalid.userid";
    }
}
