/*
 * DeleteAlertUserRequest.java
 *
 * Created on 16.12.2008 09:56:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

/**
 * Used to delete an alert user and all associated alerts.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DeleteAlertUserRequest extends AbstractAlertServerRequest {
    static final long serialVersionUID = 1L;

    public DeleteAlertUserRequest(String userID) {
        super(null, userID);
    }
}