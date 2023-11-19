/*
 * AlertExecution.java
 *
 * Created on 18.12.2008 10:59:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

/**
 * contains data about a limit that has been triggered at a certain value
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AlertExecution implements Serializable {
    static final long serialVersionUID = 1L;

    private BigDecimal boundaryValue;

    private DateTime dateTime;

    private BigDecimal executionValue;

    private List<AlertNotification> notifications;

    public AlertExecution() {
    }

    public void addNotification(AlertNotification notification) {
        if (this.notifications == null) {
            this.notifications = new ArrayList<>();
        }
        this.notifications.add(notification);
    }

    public BigDecimal getBoundaryValue() {
        return boundaryValue;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public BigDecimal getExecutionValue() {
        return executionValue;
    }

    public List<AlertNotification> getNotifications() {
        return notifications;
    }

    public void setBoundaryValue(BigDecimal boundaryValue) {
        this.boundaryValue = boundaryValue;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public void setExecutionValue(BigDecimal executionValue) {
        this.executionValue = executionValue;
    }

    public boolean containsUnacknowledgedNotifications() {
        if (this.notifications == null) {
            return false;
        }
        for (AlertNotification notification : notifications) {
            if (!notification.isAcknowledged()) {
                return true;
            }
        }
        return false;
    }

    public int countUnacknowledgedNotifications() {
        if (this.notifications == null) {
            return 0;
        }
        int result = 0;
        for (AlertNotification notification : notifications) {
            if (!notification.isAcknowledged()) {
                result++;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "AlertExecution[" + this.dateTime
                + ", boundary=" + this.boundaryValue
                + ", execution=" + this.executionValue
                + ", notifications=" + this.notifications
                + "]"
                ;
    }

}
