/*
 * EconodayProvider.java
 *
 * Created on 21.03.12 09:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @author zzhao
 */
public interface EconodayProvider {

    /**
     * A list of available events identified by an unique event code. For each event there are
     * multiple releases.
     *
     * @return
     */
    List<Event> getAvailableEvents();

    Map<Integer, ReleaseDetail> getReleaseDetails(int[] releaseIds);

    EconodaySearchResponse getReleases(EconodaySearchRequest req);

    byte[] getImage(int releaseId, int imageType);
}
