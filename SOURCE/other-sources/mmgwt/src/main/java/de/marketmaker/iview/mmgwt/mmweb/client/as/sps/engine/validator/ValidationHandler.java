package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator;

import com.google.gwt.event.shared.EventHandler;

/**
 * Created on 06.06.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public interface ValidationHandler extends EventHandler {
    void onValidation(ValidationEvent event);
}