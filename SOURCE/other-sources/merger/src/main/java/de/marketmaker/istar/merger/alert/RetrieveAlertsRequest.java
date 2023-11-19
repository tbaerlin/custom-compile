/*
 * RetrieveAlertsRequest.java
 *
 * Created on 16.12.2008 09:56:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

/**
 * This request is converted into a GetAlertExecutions call to the AlertServer
 *
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Michael Wohlfart
 */
public class RetrieveAlertsRequest extends AbstractAlertServerRequest {
    static final long serialVersionUID = 1L;

    // userID  and appId is in parent the following parameters are optional:

    String alertId;

    String vwdSymbol;

    // to be backward compatible we use ANY_UNDELETED as default here
    RetrieveAlertStatus retrieveStatus = RetrieveAlertStatus.ANY_UNDELETED;

    public RetrieveAlertsRequest(String applicationID, String userID) {
        super(applicationID, userID);
    }

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public String getVwdSymbol() {
        return vwdSymbol;
    }

    public void setVwdSymbol(String vwdSymbol) {
        this.vwdSymbol = vwdSymbol;
    }


    public RetrieveAlertStatus getRetrieveStatus() {
        return retrieveStatus;
    }

    public void setRetrieveStatus(RetrieveAlertStatus retrieveStatus) {
        this.retrieveStatus = retrieveStatus;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", alertId=").append(this.alertId)
                .append(", vwdSymbol=").append(this.vwdSymbol)
                .append(", retrieveStatus=").append(this.retrieveStatus);
    }
}