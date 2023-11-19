/*
 * AbstractAltBlock.java
 *
 * Created on 06.01.2009 09:52:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.alert;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.VwdProfile;
import de.marketmaker.istar.merger.alert.AlertProvider;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeCommandController;

/**
 * Base class for alert commands, keeps appId and vwdUserId
 * both are needed in all alert requests
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Michael Wohlfart
 */
public abstract class AbstractAltBlock extends EasytradeCommandController {
    protected static final String ALT_OK_TEMPLATE = "altok";

    public static class AlertCommand {

        private String appId;

        private String vwdUserId;

        /**
         * @return The application's id, provided by VWD.
         */
        @NotNull
        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        /**
         * @return The user's id, provided by VWD.
         * @sample 1526
         */
        @NotNull
        public String getVwdUserId() {
            return vwdUserId;
        }

        @SuppressWarnings("unused")
        public void setVwdUserId(String vwdUserId) {
            this.vwdUserId = vwdUserId;
        }

    }

    protected AlertProvider alertProvider;

    protected AbstractAltBlock(Class clazz) {
        super(clazz);
    }

    public void setAlertProvider(AlertProvider alertProvider) {
        this.alertProvider = alertProvider;
    }

    protected String getPreferredAppId(AlertCommand cmd) {
        final String result;
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (profile instanceof VwdProfile) {
            result = ((VwdProfile) profile).getAppId();
        } else {
            result = cmd.getAppId();
        }
        return result;
    }

}
