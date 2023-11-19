/*
 * Alert.java
 *
 * Created on 16.12.2008 14:53:57
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
 * data object for upper and lower limit on a single vwdCode-field combination
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Alert implements Serializable {
    static final long serialVersionUID = 1L;

    public enum State {
        UNKNOWN,   // default, used for new Alert objects
        ACTIVE,
        RESTARTED,
        EXECUTED,
        EXPIRED,
        DELETED
    }

    private boolean email;

    private List<AlertExecution> executions;

    private String id;

    private String infoText;

    private BigDecimal lowerBoundary;

    private BigDecimal lowerBoundaryPercent;

    private String name;

    private BigDecimal referenceValue;  // this must be non-null according to the soap interface spec

    private boolean sms;

    private State state = State.UNKNOWN;

    private BigDecimal upperBoundary;

    private BigDecimal upperBoundaryPercent;

    private DateTime created;     // start

    private DateTime validUntil;  // end

    private String vwdCode;

    private int fieldId;


    public void addExecution(AlertExecution execution) {
        if (this.executions == null) {
            this.executions = new ArrayList<>();
        }
        this.executions.add(execution);
    }

    public boolean containsUnacknowledgedNotifications() {
        if (this.executions == null) {
            return false;
        }
        for (AlertExecution execution : this.executions) {
            if (execution.containsUnacknowledgedNotifications()) {
                return true;
            }
        }
        return false;
    }

    public int countUnacknowledgedNotifications() {
        if (this.executions == null) {
            return 0;
        }
        int result = 0;
        for (AlertExecution execution : this.executions) {
            result += execution.countUnacknowledgedNotifications();
        }
        return result;
    }

    public List<AlertExecution> getExecutions() {
        return this.executions;
    }

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public BigDecimal getLowerBoundary() {
        return lowerBoundary;
    }

    public void setLowerBoundary(BigDecimal lowerBoundary) {
        this.lowerBoundary = lowerBoundary;
    }

    public BigDecimal getLowerBoundaryPercent() {
        return lowerBoundaryPercent;
    }

    public void setLowerBoundaryPercent(BigDecimal lowerBoundaryPercent) {
        this.lowerBoundaryPercent = lowerBoundaryPercent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getReferenceValue() {
        return referenceValue;
    }

    public void setReferenceValue(BigDecimal referenceValue) {
        this.referenceValue = referenceValue;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public BigDecimal getUpperBoundary() {
        return upperBoundary;
    }

    public void setUpperBoundary(BigDecimal upperBoundary) {
        this.upperBoundary = upperBoundary;
    }

    public BigDecimal getUpperBoundaryPercent() {
        return upperBoundaryPercent;
    }

    public void setUpperBoundaryPercent(BigDecimal upperBoundaryPercent) {
        this.upperBoundaryPercent = upperBoundaryPercent;
    }

    public DateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(DateTime validUntil) {
        this.validUntil = validUntil;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    public String getVwdCode() {
        return vwdCode;
    }

    public void setVwdCode(String vwdCode) {
        this.vwdCode = vwdCode;
    }

    public boolean isEmail() {
        return email;
    }

    public void setEmail(boolean email) {
        this.email = email;
    }

    public boolean isSms() {
        return sms;
    }

    public void setSms(boolean sms) {
        this.sms = sms;
    }

    @Override
    public String toString() {
        return "Alert[" + this.vwdCode
                + ", state=" + this.state
                + ", id=" + this.id
                + ", name=" + this.name
                + ", field=" + this.fieldId
                + ", reference=" + this.referenceValue
                + ", upper=" + this.upperBoundary
                + ", upperPct=" + this.upperBoundaryPercent
                + ", lower=" + this.lowerBoundary
                + ", lowerPct=" + this.lowerBoundaryPercent
                + ", info=" + this.infoText
                + ", email=" + this.email
                + ", sms=" + this.sms
                + ", until=" + this.validUntil
                + ", execs=" + this.executions
                + "]"
                ;
    }
}
