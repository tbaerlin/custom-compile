/*
 * IEventReleaseImporter.java
 *
 * Created on 19.03.12 16:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.io.InputStream;

/**
 * @author zzhao
 */
public interface EventReleaseImporter {
    int execute(InputStream is) throws Exception;
}
