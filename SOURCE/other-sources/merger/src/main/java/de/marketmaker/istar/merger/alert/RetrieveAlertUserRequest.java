/*
 * RetrieveAlertUserRequest.java
 *
 * Created on 16.12.2008 09:56:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

import java.util.Locale;

/**
 * Used to get information about an alert user.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RetrieveAlertUserRequest extends AbstractAlertServerRequest {
    static final long serialVersionUID = 2L;

    private Locale localeForNewUser = Locale.GERMAN;

    /**
     * Only necessary to retrieve the e-mail address in iview login via UserServiceImpl.
     * Uses GERMAN as the default locale.
     *
     * TODO: This constructor may be removed in the future, when the iview dev branch build uses the istar artifacts from dev branch builds and not from the master branch builds.
     *
     * @param userID a user ID
     */
    public RetrieveAlertUserRequest(String userID) {
        super(null, userID);
    }

    /**
     * @param userID a user ID
     * @param localeForNewUser the language locale to be used if the user is newly registered at the alert service.
     */
    public RetrieveAlertUserRequest(String userID, Locale localeForNewUser) {
        super(null, userID);
        this.localeForNewUser = localeForNewUser;
    }

    public Locale getLocaleForNewUser() {
        return localeForNewUser;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", localeForNewUser=").append(localeForNewUser);
    }
}
