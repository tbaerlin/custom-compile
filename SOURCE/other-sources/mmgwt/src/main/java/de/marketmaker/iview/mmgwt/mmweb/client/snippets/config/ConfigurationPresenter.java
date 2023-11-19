/*
 * ConfigurationPresenter.java
 *
 * Created on 05.07.13 16:59
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.HasActionPerformedHandlers;

import java.util.HashMap;

/**
 * @author Markus Dick
 */
public interface ConfigurationPresenter extends HasActionPerformedHandlers {
    public static enum Actions {
        OK, CANCEL
    }

    public void addConfigurationWidget(Widget panel, AbstractImagePrototype icon, String label);
    public HashMap<String, String> getParams();
    public void show();
    public void show(String presetSearchString);

    static final class CancelPendingRequests implements ActionPerformedHandler {
        private final HasCancellablePendingRequests target;

        CancelPendingRequests(HasCancellablePendingRequests target) {
            this.target = target;
        }

        @Override
        public void onAction(ActionPerformedEvent event) {
            if(Actions.OK.name().equals(event.getKey())
                    || Actions.CANCEL.name().equals(event.getKey())) {
                this.target.cancelPendingRequests();
            }
        }
    }
}
