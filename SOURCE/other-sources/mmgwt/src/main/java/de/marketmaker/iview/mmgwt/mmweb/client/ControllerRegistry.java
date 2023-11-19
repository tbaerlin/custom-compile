/*
 * ControllerRegistry.java
 *
 * Created on 07.12.2009 14:29:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

/**
 * @author oflege
 */
public interface ControllerRegistry {
    boolean hasController(String id);
    PageController addController(String key, PageController value);
}
