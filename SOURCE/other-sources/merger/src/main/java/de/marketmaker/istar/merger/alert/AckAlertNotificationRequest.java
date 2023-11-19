/*
 * CreateAlertRequest.java
 *
 * Created on 16.12.2008 09:56:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AckAlertNotificationRequest extends AbstractAlertServerRequest {
    static final long serialVersionUID = 1L;

    private List<String> ids;

    public AckAlertNotificationRequest(String applicationID, String userID) {
        super(applicationID, userID);
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", alertIds=").append(this.ids);
    }
}