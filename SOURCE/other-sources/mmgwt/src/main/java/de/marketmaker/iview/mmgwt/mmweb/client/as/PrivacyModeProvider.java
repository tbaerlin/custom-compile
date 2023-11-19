/*
 * PrivacyModeProvider.java
 *
 * Created on 07.05.2015 13:25
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;

import java.util.Set;

/**
 * @author mdick
 */
public interface PrivacyModeProvider extends IsPrivacyModeProvider {
    HistoryToken createPrivacyModeStartPageToken();
    Set<String> getObjectIdsAllowedInPrivacyMode();
    HistoryToken getPrivacyModeEntryToken();
    HistoryToken getPrivacyModeCustomerToken();
    void requestPrivacyModeActivatable(PrivacyModeActivatableCallback callback);

    interface PrivacyModeActivatableCallback {
        void onPrivacyModeActivatable(boolean activatable);
    }
}
