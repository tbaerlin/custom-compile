/*
 * UpdateAlertRequest.java
 *
 * Created on 16.12.2008 09:56:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.alert;

/**
 * Used to insert or update an alert. If the alert field has no defined id,
 * a new alert will be inserted. Otherwise, if deleteExisting is false (default), an existing
 * alert will be updated. If deleteExisting is true, the existing alert will be deleted and
 * a new alert will be inserted to effectively replace it.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UpdateAlertRequest extends AbstractAlertServerRequest {
    static final long serialVersionUID = 1L;

    private Alert alert;

    private boolean deleteExisting = false;

    // TODO: remove this field in 2016
    @Deprecated
    private int fieldId; // seems we can change the field but not the vwdCode :-/

    public UpdateAlertRequest(String applicationID, String userID) {
        super(applicationID, userID);
    }

    public Alert getAlert() {
        return alert;
    }

    public void setAlert(Alert alert) {
        this.alert = alert;
        this.fieldId = alert.getFieldId();
    }

    public boolean isDeleteExisting() {
        return deleteExisting;
    }

    public void setDeleteExisting(boolean deleteExisting) {
        this.deleteExisting = deleteExisting;
    }

    @Deprecated // use the fieldId in alert
    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    @Deprecated
    public int getFieldId() {
        // return this.fieldId;
        return alert.getFieldId();
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        super.appendToString(sb);
        sb.append(", deleteExisting=").append(this.deleteExisting)
                .append(", alert=").append(this.alert);
    }
}