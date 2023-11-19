/*
 * WidgetDropHandler.java
 *
 * Created on 13.11.2015 14:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author mdick
 */
public interface WidgetDropHandler {
    boolean isDropAllowed(String transferData);
    boolean onDndEnter(String transferData);
    void onDndLeave();
    void onDrop(String transferData);
}
