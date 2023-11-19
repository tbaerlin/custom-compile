/*
 * LoginExistsException.java
 *
 * Created on 04.08.2006 09:01:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.user;

import de.marketmaker.istar.merger.MergerException;

/**
 * Thrown if a new user tries to register with the same login as another user for the same company.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LoginExistsException extends MergerException {
    public LoginExistsException(String message, String login) {
        super(message, login);
    }

    public String getCode() {
        return "user.login.exists";
    }
}
