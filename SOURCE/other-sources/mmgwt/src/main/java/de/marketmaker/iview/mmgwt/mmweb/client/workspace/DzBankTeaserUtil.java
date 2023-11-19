/*
 * DzBankTeaserUtil
 *
 * Created on 09.02.2016 09:08
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;

import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.TeaserConfigForm;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.DzBankTeaserUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.server.teaser.TeaserDaoDb;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author mdick
 */
public class DzBankTeaserUtil {
    @NonNLS
    public static SafeUri getCurrentImageUrl() {
        return UriUtils.fromTrustedString(
                "/" + GuiDefsLoader.getModuleName() + TeaserConfigForm.IMG_SRC_URL
                        + "?uid=" + (int) (Math.random() * 4096)
                        + "&version=" + TeaserDaoDb.CURRENT_VERSION);
    }

    public static void fireTeaserUpdatedEvent() {
        if (!Selector.DZ_TEASER.isAllowed()) {
            return;
        }
        new TeaserConfigForm.TeaserRequest() {
            @Override
            public void teaserReady(JSONObject teaser) {
                DzBankTeaserUpdatedEvent.fire(new TeaserConfigData(teaser));
            }
        }.fireRequest();
    }

    public static boolean isTeaserHiddenByAppConfig() {
        return Boolean.parseBoolean(SessionData.INSTANCE.getUserProperty(AppConfig.HIDE_PROD_TEASER));
    }
}
