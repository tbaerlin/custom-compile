package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import de.marketmaker.iview.pmxml.GetWorkspaceResponse;

/**
 * Created on 01.10.13 13:50
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface PmWorkspaceCallback {
    void onWorkspaceAvailable(GetWorkspaceResponse response);
}
