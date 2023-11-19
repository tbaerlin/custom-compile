/*
 * AlertExecution.java
 *
 * Created on 18.12.2008 10:59:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

import java.io.Serializable;

import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AlertNotification implements Serializable {
    static final long serialVersionUID = 1L;

    private String id;

    private boolean acknowledged;

    private DateTime dateTime;

    private String informationType;

    private String target;

    private String text;

    public AlertNotification(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getInformationType() {
        return informationType;
    }

    public void setInformationType(String informationType) {
        this.informationType = informationType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return new StringBuilder(100).append("AlertNotification[")
                .append(this.dateTime)
                .append(", ack'd=").append(this.acknowledged)
                .append(", type=").append(this.informationType)
                .append(", target=").append(this.target)
                .append(", text=").append(this.text)
                .toString();
    }
}