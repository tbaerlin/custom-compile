/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;


/**
 * Marker interface for classes that are able to handle I/O events
 * raised by the SelectorThread class. This interface should not
 * be implemented directly. Instead, use one of the subinterfaces
 * define speciic functionality for a particular event.
 * @author Oliver Flege
 */
public interface SelectorHandler {
}
