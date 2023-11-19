/*
 * ItemWithNames.java
 *
 * Created on 10.12.2010 15:21:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain;

/**
 * @author oflege
 */
public interface ItemWithNames {
    String getName(Language language);
    String getNameOrDefault(Language language);
}