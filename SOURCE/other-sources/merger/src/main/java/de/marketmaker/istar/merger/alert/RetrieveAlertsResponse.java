/*
 * DeleteAlertResponse.java
 *
 * Created on 16.12.2008 09:57:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

import java.util.List;
import java.util.ArrayList;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RetrieveAlertsResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private final List<Alert> alerts = new ArrayList<>();

    public List<Alert> getAlerts() {
        return alerts;
    }

    public void add(Alert alert) {
        this.alerts.add(alert);
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", alerts=").append(this.alerts);
    }

    public boolean containsUnacknowledgedNotifications() {
        for (Alert alert : this.alerts) {
            if (alert.containsUnacknowledgedNotifications()) {
                return true;
            }
        }
        return false;
    }

    public int countUnacknowledgedNotifications() {
        int result = 0;
        for (Alert alert : this.alerts) {
            result += alert.countUnacknowledgedNotifications();
        }
        return result;
    }
}