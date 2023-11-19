/*
 * EodItem.java
 *
 * Created on 14.01.13 17:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

/**
 * @author zzhao
 */
interface EodItem {

    byte[] getBytes(boolean isPatch, int pivot);

    <T extends EodItem> void merge(T another, boolean extension);

    void withData(byte[] data);
}
