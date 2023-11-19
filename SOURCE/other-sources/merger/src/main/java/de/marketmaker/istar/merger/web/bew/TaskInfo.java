/*
 * TaskInfo.java
 *
 * Created on 06.10.2010 10:31:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.sql.Timestamp;

import org.joda.time.DateTime;

/**
 * @author oflege
 */
class TaskInfo {
    private final int id;

    private final String customer;

    private final long requestdate;

    private final int percentage;

    private final long completed;

    TaskInfo(int id, String customer, Timestamp date, int percentage, Timestamp completed) {
        this.id = id;
        this.customer = customer;
        this.requestdate = date.getTime();
        this.percentage = percentage;
        this.completed = (completed != null) ? completed.getTime() : 0L;
    }

    int getId() {
        return this.id;
    }

    String getCustomer() {
        return this.customer;
    }

    DateTime getRequestdate() {
        return new DateTime(this.requestdate);
    }

    int getPercentage() {
        return this.percentage;
    }

    DateTime getCompleted() {
        return this.completed != 0L ? new DateTime(this.completed) : null;
    }
}
