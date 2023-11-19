/*
 * TickDirectoryProvider.java
 *
 * Created on 01.04.15 14:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

import de.marketmaker.istar.feed.ordered.tick.TickDirectory;

/**
 * @author oflege
 */
interface TickDirectoryProvider {
    TickDirectory getDirectory(int yyyymmdd);
}
