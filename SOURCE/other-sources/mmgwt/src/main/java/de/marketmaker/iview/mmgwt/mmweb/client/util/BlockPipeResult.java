package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.dmxml.BlockType;

/**
 * BlockPipeResult.java
 * Created on Sep 15, 2009 11:01:09 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public interface BlockPipeResult<V extends BlockType> {
    String[] getResult(DmxmlContext.Block<V> block);    
}
