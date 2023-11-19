/*
 * BooleanYesNoRenderer.java
 *
 * Created on 24.05.13 12:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Markus Dick
 */
public class BooleanYesNoRenderer implements Renderer<Boolean> {
    @Override
    public String render(Boolean aBoolean) {
        return aBoolean == null
                ? ""
                : aBoolean ? I18n.I.yes() : I18n.I.no();
    }
}
