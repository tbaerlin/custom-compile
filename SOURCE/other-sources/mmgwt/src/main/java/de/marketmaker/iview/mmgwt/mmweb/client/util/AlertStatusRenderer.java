/*
 * PriceRenderer.java
 *
 * Created on 05.06.2008 17:04:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.dmxml.Alert;
import de.marketmaker.iview.dmxml.AlertExecution;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AlertStatusRenderer implements Renderer<Alert> {

    public static final AlertStatusRenderer INSTANCE = new AlertStatusRenderer();

    private final DateRenderer dateRenderer = DateRenderer.localTimezoneDateTime("--");

    private AlertStatusRenderer() {
    }

    public String render(Alert s) {
        if ("ACTIVE".equals(s.getState())) { // $NON-NLS-0$
            return I18n.I.no(); 
        }
        if ("EXECUTED".equals(s.getState())) { // $NON-NLS-0$
            if (s.getExecution() == null || s.getExecution().isEmpty()) {
                return I18n.I.noInstructionsAvailable(); 
            }
            final AlertExecution execution = s.getExecution().get(0);
            final String value = execution.getExecutionValue();
            final String date = execution.getDate();
            return I18n.I.by() + " " + Renderer.PRICE.render(value)  // $NON-NLS$
                    + " ("  + dateRenderer.render(date) + ")"; // $NON-NLS$
        }
        return s.getState();
    }
}
