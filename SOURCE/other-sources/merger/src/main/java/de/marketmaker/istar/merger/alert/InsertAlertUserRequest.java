/*
 * InsertAlertUserRequest.java
 *
 * Created on 16.12.2008 09:56:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

/**
 * Used to update or create an alert user, the alert user contains email, sms, locale
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class InsertAlertUserRequest extends AbstractAlertServerRequest {
    static final long serialVersionUID = 1L;

    private AlertUser user;

    public InsertAlertUserRequest(String appID, String userID, AlertUser user) {
        super(appID, userID);
        this.user = user;
    }

    public InsertAlertUserRequest(String userID, AlertUser user) {
        super(null, userID);
        this.user = user;
    }

    public AlertUser getUser() {
        return user;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", user=").append(user);
    }
}