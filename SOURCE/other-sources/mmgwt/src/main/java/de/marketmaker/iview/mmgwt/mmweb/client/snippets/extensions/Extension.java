/*
 * Extension.java
 *
 * Created on 24.10.2012 09:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions;

/**
 * @author Markus Dick
 */
public interface Extension {
    boolean isAllowed();
    void destroy();
}
