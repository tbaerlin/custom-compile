package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import de.marketmaker.iview.mmgwt.mmweb.client.Dimension;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis.config.Config;

/**
 * Created on 14.04.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public interface ContentSizeProvider {
    Dimension getContentSize(Config config);
    int getToolbarHeight();
}
