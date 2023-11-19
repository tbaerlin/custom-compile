/*
 * ShowStats.java
 *
 * Created on 04.03.2010 13:47:57
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import java.util.Properties;

/**
 * @author oflege
 */
public interface ShowStats {
    String getStats();

    void setMdpsProcessId(int pid);
}
