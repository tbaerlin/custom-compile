/*
 * DeleteAlertRequest.java
 *
 * Created on 16.12.2008 09:56:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DeleteAlertRequest extends AbstractAlertServerRequest {
    static final long serialVersionUID = 1L;

    private String alertID;

    public DeleteAlertRequest(String applicationID, String userID) {
        super(applicationID, userID);
    }

    public String getAlertID() {
        return alertID;
    }

    public void setAlertID(String alertID) {
        this.alertID = alertID;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", alertID=").append(this.alertID);
    }
}