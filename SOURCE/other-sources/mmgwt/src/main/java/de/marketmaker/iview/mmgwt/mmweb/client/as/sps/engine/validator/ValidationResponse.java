package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;

/**
 * Created on 05.06.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class ValidationResponse {
    private final BindToken targetBind;
    private final WidgetAction action;
    private final String description;

    public ValidationResponse(BindToken targetBind, WidgetAction action, String description) {
        this.targetBind = targetBind;
        this.action = action;
        this.description = description;
    }

    public ValidationResponse(BindToken targetBind, WidgetAction action) {
        this(targetBind, action, null);
    }

    public BindToken getTargetBind() {
        return this.targetBind;
    }

    public WidgetAction getAction() {
        return this.action;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public String toString() {
        if (this.description != null) {
            return "ValidationResponse { " + this.description + " for bind " + this.targetBind + " }"; // $NON-NLS$
        }
        return "ValidationResponse { bind: " + this.targetBind + " }"; // $NON-NLS$
    }
}