/*
 * UpdateAlertUserRequest.java
 *
 * Created on 16.12.2008 09:56:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

/**
 * Used to update a user.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UpdateAlertUserRequest extends InsertAlertUserRequest {
    static final long serialVersionUID = 1L;

    public UpdateAlertUserRequest(String userID, AlertUser user) {
        super(userID, user);
    }
}