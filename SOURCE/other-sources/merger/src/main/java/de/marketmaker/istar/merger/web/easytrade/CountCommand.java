/*
 * BestsellerCommand.java
 *
 * Created on 01.08.2006 14:09:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.common.validator.Range;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CountCommand {
    private int count = 5;

    @Range(min=0, max=1000)
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
