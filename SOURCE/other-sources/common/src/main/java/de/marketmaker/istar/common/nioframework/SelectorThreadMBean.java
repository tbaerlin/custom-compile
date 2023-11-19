/*
 * SelectorThreadMBean.java
 *
 * Created on 20.10.2006 17:26:20
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface SelectorThreadMBean {
    /**
     * @return description of the current selection keys
     */
    String getSelectionKeys() throws InterruptedException;
}
