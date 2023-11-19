/*
 * Disposable.java
 *
 * Created on 25.10.2004 14:15:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.lifecycle;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id: Disposable.java,v 1.1 2004/11/17 15:12:34 tkiesgen Exp $
 */
public interface Disposable {
    void dispose() throws Exception;
}
