package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import de.marketmaker.iview.pmxml.AlertsResponse;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.VerificationStatus;

/**
 * Created on 16.01.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 *
 * Common methods over all pm depot objects (Investor, Depot, Portfolio, Account, ...)
 *
 */
public interface DepotObject extends HasZone {
    String getId();
    AlertsResponse getAlertResponse();
    String getCreationDate();
    VerificationStatus getDataStatusTotal();
    String getDataStatusTotalDate();
    ShellMMType getShellMMType();
}
