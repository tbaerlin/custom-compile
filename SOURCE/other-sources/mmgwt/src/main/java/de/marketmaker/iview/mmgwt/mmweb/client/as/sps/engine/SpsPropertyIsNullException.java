/*
 * SpsPropertyIsNullException.java
 *
 * Created on 14.05.2014 10:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Markus Dick
 */
@NonNLS
public class SpsPropertyIsNullException extends IllegalStateException {
    public SpsPropertyIsNullException(String s, String key) {
        super(s + ": " + key);
    }
}
