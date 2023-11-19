/*
 * FinderLinkListener.java
 *
 * Created on 9/4/14 2:44 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Stefan Willenbrock
 */
public interface FinderLinkListener<D> extends LinkListener<D> {
    LinkContext<D> createContext(D data);
}
