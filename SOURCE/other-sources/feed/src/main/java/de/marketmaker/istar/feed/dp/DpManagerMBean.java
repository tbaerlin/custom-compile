/*
 * DpManagerMBean.java
 *
 * Created on 08.06.2005 15:49:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface DpManagerMBean {
    /**
     * Triggers write for the given file
     * @param inputFile name of an input file (can be a relative to the input directory)
     * @return info whether the write has been triggered or not
     */
    String triggerWrite(String inputFile);

    /**
     * Returns information about registered dpFiles
     */
    DpFileInfo[] getDpFileInfo();
}
