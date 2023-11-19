/*
 * AbstractPrivacyModeProvider.java
 *
 * Created on 17.07.2015 16:20
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import de.marketmaker.iview.mmgwt.mmweb.client.as.DashboardConfigDao;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyModeProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.List;

/**
 * @author mdick
 */
public abstract class AbstractPrivacyModeProvider implements PrivacyModeProvider {
    private final ShellMMType shellMMType;

    public AbstractPrivacyModeProvider(ShellMMType shellMMType) {
        this.shellMMType = shellMMType;
    }

    protected abstract HistoryToken createDepotObjectToken();

    @Override
    public HistoryToken createPrivacyModeStartPageToken() {
        final List<DashboardConfig> configsByRole = DashboardConfigDao.getInstance().getConfigsByRole(this.shellMMType.value());
        if(!configsByRole.isEmpty()) {
            final String id = configsByRole.get(0).getId();
            if(StringUtil.hasText(id)) {
                return createDepotObjectToken()
                        .with(NavItemSpec.SUBCONTROLLER_KEY,
                                DashboardPageController.toSubControllerId(id)).build();
            }
        }

        return createDepotObjectToken();
    }

    @Override
    public HistoryToken getPrivacyModeCustomerToken() {
        return createDepotObjectToken();
    }

    @Override
    public HistoryToken getPrivacyModeEntryToken() {
        return createDepotObjectToken();
    }

    @Override
    public void requestPrivacyModeActivatable(PrivacyModeActivatableCallback callback) {
        callback.onPrivacyModeActivatable(true);
    }

    @Override
    public PrivacyModeProvider asPrivacyModeProvider() {
        return this;
    }
}
