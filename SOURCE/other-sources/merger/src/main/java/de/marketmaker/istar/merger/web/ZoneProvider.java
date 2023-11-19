/*
 * ZoneProvider.java
 *
 * Created on 01.04.11 09:51
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web;

/**
 * @author oflege
 */
public interface ZoneProvider {
    Zone getZone(String name);
}
