/*
 * MultiRenderer.java
 *
 * Created on 05.06.2008 18:01:44
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MultiRenderer<T> {
    String render(T[] values);
}
