/*
 * IstarRequest.java
 *
 * Created on 02.03.2005 14:18:37
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.request;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface IstarRequest extends Serializable {
    /**
     * @return a string identifying the client that issued the request
     */
    String getClientInfo();
}
