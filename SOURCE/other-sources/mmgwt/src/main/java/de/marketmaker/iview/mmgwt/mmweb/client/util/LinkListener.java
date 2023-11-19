/*
 * LinkListener.java
 *
 * Created on 04.06.2008 16:13:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;


import com.google.gwt.dom.client.Element;

/**
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface LinkListener<D> {
    /**
     * handles clicking a link that is associated with the given LinkContext. 
     * @param context
     * @param e
     */
    void onClick(LinkContext<D> context, Element e);
}
