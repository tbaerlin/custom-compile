/*
 * AbstractAlertServerRequest.java
 *
 * Created on 18.12.2008 15:55:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractAlertServerRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    protected final String userID;

    protected final String applicationID;

    public AbstractAlertServerRequest(String applicationID, String userID) {
        this.applicationID = applicationID;
        this.userID = userID;
    }

    public String getUserID() {
        return this.userID;
    }

    public String getApplicationID() {
        return this.applicationID;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", appID=").append(this.applicationID)
                .append(", userID=").append(this.userID);
    }
}
