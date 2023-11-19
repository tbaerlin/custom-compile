/*
 * VendorkeyFilterEditor.java
 *
 * Created on 23.04.2005 13:39:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.beans.PropertyEditorSupport;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VendorkeyFilterEditor extends PropertyEditorSupport {

    public void setAsText(String text) throws IllegalArgumentException {
        setValue(VendorkeyFilterFactory.create(text));
    }

}
