/*
 * DeleteAlertResponse.java
 *
 * Created on 16.12.2008 09:57:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * Returns the result of an update or insert operation
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RetrieveAlertUserResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private AlertUser user;

    public RetrieveAlertUserResponse(AlertUser user) {
        this.user = user;
        if (this.user == null) {
            setInvalid();
        }
    }

    public AlertUser getUser() {
        return user;
    }
}