/*
 * IstarResponse.java
 *
 * Created on 02.03.2005 14:18:46
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.request;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface IstarResponse extends Serializable {
    /**
     * @return a string identifying the server that provided the response
     */
    String getServerInfo();

    /**
     * @return true iff this object contains valid response data, false iff the request
     * could not be answered for some reason.
     */
    boolean isValid();
}
