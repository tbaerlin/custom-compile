/*
 * Initializable.java
 *
 * Created on 25.10.2004 14:16:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.lifecycle;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id: Initializable.java,v 1.1 2004/11/17 15:12:34 tkiesgen Exp $
 */
public interface Initializable {
    void initialize() throws Exception;
}
