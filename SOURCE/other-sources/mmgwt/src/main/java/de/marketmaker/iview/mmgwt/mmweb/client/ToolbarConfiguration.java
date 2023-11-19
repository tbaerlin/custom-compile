/*
 * ToolbarConfiguration.java
 *
 * Created on 1/23/15 1:39 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * @author kmilyut
 */

public class ToolbarConfiguration {

    public static final ToolbarConfiguration BASIC = new ToolbarConfiguration(false).withRefreshButton().withPrintButton().withLogoutButton().withSecretActivationLabel();

    public static final ToolbarConfiguration DEFAULT = new ToolbarConfiguration(true);

    boolean withHomeButtons;

    boolean withRefreshButton;

    boolean withSearchButton;

    boolean withLimitsButton;

    boolean withPushButton;

    boolean withPrintButton;

    boolean withPdfButton;

    boolean withSaveButton;

    boolean withLogoutButton;

    boolean withSecretActivationLabel;

    public ToolbarConfiguration(boolean withAllButtons) {
        if (withAllButtons) {
            withHomeButtons().
            withRefreshButton().
            withSearchButton().
            withLimitsButton().
            withPushButton().
            withPrintButton().
            withPdfButton().
            withSaveButton().
            withLogoutButton().
            withSecretActivationLabel();
        }
    }

    ToolbarConfiguration withHomeButtons() {
        withHomeButtons = true;
        return this;
    }

    ToolbarConfiguration withRefreshButton() {
        withRefreshButton = true;
        return this;
    }

    ToolbarConfiguration withSearchButton() {
        withSearchButton = true;
        return this;
    }

    ToolbarConfiguration withLimitsButton() {
        withLimitsButton = true;
        return this;
    }

    ToolbarConfiguration withPushButton() {
        withPushButton = true;
        return this;
    }

    ToolbarConfiguration withPrintButton() {
        if (!"true".equals(SessionData.INSTANCE.getGuiDefValue("disablePrint"))) {  // $NON-NLS$
            withPrintButton = true;
        }
        return this;
    }

    ToolbarConfiguration withPdfButton() {
        if (!"true".equals(SessionData.INSTANCE.getGuiDefValue("disablePdf"))) {  // $NON-NLS$
            withPdfButton = true;
        }
        return this;
    }

    ToolbarConfiguration withSaveButton() {
        withSaveButton = true;
        return this;
    }

    ToolbarConfiguration withLogoutButton() {
        withLogoutButton = true;
        return this;
    }

    ToolbarConfiguration withSecretActivationLabel() {
        withSecretActivationLabel = true;
        return this;
    }
}
