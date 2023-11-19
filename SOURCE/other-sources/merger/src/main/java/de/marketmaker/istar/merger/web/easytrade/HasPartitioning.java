/*
 * HasPartitioning.java
 *
 * Created on 12.03.2015 14:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

/**
 * @author jkirchg
 */
public interface HasPartitioning {

    public void setPartition(boolean partition);

    public boolean isPartition();

}
