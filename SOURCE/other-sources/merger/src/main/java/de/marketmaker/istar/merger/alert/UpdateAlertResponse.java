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
 * Returns the result of an update or insert operation; contains the id of the alert that
 * has been created or updated (for an updated alert, the id will be the same as used
 * in the request).
 * 
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UpdateAlertResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    final String alertID;

    public UpdateAlertResponse(String alertID) {
        this.alertID = alertID;
        if (this.alertID == null) {
            setInvalid();
        }
    }

    public String getAlertID() {
        return this.alertID;
    }
}